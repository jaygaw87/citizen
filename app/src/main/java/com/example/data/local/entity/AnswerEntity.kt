package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * AnswerEntity represents an official acceptable answer for a USCIS civics question.
 *
 * A question may have one correct answer, multiple acceptable answers, or alternatives.
 * Normalized design with a Foreign Key to [QuestionEntity.id] provides clean relational integrity.
 */
@Entity(
    tableName = "answers",
    foreignKeys = [
        ForeignKey(
            entity = QuestionEntity::class,
            parentColumns = ["id"],
            childColumns = ["questionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["questionId"])
    ]
)
data class AnswerEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "questionId")
    val questionId: String,

    @ColumnInfo(name = "answerText")
    val answerText: String,

    /**
     * Source authority: Always "USCIS_OFFICIAL" for accepted answers.
     */
    @ColumnInfo(name = "sourceType")
    val sourceType: String = QuestionSourceTypes.USCIS_OFFICIAL,

    @ColumnInfo(name = "sourceName")
    val sourceName: String = "USCIS 2025 Civics Test (Form M-1778)",

    @ColumnInfo(name = "isPrimary")
    val isPrimary: Boolean = false,

    @ColumnInfo(name = "isAcceptable")
    val isAcceptable: Boolean = true,

    @ColumnInfo(name = "orderIndex")
    val orderIndex: Int = 0
)
