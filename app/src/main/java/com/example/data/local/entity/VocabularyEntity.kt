package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * VocabularyEntity represents a curated U.S. civics vocabulary term.
 *
 * Educational supplement to assist applicants in understanding terms used in the USCIS Civics Test.
 * Content is curated for educational clarity and clearly identified as an educational aid.
 */
@Entity(
    tableName = "civics_vocabulary",
    indices = [
        Index(value = ["word"]),
        Index(value = ["category"])
    ]
)
data class VocabularyEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "word")
    val word: String,

    @ColumnInfo(name = "pronunciation")
    val pronunciation: String,

    @ColumnInfo(name = "simpleDefinition")
    val simpleDefinition: String,

    @ColumnInfo(name = "detailedDefinition")
    val detailedDefinition: String,

    @ColumnInfo(name = "exampleSentence")
    val exampleSentence: String,

    /**
     * Comma-separated list of related question IDs (e.g., "Q001,Q002,Q004").
     */
    @ColumnInfo(name = "relatedQuestionIds")
    val relatedQuestionIds: String,

    @ColumnInfo(name = "category")
    val category: String
) {
    fun getRelatedQuestionIdList(): List<String> {
        return if (relatedQuestionIds.isBlank()) {
            emptyList()
        } else {
            relatedQuestionIds.split(",")
                .map { it.trim().removeSurrounding("\"").removeSurrounding("[").removeSurrounding("]") }
                .filter { it.isNotBlank() }
        }
    }
}
