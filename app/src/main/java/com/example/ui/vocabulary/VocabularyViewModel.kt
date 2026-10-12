package com.example.ui.vocabulary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.VocabularyItem
import com.example.data.model.VocabularyStatus
import com.example.data.repository.QuestionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class VocabFilter {
    ALL,
    FAVORITES,
    NEEDS_REVIEW,
    LEARNED,
    NOT_STARTED
}

data class VocabularyProgressOverview(
    val totalCount: Int = 0,
    val learnedCount: Int = 0,
    val needsReviewCount: Int = 0,
    val learningCount: Int = 0,
    val notStartedCount: Int = 0,
    val favoriteCount: Int = 0,
    val progressPercentage: Int = 0
)

data class VocabularyUiState(
    val isLoading: Boolean = true,
    val allItems: List<VocabularyItem> = emptyList(),
    val filteredItems: List<VocabularyItem> = emptyList(),
    val categories: List<String> = emptyList(),
    val selectedCategory: String = "ALL",
    val selectedFilter: VocabFilter = VocabFilter.ALL,
    val searchQuery: String = "",
    val progress: VocabularyProgressOverview = VocabularyProgressOverview()
)

class VocabularyViewModel(
    private val questionRepository: QuestionRepository
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow(VocabFilter.ALL)
    private val _selectedCategory = MutableStateFlow("ALL")
    private val _searchQuery = MutableStateFlow("")

    val uiState: StateFlow<VocabularyUiState> = combine(
        questionRepository.getAllVocabulary(),
        _selectedFilter,
        _selectedCategory,
        _searchQuery
    ) { allItems, filter, category, query ->
        val categories = listOf("ALL") + allItems.map { it.category }.distinct().sorted()

        val learned = allItems.count { it.status == VocabularyStatus.LEARNED }
        val needsReview = allItems.count { it.status == VocabularyStatus.NEEDS_REVIEW }
        val learning = allItems.count { it.status == VocabularyStatus.LEARNING }
        val notStarted = allItems.count { it.status == VocabularyStatus.NOT_STARTED }
        val favorites = allItems.count { it.isFavorite }
        val total = allItems.size
        val percent = if (total > 0) ((learned * 100f) / total).toInt().coerceIn(0, 100) else 0

        val progress = VocabularyProgressOverview(
            totalCount = total,
            learnedCount = learned,
            needsReviewCount = needsReview,
            learningCount = learning,
            notStartedCount = notStarted,
            favoriteCount = favorites,
            progressPercentage = percent
        )

        val filtered = allItems.filter { item ->
            val matchesCategory = if (category == "ALL") true else item.category.equals(category, ignoreCase = true)
            val matchesFilter = when (filter) {
                VocabFilter.ALL -> true
                VocabFilter.FAVORITES -> item.isFavorite
                VocabFilter.NEEDS_REVIEW -> item.status == VocabularyStatus.NEEDS_REVIEW
                VocabFilter.LEARNED -> item.status == VocabularyStatus.LEARNED
                VocabFilter.NOT_STARTED -> item.status == VocabularyStatus.NOT_STARTED
            }
            val matchesQuery = if (query.isBlank()) true else {
                item.word.contains(query, ignoreCase = true) ||
                    item.simpleDefinition.contains(query, ignoreCase = true) ||
                    item.detailedDefinition.contains(query, ignoreCase = true) ||
                    item.exampleSentence.contains(query, ignoreCase = true) ||
                    item.category.contains(query, ignoreCase = true)
            }
            matchesCategory && matchesFilter && matchesQuery
        }

        VocabularyUiState(
            isLoading = false,
            allItems = allItems,
            filteredItems = filtered,
            categories = categories,
            selectedCategory = category,
            selectedFilter = filter,
            searchQuery = query,
            progress = progress
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = VocabularyUiState()
    )

    fun setFilter(filter: VocabFilter) {
        _selectedFilter.value = filter
    }

    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleFavorite(vocabId: String) {
        viewModelScope.launch {
            questionRepository.toggleVocabularyFavorite(vocabId)
        }
    }

    fun updateStatus(vocabId: String, status: VocabularyStatus) {
        viewModelScope.launch {
            questionRepository.updateVocabularyStatus(vocabId, status)
        }
    }

    companion object {
        fun provideFactory(
            questionRepository: QuestionRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return VocabularyViewModel(questionRepository) as T
            }
        }
    }
}
