package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.data.local.entity.AnswerEntity
import com.example.data.local.entity.QuestionEntity
import com.example.data.local.entity.QuestionWithAnswers
import kotlinx.coroutines.flow.Flow

/**
 * QuestionDao provides full local access to the 128 USCIS civics questions and answers.
 */
@Dao
interface QuestionDao {

    @Transaction
    @Query("SELECT * FROM questions ORDER BY questionNumber ASC")
    fun getAllQuestionsWithAnswers(): Flow<List<QuestionWithAnswers>>

    @Query("SELECT * FROM questions ORDER BY questionNumber ASC")
    fun getAllQuestions(): Flow<List<QuestionEntity>>

    @Transaction
    @Query("SELECT * FROM questions WHERE id = :id LIMIT 1")
    fun getQuestionWithAnswersById(id: String): Flow<QuestionWithAnswers?>

    @Transaction
    @Query("SELECT * FROM questions WHERE id = :id LIMIT 1")
    suspend fun getQuestionWithAnswersByIdDirect(id: String): QuestionWithAnswers?

    @Transaction
    @Query("SELECT * FROM questions WHERE questionNumber = :number LIMIT 1")
    fun getQuestionWithAnswersByNumber(number: Int): Flow<QuestionWithAnswers?>

    @Transaction
    @Query("SELECT * FROM questions WHERE category = :category ORDER BY questionNumber ASC")
    fun getQuestionsByCategory(category: String): Flow<List<QuestionWithAnswers>>

    @Transaction
    @Query("SELECT * FROM questions WHERE subcategory = :subcategory ORDER BY questionNumber ASC")
    fun getQuestionsBySubcategory(subcategory: String): Flow<List<QuestionWithAnswers>>

    @Transaction
    @Query("SELECT * FROM questions WHERE is65_20 = 1 ORDER BY questionNumber ASC")
    fun get65_20Questions(): Flow<List<QuestionWithAnswers>>

    @Transaction
    @Query("SELECT * FROM questions WHERE isDynamicAnswer = 1 ORDER BY questionNumber ASC")
    fun getDynamicQuestions(): Flow<List<QuestionWithAnswers>>

    @Transaction
    @Query("SELECT * FROM questions WHERE dynamicAnswerType = :type ORDER BY questionNumber ASC")
    fun getQuestionsByDynamicType(type: String): Flow<List<QuestionWithAnswers>>

    @Transaction
    @Query(
        """
        SELECT * FROM questions 
        WHERE questionText LIKE '%' || :query || '%' 
           OR category LIKE '%' || :query || '%' 
           OR subcategory LIKE '%' || :query || '%' 
           OR explanation LIKE '%' || :query || '%'
        ORDER BY questionNumber ASC
        """
    )
    fun searchQuestions(query: String): Flow<List<QuestionWithAnswers>>

    @Query("SELECT COUNT(*) FROM questions")
    fun getQuestionCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM questions")
    suspend fun getQuestionCountDirect(): Int

    @Query("SELECT COUNT(*) FROM questions WHERE is65_20 = 1")
    fun get65_20Count(): Flow<Int>

    @Query("SELECT COUNT(*) FROM questions WHERE is65_20 = 1")
    suspend fun get65_20CountDirect(): Int

    @Query("SELECT COUNT(*) FROM questions WHERE isDynamicAnswer = 1")
    fun getDynamicCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM questions WHERE isDynamicAnswer = 1")
    suspend fun getDynamicCountDirect(): Int

    @Query("SELECT COUNT(*) FROM questions WHERE category = :category")
    suspend fun getCategoryCountDirect(category: String): Int

    @Query("SELECT DISTINCT category FROM questions ORDER BY category ASC")
    fun getAllCategories(): Flow<List<String>>

    @Query("SELECT DISTINCT category FROM questions ORDER BY category ASC")
    suspend fun getAllCategoriesDirect(): List<String>

    @Query("SELECT DISTINCT subcategory FROM questions WHERE category = :category ORDER BY subcategory ASC")
    fun getSubcategoriesByCategory(category: String): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<QuestionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnswers(answers: List<AnswerEntity>)

    @Transaction
    suspend fun insertQuestionsAndAnswers(
        questions: List<QuestionEntity>,
        answers: List<AnswerEntity>
    ) {
        insertQuestions(questions)
        insertAnswers(answers)
    }

    @Query("DELETE FROM answers")
    suspend fun deleteAllAnswers()

    @Query("DELETE FROM questions")
    suspend fun deleteAllQuestions()
}
