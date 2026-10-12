package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * VocabularyProgressEntity tracks user learning status, favorites, and review timestamps
 * for civics vocabulary terms independently from question progress.
 */
@Entity(
    tableName = "vocabulary_progress",
    foreignKeys = [
        ForeignKey(
            entity = VocabularyEntity::class,
            parentColumns = ["id"],
            childColumns = ["vocabId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["isFavorite"]),
        Index(value = ["status"])
    ]
)
data class VocabularyProgressEntity(
    @PrimaryKey
    @ColumnInfo(name = "vocabId")
    val vocabId: String,

    @ColumnInfo(name = "isFavorite")
    val isFavorite: Boolean = false,

    /**
     * Study status: "NOT_STARTED", "LEARNING", "LEARNED", "NEEDS_REVIEW".
     */
    @ColumnInfo(name = "status")
    val status: String = "NOT_STARTED",

    @ColumnInfo(name = "lastReviewedAt")
    val lastReviewedAt: Long = 0L
)
