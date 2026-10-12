package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.data.local.entity.VocabularyEntity
import com.example.data.local.entity.VocabularyProgressEntity
import com.example.data.local.entity.VocabularyWithProgress
import kotlinx.coroutines.flow.Flow

@Dao
interface VocabularyDao {

    @Transaction
    @Query("SELECT * FROM civics_vocabulary ORDER BY word ASC")
    fun getAllVocabularyWithProgress(): Flow<List<VocabularyWithProgress>>

    @Transaction
    @Query("SELECT * FROM civics_vocabulary WHERE id = :id")
    fun getVocabularyById(id: String): Flow<VocabularyWithProgress?>

    @Transaction
    @Query("SELECT * FROM civics_vocabulary WHERE id = :id")
    suspend fun getVocabularyByIdDirect(id: String): VocabularyWithProgress?

    @Query("SELECT COUNT(*) FROM civics_vocabulary")
    fun getVocabularyCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM civics_vocabulary")
    suspend fun getVocabularyCountDirect(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vocabularyList: List<VocabularyEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: VocabularyProgressEntity)

    @Query("SELECT * FROM vocabulary_progress WHERE vocabId = :vocabId")
    suspend fun getProgressDirect(vocabId: String): VocabularyProgressEntity?

    @Transaction
    suspend fun toggleFavorite(vocabId: String): Boolean {
        val existing = getProgressDirect(vocabId)
        val newFav = !(existing?.isFavorite ?: false)
        val updated = (existing ?: VocabularyProgressEntity(vocabId = vocabId)).copy(
            isFavorite = newFav
        )
        insertProgress(updated)
        return newFav
    }

    @Transaction
    suspend fun updateStatus(vocabId: String, status: String, reviewedAt: Long = System.currentTimeMillis()) {
        val existing = getProgressDirect(vocabId)
        val updated = (existing ?: VocabularyProgressEntity(vocabId = vocabId)).copy(
            status = status,
            lastReviewedAt = reviewedAt
        )
        insertProgress(updated)
    }

    /**
     * Resets learning and review status for vocabulary while preserving favorites.
     */
    @Query("UPDATE vocabulary_progress SET status = 'NOT_STARTED', lastReviewedAt = 0")
    suspend fun resetVocabularyLearningProgress()

    @Query("DELETE FROM civics_vocabulary")
    suspend fun clearAll()
}
