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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class VocabularyFlashcardsUiState(
    val isLoading: Boolean = true,
    val items: List<VocabularyItem> = emptyList(),
    val currentIndex: Int = 0,
    val isFlipped: Boolean = false,
    val isShuffle: Boolean = false,
    val isSpeaking: Boolean = false,
    val selectedCategory: String = "ALL",
    val selectedFilter: String = "ALL",
    val categories: List<String> = emptyList()
) {
    val currentItem: VocabularyItem?
        get() = items.getOrNull(currentIndex)

    val totalCount: Int
        get() = items.size

    val progressFraction: Float
        get() = if (totalCount > 0) (currentIndex + 1).toFloat() / totalCount else 0f
}

class VocabularyFlashcardsViewModel(
    application: Application,
    private val questionRepository: QuestionRepository,
    initialCategory: String = "ALL",
    initialFilter: String = "ALL"
) : AndroidViewModel(application) {

    private val ttsHelper = AppTextToSpeechHelper(application)
    private val _currentIndex = MutableStateFlow(0)
    private val _isFlipped = MutableStateFlow(false)
    private val _isShuffle = MutableStateFlow(false)
    private val _selectedCategory = MutableStateFlow(initialCategory)
    private val _selectedFilter = MutableStateFlow(initialFilter)

    private data class FlashcardsStateData(
        val index: Int,
        val flipped: Boolean,
        val shuffle: Boolean,
        val speaking: Boolean
    )

    private data class FlashcardsFilterData(
        val category: String,
        val filter: String
    )

    val uiState: StateFlow<VocabularyFlashcardsUiState> = combine(
        questionRepository.getAllVocabulary(),
        combine(_currentIndex, _isFlipped, _isShuffle, ttsHelper.isSpeaking) { index, flipped, shuffle, speaking ->
            FlashcardsStateData(index, flipped, shuffle, speaking)
        },
        combine(_selectedCategory, _selectedFilter) { category, filter ->
            FlashcardsFilterData(category, filter)
        }
    ) { allVocab, stateData, filterData ->
        val category = filterData.category
        val filter = filterData.filter
        val index = stateData.index
        val flipped = stateData.flipped
        val shuffle = stateData.shuffle
        val speaking = stateData.speaking

        val categories = listOf("ALL") + allVocab.map { it.category }.distinct().sorted()

        val filtered = allVocab.filter { item ->
            val matchesCategory = if (category == "ALL") true else item.category.equals(category, ignoreCase = true)
            val matchesFilter = when (filter.uppercase()) {
                "FAVORITES" -> item.isFavorite
                "NEEDS_REVIEW" -> item.status == VocabularyStatus.NEEDS_REVIEW
                "LEARNED" -> item.status == VocabularyStatus.LEARNED
                "NOT_STARTED" -> item.status == VocabularyStatus.NOT_STARTED
                else -> true
            }
            matchesCategory && matchesFilter
        }

        val displayList = if (shuffle) {
            filtered.shuffled(java.util.Random(42))
        } else {
            filtered
        }

        val safeIndex = if (displayList.isEmpty()) 0 else index.coerceIn(0, displayList.size - 1)

        VocabularyFlashcardsUiState(
            isLoading = allVocab.isEmpty(),
            items = displayList,
            currentIndex = safeIndex,
            isFlipped = flipped,
            isShuffle = shuffle,
            isSpeaking = speaking,
            selectedCategory = category,
            selectedFilter = filter,
            categories = categories
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = VocabularyFlashcardsUiState()
    )

    fun flipCard() {
        _isFlipped.value = !_isFlipped.value
    }

    fun nextCard() {
        stopAudio()
        _isFlipped.value = false
        val total = uiState.value.totalCount
        if (total > 0) {
            val next = (_currentIndex.value + 1) % total
            _currentIndex.value = next
        }
    }

    fun previousCard() {
        stopAudio()
        _isFlipped.value = false
        val total = uiState.value.totalCount
        if (total > 0) {
            val prev = if (_currentIndex.value > 0) _currentIndex.value - 1 else total - 1
            _currentIndex.value = prev
        }
    }

    fun toggleShuffle() {
        _isShuffle.value = !_isShuffle.value
        _currentIndex.value = 0
        _isFlipped.value = false
    }

    fun setCategory(category: String) {
        _selectedCategory.value = category
        _currentIndex.value = 0
        _isFlipped.value = false
    }

    fun setFilter(filter: String) {
        _selectedFilter.value = filter
        _currentIndex.value = 0
        _isFlipped.value = false
    }

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
                VocabularyStatus.NEEDS_REVIEW
            }
            questionRepository.updateVocabularyStatus(item.id, nextStatus)
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
            category: String = "ALL",
            filter: String = "ALL"
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return VocabularyFlashcardsViewModel(application, questionRepository, category, filter) as T
            }
        }
    }
}
