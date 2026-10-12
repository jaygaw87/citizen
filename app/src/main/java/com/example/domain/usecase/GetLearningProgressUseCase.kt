package com.example.domain.usecase

import com.example.data.learning.MasteryStatus
import com.example.data.model.CategoryProgressData
import com.example.data.model.StudyQuestionItem
import com.example.data.model.TestConstants
import com.example.data.repository.QuestionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Single Canonical Source of Truth for Learning Progress Metrics.
 * Used identically across Home, Progress, Study, and Category screens.
 */
data class LearningProgressOverview(
    val totalQuestions: Int = TestConstants.TOTAL_CIVICS_QUESTIONS,
    val studiedCount: Int = 0,
    val masteredCount: Int = 0,
    val needsPracticeCount: Int = 0,
    val learningCount: Int = 0,
    val notStartedCount: Int = TestConstants.TOTAL_CIVICS_QUESTIONS,
    val favoriteCount: Int = 0,
    val progressPercentage: Int = 0,
    val senior6520Total: Int = 20,
    val senior6520Studied: Int = 0,
    val senior6520Mastered: Int = 0,
    val senior6520NeedsPractice: Int = 0,
    val senior6520Learning: Int = 0,
    val senior6520NotStarted: Int = 20,
    val senior6520Favorites: Int = 0,
    val senior6520Percentage: Int = 0,
    val categoryProgressList: List<CategoryProgressData> = emptyList()
)

class GetLearningProgressUseCase(
    private val questionRepository: QuestionRepository
) {
    operator fun invoke(): Flow<LearningProgressOverview> {
        return questionRepository.getAllStudyQuestions().map { questions ->
            calculate(questions)
        }
    }

    companion object {
        fun calculate(questions: List<StudyQuestionItem>): LearningProgressOverview {
            val total = questions.size.coerceAtLeast(TestConstants.TOTAL_CIVICS_QUESTIONS)
            val studied = questions.count { it.isStudied }
            val mastered = questions.count { it.masteryStatus == MasteryStatus.MASTERED }
            val needsPractice = questions.count { it.masteryStatus == MasteryStatus.NEEDS_PRACTICE }
            val learning = questions.count { it.masteryStatus == MasteryStatus.LEARNING }
            val notStarted = questions.count { it.masteryStatus == MasteryStatus.NOT_STARTED }
            val favorites = questions.count { it.isFavorite }
            val progressPercent = if (total > 0) ((studied * 100f) / total).toInt().coerceIn(0, 100) else 0

            // Senior 65/20 Breakdown (Official 20 questions)
            val seniorList = questions.filter { it.is65_20 }
            val seniorTotal = if (seniorList.isNotEmpty()) seniorList.size else 20
            val seniorStudied = seniorList.count { it.isStudied }
            val seniorMastered = seniorList.count { it.masteryStatus == MasteryStatus.MASTERED }
            val seniorNeedsPractice = seniorList.count { it.masteryStatus == MasteryStatus.NEEDS_PRACTICE }
            val seniorLearning = seniorList.count { it.masteryStatus == MasteryStatus.LEARNING }
            val seniorNotStarted = seniorList.count { it.masteryStatus == MasteryStatus.NOT_STARTED }
            val seniorFavorites = seniorList.count { it.isFavorite }
            val seniorPercentage = if (seniorTotal > 0) ((seniorStudied * 100f) / seniorTotal).toInt().coerceIn(0, 100) else 0

            // Category breakdown:
            // American Government: 75
            // American History: 43
            // Integrated Civics: 10
            val categories = listOf("American Government", "American History", "Integrated Civics")
            val catDataList = categories.map { catName ->
                val catQuestions = questions.filter { it.category.equals(catName, ignoreCase = true) }
                val expectedTotal = when (catName) {
                    "American Government" -> 75
                    "American History" -> 43
                    else -> 10
                }
                val catTotal = if (catQuestions.isNotEmpty()) catQuestions.size else expectedTotal
                val catStudied = catQuestions.count { it.isStudied }
                val catMastered = catQuestions.count { it.masteryStatus == MasteryStatus.MASTERED }
                val catNeedsPractice = catQuestions.count { it.masteryStatus == MasteryStatus.NEEDS_PRACTICE }

                val catAttempts = catQuestions.sumOf { it.totalAttempts }
                val catCorrect = catQuestions.sumOf { it.correctCount }
                val accuracy = if (catAttempts > 0) ((catCorrect * 100f) / catAttempts).toInt() else 0

                CategoryProgressData(
                    categoryName = catName,
                    totalCount = catTotal,
                    studiedCount = catStudied,
                    masteredCount = catMastered,
                    needsPracticeCount = catNeedsPractice,
                    accuracyPercentage = accuracy
                )
            }

            return LearningProgressOverview(
                totalQuestions = total,
                studiedCount = studied,
                masteredCount = mastered,
                needsPracticeCount = needsPractice,
                learningCount = learning,
                notStartedCount = notStarted,
                favoriteCount = favorites,
                progressPercentage = progressPercent,
                senior6520Total = seniorTotal,
                senior6520Studied = seniorStudied,
                senior6520Mastered = seniorMastered,
                senior6520NeedsPractice = seniorNeedsPractice,
                senior6520Learning = seniorLearning,
                senior6520NotStarted = seniorNotStarted,
                senior6520Favorites = seniorFavorites,
                senior6520Percentage = seniorPercentage,
                categoryProgressList = catDataList
            )
        }
    }
}
