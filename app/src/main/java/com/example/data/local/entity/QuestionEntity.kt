package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Origin and authority identifiers for civics content components.
 */
object QuestionSourceTypes {
    /** Official text published by USCIS for citizenship exam administration */
    const val USCIS_OFFICIAL = "USCIS_OFFICIAL"

    /** Curated educational explanation providing historical context and memory aids */
    const val CURATED_EDUCATIONAL = "CURATED_EDUCATIONAL"
}

/**
 * QuestionEntity represents an official USCIS Civics Test Question.
 *
 * Designed around the current USCIS 2025/2026 128-question naturalization civics test (Form M-1778).
 * Uses stable, deterministic identifiers (e.g., "Q001", "Q002") so user progress and favorites
 * can safely reference question IDs across database updates without breaking progress.
 */
@Entity(
    tableName = "questions",
    indices = [
        Index(value = ["questionNumber"], unique = true),
        Index(value = ["category"]),
        Index(value = ["subcategory"]),
        Index(value = ["is65_20"]),
        Index(value = ["isDynamicAnswer"]),
        Index(value = ["dynamicAnswerType"])
    ]
)
data class QuestionEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "questionNumber")
    val questionNumber: Int,

    @ColumnInfo(name = "category")
    val category: String,

    @ColumnInfo(name = "subcategory")
    val subcategory: String,

    @ColumnInfo(name = "questionText")
    val questionText: String,

    /**
     * Source authority for question text: "USCIS_OFFICIAL".
     */
    @ColumnInfo(name = "sourceType")
    val sourceType: String = QuestionSourceTypes.USCIS_OFFICIAL,

    /**
     * Curated educational explanation providing historical/civic context.
     */
    @ColumnInfo(name = "explanation")
    val explanation: String,

    /**
     * Source authority for explanation: "CURATED_EDUCATIONAL".
     * Explicitly distinguished to avoid implying it was written by USCIS.
     */
    @ColumnInfo(name = "explanationSourceType")
    val explanationSourceType: String = QuestionSourceTypes.CURATED_EDUCATIONAL,

    /**
     * Human-readable label for UI indication: "Educational explanation".
     */
    @ColumnInfo(name = "explanationSourceLabel")
    val explanationSourceLabel: String = "Educational explanation",

    @ColumnInfo(name = "is65_20")
    val is65_20: Boolean,

    @ColumnInfo(name = "isDynamicAnswer")
    val isDynamicAnswer: Boolean,

    @ColumnInfo(name = "dynamicAnswerType")
    val dynamicAnswerType: String? = null,

    @ColumnInfo(name = "currentAnswer")
    val currentAnswer: String? = null,

    @ColumnInfo(name = "answerLastVerified")
    val answerLastVerified: String? = null,

    @ColumnInfo(name = "answerSourceName")
    val answerSourceName: String? = null,

    @ColumnInfo(name = "answerSourceUrl")
    val answerSourceUrl: String? = null,

    @ColumnInfo(name = "sourceName")
    val sourceName: String = "USCIS 2025 Civics Test (Form M-1778)",

    @ColumnInfo(name = "sourceUrl")
    val sourceUrl: String = "https://www.uscis.gov/citizenship/",

    @ColumnInfo(name = "audioQuestionAsset")
    val audioQuestionAsset: String? = null,

    @ColumnInfo(name = "audioAnswerAsset")
    val audioAnswerAsset: String? = null,

    @ColumnInfo(name = "version")
    val version: Int = 1,

    @ColumnInfo(name = "lastUpdated")
    val lastUpdated: Long = System.currentTimeMillis()
)
