package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.learning.MasteryStatus
import com.example.data.local.AppDatabase
import com.example.data.local.DatabaseInitializer
import com.example.data.local.PreferencesRepository
import com.example.data.local.entity.PracticeResultEntity
import com.example.data.local.entity.StudyActivityEntity
import com.example.data.repository.DefaultQuestionRepository
import com.example.data.repository.QuestionRepository
import com.example.domain.usecase.GetLearningProgressUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ResetProgressTest {

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
        val initializer = DatabaseInitializer(context, questionDao)
        repository = DefaultQuestionRepository(
            questionDao = questionDao,
            questionProgressDao = progressDao,
            practiceResultDao = practiceResultDao,
            studyActivityDao = studyActivityDao,
            databaseInitializer = initializer
        )
        preferencesRepository = PreferencesRepository(context)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testResetLearningProgress_preservesQuestionsFavoritesHistoryAndSettings(): Unit = runBlocking {
        // Initialize 128 questions
        repository.ensureInitialized()
        val initialQuestionCount = repository.getQuestionCount().first()
        assertEquals("Initial question count must be 128", 128, initialQuestionCount)

        // 1 & 2. Create progress: Mark some correct
        repository.recordQuestionEvaluation("Q001", true)
        repository.recordQuestionEvaluation("Q001", true)
        repository.recordQuestionEvaluation("Q002", true)

        // 3. Mark some incorrect
        repository.recordQuestionEvaluation("Q003", false)
        repository.recordQuestionEvaluation("Q003", false)

        // 4. Create favorites
        repository.toggleFavorite("Q001") // Q001 has progress and is favorite
        repository.toggleFavorite("Q004") // Q004 has no answers but is favorite

        // 5. Create practice history
        val testResult = PracticeResultEntity(
            id = "session_test_001",
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
        repository.insertPracticeResult(testResult)

        // Also add study activity
        db.studyActivityDao().insertActivity(
            StudyActivityEntity(
                date = "2026-09-23",
                activityType = "PRACTICE_TEST",
                details = "Completed Quick Practice",
                createdAt = System.currentTimeMillis()
            )
        )

        // 6. Set daily goal
        preferencesRepository.setDailyStudyGoal(20)

        // Verify state before reset
        val beforeQuestions = repository.getAllStudyQuestions().first()
        val beforeOverview = GetLearningProgressUseCase.calculate(beforeQuestions)
        assertTrue("Studied count before reset should be > 0", beforeOverview.studiedCount > 0)
        assertEquals("Favorites count before reset should be 2", 2, beforeOverview.favoriteCount)
        assertEquals("Practice history should have 1 entry", 1, repository.getAllPracticeResults().first().size)
        assertEquals("Daily goal should be 20", 20, preferencesRepository.dailyStudyGoal.first())

        // 7. Execute resetLearningProgress()
        repository.resetLearningProgress()

        // 8. Verify question progress is reset
        val afterQuestions = repository.getAllStudyQuestions().first()
        val afterOverview = GetLearningProgressUseCase.calculate(afterQuestions)

        assertEquals("Studied count must be 0 after reset", 0, afterOverview.studiedCount)

        for (q in afterQuestions) {
            assertEquals("timesSeen/viewCount must be 0 for ${q.id}", 0, q.viewCount)
            assertEquals("correctCount must be 0 for ${q.id}", 0, q.correctCount)
            assertEquals("incorrectCount must be 0 for ${q.id}", 0, q.incorrectCount)
            assertNull("lastResult must be null for ${q.id}", q.lastResult)
            assertFalse("isStudied must be false for ${q.id}", q.isStudied)
            assertFalse("needsPractice must be false for ${q.id}", q.needsPractice)
            assertFalse("isMastered must be false for ${q.id}", q.isMastered)
        }

        // 9. Verify mastery is reset
        assertEquals("Mastered count must be 0", 0, afterOverview.masteredCount)
        assertEquals("Needs practice count must be 0", 0, afterOverview.needsPracticeCount)
        assertEquals("Not started count must be 128", 128, afterOverview.notStartedCount)
        for (q in afterQuestions) {
            assertEquals("Mastery state must be NOT_STARTED for ${q.id}", MasteryStatus.NOT_STARTED, q.masteryStatus)
        }

        // 10. Verify favorites remain
        assertEquals("Favorites count must remain 2", 2, afterOverview.favoriteCount)
        val favorites = afterQuestions.filter { it.isFavorite }
        assertEquals("Favorite questions list must contain 2 questions", 2, favorites.size)
        val favIds = favorites.map { it.id }.toSet()
        assertTrue("Q001 must still be a favorite", favIds.contains("Q001"))
        assertTrue("Q004 must still be a favorite", favIds.contains("Q004"))

        // 11. Verify practice history remains
        val history = repository.getAllPracticeResults().first()
        assertEquals("Practice history must remain intact", 1, history.size)
        assertEquals("Practice history id must match", "session_test_001", history[0].id)

        // 12. Verify daily goal remains
        assertEquals("Daily goal must remain 20", 20, preferencesRepository.dailyStudyGoal.first())

        // 13. Verify question count remains 128
        val finalQuestionCount = repository.getQuestionCount().first()
        assertEquals("Total questions must remain 128", 128, finalQuestionCount)
        assertEquals("afterQuestions size must be 128", 128, afterQuestions.size)
    }
}
