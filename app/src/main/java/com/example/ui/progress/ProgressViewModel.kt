package com.example.ui.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.learning.MasteryCalculator
import com.example.data.learning.MasteryStatus
import com.example.data.learning.StudyStreakCalculator
import com.example.data.learning.WeaknessCalculator
import com.example.data.local.PreferencesRepository
import com.example.data.local.entity.PracticeResultEntity
import com.example.data.local.entity.StudyActivityEntity
import com.example.data.model.CategoryProgressData
import com.example.data.model.ProgressUiState
import com.example.data.model.ScoreTrendPoint
import com.example.data.model.StudyQuestionItem
import com.example.data.repository.QuestionRepository
import com.example.domain.usecase.GetLearningProgressUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProgressViewModel(
    private val questionRepository: QuestionRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val dateFormat = SimpleDateFormat("MMM d", Locale.US)

    private data class ProgressCoreData(
        val questions: List<StudyQuestionItem>,
        val activeDates: List<String>,
        val practiceResults: List<PracticeResultEntity>,
        val recentActivities: List<StudyActivityEntity>
    )

    private data class ProgressGoalData(
        val dailyGoal: Int,
        val todayCount: Int
    )

    val uiState: StateFlow<ProgressUiState> = combine(
        combine(
            questionRepository.getAllStudyQuestions(),
            questionRepository.getAllActivityDates(),
            questionRepository.getAllPracticeResults(),
            questionRepository.getRecentActivities(10)
        ) { q, d, r, a -> ProgressCoreData(q, d, r, a) },
        combine(
            preferencesRepository.dailyStudyGoal,
            questionRepository.getTodayActivityCount()
        ) { g, c -> ProgressGoalData(g, c) }
    ) { (questions, activeDates, practiceResults, recentActivities), (dailyGoal, todayCount) ->
        val progressOverview = GetLearningProgressUseCase.calculate(questions)
        val total = progressOverview.totalQuestions
        val studied = progressOverview.studiedCount
        val mastered = progressOverview.masteredCount
        val needsPractice = progressOverview.needsPracticeCount
        val notStarted = progressOverview.notStartedCount
        val progressPercent = progressOverview.progressPercentage

        // Categories breakdown using canonical single-source data
        val categoryDataList = progressOverview.categoryProgressList

        // 65/20 breakdown
        val seniorTotal = progressOverview.senior6520Total
        val seniorStudied = progressOverview.senior6520Studied
        val seniorMastered = progressOverview.senior6520Mastered
        val seniorNeedsPractice = progressOverview.senior6520NeedsPractice
        val seniorNotStarted = (seniorTotal - seniorStudied).coerceAtLeast(0)

        // Favorites breakdown
        val favQs = questions.filter { it.isFavorite }
        val favTotal = favQs.size
        val favStudied = favQs.count { it.isStudied }
        val favMastered = favQs.count { it.masteryStatus == MasteryStatus.MASTERED }
        val favNeedsPractice = favQs.count { it.masteryStatus == MasteryStatus.NEEDS_PRACTICE }

        // Flashcards breakdown
        val flashcardsTotal = questions.size
        val flashcardsStudied = studied
        val flashcardsMastered = mastered
        val flashcardsNeedReview = needsPractice

        // Streak
        val streak = StudyStreakCalculator.calculateStreak(activeDates.toSet())

        // Practice performance
        val testsCount = practiceResults.size
        val avgScore = if (testsCount > 0) practiceResults.sumOf { it.percentage } / testsCount else 0
        val bestScore = practiceResults.maxOfOrNull { it.percentage } ?: 0
        val passRate = if (testsCount > 0) (practiceResults.count { it.passed } * 100) / testsCount else 0
        val totalPracticed = practiceResults.sumOf { it.totalQuestions }

        // Speaking performance
        val speakingResults = practiceResults.filter { it.practiceType.equals("SPEAKING", ignoreCase = true) }
        val speakingCount = speakingResults.size
        val speakingTotal = speakingResults.sumOf { it.totalQuestions }
        val speakingAccuracy = if (speakingCount > 0) speakingResults.sumOf { it.percentage } / speakingCount else 0
        val recentSpeaking = speakingResults.firstOrNull()?.let {
            "${it.percentage}% (${it.correctAnswers}/${it.totalQuestions})"
        }

        // Score trend (last 7 tests, earliest first for chronological display)
        val trendPoints = practiceResults.take(7).reversed().map { res ->
            ScoreTrendPoint(
                testId = res.id,
                title = res.title,
                percentage = res.percentage,
                passed = res.passed,
                dateLabel = dateFormat.format(Date(res.completedAt))
            )
        }

        // Weak questions
        val rankedWeak = WeaknessCalculator.rankWeakQuestions(questions)

        ProgressUiState(
            totalQuestions = total,
            studiedCount = studied,
            masteredCount = mastered,
            needsPracticeCount = needsPractice,
            notStartedCount = notStarted,
            progressPercentage = progressPercent,
            categories = categoryDataList,
            senior6520Total = seniorTotal,
            senior6520Studied = seniorStudied,
            senior6520Mastered = seniorMastered,
            senior6520NeedsPractice = seniorNeedsPractice,
            senior6520NotStarted = seniorNotStarted,
            favoritesTotal = favTotal,
            favoritesStudied = favStudied,
            favoritesMastered = favMastered,
            favoritesNeedsPractice = favNeedsPractice,
            flashcardsTotal = flashcardsTotal,
            flashcardsStudied = flashcardsStudied,
            flashcardsMastered = flashcardsMastered,
            flashcardsNeedReview = flashcardsNeedReview,
            studyStreakDays = streak,
            dailyStudyGoal = dailyGoal,
            todayActivityCount = todayCount,
            testsCompleted = testsCount,
            averageScore = avgScore,
            bestScore = bestScore,
            passRate = passRate,
            totalQuestionsPracticed = totalPracticed,
            speakingSessionsCompleted = speakingCount,
            speakingTotalQuestions = speakingTotal,
            speakingAccuracy = speakingAccuracy,
            recentSpeakingResult = recentSpeaking,
            scoreTrendPoints = trendPoints,
            recentActivities = recentActivities,
            weakAreasCount = rankedWeak.size,
            weakQuestions = rankedWeak,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProgressUiState(isLoading = true)
    )

    companion object {
        fun provideFactory(
            questionRepository: QuestionRepository,
            preferencesRepository: PreferencesRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ProgressViewModel(questionRepository, preferencesRepository) as T
            }
        }
    }
}
