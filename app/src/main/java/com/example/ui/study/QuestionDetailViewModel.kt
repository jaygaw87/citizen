package com.example.ui.study

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.StudyQuestionItem
import com.example.data.repository.QuestionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class QuestionDetailUiState(
    val isLoading: Boolean = true,
    val question: StudyQuestionItem? = null,
    val currentIndex: Int = 0,
    val totalInContext: Int = 128,
    val contextTitle: String = "Question",
    val hasPrevious: Boolean = false,
    val hasNext: Boolean = false,
    val previousQuestionId: String? = null,
    val nextQuestionId: String? = null,
    val errorMessage: String? = null,
    val filter: StudyFilter = StudyFilter.ALL,
    val searchQuery: String = ""
)

class QuestionDetailViewModel(
    private val questionRepository: QuestionRepository,
    initialQuestionId: String,
    initialFilter: StudyFilter = StudyFilter.ALL,
    initialSearchQuery: String = ""
) : ViewModel() {

    private val currentQuestionId = MutableStateFlow(initialQuestionId)
    private val filter = MutableStateFlow(initialFilter)
    private val searchQuery = MutableStateFlow(initialSearchQuery)

    init {
        viewModelScope.launch {
            try {
                questionRepository.ensureInitialized()
            } catch (_: Exception) {
            }
        }
        // Record initial view
        recordView(initialQuestionId)
    }

    private fun recordView(id: String) {
        viewModelScope.launch {
            try {
                questionRepository.recordQuestionView(id)
            } catch (_: Exception) {
                // Ignore transient recording errors
            }
        }
    }

    val uiState: StateFlow<QuestionDetailUiState> = combine(
        questionRepository.getAllStudyQuestions(),
        currentQuestionId,
        filter,
        searchQuery
    ) { allQuestions, activeId, currentFilter, query ->
        if (allQuestions.isEmpty()) {
            return@combine QuestionDetailUiState(
                isLoading = true,
                filter = currentFilter,
                searchQuery = query
            )
        }

        // Apply same filter logic as Study screen
        var contextList = when (currentFilter) {
            StudyFilter.ALL -> allQuestions
            StudyFilter.GOVERNMENT -> allQuestions.filter { it.category == "American Government" }
            StudyFilter.HISTORY -> allQuestions.filter { it.category == "American History" }
            StudyFilter.CIVICS -> allQuestions.filter { it.category == "Integrated Civics" }
            StudyFilter.SENIOR_65_20 -> allQuestions.filter { it.is65_20 }
            StudyFilter.FAVORITES -> allQuestions.filter { it.isFavorite }
            StudyFilter.NEEDS_PRACTICE -> allQuestions.filter { it.needsPractice }
        }

        val trimmedQuery = query.trim()
        if (trimmedQuery.isNotBlank()) {
            val qLower = trimmedQuery.lowercase()
            contextList = contextList.filter { item ->
                item.questionNumber.toString() == trimmedQuery ||
                        item.id.lowercase() == qLower ||
                        item.questionText.lowercase().contains(qLower) ||
                        item.explanation.lowercase().contains(qLower) ||
                        item.category.lowercase().contains(qLower) ||
                        item.subcategory.lowercase().contains(qLower) ||
                        item.answers.any { it.answerText.lowercase().contains(qLower) }
            }
        }

        // Find active question in contextList, fallback to allQuestions if needed
        var index = contextList.indexOfFirst { it.id == activeId }
        val targetQuestion = if (index >= 0) {
            contextList[index]
        } else {
            allQuestions.firstOrNull { it.id == activeId }
        }

        if (targetQuestion == null) {
            return@combine QuestionDetailUiState(
                isLoading = false,
                question = null,
                errorMessage = "Question unavailable",
                filter = currentFilter,
                searchQuery = query
            )
        }

        val effectiveList = if (index >= 0) contextList else allQuestions
        val effectiveIndex = if (index >= 0) index else allQuestions.indexOfFirst { it.id == activeId }
        val totalCount = effectiveList.size

        // Build context-aware title
        val counterText = when {
            trimmedQuery.isNotBlank() -> "Question ${effectiveIndex + 1} of $totalCount results"
            currentFilter == StudyFilter.FAVORITES -> "Question ${effectiveIndex + 1} of $totalCount saved"
            currentFilter == StudyFilter.SENIOR_65_20 -> "Question ${effectiveIndex + 1} of $totalCount (65/20)"
            currentFilter != StudyFilter.ALL -> "Question ${effectiveIndex + 1} of $totalCount"
            else -> "Question ${targetQuestion.questionNumber} of $totalCount"
        }

        val hasPrev = effectiveIndex > 0
        val hasNext = effectiveIndex < totalCount - 1
        val prevId = if (hasPrev) effectiveList[effectiveIndex - 1].id else null
        val nextId = if (hasNext) effectiveList[effectiveIndex + 1].id else null

        QuestionDetailUiState(
            isLoading = false,
            question = targetQuestion,
            currentIndex = effectiveIndex,
            totalInContext = totalCount,
            contextTitle = counterText,
            hasPrevious = hasPrev,
            hasNext = hasNext,
            previousQuestionId = prevId,
            nextQuestionId = nextId,
            errorMessage = null,
            filter = currentFilter,
            searchQuery = query
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = QuestionDetailUiState(isLoading = true)
    )

    fun navigateToQuestion(newId: String) {
        currentQuestionId.value = newId
        recordView(newId)
    }

    fun goToPrevious() {
        val state = uiState.value
        state.previousQuestionId?.let {
            navigateToQuestion(it)
        }
    }

    fun goToNext() {
        val state = uiState.value
        state.nextQuestionId?.let {
            navigateToQuestion(it)
        }
    }

    fun toggleFavorite() {
        val id = currentQuestionId.value
        viewModelScope.launch {
            questionRepository.toggleFavorite(id)
        }
    }

    fun markStudied(isStudied: Boolean = true) {
        val id = currentQuestionId.value
        viewModelScope.launch {
            questionRepository.setStudied(id, isStudied)
        }
    }

    companion object {
        fun provideFactory(
            repository: QuestionRepository,
            initialQuestionId: String,
            initialFilter: StudyFilter = StudyFilter.ALL,
            initialSearchQuery: String = ""
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return QuestionDetailViewModel(
                    repository,
                    initialQuestionId,
                    initialFilter,
                    initialSearchQuery
                ) as T
            }
        }
    }
}
