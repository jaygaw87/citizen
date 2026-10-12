package com.example.ui.vocabulary

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.QuestionWithAnswers
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

data class VocabularyDetailUiState(
    val isLoading: Boolean = true,
    val item: VocabularyItem? = null,
    val relatedQuestions: List<QuestionWithAnswers> = emptyList(),
    val previousVocabId: String? = null,
    val nextVocabId: String? = null,
    val currentIndex: Int = 0,
    val totalCount: Int = 0,
    val isSpeaking: Boolean = false
)

class VocabularyDetailViewModel(
    application: Application,
    private val questionRepository: QuestionRepository,
    initialVocabId: String
) : AndroidViewModel(application) {

    private val ttsHelper = AppTextToSpeechHelper(application)
    private val _currentVocabId = MutableStateFlow(initialVocabId)

    val uiState: StateFlow<VocabularyDetailUiState> = combine(
        questionRepository.getAllVocabulary(),
        questionRepository.getAllQuestionsWithAnswers(),
        _currentVocabId,
        ttsHelper.isSpeaking
    ) { allVocab, allQuestions, currentId, speaking ->
        if (allVocab.isEmpty()) {
            VocabularyDetailUiState(isLoading = true)
        } else {
            val index = allVocab.indexOfFirst { it.id == currentId }.let { if (it >= 0) it else 0 }
            val currentItem = allVocab.getOrNull(index) ?: allVocab.first()
            val prevId = if (index > 0) allVocab[index - 1].id else null
            val nextId = if (index < allVocab.size - 1) allVocab[index + 1].id else null

            val relatedIds = currentItem.relatedQuestionIds
            val relatedQuestions = allQuestions.filter { q -> relatedIds.contains(q.question.id) }

            VocabularyDetailUiState(
                isLoading = false,
                item = currentItem,
                relatedQuestions = relatedQuestions,
                previousVocabId = prevId,
                nextVocabId = nextId,
                currentIndex = index + 1,
                totalCount = allVocab.size,
                isSpeaking = speaking
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = VocabularyDetailUiState()
    )

    fun navigateTo(vocabId: String) {
        stopAudio()
        _currentVocabId.value = vocabId
    }

    fun hearWord() {
        val word = uiState.value.item?.word ?: return
        ttsHelper.speak(word, "vocab_word")
    }

    fun hearDefinition() {
        val item = uiState.value.item ?: return
        val textToSpeak = "${item.word}. ${item.simpleDefinition}. For example: ${item.exampleSentence}"
        ttsHelper.speak(textToSpeak, "vocab_definition")
    }

    fun stopAudio() {
        ttsHelper.stop()
    }

    fun toggleFavorite() {
        val item = uiState.value.item ?: return
        viewModelScope.launch {
            questionRepository.toggleVocabularyFavorite(item.id)
        }
    }

    fun setStatus(status: VocabularyStatus) {
        val item = uiState.value.item ?: return
        viewModelScope.launch {
            questionRepository.updateVocabularyStatus(item.id, status)
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
            vocabId: String
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return VocabularyDetailViewModel(application, questionRepository, vocabId) as T
            }
        }
    }
}
