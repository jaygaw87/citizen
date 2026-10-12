package com.example.ui.senior6520

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.learning.MasteryStatus
import com.example.data.model.StudyQuestionItem
import com.example.data.repository.QuestionRepository
import com.example.domain.usecase.GetLearningProgressUseCase
import com.example.domain.usecase.LearningProgressOverview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SeniorFilter {
    ALL,
    NEEDS_PRACTICE,
    MASTERED,
    FAVORITES;

    companion object {
        fun fromString(value: String): SeniorFilter {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: ALL
        }
    }
}

data class Senior6520UiState(
    val isLoading: Boolean = true,
    val progress: LearningProgressOverview = LearningProgressOverview(),
    val allQuestions: List<StudyQuestionItem> = emptyList(),
    val filteredQuestions: List<StudyQuestionItem> = emptyList(),
    val selectedFilter: SeniorFilter = SeniorFilter.ALL,
    val searchQuery: String = ""
)

class Senior6520ViewModel(
    private val questionRepository: QuestionRepository,
    private val getLearningProgressUseCase: GetLearningProgressUseCase,
    initialFilter: SeniorFilter = SeniorFilter.ALL
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow(initialFilter)
    private val _searchQuery = MutableStateFlow("")

    val uiState: StateFlow<Senior6520UiState> = combine(
        questionRepository.getAllStudyQuestions(),
        getLearningProgressUseCase(),
        _selectedFilter,
        _searchQuery
    ) { questions, progress, filter, query ->
        val seniorQuestions = questions.filter { it.is65_20 }
        val filtered = seniorQuestions.filter { q ->
            val matchesFilter = when (filter) {
                SeniorFilter.ALL -> true
                SeniorFilter.NEEDS_PRACTICE -> q.needsPractice || q.masteryStatus == MasteryStatus.NEEDS_PRACTICE
                SeniorFilter.MASTERED -> q.masteryStatus == MasteryStatus.MASTERED
                SeniorFilter.FAVORITES -> q.isFavorite
            }
            val matchesQuery = if (query.isBlank()) true else {
                q.questionText.contains(query, ignoreCase = true) ||
                    q.answers.any { it.answerText.contains(query, ignoreCase = true) } ||
                    q.questionNumber.toString().contains(query)
            }
            matchesFilter && matchesQuery
        }

        Senior6520UiState(
            isLoading = false,
            progress = progress,
            allQuestions = seniorQuestions,
            filteredQuestions = filtered,
            selectedFilter = filter,
            searchQuery = query
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = Senior6520UiState()
    )

    fun setFilter(filter: SeniorFilter) {
        _selectedFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleFavorite(questionId: String) {
        viewModelScope.launch {
            questionRepository.toggleFavorite(questionId)
        }
    }

    companion object {
        fun provideFactory(
            questionRepository: QuestionRepository,
            getLearningProgressUseCase: GetLearningProgressUseCase = GetLearningProgressUseCase(questionRepository),
            initialFilter: SeniorFilter = SeniorFilter.ALL
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return Senior6520ViewModel(questionRepository, getLearningProgressUseCase, initialFilter) as T
            }
        }
    }
}
