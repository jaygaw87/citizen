package com.example.ui.speaking

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.PreferencesRepository
import com.example.data.local.entity.PracticeResultEntity
import com.example.data.model.PracticeQuestionItem
import com.example.data.model.PracticeType
import com.example.data.model.ReviewAnswerItem
import com.example.data.model.StudyQuestionItem
import com.example.data.model.UserEvaluation
import com.example.data.repository.QuestionRepository
import com.example.speech.AnswerMatcher
import com.example.speech.AppTextToSpeechHelper
import com.example.speech.MatchStatus
import com.example.speech.SpeechMatchResult
import com.example.speech.SpeechRecognitionState
import com.example.speech.SpeechRecognizerHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

enum class TtsMode {
    NONE,
    QUESTION,
    ANSWER
}

enum class EvaluationStatus {
    NOT_EVALUATED,
    CORRECT,
    INCORRECT
}

data class SpeakingPracticeUiState(
    val isLoading: Boolean = true,
    val title: String = "Speaking Practice",
    val questions: List<PracticeQuestionItem> = emptyList(),
    val currentIndex: Int = 0,
    val isAnswerRevealed: Boolean = false,
    val isTtsSpeaking: Boolean = false,
    val ttsMode: TtsMode = TtsMode.NONE,
    val speechRecognitionState: SpeechRecognitionState = SpeechRecognitionState.Idle,
    val lastSpokenText: String? = null,
    val speechMatchResult: SpeechMatchResult? = null,
    val showLeaveDialog: Boolean = false,
    val isSessionComplete: Boolean = false,
    val savedResultId: String? = null,
    val questionAudioEnabled: Boolean = true,
    val answerAudioEnabled: Boolean = true,
    val autoPlayEnabled: Boolean = false,
    val errorMessage: String? = null
) {
    val totalCount: Int get() = questions.size

    val currentQuestion: PracticeQuestionItem?
        get() = questions.getOrNull(currentIndex)

    val progressPercentage: Float
        get() = if (totalCount > 0) ((currentIndex + 1).toFloat() / totalCount) else 0f

    val correctCount: Int
        get() = questions.count { it.evaluation == UserEvaluation.GOT_IT }

    val incorrectCount: Int
        get() = questions.count { it.evaluation == UserEvaluation.MISSED_IT }

    val needsReviewCount: Int
        get() = questions.count { it.evaluation == UserEvaluation.UNANSWERED }

    val scorePercentage: Int
        get() = if (totalCount > 0) ((correctCount * 100f) / totalCount).toInt() else 0

    val evaluationStatus: EvaluationStatus
        get() = when (currentQuestion?.evaluation) {
            UserEvaluation.GOT_IT -> EvaluationStatus.CORRECT
            UserEvaluation.MISSED_IT -> EvaluationStatus.INCORRECT
            else -> EvaluationStatus.NOT_EVALUATED
        }
}

