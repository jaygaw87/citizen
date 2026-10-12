package com.example.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

/**
 * QuestionWithAnswers holds a 1-to-many relationship between a [QuestionEntity]
 * and its official [AnswerEntity] records.
 */
data class QuestionWithAnswers(
    @Embedded
    val question: QuestionEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "questionId"
    )
    val answers: List<AnswerEntity> = emptyList()
) {
    /**
     * Returns the ordered list of acceptable answer texts for display.
     */
    val answerTexts: List<String>
        get() = answers.sortedBy { it.orderIndex }.map { it.answerText }

    /**
     * Returns the primary or first answer text.
     */
    val primaryAnswerText: String
        get() = answers.firstOrNull { it.isPrimary }?.answerText
            ?: answers.minByOrNull { it.orderIndex }?.answerText
            ?: ""

    /**
     * Source authority for question text & answers: "USCIS_OFFICIAL".
     */
    val questionSourceType: String
        get() = question.sourceType

    /**
     * Source authority for explanation: "CURATED_EDUCATIONAL".
     */
    val explanationSourceType: String
        get() = question.explanationSourceType

    /**
     * Display label for explanation source attribution: "Educational explanation".
     */
    val explanationSourceLabel: String
        get() = question.explanationSourceLabel

    val isDynamic: Boolean
        get() = question.isDynamicAnswer

    val dynamicAnswerType: String?
        get() = question.dynamicAnswerType

    val currentAnswer: String?
        get() = question.currentAnswer

    val answerLastVerified: String?
        get() = question.answerLastVerified

    val answerSourceName: String?
        get() = question.answerSourceName

    val answerSourceUrl: String?
        get() = question.answerSourceUrl
}
