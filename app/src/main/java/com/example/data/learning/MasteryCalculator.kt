package com.example.data.learning

import androidx.compose.ui.graphics.Color
import com.example.data.model.StudyQuestionItem

/**
 * Mastery status for civics questions based on local study performance.
 * Note: This is an application learning metric, not an official USCIS legal mastery definition.
 */
enum class MasteryStatus(
    val label: String,
    val badgeSymbol: String,
    val badgeText: String
) {
    MASTERED(
        label = "Mastered",
        badgeSymbol = "✓",
        badgeText = "✓ Mastered"
    ),
    NEEDS_PRACTICE(
        label = "Needs Practice",
        badgeSymbol = "!",
        badgeText = "! Needs Practice"
    ),
    LEARNING(
        label = "Learning",
        badgeSymbol = "•",
        badgeText = "• Learning"
    ),
    NOT_STARTED(
        label = "Not Started",
        badgeSymbol = "○",
        badgeText = "○ Not Started"
    )
}

/**
 * Centralized calculator for determining question mastery status.
 *
 * Algorithm specifications:
 * - MASTERED when:
 *     timesCorrect >= 2 AND timesCorrect > timesIncorrect AND the most recent result is CORRECT.
 * - NEEDS_PRACTICE when:
 *     lastResult == INCORRECT OR timesIncorrect > timesCorrect.
 * - LEARNING when:
 *     has been seen/studied/attempted but does not meet above conditions.
 * - NOT_STARTED when:
 *     no study/practice interaction (viewCount == 0, correctCount == 0, incorrectCount == 0, isStudied == false).
 */
object MasteryCalculator {

    fun calculate(
        isStudied: Boolean,
        viewCount: Int,
        correctCount: Int,
        incorrectCount: Int,
        lastResult: String?
    ): MasteryStatus {
        val totalAttempts = correctCount + incorrectCount
        val hasInteracted = isStudied || viewCount > 0 || totalAttempts > 0

        if (!hasInteracted) {
            return MasteryStatus.NOT_STARTED
        }

        // MASTERED rule:
        // timesCorrect >= 2 and timesCorrect > timesIncorrect and the most recent result is CORRECT
        if (correctCount >= 2 && correctCount > incorrectCount && lastResult == "CORRECT") {
            return MasteryStatus.MASTERED
        }

        // NEEDS_PRACTICE rule:
        // lastResult == INCORRECT OR timesIncorrect > timesCorrect
        if (lastResult == "INCORRECT" || incorrectCount > correctCount) {
            return MasteryStatus.NEEDS_PRACTICE
        }

        // LEARNING: has interaction/study but not yet mastered or flagged
        return MasteryStatus.LEARNING
    }

    fun calculate(item: StudyQuestionItem): MasteryStatus {
        return calculate(
            isStudied = item.isStudied,
            viewCount = item.viewCount,
            correctCount = item.correctCount,
            incorrectCount = item.incorrectCount,
            lastResult = item.lastResult
        )
    }
}
