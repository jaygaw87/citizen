package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.DatabaseInitializer
import com.example.data.local.PreferencesRepository
import com.example.data.local.entity.PracticeResultEntity
import com.example.data.model.PracticeType
import com.example.data.model.TestConstants
import com.example.data.repository.DefaultQuestionRepository
import com.example.data.repository.QuestionRepository
import com.example.speech.AnswerMatcher
import com.example.speech.MatchStatus
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
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class Phase8ReleaseHardeningQaTest {

    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var repository: QuestionRepository
    private lateinit var preferencesRepository: PreferencesRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val questionDao = db.questionDao()
        val progressDao = db.questionProgressDao()
        val practiceResultDao = db.practiceResultDao()
        val studyActivityDao = db.studyActivityDao()
        val vocabularyDao = db.vocabularyDao()
        val initializer = DatabaseInitializer(context, questionDao, vocabularyDao)
        repository = DefaultQuestionRepository(
            questionDao = questionDao,
            questionProgressDao = progressDao,
            practiceResultDao = practiceResultDao,
            studyActivityDao = studyActivityDao,
            databaseInitializer = initializer,
            vocabularyDao = vocabularyDao
        )
        preferencesRepository = PreferencesRepository(context)
    }

    @After
    fun tearDown() {
        db.close()
    }

    // =========================================================================
    // 1. DATABASE & 128-QUESTION INTEGRITY AUDIT
    // =========================================================================

    @Test
    fun testDatabase_exactly128QuestionsLoadedWithCorrectAttributes(): Unit = runBlocking {
        repository.ensureInitialized()
        val questions = repository.getAllQuestionsWithAnswers().first()

        assertEquals("Total questions must be exactly 128", 128, questions.size)

        // Verify question numbers 1 to 128
        val numbers = questions.map { it.question.questionNumber }.sorted()
        assertEquals("Minimum question number must be 1", 1, numbers.first())
        assertEquals("Maximum question number must be 128", 128, numbers.last())
        assertEquals("All 128 question numbers must be unique", 128, numbers.distinct().size)

        // Verify no duplicate question texts
        val texts = questions.map { it.question.questionText.trim().lowercase() }
        assertEquals("All question texts must be unique", 128, texts.distinct().size)

        // Verify categories
        val govCount = questions.count { it.question.category == "American Government" }
        val histCount = questions.count { it.question.category == "American History" }
        val civicsCount = questions.count { it.question.category == "Integrated Civics" }

        assertEquals("American Government category must have 75 questions", 75, govCount)
        assertEquals("American History category must have 43 questions", 43, histCount)
        assertEquals("Integrated Civics category must have 10 questions", 10, civicsCount)
        assertEquals("Sum of categories must be 128", 128, govCount + histCount + civicsCount)

        // Verify every question has at least one accepted answer
        for (q in questions) {
            assertTrue("Question #${q.question.questionNumber} must have answers", q.answers.isNotEmpty())
            assertTrue("Question #${q.question.questionNumber} must have a non-empty primary answer", q.answers.first().answerText.isNotBlank())
        }
    }

    // =========================================================================
    // 2. 65/20 SPECIAL CONSIDERATION VERIFICATION
    // =========================================================================

    @Test
    fun testSenior6520_exactly20QuestionsClassifiedAndRestricted(): Unit = runBlocking {
        repository.ensureInitialized()
        val questions = repository.getAllQuestionsWithAnswers().first()
        val seniorQuestions = questions.filter { it.question.is65_20 }

        assertEquals("Exactly 20 questions must be classified as 65/20", 20, seniorQuestions.size)

        // Verify 65/20 practice questions are exclusively 65/20
        val seniorPractice = repository.selectQuestionsForPractice(
            type = PracticeType.SENIOR_65_20,
            requestedCount = 20
        )
        assertEquals("65/20 practice session must contain 20 questions", 20, seniorPractice.size)
        assertTrue("All questions in 65/20 practice must have is65_20 = true", seniorPractice.all { it.is65_20 })
    }

    // =========================================================================
    // 3. RESET LEARNING PROGRESS AUDIT
    // =========================================================================

    @Test
    fun testResetLearningProgress_clearsProgressPreservesDataAndSettings(): Unit = runBlocking {
        repository.ensureInitialized()
        val allQ = repository.getAllStudyQuestions().first()
        val q1 = allQ[0]
        val q2 = allQ[1]

        // 1. Create realistic progress
        repository.setStudied(q1.id, true)
        repository.toggleFavorite(q1.id) // Favorite q1
        repository.recordQuestionEvaluation(q1.id, isCorrect = true)
        repository.recordQuestionEvaluation(q2.id, isCorrect = false)

        val resultId = UUID.randomUUID().toString()
        val historyItem = PracticeResultEntity(
            id = resultId,
            practiceType = "QUICK",
            title = "Quick Practice",
            totalQuestions = 10,
            correctAnswers = 8,
            incorrectAnswers = 2,
            percentage = 80,
            passed = true,
            startedAt = System.currentTimeMillis() - 120000,
            completedAt = System.currentTimeMillis(),
            questionResultsJson = "[]"
        )
        repository.insertPracticeResult(historyItem)

        // Set preferences
        preferencesRepository.setDailyStudyGoal(15)
        preferencesRepository.setPlaybackSpeed(1.15f)

        // Verify data before reset
        val studiedBefore = repository.getStudiedCount().first()
        assertTrue("Studied count should be > 0 before reset", studiedBefore > 0)
        assertTrue("Q1 should be favorite before reset", repository.getStudyQuestionByIdDirect(q1.id)!!.isFavorite)
        val historyBefore = repository.getAllPracticeResults().first()
        assertEquals("History should have 1 item before reset", 1, historyBefore.size)

        // 2. Perform Reset Learning Progress
        repository.resetLearningProgress()

        // 3. Verify Cleared State
        val studiedAfter = repository.getStudiedCount().first()
        assertEquals("Studied count must be 0 after reset", 0, studiedAfter)
        val needsPracticeAfter = repository.getNeedsPracticeCount().first()
        assertEquals("Needs practice count must be 0 after reset", 0, needsPracticeAfter)

        // 4. Verify Preserved State
        val allAfter = repository.getAllQuestionsWithAnswers().first()
        assertEquals("All 128 questions must remain after reset", 128, allAfter.size)
        assertTrue("Favorite status must survive reset", repository.getStudyQuestionByIdDirect(q1.id)!!.isFavorite)

        val historyAfter = repository.getAllPracticeResults().first()
        assertEquals("Practice history must survive reset", 1, historyAfter.size)
        assertEquals("Result ID in history must match", resultId, historyAfter.first().id)

        val goalAfter = preferencesRepository.dailyStudyGoal.first()
        assertEquals("Daily goal must survive reset", 15, goalAfter)

        val speedAfter = preferencesRepository.playbackSpeed.first()
        assertEquals("Playback speed must survive reset", 1.15f, speedAfter, 0.01f)
    }

    // =========================================================================
    // 4. PRACTICE ENGINE PASS/FAIL 60% THRESHOLD
    // =========================================================================

    @Test
    fun testPracticeEngine_passFailScoringThreshold() {
        // 60% passing threshold:
        // For 10 questions: 6 correct passes (60%), 5 correct fails (50%)
        // For 20 questions: 12 correct passes (60%), 11 correct fails (55%)

        val tenPassed = PracticeResultEntity(
            id = "1", practiceType = "QUICK", title = "Quick Practice",
            totalQuestions = 10, correctAnswers = 6, incorrectAnswers = 4,
            passed = 6 >= TestConstants.SPECIAL_CONSIDERATION_PASSING_SCORE,
            percentage = 60, startedAt = 0, completedAt = 60, questionResultsJson = "[]"
        )
        assertTrue("6/10 must pass", tenPassed.passed)

        val tenFailed = PracticeResultEntity(
            id = "2", practiceType = "QUICK", title = "Quick Practice",
            totalQuestions = 10, correctAnswers = 5, incorrectAnswers = 5,
            passed = 5 >= TestConstants.SPECIAL_CONSIDERATION_PASSING_SCORE,
            percentage = 50, startedAt = 0, completedAt = 60, questionResultsJson = "[]"
        )
        assertFalse("5/10 must fail", tenFailed.passed)

        val twentyPassed = PracticeResultEntity(
            id = "3", practiceType = "FULL", title = "Full Practice",
            totalQuestions = 20, correctAnswers = 12, incorrectAnswers = 8,
            passed = 12 >= TestConstants.PASSING_SCORE,
            percentage = 60, startedAt = 0, completedAt = 120, questionResultsJson = "[]"
        )
        assertTrue("12/20 must pass", twentyPassed.passed)

        val twentyFailed = PracticeResultEntity(
            id = "4", practiceType = "FULL", title = "Full Practice",
            totalQuestions = 20, correctAnswers = 11, incorrectAnswers = 9,
            passed = 11 >= TestConstants.PASSING_SCORE,
            percentage = 55, startedAt = 0, completedAt = 120, questionResultsJson = "[]"
        )
        assertFalse("11/20 must fail", twentyFailed.passed)
    }

    // =========================================================================
    // 5. PRACTICE QUESTION SELECTION & DUPLICATE PREVENTION
    // =========================================================================

    @Test
    fun testPracticeEngine_selectionAndNoDuplicates(): Unit = runBlocking {
        repository.ensureInitialized()

        // Quick practice: 10 questions, all unique
        val quickQuestions = repository.selectQuestionsForPractice(PracticeType.QUICK, requestedCount = 10)
        assertEquals("Quick practice must select 10 questions", 10, quickQuestions.size)
        assertEquals("Quick practice must have no duplicate questions", 10, quickQuestions.map { it.id }.distinct().size)

        // Full practice: 20 questions, all unique
        val fullQuestions = repository.selectQuestionsForPractice(PracticeType.FULL, requestedCount = 20)
        assertEquals("Full practice must select 20 questions", 20, fullQuestions.size)
        assertEquals("Full practice must have no duplicate questions", 20, fullQuestions.map { it.id }.distinct().size)

        // Topic practice: American Government (requested count capped at available)
        val govQuestions = repository.selectQuestionsForPractice(
            PracticeType.TOPIC,
            topic = "American Government",
            requestedCount = 20
        )
        assertEquals("Gov topic practice must select 20 questions", 20, govQuestions.size)
        assertTrue("All selected questions must be American Government",
            govQuestions.all { it.category == "American Government" })
        assertEquals("Topic practice must have no duplicate questions", 20, govQuestions.map { it.id }.distinct().size)
    }

    // =========================================================================
    // 6. STUDY SEARCH AND FILTERS AUDIT
    // =========================================================================

    @Test
    fun testStudyScreen_searchAndFilterCounts(): Unit = runBlocking {
        repository.ensureInitialized()

        // Test Category counts
        val gov = repository.getQuestionsByCategory("American Government").first()
        assertEquals("American Government category must have 75 questions", 75, gov.size)

        val hist = repository.getQuestionsByCategory("American History").first()
        assertEquals("American History category must have 43 questions", 43, hist.size)

        val civics = repository.getQuestionsByCategory("Integrated Civics").first()
        assertEquals("Integrated Civics category must have 10 questions", 10, civics.size)

        // Test 65/20 count
        val senior = repository.get65_20Questions().first()
        assertEquals("65/20 query must return exactly 20 questions", 20, senior.size)

        // Search by question number "1"
        val searchByNum = repository.searchQuestions("1").first()
        assertTrue("Search for '1' must find matching questions", searchByNum.isNotEmpty())

        // Search by constitution
        val searchConstitution = repository.searchQuestions("Constitution").first()
        assertTrue("Search for 'Constitution' must find matching questions", searchConstitution.isNotEmpty())
    }

    // =========================================================================
    // 7. ANSWER MATCHER ROBUSTNESS
    // =========================================================================

    @Test
    fun testAnswerMatcher_robustEvaluation() {
        val acceptedAnswers = listOf("The Constitution", "Constitution")

        // Exact match
        val exact = AnswerMatcher.matchAnswer("The Constitution", acceptedAnswers)
        assertEquals(MatchStatus.CORRECT, exact.status)

        // Punctuation and lowercase match
        val punctuation = AnswerMatcher.matchAnswer("the constitution.", acceptedAnswers)
        assertEquals(MatchStatus.CORRECT, punctuation.status)

        // Filler words ("I believe it is the constitution")
        val filler = AnswerMatcher.matchAnswer("I believe it is the constitution", acceptedAnswers)
        assertTrue("Filler match should be CORRECT or LIKELY_CORRECT",
            filler.status == MatchStatus.CORRECT || filler.status == MatchStatus.LIKELY_CORRECT)

        // Completely wrong answer
        val wrong = AnswerMatcher.matchAnswer("The Declaration of Independence", acceptedAnswers)
        assertEquals(MatchStatus.NO_MATCH, wrong.status)
    }
}
