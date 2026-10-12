package com.example.ui.weakareas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.learning.CategoryWeakness
import com.example.data.learning.WeaknessCalculator
import com.example.data.model.StudyQuestionItem
import com.example.data.repository.QuestionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class WeakAreasUiState(
    val weakQuestions: List<StudyQuestionItem> = emptyList(),
    val weakCategories: List<CategoryWeakness> = emptyList(),
    val totalMistakesCount: Int = 0,
    val isLoading: Boolean = false
)

class WeakAreasViewModel(
    private val questionRepository: QuestionRepository
) : ViewModel() {

    val uiState: StateFlow<WeakAreasUiState> = questionRepository.getAllStudyQuestions()
        .map { allQuestions ->
            val rankedWeakQuestions = WeaknessCalculator.rankWeakQuestions(allQuestions)
            val rankedCategories = WeaknessCalculator.rankWeakCategories(allQuestions)
            val totalMistakes = allQuestions.sumOf { it.incorrectCount }

            WeakAreasUiState(
                weakQuestions = rankedWeakQuestions,
                weakCategories = rankedCategories,
                totalMistakesCount = totalMistakes,
                isLoading = false
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = WeakAreasUiState(isLoading = true)
        )

    companion object {
        fun provideFactory(repository: QuestionRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return WeakAreasViewModel(repository) as T
                }
            }
    }
}
