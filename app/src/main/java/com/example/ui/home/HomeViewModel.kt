package com.example.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.learning.MasteryStatus
import com.example.data.learning.StudyStreakCalculator
import com.example.data.learning.WeaknessCalculator
import com.example.data.local.PreferencesRepository
import com.example.data.local.entity.PracticeResultEntity
import com.example.data.local.entity.StudyActivityEntity
import com.example.data.model.CategoryOverviewItem
import com.example.data.model.HomeUiState
import com.example.data.model.StudyQuestionItem
import com.example.data.model.VocabularyItem
import com.example.data.model.VocabularyStatus
import com.example.data.repository.QuestionRepository
import com.example.domain.usecase.GetLearningProgressUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(
    private val questionRepository: QuestionRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private data class HomeCoreData(
        val questions: List<StudyQuestionItem>,
        val activeDates: List<String>,
        val practiceResults: List<PracticeResultEntity>,
        val recentActivities: List<StudyActivityEntity>
    )

    private data class HomeUserPrefsData(
        val dailyGoal: Int,
        val todayCount: Int,
        val recentQId: String?,
        val vocabList: List<VocabularyItem>
    )

    val uiState: StateFlow<HomeUiState> = combine(
        combine(
            questionRepository.getAllStudyQuestions(),
            questionRepository.getAllActivityDates(),
            questionRepository.getAllPracticeResults(),
            questionRepository.getRecentActivities(6)
        ) { q, d, r, a -> HomeCoreData(q, d, r, a) },
        combine(
            preferencesRepository.dailyStudyGoal,
            questionRepository.getTodayActivityCount(),
            questionRepository.getMostRecentViewedQuestionId(),
            questionRepository.getAllVocabulary()
        ) { g, c, qId, vocab -> HomeUserPrefsData(g, c, qId, vocab) }
    ) { (questions, activeDates, practiceResults, recentActivities), (dailyGoal, todayCount, recentQId, vocabList) ->
        val progressOverview = GetLearningProgressUseCase.calculate(questions)
        val total = progressOverview.totalQuestions
        val studied = progressOverview.studiedCount
        val unstudiedCount = (total - studied).coerceAtLeast(0)
        val favoriteCount = progressOverview.favoriteCount
        val needsPracticeCount = progressOverview.needsPracticeCount
        val senior6520Count = progressOverview.senior6520Total
        val senior6520Studied = progressOverview.senior6520Studied

        val vocabLearned = vocabList.count { it.status == VocabularyStatus.LEARNED }
        val vocabTotal = if (vocabList.isNotEmpty()) vocabList.size else 115

        // Category breakdown using canonical numbers
        val catOverviews = progressOverview.categoryProgressList.map { catData ->
            CategoryOverviewItem(
                name = catData.categoryName,
                totalCount = catData.totalCount,
                studiedCount = catData.studiedCount,
                description = when (catData.categoryName) {
                    "American Government" -> "Principles, system & rights"
                    "American History" -> "Colonial, 1800s & modern"
                    else -> "Geography, symbols & holidays"
                }
            )
        }

        // Streak
        val streak = StudyStreakCalculator.calculateStreak(activeDates.toSet())

        // Recommendation Rules:
        // 1. If user has > 20 unstudied questions:
        //    "Start with your unstudied questions."
        // 2. If recent mistakes exist:
        //    "Review your recent mistakes (X questions need practice)"
        // 3. If a category has low accuracy (< 70%):
        //    "Practice American Government" (or lowest category)
        // 4. If progress is strong:
        //    "Take a Full Practice Test to test your readiness."
        val weakCategories = WeaknessCalculator.rankWeakCategories(questions)
        val lowestAccuracyCategory = weakCategories.firstOrNull { it.totalQuestions > 0 && it.accuracyPercentage < 70 && it.totalIncorrect > 0 }

        val recTitle: String
        val recSubtitle: String
        val recAction: String
        var recCategory: String? = null

        if (unstudiedCount > 20) {
            recTitle = "Start with your unstudied questions"
            recSubtitle = "You have $unstudiedCount unstudied civics questions remaining."
            recAction = "UNSTUDIED"
        } else if (needsPracticeCount > 0) {
            recTitle = "Review your recent mistakes"
            recSubtitle = "$needsPracticeCount questions need practice to achieve mastery."
            recAction = "MISTAKES"
        } else if (lowestAccuracyCategory != null) {
            recTitle = "Practice ${lowestAccuracyCategory.categoryName}"
            recSubtitle = "Accuracy is currently ${lowestAccuracyCategory.accuracyPercentage}%. Strengthen this topic."
            recAction = "CATEGORY"
            recCategory = lowestAccuracyCategory.categoryName
        } else {
            recTitle = "Take a Full Practice Test"
            recSubtitle = "You've studied the civics material. Test your test-day readiness."
            recAction = "FULL_TEST"
        }

        // Continue Learning Label:
        val continueLabel: String
        val continueQId: String?
        val continueQNum: Int?

        if (recentQId != null) {
            val qNum = recentQId.removePrefix("Q").toIntOrNull()
            continueLabel = if (qNum != null) "Continue Question $qNum" else "Continue Study"
            continueQId = recentQId
            continueQNum = qNum
        } else if (studied > 0) {
            continueLabel = "Continue Practice"
            continueQId = null
            continueQNum = null
        } else {
            continueLabel = "Start Your First Practice"
            continueQId = null
            continueQNum = null
        }

        val testsCount = practiceResults.size
        val avgScore = if (testsCount > 0) practiceResults.sumOf { it.percentage } / testsCount else 0

        HomeUiState(
            studiedCount = studied,
            totalCount = total,
            favoriteCount = favoriteCount,
            needsPracticeCount = needsPracticeCount,
            senior6520Count = senior6520Count,
            senior6520Studied = senior6520Studied,
            recentQuestionId = recentQId,
            recentQuestionNumber = continueQNum,
            isReady = true,
            continueQuestionText = continueLabel,
            isFirstSession = studied == 0,
            testsCompleted = testsCount,
            averageScore = avgScore,
            categories = catOverviews,
            studyStreakDays = streak,
            dailyStudyGoal = dailyGoal,
            todayQuestionsCount = todayCount,
            recommendedTitle = recTitle,
            recommendedSubtitle = recSubtitle,
            recommendedActionType = recAction,
            recommendedCategoryName = recCategory,
            continueQuestionId = continueQId,
            continueQuestionNumber = continueQNum,
            recentActivities = recentActivities,
            vocabularyLearnedCount = vocabLearned,
            vocabularyTotalCount = vocabTotal
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(isReady = false)
    )

    companion object {
        fun provideFactory(
            questionRepository: QuestionRepository,
            preferencesRepository: PreferencesRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return HomeViewModel(questionRepository, preferencesRepository) as T
            }
        }
    }
}
