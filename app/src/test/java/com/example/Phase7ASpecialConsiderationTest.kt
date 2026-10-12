package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.learning.MasteryStatus
import com.example.data.local.AppDatabase
import com.example.data.local.DatabaseInitializer
import com.example.data.model.PracticeType
import com.example.data.repository.DefaultQuestionRepository
import com.example.data.repository.QuestionRepository
import com.example.domain.usecase.GetLearningProgressUseCase
import com.example.ui.flashcards.FlashcardFilter
import com.example.ui.senior6520.SeniorFilter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class Phase7ASpecialConsiderationTest {

    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var repository: QuestionRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val dao = db.questionDao()
        val progressDao = db.questionProgressDao()
        val practiceResultDao = db.practiceResultDao()
        val initializer = DatabaseInitializer(context, dao)
        repository = DefaultQuestionRepository(dao, progressDao, practiceResultDao, initializer)
        runBlocking {
            repository.ensureInitialized()
        }
    }

    @After
    fun tearDown() {
        db.close()
    }

    /**
     * Test 1: Exactly 20 questions in the dataset must have is65_20 == true.
     */
    @Test
    fun test1_exactly20QuestionsMatch6520() {
        val jsonFile = File("src/main/assets/civics_questions.json")
        assertTrue("civics_questions.json must exist in assets", jsonFile.exists())

        val jsonString = jsonFile.readText()
        val jsonRoot = org.json.JSONObject(jsonString)
        val jsonArray = jsonRoot.getJSONArray("questions")
        assertEquals("Total questions must be exactly 128", 128, jsonArray.length())

        var count6520 = 0
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            if (obj.optBoolean("is65_20", false)) {
                count6520++
            }
        }
        assertEquals("Exactly 20 questions must be designated 65/20 special consideration", 20, count6520)
    }

    /**
     * Test 2: All 20 questions derive strictly from the 128-question dataset (no fake or foreign IDs).
     */
    @Test
    fun test2_all20QuestionsDeriveFrom128Dataset() {
        runBlocking {
            val allQuestions = repository.getAllStudyQuestions().first()
            assertEquals(128, allQuestions.size)

            val seniorQuestions = allQuestions.filter { it.is65_20 }
            assertEquals(20, seniorQuestions.size)

            val allIds = allQuestions.map { it.id }.toSet()
            seniorQuestions.forEach { seniorQ ->
                assertTrue("Senior question ${seniorQ.id} must be member of 128 dataset", allIds.contains(seniorQ.id))
                assertTrue("Question number must be between 1 and 128", seniorQ.questionNumber in 1..128)
                assertTrue("Question text must not be blank", seniorQ.questionText.isNotBlank())
                assertTrue("Answers must not be empty", seniorQ.answers.isNotEmpty())
            }
        }
    }

    /**
     * Test 3: 65/20 Study screen receives and displays only 20 questions.
     */
    @Test
    fun test3_senior6520StudyFilteringDisplaysOnly20() {
        runBlocking {
            val allQuestions = repository.getAllStudyQuestions().first()
            val seniorStudyQuestions = allQuestions.filter { it.is65_20 }
            assertEquals("Study 65/20 filtered view must have exactly 20 questions", 20, seniorStudyQuestions.size)
            assertTrue(seniorStudyQuestions.all { it.is65_20 })
        }
    }

    /**
     * Test 4: 65/20 Practice mode selects only from the 20 questions.
     */
    @Test
    fun test4_senior6520PracticeModeSelectsOnlyFrom20() {
        runBlocking {
            val practiceAll20 = repository.selectQuestionsForPractice(PracticeType.SENIOR_65_20, "FULL", 20)
            assertEquals("Full 65/20 practice must return 20 questions", 20, practiceAll20.size)
            assertTrue("Every practice question must have is65_20 == true", practiceAll20.all { it.is65_20 })

            val practiceQuick10 = repository.selectQuestionsForPractice(PracticeType.SENIOR_65_20, "QUICK", 10)
            assertEquals("Quick 65/20 practice must return 10 questions", 10, practiceQuick10.size)
            assertTrue("Every quick practice question must have is65_20 == true", practiceQuick10.all { it.is65_20 })
        }
    }

    /**
     * Test 5: 65/20 Flashcards count shows 20 instead of 128.
     */
    @Test
    fun test5_senior6520FlashcardsCountShows20() {
        runBlocking {
            val allQuestions = repository.getAllStudyQuestions().first()
            val flashcardsFiltered = when (FlashcardFilter.SENIOR_65_20) {
                FlashcardFilter.SENIOR_65_20 -> allQuestions.filter { it.is65_20 }
                else -> allQuestions
            }
            assertEquals("65/20 Flashcard deck must contain exactly 20 cards", 20, flashcardsFiltered.size)
            assertTrue(flashcardsFiltered.all { it.is65_20 })
        }
    }

    /**
     * Test 6: 65/20 Speaking mode loads only the 20 questions.
     */
    @Test
    fun test6_senior6520SpeakingModeLoadsOnly20() {
        runBlocking {
            val speakingSenior = repository.selectQuestionsForPractice(PracticeType.SPEAKING, "SENIOR_65_20", 20)
            assertEquals("Speaking practice for 65/20 must return 20 questions", 20, speakingSenior.size)
            assertTrue("All speaking practice questions must have is65_20 == true", speakingSenior.all { it.is65_20 })
        }
    }

    /**
     * Test 7: 65/20 Favorites list contains only favorited questions from the 20.
     */
    @Test
    fun test7_senior6520FavoritesContainsOnlyFavoritedFrom20() {
        runBlocking {
            val all = repository.getAllStudyQuestions().first()
            val seniorList = all.filter { it.is65_20 }
            val nonSenior = all.first { !it.is65_20 }
            val targetSenior1 = seniorList[0]
            val targetSenior2 = seniorList[1]

            // Favorite two senior and one non-senior
            repository.toggleFavorite(targetSenior1.id)
            repository.toggleFavorite(targetSenior2.id)
            repository.toggleFavorite(nonSenior.id)

            val updatedAll = repository.getAllStudyQuestions().first()
            val seniorFavorites = updatedAll.filter { it.is65_20 && it.isFavorite }

            assertEquals("65/20 Favorites must contain exactly the 2 favorited senior questions", 2, seniorFavorites.size)
            assertTrue(seniorFavorites.all { it.is65_20 })
            assertTrue(seniorFavorites.all { it.isFavorite })
            assertFalse(seniorFavorites.any { it.id == nonSenior.id })
        }
    }

    /**
     * Test 8: 65/20 Needs Practice list contains only needs-practice questions from the 20.
     */
    @Test
    fun test8_senior6520NeedsPracticeContainsOnlyNeedsPracticeFrom20() {
        runBlocking {
            val all = repository.getAllStudyQuestions().first()
            val seniorList = all.filter { it.is65_20 }
            val nonSenior = all.first { !it.is65_20 }
            val targetSenior = seniorList[0]

            // Record incorrect evaluations to trigger needs practice
            repository.recordQuestionEvaluation(targetSenior.id, false)
            repository.recordQuestionEvaluation(nonSenior.id, false)

            val updatedAll = repository.getAllStudyQuestions().first()
            val seniorNeedsPractice = updatedAll.filter { it.is65_20 && (it.needsPractice || it.masteryStatus == MasteryStatus.NEEDS_PRACTICE) }

            assertEquals("65/20 Needs Practice must contain exactly 1 senior question", 1, seniorNeedsPractice.size)
            assertEquals(targetSenior.id, seniorNeedsPractice.first().id)
            assertFalse(seniorNeedsPractice.any { it.id == nonSenior.id })
        }
    }

    /**
     * Test 9: Progress updates when a 65/20 question is studied/mastered.
     */
    @Test
    fun test9_progressUpdatesWhenSeniorQuestionStudiedOrMastered() {
        runBlocking {
            val all = repository.getAllStudyQuestions().first()
            val seniorList = all.filter { it.is65_20 }

            // Mark 8 questions as studied
            for (i in 0 until 8) {
                repository.setStudied(seniorList[i].id, true)
            }

            val useCase = GetLearningProgressUseCase(repository)
            val progress = useCase().first()

            assertEquals(20, progress.senior6520Total)
            assertEquals(8, progress.senior6520Studied)
            assertEquals(40, progress.senior6520Percentage) // 8 / 20 = 40%
            assertEquals(12, progress.senior6520NotStarted) // 20 - 8 = 12
        }
    }

    /**
     * Test 10: Reset Learning Progress resets 65/20 progress back to 0.
     */
    @Test
    fun test10_resetProgressResetsSenior6520MetricsToZero() {
        runBlocking {
            val all = repository.getAllStudyQuestions().first()
            val seniorList = all.filter { it.is65_20 }

            // Mark questions studied and favorite
            repository.setStudied(seniorList[0].id, true)
            repository.setStudied(seniorList[1].id, true)
            repository.toggleFavorite(seniorList[0].id)

            val useCase = GetLearningProgressUseCase(repository)
            val progressBefore = useCase().first()
            assertEquals(2, progressBefore.senior6520Studied)

            // Reset progress
            repository.resetAllProgress()

            val progressAfter = useCase().first()
            assertEquals(20, progressAfter.senior6520Total)
            assertEquals(0, progressAfter.senior6520Studied)
            assertEquals(0, progressAfter.senior6520Mastered)
            assertEquals(0, progressAfter.senior6520NeedsPractice)
            assertEquals(20, progressAfter.senior6520NotStarted)
            assertEquals(0, progressAfter.senior6520Percentage)
            assertEquals(1, progressAfter.senior6520Favorites) // Favorites are preserved on learning progress reset
        }
    }
}
