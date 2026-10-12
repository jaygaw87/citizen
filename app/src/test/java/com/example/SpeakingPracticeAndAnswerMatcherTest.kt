package com.example

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.DatabaseInitializer
import com.example.data.model.PracticeType
import com.example.data.repository.DefaultQuestionRepository
import com.example.data.repository.QuestionRepository
import com.example.speech.AnswerMatcher
import com.example.speech.SpeechRecognitionState
import com.example.ui.speaking.EvaluationStatus
import com.example.ui.speaking.SpeakingPracticeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SpeakingPracticeAndAnswerMatcherTest {

    private lateinit var context: Context
    private lateinit var application: Application
    private lateinit var db: AppDatabase
    private lateinit var repository: QuestionRepository
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        context = ApplicationProvider.getApplicationContext()
        application = context as Application
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val dao = db.questionDao()
        val progressDao = db.questionProgressDao()
        val practiceResultDao = db.practiceResultDao()
        val initializer = DatabaseInitializer(context, dao)
        repository = DefaultQuestionRepository(dao, progressDao, practiceResultDao, initializer)
    }

    @After
    fun tearDown() {
        db.close()
        Dispatchers.resetMain()
    }

    @Test
    fun `test AnswerMatcher exact match`() {
        val acceptedAnswers = listOf("the Constitution")
        val result = AnswerMatcher.evaluateAnswer("The Constitution", acceptedAnswers)
        assertTrue("Expected exact match to evaluate to true", result.isMatch)
        assertEquals("the Constitution", result.matchedAcceptedAnswer)
    }

    @Test
    fun `test AnswerMatcher punctuation and case insensitivity`() {
        val acceptedAnswers = listOf("Life, liberty, and the pursuit of happiness")
        val result = AnswerMatcher.evaluateAnswer("life liberty and the pursuit of happiness.", acceptedAnswers)
        assertTrue("Expected punctuation/case variation to match", result.isMatch)
    }

    @Test
    fun `test AnswerMatcher number words and digits`() {
        // Test 100 vs one hundred
        val hundredAccepted = listOf("one hundred (100)", "100")
        assertTrue("Digit 100 should match one hundred", AnswerMatcher.evaluateAnswer("100", hundredAccepted).isMatch)
        assertTrue("Spoken 'one hundred' should match", AnswerMatcher.evaluateAnswer("one hundred", hundredAccepted).isMatch)

        // Test 50 vs fifty
        val fiftyAccepted = listOf("fifty (50)", "50")
        assertTrue("Spoken 'fifty' should match 50", AnswerMatcher.evaluateAnswer("fifty", fiftyAccepted).isMatch)
        assertTrue("Digit '50' should match", AnswerMatcher.evaluateAnswer("50", fiftyAccepted).isMatch)

        // Test 13 vs thirteen
        val thirteenAccepted = listOf("thirteen (13)", "13")
        assertTrue("Digit '13' should match", AnswerMatcher.evaluateAnswer("13", thirteenAccepted).isMatch)
        assertTrue("Spoken 'thirteen' should match", AnswerMatcher.evaluateAnswer("thirteen", thirteenAccepted).isMatch)

        // Test 435 vs four hundred thirty-five
        val repsAccepted = listOf("four hundred thirty-five (435)", "435")
        assertTrue("435 digits match", AnswerMatcher.evaluateAnswer("435", repsAccepted).isMatch)
        assertTrue("Spoken words match", AnswerMatcher.evaluateAnswer("four hundred thirty five", repsAccepted).isMatch)
    }

    @Test
    fun `test AnswerMatcher conversational preamble handling`() {
        val accepted = listOf("the Constitution")
        val userSpoke = "It is the constitution of the United States"
        val result = AnswerMatcher.evaluateAnswer(userSpoke, accepted)
        assertTrue("Conversational answer containing the answer should match", result.isMatch)
    }

    @Test
    fun `test AnswerMatcher incorrect answer rejected`() {
        val accepted = listOf("George Washington")
        val userSpoke = "Abraham Lincoln"
        val result = AnswerMatcher.evaluateAnswer(userSpoke, accepted)
        assertFalse("Completely incorrect answer must not match", result.isMatch)
    }

    @Test
    fun `test SpeakingPracticeViewModel lifecycle and question flow`() = runTest {
        repository.ensureInitialized()

        val viewModel = SpeakingPracticeViewModel(
            application = application,
            questionRepository = repository,
            targetQuestionCount = 10
        )

        val state = viewModel.uiState.first { !it.isLoading && it.totalCount > 0 }
        assertEquals(10, state.totalCount)
        assertEquals(0, state.currentIndex)
        assertNotNull(state.currentQuestion)

        // Test manual correct evaluation
        viewModel.evaluateSpokenText(state.currentQuestion!!.primaryAnswer)
        val evaluatedState = viewModel.uiState.value
        assertEquals(EvaluationStatus.CORRECT, evaluatedState.evaluationStatus)

        // Next question
        viewModel.nextQuestion()
        val nextState = viewModel.uiState.value
        assertEquals(1, nextState.currentIndex)
        assertEquals(EvaluationStatus.NOT_EVALUATED, nextState.evaluationStatus)

        // Test manual override
        viewModel.markCurrentQuestion(isCorrect = false)
        val manualState = viewModel.uiState.value
        assertEquals(EvaluationStatus.INCORRECT, manualState.evaluationStatus)
    }

    @Test
    fun `test QuestionRepository selectQuestionsForPractice SPEAKING type`() = runTest {
        repository.ensureInitialized()

        val speakingQuestions = repository.selectQuestionsForPractice(PracticeType.SPEAKING, requestedCount = 10)
        assertEquals(10, speakingQuestions.size)
    }

    @Test
    fun `test SpeechRecognitionState states and partial results flow`() {
        val idle = SpeechRecognitionState.Idle
        val starting = SpeechRecognitionState.Starting
        val listening = SpeechRecognitionState.Listening(partialText = "The Constitution", rmsLevel = 0.5f)
        val processing = SpeechRecognitionState.Processing
        val success = SpeechRecognitionState.Success("The Constitution")
        val error = SpeechRecognitionState.Error("Couldn't hear your answer")

        assertEquals("The Constitution", listening.partialText)
        assertEquals(0.5f, listening.rmsLevel, 0.01f)
        assertEquals("The Constitution", success.recognizedText)
        assertEquals("Couldn't hear your answer", error.message)
        assertTrue(starting is SpeechRecognitionState.Starting)
        assertTrue(processing is SpeechRecognitionState.Processing)
        assertTrue(idle is SpeechRecognitionState.Idle)
    }

    @Test
    fun `test SpeakingPracticeViewModel tryAgain resets speech state to Idle`() = runTest {
        repository.ensureInitialized()

        val viewModel = SpeakingPracticeViewModel(
            application = application,
            questionRepository = repository,
            targetQuestionCount = 5
        )

        viewModel.uiState.first { !it.isLoading && it.totalCount > 0 }
        viewModel.tryAgain()

        val state = viewModel.uiState.value
        assertEquals(SpeechRecognitionState.Idle, state.speechRecognitionState)
        assertNull(state.lastSpokenText)
        assertNull(state.speechMatchResult)
        assertFalse(state.isAnswerRevealed)
    }
}
