package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * PracticeResultEntity stores completed practice test results locally.
 *
 * Provides offline historical test performance without requiring external backends.
 */
@Entity(
    tableName = "practice_results",
    indices = [
        Index(value = ["completedAt"]),
        Index(value = ["practiceType"])
    ]
)
data class PracticeResultEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "practiceType")
    val practiceType: String,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "totalQuestions")
    val totalQuestions: Int,

    @ColumnInfo(name = "correctAnswers")
    val correctAnswers: Int,

    @ColumnInfo(name = "incorrectAnswers")
    val incorrectAnswers: Int,

    @ColumnInfo(name = "skippedAnswers")
    val skippedAnswers: Int = 0,

    @ColumnInfo(name = "percentage")
    val percentage: Int,

    @ColumnInfo(name = "passed")
    val passed: Boolean,

    @ColumnInfo(name = "startedAt")
    val startedAt: Long,

    @ColumnInfo(name = "completedAt")
    val completedAt: Long,

    /**
     * Serialized JSON containing list of question results for review.
     */
    @ColumnInfo(name = "questionResultsJson")
    val questionResultsJson: String
)
