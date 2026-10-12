package com.example.data.learning

import com.example.data.model.StudyQuestionItem

/**
 * WeaknessCalculator ranks questions and categories that need more practice.
 *
 * Factors considered:
 * - High incorrect frequency
 * - Low accuracy
 * - Recent incorrect result
 * - Recency of mistakes
 */
object WeaknessCalculator {

    /**
     * Calculates a numerical weakness priority score.
     * Higher score indicates higher practice priority (weaker knowledge).
     */
    fun calculateWeaknessScore(
        correctCount: Int,
        incorrectCount: Int,
        lastResult: String?,
        lastViewed: Long,
        currentTime: Long = System.currentTimeMillis()
    ): Float {
        val totalAttempts = correctCount + incorrectCount
        if (totalAttempts == 0) {
            return 0f
        }

        var score = 0f

        // 1. Incorrect attempts penalty (10 points per error)
        score += incorrectCount * 10f

        // 2. Error rate percentage (up to 30 points)
        val errorRatio = incorrectCount.toFloat() / totalAttempts
        score += errorRatio * 30f

        // 3. Last result weight
        if (lastResult == "INCORRECT") {
            score += 25f
        } else if (lastResult == "CORRECT") {
            score -= 10f
        }

        // 4. Low accuracy penalty
        val accuracy = (correctCount.toFloat() / totalAttempts) * 100f
        if (accuracy < 50f) {
            score += 20f
        } else if (accuracy < 70f) {
            score += 10f
        }

        // 5. Recency boost for fresh mistakes
        if (lastResult == "INCORRECT" && lastViewed > 0) {
            val hoursElapsed = (currentTime - lastViewed) / (1000 * 60 * 60)
            if (hoursElapsed < 48) {
                score += 5f
            }
        }

        return score.coerceAtLeast(0f)
    }

    fun calculateWeaknessScore(item: StudyQuestionItem, currentTime: Long = System.currentTimeMillis()): Float {
        return calculateWeaknessScore(
            correctCount = item.correctCount,
            incorrectCount = item.incorrectCount,
            lastResult = item.lastResult,
            lastViewed = item.lastViewed,
            currentTime = currentTime
        )
    }

    /**
     * Determines whether a question qualifies as a "weak question" needing review.
     */
    fun isWeakQuestion(item: StudyQuestionItem): Boolean {
        if (item.needsPractice) return true
        if (item.incorrectCount > 0) return true
        if (item.lastResult == "INCORRECT") return true
        return false
    }

    /**
     * Filters and ranks questions from weakest (highest practice priority) to least weak.
     */
    fun rankWeakQuestions(
        questions: List<StudyQuestionItem>,
        currentTime: Long = System.currentTimeMillis()
    ): List<StudyQuestionItem> {
        return questions
            .filter { isWeakQuestion(it) }
            .sortedWith(
                compareByDescending<StudyQuestionItem> { calculateWeaknessScore(it, currentTime) }
                    .thenByDescending { it.incorrectCount }
                    .thenBy { it.correctCount }
            )
    }

    /**
     * Ranks civics categories by mistake frequency / lowest accuracy.
     */
    fun rankWeakCategories(questions: List<StudyQuestionItem>): List<CategoryWeakness> {
        val categories = listOf("American Government", "American History", "Integrated Civics")
        return categories.map { categoryName ->
            val catQuestions = questions.filter { it.category.equals(categoryName, ignoreCase = true) }
            val totalAttempts = catQuestions.sumOf { it.correctCount + it.incorrectCount }
            val totalIncorrect = catQuestions.sumOf { it.incorrectCount }
            val totalCorrect = catQuestions.sumOf { it.correctCount }
            val accuracy = if (totalAttempts > 0) {
                ((totalCorrect * 100f) / totalAttempts).toInt()
            } else {
                100 // No attempts yet
            }
            val weakQuestionCount = catQuestions.count { isWeakQuestion(it) }

            CategoryWeakness(
                categoryName = categoryName,
                totalQuestions = catQuestions.size,
                weakQuestionCount = weakQuestionCount,
                accuracyPercentage = accuracy,
                totalIncorrect = totalIncorrect
            )
        }.sortedWith(
            compareByDescending<CategoryWeakness> { it.weakQuestionCount }
                .thenBy { it.accuracyPercentage }
                .thenByDescending { it.totalIncorrect }
        )
    }
}

data class CategoryWeakness(
    val categoryName: String,
    val totalQuestions: Int,
    val weakQuestionCount: Int,
    val accuracyPercentage: Int,
    val totalIncorrect: Int
)
