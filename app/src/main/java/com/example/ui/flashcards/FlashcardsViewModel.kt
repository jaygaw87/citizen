package com.example.ui.flashcards

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.StudyQuestionItem
import com.example.data.repository.QuestionRepository
import com.example.speech.AppTextToSpeechHelper
import com.example.ui.speaking.TtsMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class FlashcardFilter(val label: String) {
    ALL("All 128"),
    GOVERNMENT("American Government"),
    HISTORY("American History"),
    CIVICS("Integrated Civics"),
    SENIOR_65_20("65/20 Special"),
    FAVORITES("Favorites"),
    NEEDS_PRACTICE("Needs Practice");

    companion object {
        fun fromString(value: String): FlashcardFilter {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) || it.label.equals(value, ignoreCase = true) } ?: ALL
        }
    }
}

data class FlashcardsUiState(
    val allQuestions: List<StudyQuestionItem> = emptyList(),
    val displayQuestions: List<StudyQuestionItem> = emptyList(),
    val currentIndex: Int = 0,
    val isFlipped: Boolean = false,
    val isShuffled: Boolean = false,
    val selectedFilter: FlashcardFilter = FlashcardFilter.ALL,
    val isLoading: Boolean = true,
    val isTtsSpeaking: Boolean = false,
    val ttsMode: TtsMode = TtsMode.NONE
) {
    val totalCount: Int
        get() = displayQuestions.size

    val currentCard: StudyQuestionItem?
        get() = displayQuestions.getOrNull(currentIndex)

    val progressFraction: Float
        get() = if (totalCount > 0) (currentIndex + 1).toFloat() / totalCount else 0f
}

class FlashcardsViewModel(
    application: Application,
    private val questionRepository: QuestionRepository,
    initialFilter: FlashcardFilter = FlashcardFilter.ALL
) : AndroidViewModel(application) {

    private val ttsHelper = AppTextToSpeechHelper(application)

    private val _uiState = MutableStateFlow(
        FlashcardsUiState(
            selectedFilter = initialFilter,
            isLoading = true
        )
    )
    val uiState: StateFlow<FlashcardsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                questionRepository.ensureInitialized()
            } catch (_: Exception) {
            }
        }

        viewModelScope.launch {
            ttsHelper.isSpeaking.collect { isSpeaking ->
                _uiState.update {
                    it.copy(
                        isTtsSpeaking = isSpeaking,
                        ttsMode = if (isSpeaking) it.ttsMode else TtsMode.NONE
                    )
                }
            }
        }

        questionRepository.getAllStudyQuestions()
            .onEach { questions ->
                _uiState.update { state ->
                    val filtered = filterQuestions(questions, state.selectedFilter, state.isShuffled)
                    val safeIndex = if (filtered.isEmpty()) 0 else state.currentIndex.coerceIn(0, filtered.size - 1)
                    state.copy(
                        allQuestions = questions,
                        displayQuestions = filtered,
                        currentIndex = safeIndex,
                        isLoading = false
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun filterQuestions(
        questions: List<StudyQuestionItem>,
        filter: FlashcardFilter,
        shuffle: Boolean
    ): List<StudyQuestionItem> {
        val filtered = when (filter) {
            FlashcardFilter.ALL -> questions
            FlashcardFilter.GOVERNMENT -> questions.filter { it.category.equals("American Government", ignoreCase = true) }
            FlashcardFilter.HISTORY -> questions.filter { it.category.equals("American History", ignoreCase = true) }
            FlashcardFilter.CIVICS -> questions.filter { it.category.equals("Integrated Civics", ignoreCase = true) }
            FlashcardFilter.SENIOR_65_20 -> questions.filter { it.is65_20 }
            FlashcardFilter.FAVORITES -> questions.filter { it.isFavorite }
            FlashcardFilter.NEEDS_PRACTICE -> questions.filter { it.needsPractice }
        }
        return if (shuffle) filtered.shuffled() else filtered
    }

    fun setFilter(filter: FlashcardFilter) {
        ttsHelper.stop()
        _uiState.update { state ->
            val filtered = filterQuestions(state.allQuestions, filter, state.isShuffled)
            state.copy(
                selectedFilter = filter,
                displayQuestions = filtered,
                currentIndex = 0,
                isFlipped = false
            )
        }
    }

    fun flipCard() {
        ttsHelper.stop()
        _uiState.update { it.copy(isFlipped = !it.isFlipped) }
    }

    fun nextCard() {
        ttsHelper.stop()
        _uiState.update { state ->
            if (state.currentIndex < state.displayQuestions.size - 1) {
                state.copy(currentIndex = state.currentIndex + 1, isFlipped = false)
            } else {
                state.copy(currentIndex = 0, isFlipped = false) // Wrap around
            }
        }
    }

    fun previousCard() {
        ttsHelper.stop()
        _uiState.update { state ->
            if (state.currentIndex > 0) {
                state.copy(currentIndex = state.currentIndex - 1, isFlipped = false)
            } else if (state.displayQuestions.isNotEmpty()) {
                state.copy(currentIndex = state.displayQuestions.size - 1, isFlipped = false) // Wrap to end
            } else {
                state
            }
        }
    }

    fun hearQuestion() {
        val current = _uiState.value.currentCard ?: return
        if (_uiState.value.isTtsSpeaking && _uiState.value.ttsMode == TtsMode.QUESTION) {
            ttsHelper.stop()
            _uiState.update { it.copy(isTtsSpeaking = false, ttsMode = TtsMode.NONE) }
        } else {
            _uiState.update { it.copy(ttsMode = TtsMode.QUESTION) }
            ttsHelper.speak(current.questionText)
        }
    }

    fun hearAnswer() {
        val current = _uiState.value.currentCard ?: return
        if (_uiState.value.isTtsSpeaking && _uiState.value.ttsMode == TtsMode.ANSWER) {
            ttsHelper.stop()
            _uiState.update { it.copy(isTtsSpeaking = false, ttsMode = TtsMode.NONE) }
        } else {
            val answerText = if (current.answerTexts.size > 1) {
                current.answerTexts.joinToString(separator = ". ")
            } else {
                current.primaryAnswerText
            }
            _uiState.update { it.copy(ttsMode = TtsMode.ANSWER) }
            ttsHelper.speak(answerText)
        }
    }

    fun toggleShuffle() {
        ttsHelper.stop()
        _uiState.update { state ->
            val newShuffled = !state.isShuffled
            val updatedList = filterQuestions(state.allQuestions, state.selectedFilter, newShuffled)
            state.copy(
                isShuffled = newShuffled,
                displayQuestions = updatedList,
                currentIndex = 0,
                isFlipped = false
            )
        }
    }

    fun toggleFavorite() {
        val current = _uiState.value.currentCard ?: return
        viewModelScope.launch {
            questionRepository.toggleFavorite(current.id)
        }
    }

    fun toggleStudied() {
        val current = _uiState.value.currentCard ?: return
        val newStudied = !current.isStudied
        viewModelScope.launch {
            questionRepository.setStudied(current.id, newStudied)
        }
    }

    fun stopAudio() {
        ttsHelper.stop()
        _uiState.update { it.copy(isTtsSpeaking = false, ttsMode = TtsMode.NONE) }
    }

    override fun onCleared() {
        super.onCleared()
        ttsHelper.release()
    }

    companion object {
        fun provideFactory(
            application: Application,
            repository: QuestionRepository,
            initialFilter: FlashcardFilter = FlashcardFilter.ALL
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return FlashcardsViewModel(application, repository, initialFilter) as T
            }
        }
    }
}
