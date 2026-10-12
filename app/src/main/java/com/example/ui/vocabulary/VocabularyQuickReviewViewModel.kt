package com.example.ui.vocabulary

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.VocabularyItem
import com.example.data.model.VocabularyStatus
import com.example.data.repository.QuestionRepository
import com.example.speech.AppTextToSpeechHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class VocabularyQuickReviewUiState(
    val isLoading: Boolean = true,
    val items: List<VocabularyItem> = emptyList(),
    val currentIndex: Int = 0,
    val isSpeaking: Boolean = false,
    val isCompleted: Boolean = false,
    val sessionLearnedCount: Int = 0,
    val sessionNeedsReviewCount: Int = 0,
    val filterMode: String = "ALL"
) {
    val currentItem: VocabularyItem?
        get() = items.getOrNull(currentIndex)

    val totalCount: Int
        get() = items.size

    val progressFraction: Float
        get() = if (totalCount > 0) (currentIndex + 1).toFloat() / totalCount else 0f
}

class VocabularyQuickReviewViewModel(
    application: Application,
    private val questionRepository: QuestionRepository,
    private val initialFilterMode: String = "ALL"
) : AndroidViewModel(application) {

    private val ttsHelper = AppTextToSpeechHelper(application)
    private val _currentIndex = MutableStateFlow(0)
    private val _sessionItems = MutableStateFlow<List<VocabularyItem>>(emptyList())
    private val _isCompleted = MutableStateFlow(false)
    private val _sessionLearnedCount = MutableStateFlow(0)
    private val _sessionNeedsReviewCount = MutableStateFlow(0)
    private var isSessionInitialized = false

    init {
        viewModelScope.launch {
            questionRepository.getAllVocabulary().collect { allVocab ->
                if (!isSessionInitialized && allVocab.isNotEmpty()) {
                    isSessionInitialized = true
                    startNewSession(allVocab, initialFilterMode)
                } else if (isSessionInitialized) {
                    val currentList = _sessionItems.value
                    if (currentList.isNotEmpty()) {
                        val vocabMap = allVocab.associateBy { it.id }
                        val updatedList = currentList.map { item ->
                            vocabMap[item.id] ?: item
                        }
                        _sessionItems.value = updatedList
                    }
                }
            }
        }
    }

    private fun startNewSession(allVocab: List<VocabularyItem>, filter: String) {
        val eligible = when (filter.uppercase()) {
            "FAVORITES" -> allVocab.filter { it.isFavorite }
            "NEEDS_REVIEW" -> allVocab.filter { it.status == VocabularyStatus.NEEDS_REVIEW }
            "NOT_STARTED" -> allVocab.filter { it.status == VocabularyStatus.NOT_STARTED }
            else -> allVocab
        }
        val pool = if (eligible.isNotEmpty()) eligible else allVocab
        val selected = pool.shuffled().take(10)
        _sessionItems.value = selected
        _currentIndex.value = 0
        _isCompleted.value = false
        _sessionLearnedCount.value = 0
        _sessionNeedsReviewCount.value = 0
    }

    val uiState: StateFlow<VocabularyQuickReviewUiState> = combine(
        combine(_sessionItems, _currentIndex, _isCompleted) { items, idx, completed ->
            Triple(items, idx, completed)
        },
        combine(_sessionLearnedCount, _sessionNeedsReviewCount, ttsHelper.isSpeaking) { learned, review, speaking ->
            Triple(learned, review, speaking)
        }
    ) { (items, idx, completed), (learned, review, speaking) ->
        VocabularyQuickReviewUiState(
            isLoading = items.isEmpty(),
            items = items,
            currentIndex = idx,
            isSpeaking = speaking,
            isCompleted = completed,
            sessionLearnedCount = learned,
            sessionNeedsReviewCount = review,
            filterMode = initialFilterMode
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = VocabularyQuickReviewUiState()
    )

    fun hearWord() {
        val word = uiState.value.currentItem?.word ?: return
        ttsHelper.speak(word)
    }

    fun hearDefinition() {
        val item = uiState.value.currentItem ?: return
        val textToSpeak = "${item.word}. ${item.simpleDefinition}"
        ttsHelper.speak(textToSpeak)
    }

    fun stopAudio() {
        ttsHelper.stop()
    }

    fun toggleFavorite() {
        val item = uiState.value.currentItem ?: return
        viewModelScope.launch {
            questionRepository.toggleVocabularyFavorite(item.id)
        }
    }

    fun markLearned() {
        val item = uiState.value.currentItem ?: return
        viewModelScope.launch {
            val nextStatus = if (item.status == VocabularyStatus.LEARNED) {
                VocabularyStatus.NOT_STARTED
            } else {
                _sessionLearnedCount.value += 1
                VocabularyStatus.LEARNED
            }
            questionRepository.updateVocabularyStatus(item.id, nextStatus)
        }
    }

    fun markNeedsReview() {
        val item = uiState.value.currentItem ?: return
        viewModelScope.launch {
            val nextStatus = if (item.status == VocabularyStatus.NEEDS_REVIEW) {
                VocabularyStatus.NOT_STARTED
            } else {
                _sessionNeedsReviewCount.value += 1
                VocabularyStatus.NEEDS_REVIEW
            }
            questionRepository.updateVocabularyStatus(item.id, nextStatus)
        }
    }

    fun nextCard() {
        stopAudio()
        val current = _currentIndex.value
        val total = _sessionItems.value.size
        if (current < total - 1) {
            _currentIndex.value = current + 1
        } else {
            _isCompleted.value = true
        }
    }

    fun previousCard() {
        stopAudio()
        val current = _currentIndex.value
        if (current > 0) {
            _currentIndex.value = current - 1
        }
    }

    fun restartSession() {
        stopAudio()
        viewModelScope.launch {
            val allVocab = questionRepository.getAllVocabulary().first()
            startNewSession(allVocab, initialFilterMode)
        }
    }

    override fun onCleared() {
        super.onCleared()
        ttsHelper.release()
    }

    companion object {
        fun provideFactory(
            application: Application,
            questionRepository: QuestionRepository,
            filterMode: String
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return VocabularyQuickReviewViewModel(application, questionRepository, filterMode) as T
            }
        }
    }
}
