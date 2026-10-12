package com.example.data.local

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.repository.DefaultQuestionRepository
import com.example.data.repository.QuestionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * DatabaseProvider supplies thread-safe singletons for [AppDatabase] and [QuestionRepository].
 */
object DatabaseProvider {
    private const val TAG = "CivicsDiagnostic"

    @Volatile
    private var databaseInstance: AppDatabase? = null

    @Volatile
    private var repositoryInstance: QuestionRepository? = null

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun getDatabase(context: Context): AppDatabase {
        return databaseInstance ?: synchronized(this) {
            databaseInstance ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                AppDatabase.DATABASE_NAME
            )
                .addMigrations(
                    AppDatabase.MIGRATION_1_2,
                    AppDatabase.MIGRATION_2_3,
                    AppDatabase.MIGRATION_1_3,
                    AppDatabase.MIGRATION_3_4,
                    AppDatabase.MIGRATION_2_4,
                    AppDatabase.MIGRATION_1_4,
                    AppDatabase.MIGRATION_4_5,
                    AppDatabase.MIGRATION_3_5,
                    AppDatabase.MIGRATION_2_5,
                    AppDatabase.MIGRATION_1_5,
                    AppDatabase.MIGRATION_5_6,
                    AppDatabase.MIGRATION_4_6,
                    AppDatabase.MIGRATION_3_6,
                    AppDatabase.MIGRATION_2_6,
                    AppDatabase.MIGRATION_1_6
                )
                .fallbackToDestructiveMigration(dropAllTables = true)
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        Log.i(TAG, "[DB] RoomDatabase onCreate callback triggered")
                    }

                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        Log.i(TAG, "[DB] RoomDatabase onOpen callback triggered")
                    }
                })
                .build()
                .also { databaseInstance = it }
        }
    }

    fun getQuestionRepository(context: Context): QuestionRepository {
        return repositoryInstance ?: synchronized(this) {
            repositoryInstance ?: run {
                val db = getDatabase(context)
                val dao = db.questionDao()
                val progressDao = db.questionProgressDao()
                val practiceResultDao = db.practiceResultDao()
                val studyActivityDao = db.studyActivityDao()
                val vocabularyDao = db.vocabularyDao()
                val initializer = DatabaseInitializer(context.applicationContext, dao, vocabularyDao)
                DefaultQuestionRepository(dao, progressDao, practiceResultDao, studyActivityDao, initializer, vocabularyDao).also { repo ->
                    repositoryInstance = repo
                    // Kick off background initialization eagerly so data is ready immediately
                    applicationScope.launch {
                        try {
                            repo.ensureInitialized()
                        } catch (e: Exception) {
                            Log.e(TAG, "[DB] Eager initialization in DatabaseProvider failed", e)
                        }
                    }
                }
            }
        }
    }
}
