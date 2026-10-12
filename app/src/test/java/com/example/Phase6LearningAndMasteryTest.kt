package com.example

import com.example.data.learning.MasteryCalculator
import com.example.data.learning.MasteryStatus
import com.example.data.learning.StudyStreakCalculator
import com.example.data.learning.WeaknessCalculator
import com.example.data.local.entity.AnswerEntity
import com.example.data.local.entity.QuestionEntity
import com.example.data.model.StudyQuestionItem
import org.junit.Assert.assertEquals
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class Phase6LearningAndMasteryTest {

    @Test
    fun testMasteryStatusNotStarted() {
        val status = MasteryCalculator.calculate(
            isStudied = false,
            viewCount = 0,
            correctCount = 0,
            incorrectCount = 0,
            lastResult = null
        )
        assertEquals(MasteryStatus.NOT_STARTED, status)
    }

    @Test
    fun testMasteryStatusNeedsPracticeWhenLastResultIncorrect() {
        val status = MasteryCalculator.calculate(
            isStudied = true,
            viewCount = 5,
            correctCount = 4,
            incorrectCount = 1,
            lastResult = "INCORRECT"
        )
        assertEquals(MasteryStatus.NEEDS_PRACTICE, status)
    }

    @Test
    fun testMasteryStatusNeedsPracticeWhenIncorrectGreaterThanCorrect() {
        val status = MasteryCalculator.calculate(
            isStudied = true,
            viewCount = 4,
            correctCount = 1,
            incorrectCount = 3,
            lastResult = "CORRECT"
        )
        assertEquals(MasteryStatus.NEEDS_PRACTICE, status)
    }

    @Test
    fun testMasteryStatusMasteredConditions() {
        // timesCorrect >= 2 and timesCorrect > timesIncorrect and lastResult == CORRECT
        val status = MasteryCalculator.calculate(
            isStudied = true,
            viewCount = 4,
            correctCount = 3,
            incorrectCount = 1,
            lastResult = "CORRECT"
        )
        assertEquals(MasteryStatus.MASTERED, status)
    }

    @Test
    fun testMasteryStatusLearningFallback() {
        // Seen once, answered correctly once (not yet 2 times)
        val status = MasteryCalculator.calculate(
            isStudied = true,
            viewCount = 1,
            correctCount = 1,
            incorrectCount = 0,
            lastResult = "CORRECT"
        )
        assertEquals(MasteryStatus.LEARNING, status)
    }

    @Test
    fun testStudyStreakCalculation() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val cal = Calendar.getInstance()

        val today = sdf.format(cal.time)
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterday = sdf.format(cal.time)
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val twoDaysAgo = sdf.format(cal.time)

        // 3-day streak active today
        val streakActive = StudyStreakCalculator.calculateStreak(setOf(today, yesterday, twoDaysAgo))
        assertEquals(3, streakActive)

        // Streak when today hasn't been done yet but yesterday was done (grace period)
        val streakYesterday = StudyStreakCalculator.calculateStreak(setOf(yesterday, twoDaysAgo))
        assertEquals(2, streakYesterday)

        // Broken streak (gap of > 1 day)
        cal.add(Calendar.DAY_OF_YEAR, -5)
        val oldDate = sdf.format(cal.time)
        val brokenStreak = StudyStreakCalculator.calculateStreak(setOf(oldDate))
        assertEquals(0, brokenStreak)
    }

    @Test
    fun testWeaknessRanking() {
        val q1 = createDummyQuestion(
            id = "Q1",
            category = "American Government",
            correct = 0,
            incorrect = 3,
            lastResult = "INCORRECT"
        )
        val q2 = createDummyQuestion(
            id = "Q2",
            category = "American Government",
            correct = 5,
            incorrect = 0,
            lastResult = "CORRECT"
        )
        val q3 = createDummyQuestion(
            id = "Q3",
            category = "American History",
            correct = 1,
            incorrect = 2,
            lastResult = "INCORRECT"
        )

        val weakList = WeaknessCalculator.rankWeakQuestions(listOf(q1, q2, q3))
        assertEquals(2, weakList.size)
        // Q1 has 3 mistakes and 0% accuracy -> should be ranked highest in weakness
        assertEquals("Q1", weakList[0].id)
        assertEquals("Q3", weakList[1].id)
    }

    private fun createDummyQuestion(
        id: String,
        category: String,
        correct: Int,
        incorrect: Int,
        lastResult: String?
    ): StudyQuestionItem {
        val questionEntity = QuestionEntity(
            id = id,
            questionNumber = 1,
            category = category,
            subcategory = "Subcategory",
            questionText = "Sample Question",
            is65_20 = false,
            isDynamicAnswer = false,
            dynamicAnswerType = null,
            currentAnswer = null,
            answerLastVerified = null,
            answerSourceName = null,
            answerSourceUrl = null,
            explanation = "Explanation",
            explanationSourceType = "USCIS",
            explanationSourceLabel = "USCIS",
            sourceType = "USCIS",
            sourceName = "USCIS",
            sourceUrl = "https://uscis.gov"
        )
        val answer = AnswerEntity(
            id = 1L,
            questionId = id,
            answerText = "Sample Answer",
            isPrimary = true,
            orderIndex = 1
        )
        return StudyQuestionItem(
            question = questionEntity,
            answers = listOf(answer),
            isStudied = true,
            correctCount = correct,
            incorrectCount = incorrect,
            lastResult = lastResult
        )
    }
}

