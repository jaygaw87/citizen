package com.example.ui.practice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.PracticeResultEntity
import com.example.data.repository.QuestionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PracticeHistoryUiState(
    val results: List<PracticeResultEntity> = emptyList(),
    val isLoading: Boolean = false
) {
    val totalTests: Int
        get() = results.size

    val passedTests: Int
        get() = results.count { it.passed }

    val averageScore: Int
        get() = if (results.isNotEmpty()) results.sumOf { it.percentage } / results.size else 0
}

class PracticeHistoryViewModel(
    private val questionRepository: QuestionRepository
) : ViewModel() {

    val uiState: StateFlow<PracticeHistoryUiState> = questionRepository.getAllPracticeResults()
        .map { list ->
            PracticeHistoryUiState(
                results = list,
                isLoading = false
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PracticeHistoryUiState(isLoading = true)
        )

    fun clearHistory() {
        viewModelScope.launch {
            questionRepository.clearPracticeHistory()
        }
    }

    fun deleteResult(id: String) {
        viewModelScope.launch {
            questionRepository.deletePracticeResult(id)
        }
    }

    companion object {
        fun provideFactory(repository: QuestionRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return PracticeHistoryViewModel(repository) as T
                }
            }
    }
}
