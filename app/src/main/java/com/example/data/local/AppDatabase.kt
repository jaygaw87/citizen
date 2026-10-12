package com.example.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.PracticeResultDao
import com.example.data.local.dao.QuestionDao
import com.example.data.local.dao.QuestionProgressDao
import com.example.data.local.entity.AnswerEntity
import com.example.data.local.entity.PracticeResultEntity
import com.example.data.local.entity.QuestionEntity
import com.example.data.local.entity.QuestionProgressEntity

/**
 * AppDatabase is the primary Room database for the US Citizenship Test app.
 * Stores official 128 USCIS civics questions, accepted answers, user study progress, and test history.
 */
@Database(
    entities = [
        QuestionEntity::class,
        AnswerEntity::class,
        QuestionProgressEntity::class,
        PracticeResultEntity::class,
        com.example.data.local.entity.StudyActivityEntity::class,
        com.example.data.local.entity.VocabularyEntity::class,
        com.example.data.local.entity.VocabularyProgressEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun questionDao(): QuestionDao
    abstract fun questionProgressDao(): QuestionProgressDao
    abstract fun practiceResultDao(): PracticeResultDao
    abstract fun studyActivityDao(): com.example.data.local.dao.StudyActivityDao
    abstract fun vocabularyDao(): com.example.data.local.dao.VocabularyDao

    companion object {
        const val DATABASE_NAME = "us_citizenship_civics.db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Version 1 and 2 had identical schema
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `question_progress` (
                        `questionId` TEXT NOT NULL,
                        `isFavorite` INTEGER NOT NULL DEFAULT 0,
                        `isStudied` INTEGER NOT NULL DEFAULT 0,
                        `needsPractice` INTEGER NOT NULL DEFAULT 0,
                        `isMastered` INTEGER NOT NULL DEFAULT 0,
                        `lastViewed` INTEGER NOT NULL DEFAULT 0,
                        `viewCount` INTEGER NOT NULL DEFAULT 0,
                        `correctCount` INTEGER NOT NULL DEFAULT 0,
                        `incorrectCount` INTEGER NOT NULL DEFAULT 0,
                        `nextReview` INTEGER NOT NULL DEFAULT 0,
                        PRIMARY KEY(`questionId`),
                        FOREIGN KEY(`questionId`) REFERENCES `questions`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_question_progress_questionId` ON `question_progress` (`questionId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_question_progress_isFavorite` ON `question_progress` (`isFavorite`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_question_progress_isStudied` ON `question_progress` (`isStudied`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_question_progress_needsPractice` ON `question_progress` (`needsPractice`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_question_progress_isMastered` ON `question_progress` (`isMastered`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_question_progress_lastViewed` ON `question_progress` (`lastViewed`)")
            }
        }

        val MIGRATION_1_3 = object : Migration(1, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                MIGRATION_2_3.migrate(db)
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add lastResult column to question_progress
                db.execSQL("ALTER TABLE `question_progress` ADD COLUMN `lastResult` TEXT DEFAULT NULL")

                // Create practice_results table
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
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_practice_results_completedAt` ON `practice_results` (`completedAt`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_practice_results_practiceType` ON `practice_results` (`practiceType`)")
            }
        }

        val MIGRATION_2_4 = object : Migration(2, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                MIGRATION_2_3.migrate(db)
                MIGRATION_3_4.migrate(db)
            }
        }

        val MIGRATION_1_4 = object : Migration(1, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                MIGRATION_1_3.migrate(db)
                MIGRATION_3_4.migrate(db)
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `study_activities` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `date` TEXT NOT NULL,
                        `activityType` TEXT NOT NULL,
                        `details` TEXT NOT NULL DEFAULT '',
                        `createdAt` INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_study_activities_date` ON `study_activities` (`date`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_study_activities_createdAt` ON `study_activities` (`createdAt`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_study_activities_activityType` ON `study_activities` (`activityType`)")
            }
        }

        val MIGRATION_3_5 = object : Migration(3, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                MIGRATION_3_4.migrate(db)
                MIGRATION_4_5.migrate(db)
            }
        }

        val MIGRATION_2_5 = object : Migration(2, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                MIGRATION_2_4.migrate(db)
                MIGRATION_4_5.migrate(db)
            }
        }

        val MIGRATION_1_5 = object : Migration(1, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                MIGRATION_1_4.migrate(db)
                MIGRATION_4_5.migrate(db)
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `civics_vocabulary` (
                        `id` TEXT NOT NULL PRIMARY KEY,
                        `word` TEXT NOT NULL,
                        `pronunciation` TEXT NOT NULL,
                        `simpleDefinition` TEXT NOT NULL,
                        `detailedDefinition` TEXT NOT NULL,
                        `exampleSentence` TEXT NOT NULL,
                        `relatedQuestionIds` TEXT NOT NULL,
                        `category` TEXT NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_civics_vocabulary_word` ON `civics_vocabulary` (`word`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_civics_vocabulary_category` ON `civics_vocabulary` (`category`)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `vocabulary_progress` (
                        `vocabId` TEXT NOT NULL PRIMARY KEY,
                        `isFavorite` INTEGER NOT NULL DEFAULT 0,
                        `status` TEXT NOT NULL DEFAULT 'NOT_STARTED',
                        `lastReviewedAt` INTEGER NOT NULL DEFAULT 0,
                        FOREIGN KEY(`vocabId`) REFERENCES `civics_vocabulary`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_vocabulary_progress_isFavorite` ON `vocabulary_progress` (`isFavorite`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_vocabulary_progress_status` ON `vocabulary_progress` (`status`)")
            }
        }

        val MIGRATION_4_6 = object : Migration(4, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                MIGRATION_4_5.migrate(db)
                MIGRATION_5_6.migrate(db)
            }
        }

        val MIGRATION_3_6 = object : Migration(3, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                MIGRATION_3_5.migrate(db)
                MIGRATION_5_6.migrate(db)
            }
        }

        val MIGRATION_2_6 = object : Migration(2, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                MIGRATION_2_5.migrate(db)
                MIGRATION_5_6.migrate(db)
            }
        }

        val MIGRATION_1_6 = object : Migration(1, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                MIGRATION_1_5.migrate(db)
                MIGRATION_5_6.migrate(db)
            }
        }
    }
}
