package com.example

import com.example.data.learning.MasteryStatus
import com.example.data.local.entity.AnswerEntity
import com.example.data.local.entity.QuestionEntity
import com.example.data.local.entity.VocabularyEntity
import com.example.data.local.entity.VocabularyProgressEntity
import com.example.data.local.entity.VocabularyWithProgress
import com.example.data.model.StudyQuestionItem
import com.example.data.model.VocabularyItem
import com.example.data.model.VocabularyStatus
import com.example.domain.usecase.GetLearningProgressUseCase
import com.example.ui.quickreview.QuickReviewSection
import org.json.JSONArray
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class Phase7FeaturesTest {

    @Test
    fun testCivicsVocabularyJsonIntegrity() {
        val jsonFile = File("src/main/assets/civics_vocabulary.json")
        assertTrue("civics_vocabulary.json must exist in assets", jsonFile.exists())

        val jsonString = jsonFile.readText()
        val jsonArray = JSONArray(jsonString)

        assertTrue("Must contain approximately 100-150 curated civics terms, found: ${jsonArray.length()}", jsonArray.length() in 100..150)

        val ids = mutableSetOf<String>()
        val words = mutableSetOf<String>()

        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            val id = obj.getString("id")
            val word = obj.getString("word")
            val simpleDef = obj.getString("simpleDefinition")
            val detailedDef = obj.getString("detailedDefinition")
            val category = obj.getString("category")
            val example = obj.getString("exampleSentence")

            assertTrue("ID must be unique: $id", ids.add(id))
            assertTrue("Word must be unique: $word", words.add(word.lowercase()))
            assertTrue("Word must not be empty", word.isNotBlank())
            assertTrue("Simple definition must not be empty", simpleDef.isNotBlank())
            assertTrue("Detailed definition must not be empty", detailedDef.isNotBlank())
            assertTrue("Category must not be empty", category.isNotBlank())
            assertTrue("Example sentence must not be empty", example.isNotBlank())

            val relatedQs = obj.getJSONArray("relatedQuestionIds")
            assertTrue("Each term should link to at least one civics question", relatedQs.length() > 0)
        }
    }

    @Test
    fun testVocabularyWithProgressMapping() {
        val vocab = VocabularyEntity(
            id = "v001",
            word = "Constitution",
            simpleDefinition = "The supreme law of the land.",
            detailedDefinition = "The written document establishing the framework of the United States government.",
            pronunciation = "/ˌkɑːnstɪˈtuːʃn/",
            exampleSentence = "The Constitution is the supreme law of the land.",
            category = "Principles of Democracy",
            relatedQuestionIds = "Q001,Q002"
        )
        val progress = VocabularyProgressEntity(
            vocabId = "v001",
            isFavorite = true,
            status = "LEARNED",
            lastReviewedAt = 12345L
        )
        val withProgress = VocabularyWithProgress(vocab, progress)
        val item = VocabularyItem.fromRelation(withProgress)

        assertEquals("v001", item.id)
        assertEquals("Constitution", item.word)
        assertTrue(item.isFavorite)
        assertEquals(VocabularyStatus.LEARNED, item.status)
        assertEquals(listOf("Q001", "Q002"), item.relatedQuestionIds)
    }

    @Test
    fun testSenior6520LearningProgressMetrics() {
        val mockQuestions = (1..20).map { i ->
            val is65_20 = i <= 20
            val q = QuestionEntity(
                id = "Q%03d".format(i),
                questionNumber = i,
                category = "American Government",
                subcategory = "Principles of American Democracy",
                questionText = "Question $i",
                explanation = "Explanation $i",
                is65_20 = is65_20,
                isDynamicAnswer = false
            )
            val answers = listOf(
                AnswerEntity(id = i.toLong(), questionId = q.id, answerText = "Answer $i", isPrimary = true)
            )
            StudyQuestionItem(
                question = q,
                answers = answers,
                isStudied = i <= 8,
                isMastered = i <= 3,
                correctCount = if (i <= 3) 3 else 0,
                incorrectCount = if (i in 4..5) 2 else 0,
                needsPractice = i in 4..5,
                lastResult = if (i <= 3) "CORRECT" else if (i in 4..5) "INCORRECT" else null,
                isFavorite = i == 1
            )
        }

        val overview = GetLearningProgressUseCase.calculate(mockQuestions)

        assertEquals(20, overview.senior6520Total)
        assertEquals(8, overview.senior6520Studied)
        assertEquals(3, overview.senior6520Mastered)
        assertEquals(2, overview.senior6520NeedsPractice)
        assertEquals(3, overview.senior6520Learning) // 8 studied minus 3 mastered minus 2 needs practice = 3
        assertEquals(12, overview.senior6520NotStarted) // 20 - 8 studied = 12
        assertEquals(1, overview.senior6520Favorites)
        assertEquals(40, overview.senior6520Percentage) // 8 / 20 = 40%
    }

    @Test
    fun testQuickReviewSectionFiltering() {
        val q1 = createMockItem("Q001", 1, isFav = true, isStudied = true, isMastered = true)
        val q2 = createMockItem("Q002", 2, isFav = false, isStudied = true, incorrect = 2, needsPractice = true)
        val q3 = createMockItem("Q003", 3, isFav = false, isStudied = false, lastViewed = 5000L)
        val list = listOf(q1, q2, q3)

        // Needs Practice
        val needsPractice = list.filter { it.needsPractice || it.masteryStatus == MasteryStatus.NEEDS_PRACTICE }
        assertEquals(1, needsPractice.size)
        assertEquals("Q002", needsPractice.first().id)

        // Favorites
        val favorites = list.filter { it.isFavorite }
        assertEquals(1, favorites.size)
        assertEquals("Q001", favorites.first().id)

        // Missed
        val missed = list.filter { it.incorrectCount > 0 }
        assertEquals(1, missed.size)
        assertEquals("Q002", missed.first().id)

        // Mastered
        val mastered = list.filter { it.masteryStatus == MasteryStatus.MASTERED }
        assertEquals(1, mastered.size)
        assertEquals("Q001", mastered.first().id)

        // Viewed
        val viewed = list.filter { it.lastViewed > 0L }
        assertEquals(1, viewed.size)
        assertEquals("Q003", viewed.first().id)
    }

    private fun createMockItem(
        id: String,
        number: Int,
        isFav: Boolean = false,
        isStudied: Boolean = false,
        isMastered: Boolean = false,
        incorrect: Int = 0,
        needsPractice: Boolean = false,
        lastViewed: Long = 0L
    ): StudyQuestionItem {
        val q = QuestionEntity(
            id = id,
            questionNumber = number,
            category = "American Government",
            subcategory = "Principles",
            questionText = "Question $number",
            explanation = "Explanation $number",
            is65_20 = number <= 20,
            isDynamicAnswer = false
        )
        val answers = listOf(
            AnswerEntity(id = number.toLong(), questionId = id, answerText = "Ans $number", isPrimary = true)
        )
        return StudyQuestionItem(
            question = q,
            answers = answers,
            isFavorite = isFav,
            isStudied = isStudied,
            isMastered = isMastered,
            correctCount = if (isMastered) 3 else 0,
            incorrectCount = incorrect,
            needsPractice = needsPractice,
            lastResult = if (isMastered) "CORRECT" else if (incorrect > 0) "INCORRECT" else null,
            lastViewed = lastViewed
        )
    }
}
