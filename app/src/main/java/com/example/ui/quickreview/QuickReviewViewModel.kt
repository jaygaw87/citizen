package com.example.ui.quickreview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.learning.MasteryStatus
import com.example.data.learning.WeaknessCalculator
import com.example.data.model.StudyQuestionItem
import com.example.data.repository.QuestionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class QuickReviewSection(val displayName: String, val icon: String) {
    ALL("All", "📋"),
    NEEDS_PRACTICE("Needs Practice", "⚠️"),
    FAVORITES("Favorites", "❤️"),
    RECENTLY_MISSED("Recently Missed", "🧠"),
    MASTERED("Mastered", "🏆"),
    RECENTLY_VIEWED("Recently Viewed", "🕐")
}

data class QuickReviewUiState(
    val isLoading: Boolean = true,
    val selectedSection: QuickReviewSection = QuickReviewSection.ALL,
    val needsPracticeQuestions: List<StudyQuestionItem> = emptyList(),
    val favoriteQuestions: List<StudyQuestionItem> = emptyList(),
    val recentlyMissedQuestions: List<StudyQuestionItem> = emptyList(),
    val masteredQuestions: List<StudyQuestionItem> = emptyList(),
    val recentlyViewedQuestions: List<StudyQuestionItem> = emptyList(),
    val continueLearningQuestions: List<StudyQuestionItem> = emptyList(),
    val currentDisplayQuestions: List<StudyQuestionItem> = emptyList(),
    val searchQuery: String = ""
)

class QuickReviewViewModel(
    private val questionRepository: QuestionRepository,
    initialSection: QuickReviewSection = QuickReviewSection.ALL
) : ViewModel() {

    private val _selectedSection = MutableStateFlow(initialSection)
    private val _searchQuery = MutableStateFlow("")

    val uiState: StateFlow<QuickReviewUiState> = combine(
        questionRepository.getAllStudyQuestions(),
        _selectedSection,
        _searchQuery
    ) { questions, section, query ->
        val needsPractice = questions.filter { it.needsPractice || it.masteryStatus == MasteryStatus.NEEDS_PRACTICE || WeaknessCalculator.isWeakQuestion(it) }
        val favorites = questions.filter { it.isFavorite }
        val missed = questions.filter { it.incorrectCount > 0 }.sortedByDescending { it.incorrectCount }
        val mastered = questions.filter { it.masteryStatus == MasteryStatus.MASTERED }
        val viewed = questions.filter { it.lastViewed > 0L }.sortedByDescending { it.lastViewed }
        val continueLearning = questions.filter { it.isStudied && it.masteryStatus != MasteryStatus.MASTERED }
            .sortedByDescending { it.lastViewed }

        val activeList = when (section) {
            QuickReviewSection.ALL -> questions
            QuickReviewSection.NEEDS_PRACTICE -> needsPractice
            QuickReviewSection.FAVORITES -> favorites
            QuickReviewSection.RECENTLY_MISSED -> missed
            QuickReviewSection.MASTERED -> mastered
            QuickReviewSection.RECENTLY_VIEWED -> viewed
        }

        val filtered = if (query.isBlank()) activeList else {
            activeList.filter { q ->
                q.questionText.contains(query, ignoreCase = true) ||
                    q.answers.any { it.answerText.contains(query, ignoreCase = true) } ||
                    q.questionNumber.toString().contains(query)
            }
        }

        QuickReviewUiState(
            isLoading = false,
            selectedSection = section,
            needsPracticeQuestions = needsPractice,
            favoriteQuestions = favorites,
            recentlyMissedQuestions = missed,
            masteredQuestions = mastered,
            recentlyViewedQuestions = viewed,
            continueLearningQuestions = continueLearning,
            currentDisplayQuestions = filtered,
            searchQuery = query
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = QuickReviewUiState()
    )

    fun setSection(section: QuickReviewSection) {
        _selectedSection.value = section
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
            initialSection: QuickReviewSection = QuickReviewSection.ALL
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return QuickReviewViewModel(questionRepository, initialSection) as T
            }
        }
    }
}
