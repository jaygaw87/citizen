package com.example.ui.practice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.PracticeResultEntity
import com.example.data.model.ReviewAnswerItem
import com.example.data.repository.QuestionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PracticeResultUiState(
    val result: PracticeResultEntity? = null,
    val reviewItems: List<ReviewAnswerItem> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
) {
    val totalCount: Int
        get() = result?.totalQuestions ?: reviewItems.size

    val correctCount: Int
        get() = result?.correctAnswers ?: reviewItems.count { it.isCorrect }

    val incorrectCount: Int
        get() = result?.incorrectAnswers ?: reviewItems.count { !it.isCorrect }

    val percentage: Int
        get() = result?.percentage ?: if (totalCount > 0) (correctCount * 100) / totalCount else 0

    val isPassed: Boolean
        get() = result?.passed ?: (percentage >= 60)

    val isFullPractice: Boolean
        get() = result?.practiceType == "FULL" || totalCount == 20

    val hasMistakes: Boolean
        get() = incorrectCount > 0
}

class PracticeResultViewModel(
    private val questionRepository: QuestionRepository,
    private val resultId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(PracticeResultUiState(isLoading = true))
    val uiState: StateFlow<PracticeResultUiState> = _uiState.asStateFlow()

    init {
        loadResult()
    }

    private fun loadResult() {
        viewModelScope.launch {
            try {
                questionRepository.ensureInitialized()
                val entity = questionRepository.getPracticeResultByIdDirect(resultId)
                if (entity != null) {
                    val items = ReviewAnswerItem.listFromJsonString(entity.questionResultsJson)
                    _uiState.update {
                        it.copy(
                            result = entity,
                            reviewItems = items,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Test result could not be found."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error loading test result."
                    )
                }
            }
        }
    }

    companion object {
        fun provideFactory(
            repository: QuestionRepository,
            resultId: String
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return PracticeResultViewModel(repository, resultId) as T
            }
        }
    }
}
