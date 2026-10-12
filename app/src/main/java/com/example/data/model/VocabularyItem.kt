package com.example.data.model

import com.example.data.local.entity.VocabularyEntity
import com.example.data.local.entity.VocabularyProgressEntity
import com.example.data.local.entity.VocabularyWithProgress

enum class VocabularyStatus(val displayName: String) {
    NOT_STARTED("Not Started"),
    LEARNING("Learning"),
    LEARNED("Learned"),
    NEEDS_REVIEW("Needs Review");

    companion object {
        fun fromString(value: String?): VocabularyStatus {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
                ?: NOT_STARTED
        }
    }
}

/**
 * Domain representation of a civics vocabulary word with user study state.
 */
data class VocabularyItem(
    val entity: VocabularyEntity,
    val isFavorite: Boolean = false,
    val status: VocabularyStatus = VocabularyStatus.NOT_STARTED,
    val lastReviewedAt: Long = 0L
) {
    val id: String get() = entity.id
    val word: String get() = entity.word
    val pronunciation: String get() = entity.pronunciation
    val simpleDefinition: String get() = entity.simpleDefinition
    val detailedDefinition: String get() = entity.detailedDefinition
    val exampleSentence: String get() = entity.exampleSentence
    val category: String get() = entity.category
    val relatedQuestionIds: List<String> get() = entity.getRelatedQuestionIdList()

    companion object {
        fun fromRelation(relation: VocabularyWithProgress): VocabularyItem {
            val prog = relation.progress
            return VocabularyItem(
                entity = relation.vocabulary,
                isFavorite = prog?.isFavorite ?: false,
                status = VocabularyStatus.fromString(prog?.status),
                lastReviewedAt = prog?.lastReviewedAt ?: 0L
            )
        }

        fun fromEntities(entity: VocabularyEntity, progress: VocabularyProgressEntity?): VocabularyItem {
            return VocabularyItem(
                entity = entity,
                isFavorite = progress?.isFavorite ?: false,
                status = VocabularyStatus.fromString(progress?.status),
                lastReviewedAt = progress?.lastReviewedAt ?: 0L
            )
        }
    }
}
