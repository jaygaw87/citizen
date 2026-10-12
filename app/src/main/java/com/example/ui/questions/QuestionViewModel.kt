package com.example.ui.questions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.QuestionWithAnswers
import com.example.data.repository.QuestionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class QuestionFilter {
    ALL,
    SENIOR_65_20,
    DYNAMIC
}

data class QuestionListUiState(
    val isLoading: Boolean = true,
    val questions: List<QuestionWithAnswers> = emptyList(),
    val totalCount: Int = 0,
    val seniorCount: Int = 0,
    val dynamicCount: Int = 0,
    val categories: List<String> = emptyList(),
    val selectedCategory: String? = null,
    val selectedFilter: QuestionFilter = QuestionFilter.ALL,
    val searchQuery: String = "",
    val errorMessage: String? = null
)

class QuestionViewModel(
    private val questionRepository: QuestionRepository
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val selectedCategory = MutableStateFlow<String?>(null)
    private val selectedFilter = MutableStateFlow(QuestionFilter.ALL)
    private val isInitializing = MutableStateFlow(true)
    private val initError = MutableStateFlow<String?>(null)

    init {
        initializeDatabase()
    }

    private fun initializeDatabase() {
        viewModelScope.launch {
            isInitializing.value = true
            try {
                questionRepository.ensureInitialized()
                initError.value = null
            } catch (e: Exception) {
                initError.value = e.localizedMessage ?: "Failed to load questions database"
            } finally {
                isInitializing.value = false
            }
        }
    }

    private val filterParams = combine(
        searchQuery,
        selectedCategory,
        selectedFilter
    ) { query, cat, filter ->
        Triple(query, cat, filter)
    }

    val uiState: StateFlow<QuestionListUiState> = combine(
        questionRepository.getAllQuestionsWithAnswers(),
        filterParams,
        questionRepository.getAllCategories(),
        isInitializing,
        initError
    ) { rawQuestions, (query, cat, filter), categories, loading, error ->
        var filtered = rawQuestions

        // Filter by 65/20 or Dynamic
        when (filter) {
            QuestionFilter.ALL -> {}
            QuestionFilter.SENIOR_65_20 -> filtered = filtered.filter { it.question.is65_20 }
            QuestionFilter.DYNAMIC -> filtered = filtered.filter { it.question.isDynamicAnswer }
        }

        // Filter by category
        if (!cat.isNullOrBlank()) {
            filtered = filtered.filter { it.question.category == cat }
        }

        // Filter by search query
        if (query.isNotBlank()) {
            val qLower = query.trim().lowercase()
            filtered = filtered.filter { item ->
                item.question.questionText.lowercase().contains(qLower) ||
                        item.question.explanation.lowercase().contains(qLower) ||
                        item.question.category.lowercase().contains(qLower) ||
                        item.question.subcategory.lowercase().contains(qLower) ||
                        item.answers.any { it.answerText.lowercase().contains(qLower) }
            }
        }

        val totalCount = rawQuestions.size
        val seniorCount = rawQuestions.count { it.question.is65_20 }
        val dynamicCount = rawQuestions.count { it.question.isDynamicAnswer }

        QuestionListUiState(
            isLoading = loading && rawQuestions.isEmpty(),
            questions = filtered,
            totalCount = totalCount,
            seniorCount = seniorCount,
            dynamicCount = dynamicCount,
            categories = categories,
            selectedCategory = cat,
            selectedFilter = filter,
            searchQuery = query,
            errorMessage = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = QuestionListUiState()
    )

    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
    }

    fun onCategorySelected(category: String?) {
        selectedCategory.value = category
    }

    fun onFilterSelected(filter: QuestionFilter) {
        selectedFilter.value = filter
    }

    fun retry() {
        initializeDatabase()
    }

    companion object {
        fun provideFactory(repository: QuestionRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return QuestionViewModel(repository) as T
                }
            }
    }
}
