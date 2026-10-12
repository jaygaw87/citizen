package com.example.ui.practice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.PracticeResultEntity
import com.example.data.repository.QuestionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PracticeHomeUiState(
    val favoriteCount: Int = 0,
    val needsPracticeCount: Int = 0,
    val totalCount: Int = 128,
    val governmentCount: Int = 75,
    val historyCount: Int = 43,
    val civicsCount: Int = 10,
    val seniorCount: Int = 20,
    val recentResults: List<PracticeResultEntity> = emptyList(),
    val latestResult: PracticeResultEntity? = null,
    val isLoading: Boolean = false
)

class PracticeHomeViewModel(
    private val questionRepository: QuestionRepository
) : ViewModel() {

    init {
        viewModelScope.launch {
            try {
                questionRepository.ensureInitialized()
            } catch (_: Exception) {
            }
        }
    }

    val uiState: StateFlow<PracticeHomeUiState> = combine(
        questionRepository.getFavoriteCount(),
        questionRepository.getNeedsPracticeCount(),
        questionRepository.getRecentPracticeResults(5),
        questionRepository.getLatestPracticeResult()
    ) { favCount, needsCount, recentResults, latestResult ->
        PracticeHomeUiState(
            favoriteCount = favCount,
            needsPracticeCount = needsCount,
            totalCount = 128,
            governmentCount = 75,
            historyCount = 43,
            civicsCount = 10,
            seniorCount = 20,
            recentResults = recentResults,
            latestResult = latestResult,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PracticeHomeUiState(isLoading = true)
    )

    companion object {
        fun provideFactory(repository: QuestionRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return PracticeHomeViewModel(repository) as T
                }
            }
    }
}
