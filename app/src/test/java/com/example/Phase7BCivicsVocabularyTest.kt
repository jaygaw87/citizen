package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.DatabaseInitializer
import com.example.data.local.PreferencesRepository
import com.example.data.local.dao.VocabularyDao
import com.example.data.local.entity.VocabularyEntity
import com.example.data.model.VocabularyItem
import com.example.data.model.VocabularyStatus
import com.example.data.repository.DefaultQuestionRepository
import com.example.data.repository.QuestionRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
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
class Phase7BCivicsVocabularyTest {

    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var vocabularyDao: VocabularyDao
    private lateinit var repository: QuestionRepository
    private lateinit var preferencesRepository: PreferencesRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        val questionDao = db.questionDao()
        val questionProgressDao = db.questionProgressDao()
        val practiceResultDao = db.practiceResultDao()
        val studyActivityDao = db.studyActivityDao()
        vocabularyDao = db.vocabularyDao()

        val initializer = DatabaseInitializer(context, questionDao, vocabularyDao)
        repository = DefaultQuestionRepository(
            questionDao = questionDao,
            questionProgressDao = questionProgressDao,
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

    @Test
    fun testVocabularyDatasetIntegrity() {
        val jsonFile = File("src/main/assets/civics_vocabulary.json")
        assertTrue("civics_vocabulary.json must exist in assets", jsonFile.exists())

        val jsonString = jsonFile.readText()
        val jsonArray = JSONArray(jsonString)

        assertTrue(
            "Vocabulary count should be in range 100-150. Current count: ${jsonArray.length()}",
            jsonArray.length() in 100..150
        )

        val validCategories = setOf(
            "Government",
            "American Government",
            "Rights & Responsibilities",
            "American History",
            "Geography & Civics",
            "Geography & Integrated Civics"
        )

        val uniqueIds = mutableSetOf<String>()
        val uniqueWords = mutableSetOf<String>()

        for (i in 0 until jsonArray.length()) {
            val item = jsonArray.getJSONObject(i)
            val id = item.getString("id")
            val word = item.getString("word")
            val pronunciation = item.optString("pronunciation", "")
            val simpleDef = item.getString("simpleDefinition")
            val category = item.getString("category")
            val example = item.getString("exampleSentence")
            val relatedQs = item.getJSONArray("relatedQuestionIds")

            assertTrue("ID must be unique: $id", uniqueIds.add(id))
            assertTrue("Word must be unique: $word", uniqueWords.add(word.lowercase()))
            assertTrue("ID cannot be blank", id.isNotBlank())
            assertTrue("Word cannot be blank", word.isNotBlank())
            assertTrue("Pronunciation cannot be blank", pronunciation.isNotBlank())
            assertTrue("Simple definition cannot be blank", simpleDef.isNotBlank())
            assertTrue("Example sentence cannot be blank", example.isNotBlank())
            assertTrue("Category '$category' must be in recognized civics categories", validCategories.contains(category))

            for (qIdx in 0 until relatedQs.length()) {
                val qStr = relatedQs.getString(qIdx)
                val qNum = qStr.removePrefix("Q").toIntOrNull()
                assertNotNull("Related question ID '$qStr' should have a numeric index", qNum)
                assertTrue("Related question number must be between 1 and 128: $qNum", qNum!! in 1..128)
            }
        }
    }

    @Test
    fun testDatabaseSeedingAndDaoOperations() = runBlocking {
        // Insert sample vocabulary entities
        val sampleEntity1 = VocabularyEntity(
            id = "v001",
            word = "Constitution",
            pronunciation = "kon-stuh-TOO-shun",
            simpleDefinition = "The supreme law of the United States.",
            detailedDefinition = "The highest legal document establishing federal governance.",
            category = "American Government",
            exampleSentence = "The Constitution sets the framework of our government.",
            relatedQuestionIds = "[1, 2]"
        )
        val sampleEntity2 = VocabularyEntity(
            id = "v002",
            word = "Amendment",
            pronunciation = "uh-MEND-munt",
            simpleDefinition = "A change or addition to the Constitution.",
            detailedDefinition = "An alteration to the supreme law of the land.",
            category = "American Government",
            exampleSentence = "The first ten amendments are known as the Bill of Rights.",
            relatedQuestionIds = "[4, 5]"
        )

        vocabularyDao.insertAll(listOf(sampleEntity1, sampleEntity2))

        val loaded = vocabularyDao.getAllVocabularyWithProgress().first()
        assertEquals(2, loaded.size)

        // Test toggle favorite
        vocabularyDao.toggleFavorite("v001")
        val afterFav = vocabularyDao.getAllVocabularyWithProgress().first()
        val item1 = afterFav.find { it.vocabulary.id == "v001" }
        assertNotNull(item1)
        assertTrue(item1!!.progress?.isFavorite == true)

        // Test update status to LEARNED
        vocabularyDao.updateStatus("v001", VocabularyStatus.LEARNED.name, System.currentTimeMillis())
        val afterLearned = vocabularyDao.getAllVocabularyWithProgress().first()
        val item1Learned = afterLearned.find { it.vocabulary.id == "v001" }
        assertEquals(VocabularyStatus.LEARNED.name, item1Learned?.progress?.status)

        // Test update status to NEEDS_REVIEW
        vocabularyDao.updateStatus("v002", VocabularyStatus.NEEDS_REVIEW.name, System.currentTimeMillis())
        val afterReview = vocabularyDao.getAllVocabularyWithProgress().first()
        val item2Review = afterReview.find { it.vocabulary.id == "v002" }
        assertEquals(VocabularyStatus.NEEDS_REVIEW.name, item2Review?.progress?.status)
    }

    @Test
    fun testResetLearningProgressPreservesVocabularyFavorites() = runBlocking {
        val sampleEntities = listOf(
            VocabularyEntity(
                id = "v101",
                word = "Liberty",
                pronunciation = "LIB-er-tee",
                simpleDefinition = "Freedom from arbitrary or despotic control.",
                detailedDefinition = "The state of being free within society.",
                category = "Rights & Responsibilities",
                exampleSentence = "The Declaration emphasizes life, liberty, and the pursuit of happiness.",
                relatedQuestionIds = "[9]"
            ),
            VocabularyEntity(
                id = "v102",
                word = "Democracy",
                pronunciation = "dih-MOK-ruh-see",
                simpleDefinition = "A government where supreme power is vested in the people.",
                detailedDefinition = "A form of governance ruled by elected representatives.",
                category = "American Government",
                exampleSentence = "The United States is a constitutional federal republic.",
                relatedQuestionIds = "[1, 2]"
            ),
            VocabularyEntity(
                id = "v103",
                word = "Colony",
                pronunciation = "KOL-uh-nee",
                simpleDefinition = "A territory governed by another country.",
                detailedDefinition = "An area under the political control of another state.",
                category = "American History",
                exampleSentence = "There were thirteen original colonies.",
                relatedQuestionIds = "[59, 64]"
            )
        )

        vocabularyDao.insertAll(sampleEntities)

        // Set v101 as LEARNED and FAVORITE
        vocabularyDao.updateStatus("v101", VocabularyStatus.LEARNED.name, System.currentTimeMillis())
        vocabularyDao.toggleFavorite("v101") // true

        // Set v102 as NEEDS_REVIEW (not favorite)
        vocabularyDao.updateStatus("v102", VocabularyStatus.NEEDS_REVIEW.name, System.currentTimeMillis())

        // Set v103 as NOT_STARTED, but FAVORITE
        vocabularyDao.toggleFavorite("v103") // true

        // Verify initial state
        val beforeReset = repository.getAllVocabulary().first()
        val v101Before = beforeReset.find { it.id == "v101" }!!
        val v102Before = beforeReset.find { it.id == "v102" }!!
        val v103Before = beforeReset.find { it.id == "v103" }!!

        assertEquals(VocabularyStatus.LEARNED, v101Before.status)
        assertTrue(v101Before.isFavorite)

        assertEquals(VocabularyStatus.NEEDS_REVIEW, v102Before.status)
        assertFalse(v102Before.isFavorite)

        assertEquals(VocabularyStatus.NOT_STARTED, v103Before.status)
        assertTrue(v103Before.isFavorite)

        // Execute Reset Learning Progress
        repository.resetLearningProgress()

        // Verify state after reset
        val afterReset = repository.getAllVocabulary().first()
        val v101After = afterReset.find { it.id == "v101" }!!
        val v102After = afterReset.find { it.id == "v102" }!!
        val v103After = afterReset.find { it.id == "v103" }!!

        // Learned status MUST be reset to NOT_STARTED
        assertEquals(VocabularyStatus.NOT_STARTED, v101After.status)
        // Needs-review status MUST be reset to NOT_STARTED
        assertEquals(VocabularyStatus.NOT_STARTED, v102After.status)
        assertEquals(VocabularyStatus.NOT_STARTED, v103After.status)

        // FAVORITES MUST BE PRESERVED
        assertTrue("v101 favorite status must be preserved after reset", v101After.isFavorite)
        assertFalse("v102 favorite status was false and must remain false", v102After.isFavorite)
        assertTrue("v103 favorite status must be preserved after reset", v103After.isFavorite)
    }

    @Test
    fun testVocabularyOfflineSearchAndFiltering() {
        val sampleList = listOf(
            VocabularyItem(
                entity = VocabularyEntity(
                    id = "v001",
                    word = "Constitution",
                    pronunciation = "kon-stuh-TOO-shun",
                    simpleDefinition = "The supreme law of the United States.",
                    detailedDefinition = "The highest legal document establishing federal governance.",
                    category = "American Government",
                    exampleSentence = "The Constitution sets the framework of our government.",
                    relatedQuestionIds = "[1, 2]"
                ),
                status = VocabularyStatus.LEARNED,
                isFavorite = true
            ),
            VocabularyItem(
                entity = VocabularyEntity(
                    id = "v002",
                    word = "Amendment",
                    pronunciation = "uh-MEND-munt",
                    simpleDefinition = "A change or addition to the Constitution.",
                    detailedDefinition = "An alteration to the supreme law of the land.",
                    category = "American Government",
                    exampleSentence = "The first ten amendments are known as the Bill of Rights.",
                    relatedQuestionIds = "[4, 5]"
                ),
                status = VocabularyStatus.NEEDS_REVIEW,
                isFavorite = false
            ),
            VocabularyItem(
                entity = VocabularyEntity(
                    id = "v003",
                    word = "Declaration of Independence",
                    pronunciation = "dek-luh-RAY-shun uv in-dih-PEN-duns",
                    simpleDefinition = "A historic document declaring freedom from Great Britain.",
                    detailedDefinition = "Adopted on July 4, 1776, announcing separation from British rule.",
                    category = "American History",
                    exampleSentence = "Thomas Jefferson drafted the Declaration of Independence.",
                    relatedQuestionIds = "[8, 9]"
                ),
                status = VocabularyStatus.NOT_STARTED,
                isFavorite = true
            )
        )

        // 1. Search by exact or partial word
        val searchWord = sampleList.filter { it.word.contains("amend", ignoreCase = true) }
        assertEquals(1, searchWord.size)
        assertEquals("Amendment", searchWord.first().word)

        // 2. Search by definition text
        val searchDef = sampleList.filter { it.simpleDefinition.contains("supreme", ignoreCase = true) }
        assertEquals(1, searchDef.size)
        assertEquals("Constitution", searchDef.first().word)

        // 3. Search by example sentence
        val searchExample = sampleList.filter { it.exampleSentence.contains("Jefferson", ignoreCase = true) }
        assertEquals(1, searchExample.size)
        assertEquals("Declaration of Independence", searchExample.first().word)

        // 4. Category filtering
        val govCategory = sampleList.filter { it.category == "American Government" }
        assertEquals(2, govCategory.size)

        val histCategory = sampleList.filter { it.category == "American History" }
        assertEquals(1, histCategory.size)

        // 5. Status filtering
        val favorites = sampleList.filter { it.isFavorite }
        assertEquals(2, favorites.size)

        val needsReview = sampleList.filter { it.status == VocabularyStatus.NEEDS_REVIEW }
        assertEquals(1, needsReview.size)
        assertEquals("Amendment", needsReview.first().word)

        val learned = sampleList.filter { it.status == VocabularyStatus.LEARNED }
        assertEquals(1, learned.size)
        assertEquals("Constitution", learned.first().word)
    }

    @Test
    fun testQuickReview10TermsSelectionNoDuplicates() {
        val allTerms = (1..30).map { i ->
            VocabularyItem(
                entity = VocabularyEntity(
                    id = "term_$i",
                    word = "Word $i",
                    pronunciation = "Pron $i",
                    simpleDefinition = "Definition $i",
                    detailedDefinition = "Detailed $i",
                    category = "American Government",
                    exampleSentence = "Example $i",
                    relatedQuestionIds = "[$i]"
                ),
                status = if (i % 3 == 0) VocabularyStatus.NEEDS_REVIEW else VocabularyStatus.NOT_STARTED,
                isFavorite = (i % 2 == 0)
            )
        }

        // Test 10 terms selection for ALL filter
        val session10 = allTerms.shuffled().take(10)
        assertEquals(10, session10.size)
        assertEquals("No duplicates in session", 10, session10.map { it.id }.distinct().size)

        // Test favorites session
        val favTerms = allTerms.filter { it.isFavorite }.shuffled().take(10)
        assertTrue(favTerms.size <= 10)
        favTerms.forEach { assertTrue(it.isFavorite) }

        // Test needs review session
        val reviewTerms = allTerms.filter { it.status == VocabularyStatus.NEEDS_REVIEW }.shuffled().take(10)
        assertTrue(reviewTerms.size <= 10)
        reviewTerms.forEach { assertEquals(VocabularyStatus.NEEDS_REVIEW, it.status) }
    }
}
