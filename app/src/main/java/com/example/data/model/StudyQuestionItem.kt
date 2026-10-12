package com.example.data.model

import com.example.data.learning.MasteryCalculator
import com.example.data.learning.MasteryStatus
import com.example.data.local.entity.AnswerEntity
import com.example.data.local.entity.QuestionEntity
import com.example.data.local.entity.QuestionWithAnswers

/**
 * Represents a civics question united with its accepted answers and local user study state.
 */
data class StudyQuestionItem(
    val question: QuestionEntity,
    val answers: List<AnswerEntity>,
    val isFavorite: Boolean = false,
    val isStudied: Boolean = false,
    val needsPractice: Boolean = false,
    val isMastered: Boolean = false,
    val lastViewed: Long = 0L,
    val viewCount: Int = 0,
    val correctCount: Int = 0,
    val incorrectCount: Int = 0,
    val lastResult: String? = null
) {
    val id: String get() = question.id
    val questionNumber: Int get() = question.questionNumber
    val category: String get() = question.category
    val subcategory: String get() = question.subcategory
    val questionText: String get() = question.questionText
    val is65_20: Boolean get() = question.is65_20
    val isDynamicAnswer: Boolean get() = question.isDynamicAnswer
    val dynamicAnswerType: String? get() = question.dynamicAnswerType
    val currentAnswer: String? get() = question.currentAnswer
    val answerLastVerified: String? get() = question.answerLastVerified
    val answerSourceName: String? get() = question.answerSourceName
    val answerSourceUrl: String? get() = question.answerSourceUrl
    val explanation: String get() = question.explanation
    val explanationSourceType: String get() = question.explanationSourceType
    val explanationSourceLabel: String get() = question.explanationSourceLabel
    val sourceType: String get() = question.sourceType
    val sourceName: String get() = question.sourceName
    val sourceUrl: String get() = question.sourceUrl

    val totalAttempts: Int get() = correctCount + incorrectCount

    val accuracyPercentage: Int
        get() = if (totalAttempts > 0) ((correctCount * 100f) / totalAttempts).toInt() else 0

    val masteryStatus: MasteryStatus
        get() = MasteryCalculator.calculate(this)

    val primaryAnswerText: String
        get() = answers.firstOrNull { it.isPrimary }?.answerText
            ?: answers.firstOrNull()?.answerText
            ?: ""

    val answerTexts: List<String>
        get() = answers.sortedBy { it.orderIndex }.map { it.answerText }

    companion object {
        fun from(
            qwa: QuestionWithAnswers,
            isFavorite: Boolean = false,
            isStudied: Boolean = false,
            needsPractice: Boolean = false,
            isMastered: Boolean = false,
            lastViewed: Long = 0L,
            viewCount: Int = 0,
            correctCount: Int = 0,
            incorrectCount: Int = 0,
            lastResult: String? = null
        ): StudyQuestionItem {
            return StudyQuestionItem(
                question = qwa.question,
                answers = qwa.answers,
                isFavorite = isFavorite,
                isStudied = isStudied,
                needsPractice = needsPractice,
                isMastered = isMastered,
                lastViewed = lastViewed,
                viewCount = viewCount,
                correctCount = correctCount,
                incorrectCount = incorrectCount,
                lastResult = lastResult
            )
        }
    }
}
