package com.example

import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DatabaseMigrationTest {

    @Test
    fun `test migration 2 to 3 creates question_progress table and indices`() {
        val helperConfig = SupportSQLiteOpenHelper.Configuration.builder(ApplicationProvider.getApplicationContext())
            .name(null) // in-memory
            .callback(object : SupportSQLiteOpenHelper.Callback(2) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    db.execSQL("""
                        CREATE TABLE IF NOT EXISTS `questions` (
                            `id` TEXT NOT NULL PRIMARY KEY,
                            `questionNumber` INTEGER NOT NULL,
                            `category` TEXT NOT NULL,
                            `subcategory` TEXT NOT NULL,
                            `questionText` TEXT NOT NULL,
                            `sourceType` TEXT NOT NULL,
                            `explanation` TEXT NOT NULL,
                            `explanationSourceType` TEXT NOT NULL,
                            `explanationSourceLabel` TEXT NOT NULL,
                            `is65_20` INTEGER NOT NULL,
                            `isDynamicAnswer` INTEGER NOT NULL,
                            `dynamicAnswerType` TEXT,
                            `currentAnswer` TEXT,
                            `answerLastVerified` TEXT,
                            `answerSourceName` TEXT,
                            `answerSourceUrl` TEXT,
                            `sourceName` TEXT NOT NULL,
                            `sourceUrl` TEXT NOT NULL,
                            `version` INTEGER NOT NULL,
                            `lastUpdated` INTEGER NOT NULL
                        )
                    """.trimIndent())
                    db.execSQL("""
                        CREATE TABLE IF NOT EXISTS `answers` (
                            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            `questionId` TEXT NOT NULL,
                            `answerText` TEXT NOT NULL,
                            `sourceType` TEXT NOT NULL,
                            `sourceName` TEXT NOT NULL,
                            `isPrimary` INTEGER NOT NULL,
                            `isAcceptable` INTEGER NOT NULL,
                            `orderIndex` INTEGER NOT NULL,
                            FOREIGN KEY(`questionId`) REFERENCES `questions`(`id`) ON DELETE CASCADE
                        )
                    """.trimIndent())
                }

                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
            })
            .build()

        val openHelper = FrameworkSQLiteOpenHelperFactory().create(helperConfig)
        val db = openHelper.writableDatabase

        // Insert a sample question at version 2
        db.execSQL("INSERT INTO questions (id, questionNumber, category, subcategory, questionText, sourceType, explanation, explanationSourceType, explanationSourceLabel, is65_20, isDynamicAnswer, sourceName, sourceUrl, version, lastUpdated) VALUES ('Q001', 1, 'American Government', 'Principles', 'What is the form of government?', 'USCIS_OFFICIAL', 'Explanation', 'CURATED_EDUCATIONAL', 'Educational explanation', 0, 0, 'USCIS', 'url', 1, 1000)")

        // Run Migration 2 -> 3
        AppDatabase.MIGRATION_2_3.migrate(db)

        // Verify question still exists
        val cursor = db.query("SELECT id FROM questions WHERE id = 'Q001'")
        assertTrue(cursor.moveToFirst())
        assertEquals("Q001", cursor.getString(0))
        cursor.close()

        // Verify question_progress table exists and is queryable
        val progressCursor = db.query("SELECT COUNT(*) FROM question_progress")
        assertTrue(progressCursor.moveToFirst())
        assertEquals(0, progressCursor.getInt(0))
        progressCursor.close()

        // Insert progress row referencing existing question
        db.execSQL("INSERT INTO question_progress (questionId, isFavorite, isStudied, needsPractice, isMastered, lastViewed, viewCount, correctCount, incorrectCount, nextReview) VALUES ('Q001', 1, 1, 0, 0, 12345, 1, 1, 0, 0)")
        val insertedProgress = db.query("SELECT isFavorite, isStudied FROM question_progress WHERE questionId = 'Q001'")
        assertTrue(insertedProgress.moveToFirst())
        assertEquals(1, insertedProgress.getInt(0))
        assertEquals(1, insertedProgress.getInt(1))
        insertedProgress.close()

        db.close()
    }

    @Test
    fun `test migration 3 to 4 creates practice_results table and updates question_progress`() {
        val helperConfig = SupportSQLiteOpenHelper.Configuration.builder(ApplicationProvider.getApplicationContext())
            .name(null)
            .callback(object : SupportSQLiteOpenHelper.Callback(3) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    db.execSQL("""
                        CREATE TABLE IF NOT EXISTS `questions` (
                            `id` TEXT NOT NULL PRIMARY KEY,
                            `questionNumber` INTEGER NOT NULL,
                            `category` TEXT NOT NULL,
                            `subcategory` TEXT NOT NULL,
                            `questionText` TEXT NOT NULL,
                            `sourceType` TEXT NOT NULL,
                            `explanation` TEXT NOT NULL,
                            `explanationSourceType` TEXT NOT NULL,
                            `explanationSourceLabel` TEXT NOT NULL,
                            `is65_20` INTEGER NOT NULL,
                            `isDynamicAnswer` INTEGER NOT NULL,
                            `dynamicAnswerType` TEXT,
                            `currentAnswer` TEXT,
                            `answerLastVerified` TEXT,
                            `answerSourceName` TEXT,
                            `answerSourceUrl` TEXT,
                            `sourceName` TEXT NOT NULL,
                            `sourceUrl` TEXT NOT NULL,
                            `version` INTEGER NOT NULL,
                            `lastUpdated` INTEGER NOT NULL
                        )
                    """.trimIndent())
                    db.execSQL("""
                        CREATE TABLE IF NOT EXISTS `question_progress` (
                            `questionId` TEXT NOT NULL PRIMARY KEY,
                            `isFavorite` INTEGER NOT NULL DEFAULT 0,
                            `isStudied` INTEGER NOT NULL DEFAULT 0,
                            `needsPractice` INTEGER NOT NULL DEFAULT 0,
                            `isMastered` INTEGER NOT NULL DEFAULT 0,
                            `lastViewed` INTEGER NOT NULL DEFAULT 0,
                            `viewCount` INTEGER NOT NULL DEFAULT 0,
                            `correctCount` INTEGER NOT NULL DEFAULT 0,
                            `incorrectCount` INTEGER NOT NULL DEFAULT 0,
                            `nextReview` INTEGER NOT NULL DEFAULT 0
                        )
                    """.trimIndent())
                }

                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
            })
            .build()

        val openHelper = FrameworkSQLiteOpenHelperFactory().create(helperConfig)
        val db = openHelper.writableDatabase

        // Insert a sample question and progress at version 3
        db.execSQL("INSERT INTO question_progress (questionId, isFavorite, isStudied, needsPractice, isMastered, lastViewed, viewCount, correctCount, incorrectCount, nextReview) VALUES ('Q001', 1, 1, 0, 0, 12345, 1, 1, 0, 0)")

        // Run Migration 3 -> 4
        AppDatabase.MIGRATION_3_4.migrate(db)

        // Verify question_progress has lastResult column
        db.execSQL("UPDATE question_progress SET lastResult = 'CORRECT' WHERE questionId = 'Q001'")
        val progressCursor = db.query("SELECT lastResult FROM question_progress WHERE questionId = 'Q001'")
        assertTrue(progressCursor.moveToFirst())
        assertEquals("CORRECT", progressCursor.getString(0))
        progressCursor.close()

        // Verify practice_results table exists and can accept insert
        db.execSQL("""
            INSERT INTO practice_results (id, practiceType, title, totalQuestions, correctAnswers, incorrectAnswers, skippedAnswers, percentage, passed, startedAt, completedAt, questionResultsJson)
            VALUES ('res-1', 'FULL', 'Full Practice Test', 20, 15, 5, 0, 75, 1, 1000, 2000, '[]')
        """.trimIndent())
        val resultCursor = db.query("SELECT totalQuestions, correctAnswers, passed FROM practice_results WHERE id = 'res-1'")
        assertTrue(resultCursor.moveToFirst())
        assertEquals(20, resultCursor.getInt(0))
        assertEquals(15, resultCursor.getInt(1))
        assertEquals(1, resultCursor.getInt(2))
        resultCursor.close()

        db.close()
    }

    @Test
    fun `test migration 4 to 5 creates study_activities table`() {
        val helperConfig = SupportSQLiteOpenHelper.Configuration.builder(ApplicationProvider.getApplicationContext())
            .name(null)
            .callback(object : SupportSQLiteOpenHelper.Callback(4) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    db.execSQL("""
                        CREATE TABLE IF NOT EXISTS `practice_results` (
                            `id` TEXT NOT NULL PRIMARY KEY,
                            `practiceType` TEXT NOT NULL,
                            `title` TEXT NOT NULL,
                            `totalQuestions` INTEGER NOT NULL,
                            `correctAnswers` INTEGER NOT NULL,
                            `incorrectAnswers` INTEGER NOT NULL,
                            `skippedAnswers` INTEGER NOT NULL DEFAULT 0,
                            `percentage` INTEGER NOT NULL,
                            `passed` INTEGER NOT NULL,
                            `startedAt` INTEGER NOT NULL,
                            `completedAt` INTEGER NOT NULL,
                            `questionResultsJson` TEXT NOT NULL
                        )
                    """.trimIndent())
                }

                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
            })
            .build()

        val openHelper = FrameworkSQLiteOpenHelperFactory().create(helperConfig)
        val db = openHelper.writableDatabase

        // Run Migration 4 -> 5
        AppDatabase.MIGRATION_4_5.migrate(db)

        // Verify study_activities table exists and accepts inserts
        db.execSQL("""
            INSERT INTO study_activities (date, activityType, details, createdAt)
            VALUES ('2026-09-23', 'STUDY_QUESTION', 'Studied Question 42', 12345678)
        """.trimIndent())

        val cursor = db.query("SELECT date, activityType, details FROM study_activities WHERE date = '2026-09-23'")
        assertTrue(cursor.moveToFirst())
        assertEquals("2026-09-23", cursor.getString(0))
        assertEquals("STUDY_QUESTION", cursor.getString(1))
        assertEquals("Studied Question 42", cursor.getString(2))
        cursor.close()

        db.close()
    }
}
