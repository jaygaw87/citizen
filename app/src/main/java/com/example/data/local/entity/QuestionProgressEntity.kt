package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * QuestionProgressEntity tracks local user study progress, favorites, and mastery state
 * for each civics question.
 *
 * Stored locally in SQLite via Room, fully offline.
 */
@Entity(
    tableName = "question_progress",
    foreignKeys = [
        ForeignKey(
            entity = QuestionEntity::class,
            parentColumns = ["id"],
            childColumns = ["questionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["questionId"], unique = true),
        Index(value = ["isFavorite"]),
        Index(value = ["isStudied"]),
        Index(value = ["needsPractice"]),
        Index(value = ["isMastered"]),
        Index(value = ["lastViewed"])
    ]
)
data class QuestionProgressEntity(
    @PrimaryKey
    @ColumnInfo(name = "questionId")
    val questionId: String,

    @ColumnInfo(name = "isFavorite")
    val isFavorite: Boolean = false,

    @ColumnInfo(name = "isStudied")
    val isStudied: Boolean = false,

    @ColumnInfo(name = "needsPractice")
    val needsPractice: Boolean = false,

    @ColumnInfo(name = "isMastered")
    val isMastered: Boolean = false,

    @ColumnInfo(name = "lastViewed")
    val lastViewed: Long = 0L,

    @ColumnInfo(name = "viewCount")
    val viewCount: Int = 0,

    @ColumnInfo(name = "correctCount")
    val correctCount: Int = 0,

    @ColumnInfo(name = "incorrectCount")
    val incorrectCount: Int = 0,

    @ColumnInfo(name = "nextReview")
    val nextReview: Long = 0L,

    @ColumnInfo(name = "lastResult")
    val lastResult: String? = null
)
