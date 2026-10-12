package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.StudyActivityEntity
import kotlinx.coroutines.flow.Flow

/**
 * StudyActivityDao provides access to study actions for streaks, daily goals, and recent activity.
 */
@Dao
interface StudyActivityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: StudyActivityEntity): Long

    @Query("SELECT DISTINCT date FROM study_activities ORDER BY date DESC")
    fun getAllActivityDates(): Flow<List<String>>

    @Query("SELECT DISTINCT date FROM study_activities ORDER BY date DESC")
    suspend fun getAllActivityDatesDirect(): List<String>

    @Query("SELECT * FROM study_activities ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentActivities(limit: Int = 10): Flow<List<StudyActivityEntity>>

    @Query("SELECT COUNT(*) FROM study_activities WHERE date = :date")
    fun getActivityCountForDate(date: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM study_activities WHERE date = :date")
    suspend fun getActivityCountForDateDirect(date: String): Int

    @Query("SELECT COUNT(DISTINCT date) FROM study_activities")
    fun getTotalActiveDaysCount(): Flow<Int>

    @Query("DELETE FROM study_activities")
    suspend fun clearActivities()
}
