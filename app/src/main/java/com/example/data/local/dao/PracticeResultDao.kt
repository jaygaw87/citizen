package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.PracticeResultEntity
import kotlinx.coroutines.flow.Flow

/**
 * PracticeResultDao manages storage and retrieval of completed test results.
 */
@Dao
interface PracticeResultDao {

    @Query("SELECT * FROM practice_results ORDER BY completedAt DESC")
    fun getAllResults(): Flow<List<PracticeResultEntity>>

    @Query("SELECT * FROM practice_results ORDER BY completedAt DESC LIMIT :limit")
    fun getRecentResults(limit: Int = 10): Flow<List<PracticeResultEntity>>

    @Query("SELECT * FROM practice_results ORDER BY completedAt DESC LIMIT 1")
    fun getLatestResult(): Flow<PracticeResultEntity?>

    @Query("SELECT * FROM practice_results WHERE id = :id LIMIT 1")
    fun getResultById(id: String): Flow<PracticeResultEntity?>

    @Query("SELECT * FROM practice_results WHERE id = :id LIMIT 1")
    suspend fun getResultByIdDirect(id: String): PracticeResultEntity?

    @Query("SELECT COUNT(*) FROM practice_results")
    fun getResultCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResult(result: PracticeResultEntity)

    @Query("DELETE FROM practice_results WHERE id = :id")
    suspend fun deleteResult(id: String)

    @Query("DELETE FROM practice_results")
    suspend fun clearHistory()
}
