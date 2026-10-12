package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.data.local.entity.QuestionProgressEntity
import kotlinx.coroutines.flow.Flow

/**
 * QuestionProgressDao manages user study progress, favorites, and history.
 */
@Dao
interface QuestionProgressDao {

    @Query("SELECT * FROM question_progress")
    fun getAllProgress(): Flow<List<QuestionProgressEntity>>

    @Query("SELECT * FROM question_progress WHERE questionId = :questionId LIMIT 1")
    fun getProgressForQuestion(questionId: String): Flow<QuestionProgressEntity?>

    @Query("SELECT * FROM question_progress WHERE questionId = :questionId LIMIT 1")
    suspend fun getProgressForQuestionDirect(questionId: String): QuestionProgressEntity?

    @Query("SELECT COUNT(*) FROM question_progress WHERE isStudied = 1")
    fun getStudiedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM question_progress WHERE isStudied = 1")
    suspend fun getStudiedCountDirect(): Int

    @Query("SELECT COUNT(*) FROM question_progress WHERE isFavorite = 1")
    fun getFavoriteCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM question_progress WHERE isFavorite = 1")
    suspend fun getFavoriteCountDirect(): Int

    @Query("SELECT COUNT(*) FROM question_progress WHERE needsPractice = 1")
    fun getNeedsPracticeCount(): Flow<Int>

    @Query("SELECT questionId FROM question_progress WHERE isFavorite = 1")
    fun getFavoriteQuestionIds(): Flow<List<String>>

    @Query("SELECT questionId FROM question_progress WHERE isStudied = 1")
    fun getStudiedQuestionIds(): Flow<List<String>>

    @Query("SELECT questionId FROM question_progress WHERE lastViewed > 0 ORDER BY lastViewed DESC LIMIT 1")
    fun getMostRecentViewedQuestionId(): Flow<String?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(progress: QuestionProgressEntity)

    @Transaction
    suspend fun toggleFavorite(questionId: String): Boolean {
        val existing = getProgressForQuestionDirect(questionId)
        val newFav = !(existing?.isFavorite ?: false)
        val updated = (existing ?: QuestionProgressEntity(questionId = questionId)).copy(
            isFavorite = newFav
        )
        insertOrUpdate(updated)
        return newFav
    }

    @Transaction
    suspend fun setFavorite(questionId: String, isFavorite: Boolean) {
        val existing = getProgressForQuestionDirect(questionId)
        val updated = (existing ?: QuestionProgressEntity(questionId = questionId)).copy(
            isFavorite = isFavorite
        )
        insertOrUpdate(updated)
    }

    @Transaction
    suspend fun setStudied(questionId: String, isStudied: Boolean) {
        val existing = getProgressForQuestionDirect(questionId)
        val updated = (existing ?: QuestionProgressEntity(questionId = questionId)).copy(
            isStudied = isStudied
        )
        insertOrUpdate(updated)
    }

    @Transaction
    suspend fun setNeedsPractice(questionId: String, needsPractice: Boolean) {
        val existing = getProgressForQuestionDirect(questionId)
        val updated = (existing ?: QuestionProgressEntity(questionId = questionId)).copy(
            needsPractice = needsPractice
        )
        insertOrUpdate(updated)
    }

    @Transaction
    suspend fun recordQuestionView(questionId: String, timestamp: Long = System.currentTimeMillis()) {
        val existing = getProgressForQuestionDirect(questionId)
        val updated = (existing ?: QuestionProgressEntity(questionId = questionId)).copy(
            lastViewed = timestamp,
            viewCount = (existing?.viewCount ?: 0) + 1,
            isStudied = true // Opening a question in detail marks it as viewed/studied
        )
        insertOrUpdate(updated)
    }

    @Transaction
    suspend fun recordEvaluation(
        questionId: String,
        isCorrect: Boolean,
        timestamp: Long = System.currentTimeMillis()
    ) {
        val existing = getProgressForQuestionDirect(questionId)
        val updated = (existing ?: QuestionProgressEntity(questionId = questionId)).copy(
            lastViewed = timestamp,
            viewCount = (existing?.viewCount ?: 0) + 1,
            isStudied = true,
            correctCount = (existing?.correctCount ?: 0) + if (isCorrect) 1 else 0,
            incorrectCount = (existing?.incorrectCount ?: 0) + if (isCorrect) 0 else 1,
            needsPractice = !isCorrect,
            isMastered = if (isCorrect && ((existing?.correctCount ?: 0) + 1) >= 2) true else existing?.isMastered ?: false,
            lastResult = if (isCorrect) "CORRECT" else "INCORRECT"
        )
        insertOrUpdate(updated)
    }

    @Query("UPDATE question_progress SET isStudied = 0, needsPractice = 0, isMastered = 0, lastViewed = 0, viewCount = 0, correctCount = 0, incorrectCount = 0, nextReview = 0, lastResult = NULL WHERE isFavorite = 1")
    suspend fun resetFavoritesLearningData()

    @Query("DELETE FROM question_progress WHERE isFavorite = 0")
    suspend fun deleteNonFavorites()

    @Transaction
    suspend fun resetLearningProgress() {
        resetFavoritesLearningData()
        deleteNonFavorites()
    }

    @Transaction
    suspend fun resetAllProgress() {
        resetLearningProgress()
    }
}
