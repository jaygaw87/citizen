package com.example.ui.study

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.StudyQuestionItem
import com.example.data.repository.QuestionRepository
import com.example.domain.usecase.GetLearningProgressUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CategoryProgress(
    val categoryName: String,
    val totalCount: Int,
    val studiedCount: Int
) {
    val percentage: Int
        get() = if (totalCount > 0) ((studiedCount * 100f) / totalCount).toInt() else 0
}

data class StudyScreenUiState(
    val questions: List<StudyQuestionItem> = emptyList(),
    val totalCount: Int = 128,
    val studiedCount: Int = 0,
    val favoriteCount: Int = 0,
    val needsPracticeCount: Int = 0,
    val masteredCount: Int = 0,
    val notStartedCount: Int = 128,
    val senior6520Count: Int = 20,
    val senior6520StudiedCount: Int = 0,
    val selectedFilter: StudyFilter = StudyFilter.ALL,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val categoryProgressList: List<CategoryProgress> = emptyList(),
    val errorMessage: String? = null
) {
    val progressPercentage: Int
        get() = if (totalCount > 0) ((studiedCount * 100f) / totalCount).toInt().coerceIn(0, 100) else 0

    val filteredCount: Int
        get() = questions.size
}

class StudyViewModel(
    private val questionRepository: QuestionRepository,
    initialFilter: StudyFilter = StudyFilter.ALL
) : ViewModel() {

    private val selectedFilter = MutableStateFlow(initialFilter)
    private val searchQuery = MutableStateFlow("")

    init {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                questionRepository.ensureInitialized()
            } catch (e: Exception) {
                Log.e(TAG, "[StudyViewModel] ensureInitialized failed", e)
            }
        }
    }

    val uiState: StateFlow<StudyScreenUiState> = combine(
        questionRepository.getAllStudyQuestions(),
        selectedFilter,
        searchQuery
    ) { allQuestions, filter, query ->
        val overview = GetLearningProgressUseCase.calculate(allQuestions)
        val totalCount = overview.totalQuestions
        val studiedCount = overview.studiedCount
        val favoriteCount = overview.favoriteCount
        val needsPracticeCount = overview.needsPracticeCount

        val seniorTotal = overview.senior6520Total
        val seniorStudied = overview.senior6520Studied

        // Category breakdown
        val gov = allQuestions.filter { it.category == "American Government" }
        val hist = allQuestions.filter { it.category == "American History" }
        val civics = allQuestions.filter { it.category == "Integrated Civics" }

        val categoryProgressList = overview.categoryProgressList.map {
            CategoryProgress(it.categoryName, it.totalCount, it.studiedCount)
        } + CategoryProgress("65/20 Special Consideration", seniorTotal, seniorStudied)

        // 1. Filter by category / tag
        var filtered = when (filter) {
            StudyFilter.ALL -> allQuestions
            StudyFilter.GOVERNMENT -> gov
            StudyFilter.HISTORY -> hist
            StudyFilter.CIVICS -> civics
            StudyFilter.SENIOR_65_20 -> allQuestions.filter { it.is65_20 }
            StudyFilter.FAVORITES -> allQuestions.filter { it.isFavorite }
            StudyFilter.NEEDS_PRACTICE -> allQuestions.filter { it.needsPractice }
        }

        // 2. Filter by search query
        val trimmedQuery = query.trim()
        if (trimmedQuery.isNotBlank()) {
            val qLower = trimmedQuery.lowercase()
            filtered = filtered.filter { item ->
                item.questionNumber.toString() == trimmedQuery ||
                        item.id.lowercase() == qLower ||
                        item.questionText.lowercase().contains(qLower) ||
                        item.explanation.lowercase().contains(qLower) ||
                        item.category.lowercase().contains(qLower) ||
                        item.subcategory.lowercase().contains(qLower) ||
                        item.answers.any { it.answerText.lowercase().contains(qLower) }
            }
        }

        Log.i(TAG, "[VIEWMODEL] questions = ${filtered.size}")

        StudyScreenUiState(
            questions = filtered,
            totalCount = if (totalCount > 0) totalCount else 128,
            studiedCount = studiedCount,
            favoriteCount = favoriteCount,
            needsPracticeCount = needsPracticeCount,
            masteredCount = overview.masteredCount,
            notStartedCount = overview.notStartedCount,
            senior6520Count = seniorTotal,
            senior6520StudiedCount = seniorStudied,
            selectedFilter = filter,
            searchQuery = query,
            isLoading = false,
            categoryProgressList = categoryProgressList,
            errorMessage = null
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StudyScreenUiState(isLoading = true, selectedFilter = initialFilter)
    )

    fun onFilterSelected(filter: StudyFilter) {
        selectedFilter.value = filter
    }

    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
    }

    fun toggleFavorite(questionId: String) {
        viewModelScope.launch {
            questionRepository.toggleFavorite(questionId)
        }
    }

    fun markStudied(questionId: String, isStudied: Boolean = true) {
        viewModelScope.launch {
            questionRepository.setStudied(questionId, isStudied)
        }
    }

    companion object {
        private const val TAG = "CivicsDiagnostic"

        fun provideFactory(
            repository: QuestionRepository,
            initialFilter: StudyFilter = StudyFilter.ALL
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return StudyViewModel(repository, initialFilter) as T
            }
        }
    }
}
