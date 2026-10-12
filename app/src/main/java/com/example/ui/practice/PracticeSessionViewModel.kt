package com.example.ui.practice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.PracticeResultEntity
import com.example.data.model.PracticeQuestionItem
import com.example.data.model.PracticeType
import com.example.data.model.ReviewAnswerItem
import com.example.data.model.UserEvaluation
import com.example.data.repository.QuestionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class PracticeSessionUiState(
    val practiceType: PracticeType = PracticeType.QUICK,
    val topic: String? = null,
    val title: String = "",
    val questions: List<PracticeQuestionItem> = emptyList(),
    val currentIndex: Int = 0,
    val isLoading: Boolean = true,
    val showLeaveDialog: Boolean = false,
    val completedResultId: String? = null,
    val emptyMessage: String? = null
) {
    val totalCount: Int
        get() = questions.size

    val currentQuestion: PracticeQuestionItem?
        get() = questions.getOrNull(currentIndex)

    val correctCount: Int
        get() = questions.count { it.evaluation == UserEvaluation.GOT_IT }

    val incorrectCount: Int
        get() = questions.count { it.evaluation == UserEvaluation.MISSED_IT }

    val answeredCount: Int
        get() = questions.count { it.isAnswered }

    val isCurrentQuestionAnswered: Boolean
        get() = currentQuestion?.isAnswered == true

    val isLastQuestion: Boolean
        get() = currentIndex >= (questions.size - 1)

    val progressFraction: Float
        get() = if (totalCount > 0) (currentIndex + 1).toFloat() / totalCount else 0f
}

class PracticeSessionViewModel(
    private val questionRepository: QuestionRepository,
    private val practiceType: PracticeType,
    private val topic: String? = null,
    private val requestedCount: Int = practiceType.defaultCount
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        PracticeSessionUiState(
            practiceType = practiceType,
            topic = topic,
            title = when (practiceType) {
                PracticeType.TOPIC -> topic ?: "Topic Practice"
                else -> practiceType.title
            },
            isLoading = true
        )
    )
    val uiState: StateFlow<PracticeSessionUiState> = _uiState.asStateFlow()

    private val startedAt: Long = System.currentTimeMillis()

    init {
        loadQuestions()
    }

    private fun loadQuestions() {
        viewModelScope.launch {
            try {
                questionRepository.ensureInitialized()
                val selected = questionRepository.selectQuestionsForPractice(
                    type = practiceType,
                    topic = topic,
                    requestedCount = requestedCount
                )

                if (selected.isEmpty()) {
                    val emptyMsg = when (practiceType) {
                        PracticeType.FAVORITES -> "You don't have any favorite questions saved yet. Add questions to Favorites in Study or Flashcards to practice them here."
                        PracticeType.NEEDS_PRACTICE -> "No questions currently need practice. When you mark questions as missed during practice, they will appear here."
                        else -> "No questions available for this practice session."
                    }
                    _uiState.update { it.copy(isLoading = false, emptyMessage = emptyMsg) }
                    return@launch
                }

                val practiceItems = selected.map { studyItem ->
                    PracticeQuestionItem(
                        questionId = studyItem.id,
                        questionNumber = studyItem.questionNumber,
                        questionText = studyItem.questionText,
                        category = studyItem.category,
                        subcategory = studyItem.subcategory,
                        is65_20 = studyItem.is65_20,
                        primaryAnswer = studyItem.primaryAnswerText,
                        allAnswers = studyItem.answerTexts,
                        explanation = studyItem.explanation,
                        explanationSourceType = studyItem.explanationSourceType,
                        explanationSourceLabel = studyItem.explanationSourceLabel,
                        isFavorite = studyItem.isFavorite,
                        isStudied = studyItem.isStudied
                    )
                }

                _uiState.update {
                    it.copy(
                        questions = practiceItems,
                        currentIndex = 0,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        emptyMessage = "Questions could not be loaded. Please try again."
                    )
                }
            }
        }
    }

    fun revealAnswer() {
        _uiState.update { state ->
            val updated = state.questions.toMutableList()
            val current = updated.getOrNull(state.currentIndex) ?: return@update state
            updated[state.currentIndex] = current.copy(isAnswerRevealed = true)
            state.copy(questions = updated)
        }
    }

    fun evaluateAnswer(isCorrect: Boolean) {
        val state = _uiState.value
        val current = state.currentQuestion ?: return

        val newEvaluation = if (isCorrect) UserEvaluation.GOT_IT else UserEvaluation.MISSED_IT

        // Update in-memory state
        val updatedQuestions = state.questions.toMutableList()
        updatedQuestions[state.currentIndex] = current.copy(
            evaluation = newEvaluation,
            isAnswerRevealed = true
        )

        _uiState.update { it.copy(questions = updatedQuestions) }

        // Persist to Room local database
        viewModelScope.launch {
            questionRepository.recordQuestionEvaluation(current.questionId, isCorrect)
        }
    }

    fun nextQuestion() {
        val state = _uiState.value
        if (state.isLastQuestion) {
            completeSession()
        } else {
            _uiState.update { it.copy(currentIndex = it.currentIndex + 1) }
        }
    }

    fun previousQuestion() {
        val state = _uiState.value
        if (state.currentIndex > 0) {
            _uiState.update { it.copy(currentIndex = it.currentIndex - 1) }
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

    fun showLeaveConfirmation(show: Boolean) {
        _uiState.update { it.copy(showLeaveDialog = show) }
    }

    private fun completeSession() {
        val state = _uiState.value
        val total = state.totalCount
        if (total == 0) return

        val correct = state.correctCount
        val incorrect = state.incorrectCount
        val skipped = total - (correct + incorrect)
        val percentage = ((correct * 100f) / total).toInt()
        val completedAt = System.currentTimeMillis()

        // 12/20 rule for Full Practice test
        val passed = if (practiceType == PracticeType.FULL && total == 20) {
            correct >= 12
        } else {
            percentage >= 60
        }

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
            practiceType = practiceType.name,
            title = state.title,
            totalQuestions = total,
            correctAnswers = correct,
            incorrectAnswers = incorrect,
            skippedAnswers = skipped,
            percentage = percentage,
            passed = passed,
            startedAt = startedAt,
            completedAt = completedAt,
            questionResultsJson = ReviewAnswerItem.listToJsonString(reviewItems)
        )

        viewModelScope.launch {
            questionRepository.insertPracticeResult(resultEntity)
            _uiState.update { it.copy(completedResultId = resultId) }
        }
    }

    companion object {
        fun provideFactory(
            repository: QuestionRepository,
            practiceType: PracticeType,
            topic: String? = null,
            requestedCount: Int = practiceType.defaultCount
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return PracticeSessionViewModel(repository, practiceType, topic, requestedCount) as T
            }
        }
    }
}
