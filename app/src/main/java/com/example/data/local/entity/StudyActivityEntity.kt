package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * StudyActivityEntity records meaningful user study actions locally:
 * - Studied a question
 * - Completed flashcards
 * - Completed practice test
 * - Completed speaking practice
 *
 * Used to calculate genuine study streaks, daily study goals, and the recent activity feed.
 */
@Entity(
    tableName = "study_activities",
    indices = [
        Index(value = ["date"]),
        Index(value = ["createdAt"]),
        Index(value = ["activityType"])
    ]
)
data class StudyActivityEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,

    /**
     * Date formatted as "YYYY-MM-DD" for daily streak grouping.
     */
    @ColumnInfo(name = "date")
    val date: String,

    /**
     * Type of activity: STUDY_QUESTION, FLASHCARD, PRACTICE_TEST, SPEAKING_PRACTICE
     */
    @ColumnInfo(name = "activityType")
    val activityType: String,

    /**
     * Human-readable detail (e.g. "Studied Question 42", "Full Practice Test — 16/20 (Passed)")
     */
    @ColumnInfo(name = "details")
    val details: String = "",

    /**
     * Timestamp of the activity.
     */
    @ColumnInfo(name = "createdAt")
    val createdAt: Long = System.currentTimeMillis()
)