class SpeakingPracticeViewModel(
    application: Application,
    private val questionRepository: QuestionRepository,
    private val preferencesRepository: PreferencesRepository? = null,
    val requestedCount: Int = 20,
    private val topicFilter: String? = null
) : AndroidViewModel(application) {

    // Secondary constructor supporting targetQuestionCount parameter
    constructor(
        application: Application,
        questionRepository: QuestionRepository,
        targetQuestionCount: Int
    ) : this(
        application = application,
        questionRepository = questionRepository,
        preferencesRepository = null,
        requestedCount = targetQuestionCount,
        topicFilter = null
    )

    private val _uiState = MutableStateFlow(SpeakingPracticeUiState())
    val uiState: StateFlow<SpeakingPracticeUiState> = _uiState.asStateFlow()

    private val ttsHelper = AppTextToSpeechHelper(application)
    private val speechHelper = SpeechRecognizerHelper(application)
    private val startTime: Long = System.currentTimeMillis()

    init {
        observeSpeechRecognizer()
        observeTts()
        loadPreferences()
        loadQuestions()
    }

    private fun loadPreferences() {
        val prefs = preferencesRepository ?: return
        viewModelScope.launch {
            val qAudio = prefs.questionAudio.first()
            val aAudio = prefs.answerAudio.first()
            val autoPlay = prefs.audioAutoplay.first()
            val speed = prefs.playbackSpeed.first()

            ttsHelper.setSpeechRate(speed)

            _uiState.update {
                it.copy(
                    questionAudioEnabled = qAudio,
                    answerAudioEnabled = aAudio,
                    autoPlayEnabled = autoPlay
                )
            }
        }
    }

    private fun observeSpeechRecognizer() {
        viewModelScope.launch {
            speechHelper.state.collect { recState ->
                _uiState.update { it.copy(speechRecognitionState = recState) }
                when (recState) {
                    is SpeechRecognitionState.Success -> {
                        evaluateSpokenText(recState.recognizedText)
                    }
                    is SpeechRecognitionState.Error -> {
                        // Error handled via state
                    }
                    else -> {}
                }
            }
        }
    }

    private fun observeTts() {
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
    }

    private fun loadQuestions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val studyItems = questionRepository.selectQuestionsForPractice(
                    type = PracticeType.SPEAKING,
                    topic = topicFilter,
                    requestedCount = requestedCount
                )

                val practiceItems = studyItems.map { it.toPracticeItem() }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        questions = practiceItems,
                        currentIndex = 0
                    )
                }

                if (practiceItems.isNotEmpty() && _uiState.value.autoPlayEnabled && _uiState.value.questionAudioEnabled) {
                    playCurrentQuestionAudio()
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to load questions: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun startListening() {
        ttsHelper.stop()
        _uiState.update {
            it.copy(
                lastSpokenText = null,
                speechMatchResult = null,
                errorMessage = null
            )
        }
        speechHelper.startListening()
    }

    fun stopAudio() {
        ttsHelper.stop()
        _uiState.update { it.copy(isTtsSpeaking = false, ttsMode = TtsMode.NONE) }
    }

    fun stopListening() {
        ttsHelper.stop()
        _uiState.update { it.copy(isTtsSpeaking = false, ttsMode = TtsMode.NONE) }
        val currentState = _uiState.value.speechRecognitionState
        if (currentState is SpeechRecognitionState.Listening && currentState.partialText.isNotBlank()) {
            val partial = currentState.partialText
            speechHelper.stopListening()
            _uiState.update { it.copy(speechRecognitionState = SpeechRecognitionState.Success(partial)) }
            evaluateSpokenText(partial)
        } else {
            speechHelper.resetState()
            _uiState.update { it.copy(speechRecognitionState = SpeechRecognitionState.Idle) }
        }
    }

    fun hearQuestion() {
        val current = _uiState.value.currentQuestion ?: return
        if (_uiState.value.isTtsSpeaking && _uiState.value.ttsMode == TtsMode.QUESTION) {
            ttsHelper.stop()
            _uiState.update { it.copy(isTtsSpeaking = false, ttsMode = TtsMode.NONE) }
        } else {
            playCurrentQuestionAudio()
        }
    }

    fun hearAnswer() {
        val current = _uiState.value.currentQuestion ?: return
        if (_uiState.value.isTtsSpeaking && _uiState.value.ttsMode == TtsMode.ANSWER) {
            ttsHelper.stop()
            _uiState.update { it.copy(isTtsSpeaking = false, ttsMode = TtsMode.NONE) }
        } else {
            // Read primary answer or accepted answers clearly
            val answerText = if (current.allAnswers.size > 1) {
                "Accepted answers: " + current.allAnswers.joinToString(separator = ". ")
            } else {
                current.primaryAnswer
            }
            _uiState.update { it.copy(ttsMode = TtsMode.ANSWER) }
            ttsHelper.speak(answerText)
        }
    }

    private fun playCurrentQuestionAudio() {
        val current = _uiState.value.currentQuestion ?: return
        _uiState.update { it.copy(ttsMode = TtsMode.QUESTION) }
        ttsHelper.speak(current.questionText)
    }

    fun revealAnswer() {
        _uiState.update { it.copy(isAnswerRevealed = true) }
    }

    fun evaluateSpokenText(spoken: String) {
        val current = _uiState.value.currentQuestion ?: return
        val matchResult = AnswerMatcher.matchAnswer(spoken, current.allAnswers)

        // Automatically reveal answer and suggest evaluation
        val evaluation = when (matchResult.status) {
            MatchStatus.CORRECT, MatchStatus.LIKELY_CORRECT -> UserEvaluation.GOT_IT
            MatchStatus.NEEDS_REVIEW -> UserEvaluation.UNANSWERED
            MatchStatus.NO_MATCH -> UserEvaluation.MISSED_IT
        }

        updateCurrentQuestionEvaluation(evaluation)

        _uiState.update {
            it.copy(
                lastSpokenText = spoken,
                speechMatchResult = matchResult,
                isAnswerRevealed = true
            )
        }
    }

    fun manualOverride(isCorrect: Boolean) {
        val eval = if (isCorrect) UserEvaluation.GOT_IT else UserEvaluation.MISSED_IT
        updateCurrentQuestionEvaluation(eval)
    }

    fun markCurrentQuestion(isCorrect: Boolean) = manualOverride(isCorrect)

    private fun updateCurrentQuestionEvaluation(evaluation: UserEvaluation) {
        val state = _uiState.value
        val current = state.currentQuestion ?: return
        val updatedList = state.questions.toMutableList()
        updatedList[state.currentIndex] = current.copy(
            isAnswerRevealed = true,
            evaluation = evaluation
        )
        _uiState.update {
            it.copy(questions = updatedList)
        }

        // Record question evaluation in database
        viewModelScope.launch {
            val isCorrect = evaluation == UserEvaluation.GOT_IT
            questionRepository.recordQuestionEvaluation(current.questionId, isCorrect)
        }
    }

    fun tryAgain() {
        ttsHelper.stop()
        speechHelper.resetState()
        val state = _uiState.value
        val current = state.currentQuestion
        val updatedList = if (current != null) {
            val list = state.questions.toMutableList()
            list[state.currentIndex] = current.copy(
                isAnswerRevealed = false,
                evaluation = UserEvaluation.UNANSWERED
            )
            list
        } else {
            state.questions
        }
        _uiState.update {
            it.copy(
                questions = updatedList,
                speechRecognitionState = SpeechRecognitionState.Idle,
                lastSpokenText = null,
                speechMatchResult = null,
                isAnswerRevealed = false
            )
        }
    }

    fun toggleFavoriteCurrent() {
        val state = _uiState.value
        val current = state.currentQuestion ?: return
        val newFav = !current.isFavorite

        val updated = state.questions.toMutableList()
        updated[state.currentIndex] = current.copy(isFavorite = newFav)
        _uiState.update { it.copy(questions = updated) }

        viewModelScope.launch {
            questionRepository.toggleFavorite(current.questionId)
        }
    }

    fun nextQuestion() {
        ttsHelper.stop()
        speechHelper.resetState()

        val state = _uiState.value
        if (state.currentIndex + 1 < state.totalCount) {
            _uiState.update {
                it.copy(
                    currentIndex = it.currentIndex + 1,
                    isAnswerRevealed = false,
                    lastSpokenText = null,
                    speechMatchResult = null
                )
            }

            if (_uiState.value.autoPlayEnabled && _uiState.value.questionAudioEnabled) {
                playCurrentQuestionAudio()
            }
        } else {
            completeSession()
        }
    }

    fun showLeaveConfirmation(show: Boolean) {
        _uiState.update { it.copy(showLeaveDialog = show) }
    }

    private fun completeSession() {
        val state = _uiState.value
        val total = state.totalCount
        if (total == 0) return

        val correct = state.correctCount
        val incorrect = state.incorrectCount
        val skipped = state.needsReviewCount
        val percentage = ((correct * 100f) / total).toInt()
        val passed = percentage >= 60

        val reviewItems = state.questions.map { item ->
            ReviewAnswerItem(
                questionId = item.questionId,
                questionNumber = item.questionNumber,
                questionText = item.questionText,
                category = item.category,
                primaryAnswer = item.primaryAnswer,
                allAnswers = item.allAnswers,
                explanation = item.explanation,
                isCorrect = item.evaluation == UserEvaluation.GOT_IT
            )
        }

        val resultId = UUID.randomUUID().toString()
        val resultEntity = PracticeResultEntity(
            id = resultId,
            practiceType = PracticeType.SPEAKING.name,
            title = "Speaking Practice",
            totalQuestions = total,
            correctAnswers = correct,
            incorrectAnswers = incorrect,
            skippedAnswers = skipped,
            percentage = percentage,
            passed = passed,
            startedAt = startTime,
            completedAt = System.currentTimeMillis(),
            questionResultsJson = ReviewAnswerItem.listToJsonString(reviewItems)
        )

        viewModelScope.launch {
            questionRepository.insertPracticeResult(resultEntity)
            _uiState.update {
                it.copy(
                    isSessionComplete = true,
                    savedResultId = resultId
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        ttsHelper.release()
        speechHelper.release()
    }

    private fun StudyQuestionItem.toPracticeItem(): PracticeQuestionItem {
        return PracticeQuestionItem(
            questionId = this.id,
            questionNumber = this.questionNumber,
            questionText = this.questionText,
            category = this.category,
            subcategory = this.subcategory,
            is65_20 = this.is65_20,
            primaryAnswer = this.primaryAnswerText,
            allAnswers = this.answerTexts,
            explanation = this.explanation,
            explanationSourceType = this.explanationSourceType,
            explanationSourceLabel = this.explanationSourceLabel,
            isFavorite = this.isFavorite,
            isStudied = this.isStudied,
            isAnswerRevealed = false,
            evaluation = UserEvaluation.UNANSWERED
        )
    }

    companion object {
        fun provideFactory(
            application: Application,
            questionRepository: QuestionRepository,
            preferencesRepository: PreferencesRepository,
            requestedCount: Int = 20,
            topicFilter: String? = null
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SpeakingPracticeViewModel(
                    application = application,
                    questionRepository = questionRepository,
                    preferencesRepository = preferencesRepository,
                    requestedCount = requestedCount,
                    topicFilter = topicFilter
                ) as T
            }
        }
    }
}
