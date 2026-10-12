package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.DatabaseInitializer
import com.example.data.repository.DefaultQuestionRepository
import com.example.data.repository.QuestionRepository
import com.example.ui.study.StudyFilter
import com.example.ui.study.StudyViewModel
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

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class StudyExperienceTest {

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
    fun `test repository initializes database automatically without explicit ensureInitialized call`() = runTest {
        // Do NOT call repository.ensureInitialized() manually.
        // getAllStudyQuestions() must automatically invoke ensureInitialized()
        val questions = repository.getAllStudyQuestions().first()
        assertEquals("Database must seed all 128 questions automatically", 128, questions.size)
    }

    @Test
    fun `test StudyViewModel receives all 128 questions on clean launch`() = runTest {
        val viewModel = StudyViewModel(repository, StudyFilter.ALL)
        val state = viewModel.uiState.first { !it.isLoading }

        assertEquals(128, state.totalCount)
        assertEquals(128, state.questions.size)
        assertEquals(StudyFilter.ALL, state.selectedFilter)
        assertEquals("", state.searchQuery)
        assertFalse(state.isLoading)

        // Filter: 65/20
        viewModel.onFilterSelected(StudyFilter.SENIOR_65_20)
        val seniorState = viewModel.uiState.first { it.selectedFilter == StudyFilter.SENIOR_65_20 }
        assertEquals(20, seniorState.questions.size)

        // Filter: American Government
        viewModel.onFilterSelected(StudyFilter.GOVERNMENT)
        val govState = viewModel.uiState.first { it.selectedFilter == StudyFilter.GOVERNMENT }
        assertEquals(75, govState.questions.size)

        // Filter: American History
        viewModel.onFilterSelected(StudyFilter.HISTORY)
        val histState = viewModel.uiState.first { it.selectedFilter == StudyFilter.HISTORY }
        assertEquals(43, histState.questions.size)

        // Filter: Integrated Civics
        viewModel.onFilterSelected(StudyFilter.CIVICS)
        val civicsState = viewModel.uiState.first { it.selectedFilter == StudyFilter.CIVICS }
        assertEquals(10, civicsState.questions.size)

        // Reset to ALL
        viewModel.onFilterSelected(StudyFilter.ALL)
        val allState = viewModel.uiState.first { it.selectedFilter == StudyFilter.ALL }
        assertEquals(128, allState.questions.size)
    }

    @Test
    fun `test favorite toggle updates count and item status`() = runTest {
        assertEquals(0, repository.getFavoriteCount().first())

        // Favorite Question 1
        repository.toggleFavorite("Q001")
        assertEquals(1, repository.getFavoriteCount().first())

        val q1 = repository.getStudyQuestionById("Q001").first()
        assertNotNull(q1)
        assertTrue("Q001 must be marked as favorite", q1!!.isFavorite)

        // Favorite Question 2
        repository.toggleFavorite("Q002")
        assertEquals(2, repository.getFavoriteCount().first())

        // Un-favorite Question 1
        repository.toggleFavorite("Q001")
        assertEquals(1, repository.getFavoriteCount().first())
        val q1After = repository.getStudyQuestionById("Q001").first()
        assertFalse("Q001 must no longer be favorite", q1After!!.isFavorite)
    }

    @Test
    fun `test studied toggle updates status and count`() = runTest {
        assertEquals(0, repository.getStudiedCount().first())

        // Mark studied
        repository.setStudied("Q005", true)
        assertEquals(1, repository.getStudiedCount().first())

        val q5 = repository.getStudyQuestionById("Q005").first()
        assertTrue("Q005 must be marked studied", q5!!.isStudied)

        // Mark another studied
        repository.setStudied("Q006", true)
        assertEquals(2, repository.getStudiedCount().first())

        // Unmark studied
        repository.setStudied("Q005", false)
        assertEquals(1, repository.getStudiedCount().first())
        val q5After = repository.getStudyQuestionById("Q005").first()
        assertFalse("Q005 must not be studied", q5After!!.isStudied)
    }

    @Test
    fun `test recent question view recording`() = runTest {
        repository.recordQuestionView("Q042")
        val recentId = repository.getMostRecentViewedQuestionId().first()
        assertEquals("Q042", recentId)

        repository.recordQuestionView("Q099")
        val updatedRecent = repository.getMostRecentViewedQuestionId().first()
        assertEquals("Q099", updatedRecent)
    }

    @Test
    fun `test study category breakdown matches official USCIS 128 test structure`() = runTest {
        val allQuestions = repository.getAllStudyQuestions().first()
        assertEquals(128, allQuestions.size)

        // 65/20 filter count
        val seniorQuestions = allQuestions.filter { it.is65_20 }
        assertEquals("65/20 questions must equal exactly 20", 20, seniorQuestions.size)

        // Category breakdown
        val gov = allQuestions.filter { it.category == "American Government" }
        val hist = allQuestions.filter { it.category == "American History" }
        val civics = allQuestions.filter { it.category == "Integrated Civics" }

        assertEquals(75, gov.size)
        assertEquals(43, hist.size)
        assertEquals(10, civics.size)
        assertEquals(128, gov.size + hist.size + civics.size)
    }

    @Test
    fun `test study question items have complete metadata`() = runTest {
        val allQuestions = repository.getAllStudyQuestions().first()
        val q1 = allQuestions.first { it.id == "Q001" }

        assertEquals("Q001", q1.id)
        assertEquals(1, q1.questionNumber)
        assertEquals("What is the form of government of the United States?", q1.questionText)
        assertEquals("American Government", q1.category)
        assertTrue(q1.answerTexts.isNotEmpty())
        assertTrue(q1.explanation.isNotBlank())
        assertEquals("CURATED_EDUCATIONAL", q1.explanationSourceType)
    }
}
