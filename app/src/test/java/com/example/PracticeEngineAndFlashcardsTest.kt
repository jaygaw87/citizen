package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.DatabaseInitializer
import com.example.data.local.entity.PracticeResultEntity
import com.example.data.model.PracticeType
import com.example.data.model.ReviewAnswerItem
import com.example.data.repository.DefaultQuestionRepository
import com.example.data.repository.QuestionRepository
import com.example.ui.flashcards.FlashcardFilter
import com.example.ui.flashcards.FlashcardsViewModel
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PracticeEngineAndFlashcardsTest {

    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var repository: QuestionRepository
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        context = ApplicationProvider.getApplicationContext()
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
    fun `test quick practice selects 10 questions with no duplicates`() = runTest {
        repository.ensureInitialized()

        val questions = repository.selectQuestionsForPractice(PracticeType.QUICK, requestedCount = 10)
        assertEquals("Quick practice must select 10 questions", 10, questions.size)

        val uniqueIds = questions.map { it.id }.toSet()
        assertEquals("Quick practice questions must not have duplicates", 10, uniqueIds.size)
    }

    @Test
    fun `test full practice selects 20 questions with no duplicates`() = runTest {
        repository.ensureInitialized()

        val questions = repository.selectQuestionsForPractice(PracticeType.FULL, requestedCount = 20)
        assertEquals("Full practice must select 20 questions", 20, questions.size)

        val uniqueIds = questions.map { it.id }.toSet()
        assertEquals("Full practice questions must not have duplicates", 20, uniqueIds.size)
    }

    @Test
    fun `test topic practice selects questions from requested topic only`() = runTest {
        repository.ensureInitialized()

        val questions = repository.selectQuestionsForPractice(
            PracticeType.TOPIC,
            topic = "American Government",
            requestedCount = 15
        )
        assertEquals(15, questions.size)
        assertTrue(questions.all { it.category.equals("American Government", ignoreCase = true) })
    }

    @Test
    fun `test practice with fewer available questions does not duplicate`() = runTest {
        repository.ensureInitialized()

        // Favorite only 2 questions
        repository.setFavorite("Q001", true)
        repository.setFavorite("Q002", true)

        // Request 10 favorite questions
        val questions = repository.selectQuestionsForPractice(PracticeType.FAVORITES, requestedCount = 10)

        assertEquals("Must return only the 2 available favorite questions without duplication", 2, questions.size)
        assertEquals(2, questions.map { it.id }.toSet().size)
    }

    @Test
    fun `test evaluate question updates progress and needs practice`() = runTest {
        repository.ensureInitialized()

        // Question 1: I Got It
        repository.recordQuestionEvaluation("Q001", isCorrect = true)
        val q1 = repository.getStudyQuestionByIdDirect("Q001")
        assertNotNull(q1)
        assertTrue("Answering question marks it studied", q1!!.isStudied)
        assertEquals(1, q1.correctCount)
        assertEquals(0, q1.incorrectCount)
        assertFalse("Correct answer does not need practice", q1.needsPractice)

        // Question 2: I Missed It
        repository.recordQuestionEvaluation("Q002", isCorrect = false)
        val q2 = repository.getStudyQuestionByIdDirect("Q002")
        assertNotNull(q2)
        assertTrue("Answering question marks it studied", q2!!.isStudied)
        assertEquals(0, q2.correctCount)
        assertEquals(1, q2.incorrectCount)
        assertTrue("Missed answer must be marked needs practice", q2.needsPractice)

        // Verify needs practice count
        val needsPracticeCount = repository.getNeedsPracticeCount().first()
        assertEquals(1, needsPracticeCount)
    }

    @Test
    fun `test practice result storage, pass calculation, and history clearing`() = runTest {
        repository.ensureInitialized()

        val reviewItems = listOf(
            ReviewAnswerItem("Q001", 1, "Question 1", "Gov", "Ans", listOf("Ans"), "Exp", true),
            ReviewAnswerItem("Q002", 2, "Question 2", "Gov", "Ans", listOf("Ans"), "Exp", false)
        )

        // Save a test result with 14/20 (Passed)
        val result1 = PracticeResultEntity(
            id = UUID.randomUUID().toString(),
            practiceType = "FULL",
            title = "Full Practice Test",
            totalQuestions = 20,
            correctAnswers = 14,
            incorrectAnswers = 6,
            percentage = 70,
            passed = true,
            startedAt = 1000L,
            completedAt = 2000L,
            questionResultsJson = ReviewAnswerItem.listToJsonString(reviewItems)
        )
        repository.insertPracticeResult(result1)

        // Verify retrieval
        val retrieved = repository.getPracticeResultByIdDirect(result1.id)
        assertNotNull(retrieved)
        assertEquals(14, retrieved!!.correctAnswers)
        assertTrue(retrieved.passed)

        val parsedItems = ReviewAnswerItem.listFromJsonString(retrieved.questionResultsJson)
        assertEquals(2, parsedItems.size)
        assertEquals("Q001", parsedItems[0].questionId)
        assertTrue(parsedItems[0].isCorrect)
        assertFalse(parsedItems[1].isCorrect)

        // Clear history
        repository.clearPracticeHistory()
        val allResults = repository.getAllPracticeResults().first()
        assertEquals(0, allResults.size)
    }

    @Test
    fun `test flashcards view model filter and navigation`() = runTest {
        repository.ensureInitialized()

        val viewModel = FlashcardsViewModel(context as android.app.Application, repository, FlashcardFilter.ALL)
        val state = viewModel.uiState.first { !it.isLoading && it.displayQuestions.isNotEmpty() }

        assertEquals(128, state.totalCount)
        assertEquals(0, state.currentIndex)
        assertFalse(state.isFlipped)

        // Flip card
        viewModel.flipCard()
        assertTrue(viewModel.uiState.value.isFlipped)

        // Next card resets flip to question front
        viewModel.nextCard()
        assertEquals(1, viewModel.uiState.value.currentIndex)
        assertFalse(viewModel.uiState.value.isFlipped)

        // Filter to 65/20
        viewModel.setFilter(FlashcardFilter.SENIOR_65_20)
        val seniorState = viewModel.uiState.first { it.selectedFilter == FlashcardFilter.SENIOR_65_20 }
        assertEquals(20, seniorState.totalCount)
    }
}
