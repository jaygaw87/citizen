package com.example.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Relation uniting a vocabulary definition with its optional user progress state.
 */
data class VocabularyWithProgress(
    @Embedded
    val vocabulary: VocabularyEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "vocabId"
    )
    val progress: VocabularyProgressEntity? = null
)
