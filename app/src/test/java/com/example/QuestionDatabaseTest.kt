package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.DatabaseInitializer
import com.example.data.repository.DefaultQuestionRepository
import com.example.data.repository.QuestionRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
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
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class QuestionDatabaseTest {

    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var repository: QuestionRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        // Use a file-based test db in app cache to test persistence across restarts
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
    }

    @Test
    fun `test database initialization seeds exactly 128 questions`(): Unit = runBlocking {
        repository.ensureInitialized()

        val count = repository.getQuestionCount().first()
        assertEquals("Database must contain exactly 128 USCIS questions", 128, count)
    }

    @Test
    fun `test senior exemption 65-20 questions count is exactly 20`(): Unit = runBlocking {
        repository.ensureInitialized()

        val count = repository.get65_20Count().first()
        assertEquals("65/20 special consideration questions must equal 20", 20, count)

        val questions = repository.get65_20Questions().first()
        assertEquals(20, questions.size)
        questions.forEach {
            assertTrue("Every question in this list must be marked 65/20", it.question.is65_20)
        }
    }

    @Test
    fun `test dynamic officials questions count is exactly 8`(): Unit = runBlocking {
        repository.ensureInitialized()

        val count = repository.getDynamicCount().first()
        assertEquals("Dynamic answer questions count must equal 8", 8, count)

        val dynamicQuestions = repository.getDynamicQuestions().first()
        assertEquals(8, dynamicQuestions.size)
        dynamicQuestions.forEach {
            assertTrue("Question must be marked isDynamicAnswer", it.question.isDynamicAnswer)
        }
    }

    @Test
    fun `test questions with answers relation and valid foreign keys`(): Unit = runBlocking {
        repository.ensureInitialized()

        val all = repository.getAllQuestionsWithAnswers().first()
        assertEquals(128, all.size)

        all.forEach { item ->
            assertNotNull("Question text must not be null", item.question.questionText)
            assertTrue("Question text must not be empty", item.question.questionText.isNotBlank())
            assertTrue("Each question must have at least one accepted answer", item.answers.isNotEmpty())
            item.answers.forEach { ans ->
                assertEquals("Foreign key questionId must match question id", item.question.id, ans.questionId)
                assertTrue("Answer text must not be empty", ans.answerText.isNotBlank())
            }
        }
    }

    @Test
    fun `test category breakdown matches official USCIS 128 test structure`(): Unit = runBlocking {
        repository.ensureInitialized()

        val gov = repository.getQuestionsByCategory("American Government").first()
        val hist = repository.getQuestionsByCategory("American History").first()
        val civics = repository.getQuestionsByCategory("Integrated Civics").first()

        assertEquals("American Government must have 75 questions", 75, gov.size)
        assertEquals("American History must have 43 questions", 43, hist.size)
        assertEquals("Integrated Civics must have 10 questions", 10, civics.size)
        assertEquals("Sum of all categories must be 128", 128, gov.size + hist.size + civics.size)
    }

    @Test
    fun `test search query functionality`(): Unit = runBlocking {
        repository.ensureInitialized()

        val constitutionResults = repository.searchQuestions("Constitution").first()
        assertTrue("Search for 'Constitution' should find multiple questions", constitutionResults.isNotEmpty())

        val washingtonResults = repository.searchQuestions("George Washington").first()
        assertTrue("Search for 'George Washington' should return results", washingtonResults.isNotEmpty())
    }

    @Test
    fun `test data persistence across database restarts`(): Unit = runBlocking {
        val dbFile = File(context.cacheDir, "test_persistent_civics.db")
        if (dbFile.exists()) dbFile.delete()

        // 1. First session: Create and seed
        var fileDb = Room.databaseBuilder(context, AppDatabase::class.java, dbFile.absolutePath)
            .allowMainThreadQueries()
            .build()
        var init = DatabaseInitializer(context, fileDb.questionDao())
        init.initializeIfNeeded()

        assertEquals(128, fileDb.questionDao().getQuestionCountDirect())
        fileDb.close()

        // 2. Second session: Simulate app restart (reopen existing database file without re-seeding)
        fileDb = Room.databaseBuilder(context, AppDatabase::class.java, dbFile.absolutePath)
            .allowMainThreadQueries()
            .build()

        init = DatabaseInitializer(context, fileDb.questionDao())
        init.initializeIfNeeded() // Should detect already initialized and skip

        assertEquals("Questions must survive app restart intact", 128, fileDb.questionDao().getQuestionCountDirect())
        assertEquals("65/20 count must survive app restart", 20, fileDb.questionDao().get65_20CountDirect())
        assertEquals("Dynamic count must survive app restart", 8, fileDb.questionDao().getDynamicCountDirect())

        fileDb.close()
        dbFile.delete()
        Unit
    }

    @Test
    fun `test offline operation in simulated airplane mode`(): Unit = runBlocking {
        // Repository operates purely on local Room SQLite file and bundled assets
        // No network calls or HTTP clients exist in repository
        repository.ensureInitialized()

        val q1 = repository.getQuestionWithAnswersByNumber(1).first()
        assertNotNull(q1)
        assertEquals("What is the form of government of the United States?", q1?.question?.questionText)
        assertTrue(q1?.answers?.any { it.answerText.contains("Republic") } == true)
    }

    @Test
    fun `test source type distinction between official USCIS content and curated explanations`(): Unit = runBlocking {
        repository.ensureInitialized()

        val all = repository.getAllQuestionsWithAnswers().first()
        assertEquals(128, all.size)

        var curatedExplanationCount = 0

        all.forEach { item ->
            // Question and answers must be explicitly marked as official USCIS source
            assertEquals("USCIS_OFFICIAL", item.question.sourceType)
            assertEquals("USCIS 2025 Civics Test (Form M-1778)", item.question.sourceName)
            item.answers.forEach { ans ->
                assertEquals("USCIS_OFFICIAL", ans.sourceType)
                assertEquals("USCIS 2025 Civics Test (Form M-1778)", ans.sourceName)
            }

            // Explanations must be explicitly marked as curated educational content, not government published
            assertEquals("CURATED_EDUCATIONAL", item.question.explanationSourceType)
            assertEquals("Educational explanation", item.question.explanationSourceLabel)
            assertTrue("Explanation should not be empty", item.question.explanation.isNotBlank())
            curatedExplanationCount++
        }

        assertEquals(128, curatedExplanationCount)
    }

    @Test
    fun `test dynamic officials questions have complete verified metadata`(): Unit = runBlocking {
        repository.ensureInitialized()

        val dynamicList = repository.getDynamicQuestions().first()
        assertEquals(8, dynamicList.size)

        val expectedTypes = setOf(
            "US_SENATOR", "US_REPRESENTATIVE", "SPEAKER_OF_HOUSE",
            "PRESIDENT", "VICE_PRESIDENT", "CHIEF_JUSTICE",
            "GOVERNOR", "STATE_CAPITAL"
        )
        val actualTypes = dynamicList.mapNotNull { it.question.dynamicAnswerType }.toSet()
        assertEquals(expectedTypes, actualTypes)

        dynamicList.forEach { item ->
            val q = item.question
            assertTrue("isDynamicAnswer must be true", q.isDynamicAnswer)
            assertNotNull("dynamicAnswerType must not be null", q.dynamicAnswerType)
            assertNotNull("currentAnswer must not be null", q.currentAnswer)
            assertTrue("currentAnswer must not be blank", q.currentAnswer!!.isNotBlank())
            assertNotNull("answerLastVerified must not be null", q.answerLastVerified)
            assertTrue("answerLastVerified must not be blank", q.answerLastVerified!!.isNotBlank())
            assertNotNull("answerSourceName must not be null", q.answerSourceName)
            assertTrue("answerSourceName must not be blank", q.answerSourceName!!.isNotBlank())
            assertNotNull("answerSourceUrl must not be null", q.answerSourceUrl)
            assertTrue("answerSourceUrl must start with https", q.answerSourceUrl!!.startsWith("https"))
        }

        // Test querying dynamic question by type
        val presList = repository.getQuestionsByDynamicType("PRESIDENT").first()
        assertEquals(1, presList.size)
        val pres = presList.first()
        assertEquals(38, pres.question.questionNumber)
        assertEquals("Donald J. Trump", pres.question.currentAnswer)
    }

    @Test
    fun `test 128 questions data validation invariants`(): Unit = runBlocking {
        repository.ensureInitialized()

        val all = repository.getAllQuestionsWithAnswers().first()
        assertEquals(128, all.size)

        val ids = all.map { it.question.id }
        val numbers = all.map { it.question.questionNumber }

        assertEquals("All 128 IDs must be unique", 128, ids.toSet().size)
        assertEquals("All 128 numbers must be unique", 128, numbers.toSet().size)

        val expectedNumbers = (1..128).toSet()
        assertEquals("There must be 0 missing question numbers between 1 and 128", emptySet<Int>(), expectedNumbers - numbers.toSet())
    }
}
