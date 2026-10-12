package com.example.ui.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.learning.MasteryCalculator
import com.example.data.learning.MasteryStatus
import com.example.data.learning.WeaknessCalculator
import com.example.data.local.entity.StudyActivityEntity
import com.example.data.model.StudyQuestionItem
import com.example.data.repository.QuestionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class CategoryProgressUiState(
    val categoryName: String = "",
    val totalCount: Int = 0,
    val studiedCount: Int = 0,
    val masteredCount: Int = 0,
    val learningCount: Int = 0,
    val needsPracticeCount: Int = 0,
    val notStartedCount: Int = 0,
    val accuracyPercentage: Int = 0,
    val weakQuestions: List<StudyQuestionItem> = emptyList(),
    val strongQuestions: List<StudyQuestionItem> = emptyList(),
    val recentActivities: List<StudyActivityEntity> = emptyList(),
    val isLoading: Boolean = false
) {
    val progressPercentage: Int
        get() = if (totalCount > 0) ((studiedCount * 100f) / totalCount).toInt().coerceIn(0, 100) else 0
}

class CategoryProgressViewModel(
    private val categoryName: String,
    private val questionRepository: QuestionRepository
) : ViewModel() {

    val uiState: StateFlow<CategoryProgressUiState> = combine(
        questionRepository.getAllStudyQuestions(),
        questionRepository.getRecentActivities(20)
    ) { allQuestions, activities ->
        val catQuestions = allQuestions.filter { it.category.equals(categoryName, ignoreCase = true) }
        val total = catQuestions.size

        val studied = catQuestions.count { it.isStudied }
        val mastered = catQuestions.count { it.masteryStatus == MasteryStatus.MASTERED }
        val learning = catQuestions.count { it.masteryStatus == MasteryStatus.LEARNING }
        val needsPractice = catQuestions.count { it.masteryStatus == MasteryStatus.NEEDS_PRACTICE }
        val notStarted = catQuestions.count { it.masteryStatus == MasteryStatus.NOT_STARTED }

        val totalAttempts = catQuestions.sumOf { it.totalAttempts }
        val totalCorrect = catQuestions.sumOf { it.correctCount }
        val accuracy = if (totalAttempts > 0) {
            ((totalCorrect * 100f) / totalAttempts).toInt()
        } else {
            0
        }

        val weak = WeaknessCalculator.rankWeakQuestions(catQuestions)
        val strong = catQuestions.filter { it.masteryStatus == MasteryStatus.MASTERED }

        // Filter activities relevant to this category or questions in this category
        val catQuestionNumbers = catQuestions.map { "Question ${it.questionNumber}" }.toSet()
        val catActivities = activities.filter { act ->
            act.details.contains(categoryName, ignoreCase = true) ||
                    catQuestionNumbers.any { act.details.contains(it, ignoreCase = true) }
        }.take(5)

        CategoryProgressUiState(
            categoryName = categoryName,
            totalCount = total,
            studiedCount = studied,
            masteredCount = mastered,
            learningCount = learning,
            needsPracticeCount = needsPractice,
            notStartedCount = notStarted,
            accuracyPercentage = accuracy,
            weakQuestions = weak,
            strongQuestions = strong,
            recentActivities = catActivities,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CategoryProgressUiState(categoryName = categoryName, isLoading = true)
    )

    companion object {
        fun provideFactory(
            categoryName: String,
            repository: QuestionRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return CategoryProgressViewModel(categoryName, repository) as T
            }
        }
    }
}
