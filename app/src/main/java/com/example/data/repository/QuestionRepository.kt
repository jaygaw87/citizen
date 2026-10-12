package com.example.data.repository

import android.util.Log
import com.example.data.learning.StudyStreakCalculator
import com.example.data.learning.WeaknessCalculator
import com.example.data.local.DatabaseInitializer
import com.example.data.local.dao.PracticeResultDao
import com.example.data.local.dao.QuestionDao
import com.example.data.local.dao.QuestionProgressDao
import com.example.data.local.dao.StudyActivityDao
import com.example.data.local.dao.VocabularyDao
import com.example.data.local.entity.PracticeResultEntity
import com.example.data.local.entity.QuestionWithAnswers
import com.example.data.local.entity.StudyActivityEntity
import com.example.data.model.PracticeType
import com.example.data.model.StudyQuestionItem
import com.example.data.model.VocabularyItem
import com.example.data.model.VocabularyStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/**
 * Repository interface defining operations on USCIS civics questions, accepted answers,
 * user study progress, practice session history, and study activities.
 */
interface QuestionRepository {
    fun getAllQuestionsWithAnswers(): Flow<List<QuestionWithAnswers>>
    fun getQuestionWithAnswersById(id: String): Flow<QuestionWithAnswers?>
    suspend fun getQuestionWithAnswersByIdDirect(id: String): QuestionWithAnswers?
    fun getQuestionWithAnswersByNumber(number: Int): Flow<QuestionWithAnswers?>
    fun getQuestionsByCategory(category: String): Flow<List<QuestionWithAnswers>>
    fun getQuestionsBySubcategory(subcategory: String): Flow<List<QuestionWithAnswers>>
    fun get65_20Questions(): Flow<List<QuestionWithAnswers>>
    fun getDynamicQuestions(): Flow<List<QuestionWithAnswers>>
    fun getQuestionsByDynamicType(type: String): Flow<List<QuestionWithAnswers>>
    fun searchQuestions(query: String): Flow<List<QuestionWithAnswers>>
    fun getQuestionCount(): Flow<Int>
    suspend fun getQuestionCountDirect(): Int
    fun get65_20Count(): Flow<Int>
    fun getDynamicCount(): Flow<Int>
    fun getAllCategories(): Flow<List<String>>
    fun getSubcategoriesByCategory(category: String): Flow<List<String>>
    suspend fun ensureInitialized(): DatabaseInitializer.InitializationResult

    // Study & Progress Methods
    fun getAllStudyQuestions(): Flow<List<StudyQuestionItem>>
    fun getStudyQuestionById(id: String): Flow<StudyQuestionItem?>
    suspend fun getStudyQuestionByIdDirect(id: String): StudyQuestionItem?
    suspend fun toggleFavorite(questionId: String): Boolean
    suspend fun setFavorite(questionId: String, isFavorite: Boolean)
    suspend fun setStudied(questionId: String, isStudied: Boolean)
    suspend fun recordQuestionView(questionId: String)
    suspend fun recordQuestionEvaluation(questionId: String, isCorrect: Boolean)
    fun getStudiedCount(): Flow<Int>
    fun getFavoriteCount(): Flow<Int>
    fun getNeedsPracticeCount(): Flow<Int>
    fun getMostRecentViewedQuestionId(): Flow<String?>
    suspend fun resetLearningProgress()
    suspend fun resetAllProgress()

    // Study Activity & Streak Methods
    fun getAllActivityDates(): Flow<List<String>>
    fun getRecentActivities(limit: Int = 10): Flow<List<StudyActivityEntity>>
    fun getTodayActivityCount(): Flow<Int>
    suspend fun recordStudyActivity(activityType: String, details: String)
    suspend fun clearStudyActivities()

    // Practice Selection & Test Engine
    suspend fun selectQuestionsForPractice(
        type: PracticeType,
        topic: String? = null,
        requestedCount: Int = type.defaultCount
    ): List<StudyQuestionItem>

    // Practice Results & Test History
    fun getAllPracticeResults(): Flow<List<PracticeResultEntity>>
    fun getRecentPracticeResults(limit: Int = 10): Flow<List<PracticeResultEntity>>
    fun getLatestPracticeResult(): Flow<PracticeResultEntity?>
    fun getPracticeResultById(id: String): Flow<PracticeResultEntity?>
    suspend fun getPracticeResultByIdDirect(id: String): PracticeResultEntity?
    fun getPracticeResultCount(): Flow<Int>
    suspend fun insertPracticeResult(result: PracticeResultEntity)
    suspend fun deletePracticeResult(id: String)
    suspend fun clearPracticeHistory()

    // Civics Vocabulary Methods
    fun getAllVocabulary(): Flow<List<VocabularyItem>>
    fun getVocabularyById(id: String): Flow<VocabularyItem?>
    suspend fun getVocabularyByIdDirect(id: String): VocabularyItem?
    fun getVocabularyCount(): Flow<Int>
    suspend fun toggleVocabularyFavorite(vocabId: String): Boolean
    suspend fun updateVocabularyStatus(vocabId: String, status: VocabularyStatus)
}

/**
 * Default implementation of [QuestionRepository] backed by Room and [DatabaseInitializer].
 */
class DefaultQuestionRepository(
    private val questionDao: QuestionDao,
    private val questionProgressDao: QuestionProgressDao,
    private val practiceResultDao: PracticeResultDao,
    private val studyActivityDao: StudyActivityDao,
    private val databaseInitializer: DatabaseInitializer,
    private val vocabularyDao: VocabularyDao? = null
) : QuestionRepository {

    constructor(
        questionDao: QuestionDao,
        questionProgressDao: QuestionProgressDao,
        practiceResultDao: PracticeResultDao,
        studyActivityDao: StudyActivityDao,
        databaseInitializer: DatabaseInitializer
    ) : this(
        questionDao = questionDao,
        questionProgressDao = questionProgressDao,
        practiceResultDao = practiceResultDao,
        studyActivityDao = studyActivityDao,
        databaseInitializer = databaseInitializer,
        vocabularyDao = null
    )

    constructor(
        questionDao: QuestionDao,
        questionProgressDao: QuestionProgressDao,
        practiceResultDao: PracticeResultDao,
        databaseInitializer: DatabaseInitializer
    ) : this(
        questionDao = questionDao,
        questionProgressDao = questionProgressDao,
        practiceResultDao = practiceResultDao,
        studyActivityDao = object : StudyActivityDao {
            override fun getAllActivityDates() = kotlinx.coroutines.flow.flowOf(emptyList<String>())
            override suspend fun getAllActivityDatesDirect(): List<String> = emptyList()
            override fun getRecentActivities(limit: Int) = kotlinx.coroutines.flow.flowOf(emptyList<StudyActivityEntity>())
            override fun getActivityCountForDate(date: String) = kotlinx.coroutines.flow.flowOf(0)
            override suspend fun getActivityCountForDateDirect(date: String): Int = 0
            override fun getTotalActiveDaysCount() = kotlinx.coroutines.flow.flowOf(0)
            override suspend fun insertActivity(activity: StudyActivityEntity): Long = 0L
            override suspend fun clearActivities() {}
        },
        databaseInitializer = databaseInitializer,
        vocabularyDao = null
    )

    companion object {
        private const val TAG = "CivicsDiagnostic"
    }

    override fun getAllQuestionsWithAnswers(): Flow<List<QuestionWithAnswers>> = flow {
        ensureInitialized()
        emitAll(questionDao.getAllQuestionsWithAnswers())
    }

    override fun getQuestionWithAnswersById(id: String): Flow<QuestionWithAnswers?> = flow {
        ensureInitialized()
        emitAll(questionDao.getQuestionWithAnswersById(id))
    }

    override suspend fun getQuestionWithAnswersByIdDirect(id: String): QuestionWithAnswers? {
        ensureInitialized()
        return questionDao.getQuestionWithAnswersByIdDirect(id)
    }

    override fun getQuestionWithAnswersByNumber(number: Int): Flow<QuestionWithAnswers?> = flow {
        ensureInitialized()
        emitAll(questionDao.getQuestionWithAnswersByNumber(number))
    }

    override fun getQuestionsByCategory(category: String): Flow<List<QuestionWithAnswers>> = flow {
        ensureInitialized()
        emitAll(questionDao.getQuestionsByCategory(category))
    }

    override fun getQuestionsBySubcategory(subcategory: String): Flow<List<QuestionWithAnswers>> = flow {
        ensureInitialized()
        emitAll(questionDao.getQuestionsBySubcategory(subcategory))
    }

    override fun get65_20Questions(): Flow<List<QuestionWithAnswers>> = flow {
        ensureInitialized()
        emitAll(questionDao.get65_20Questions())
    }

    override fun getDynamicQuestions(): Flow<List<QuestionWithAnswers>> = flow {
        ensureInitialized()
        emitAll(questionDao.getDynamicQuestions())
    }

    override fun getQuestionsByDynamicType(type: String): Flow<List<QuestionWithAnswers>> = flow {
        ensureInitialized()
        emitAll(questionDao.getQuestionsByDynamicType(type))
    }

    override fun searchQuestions(query: String): Flow<List<QuestionWithAnswers>> = flow {
        ensureInitialized()
        emitAll(questionDao.searchQuestions(query))
    }

    override fun getQuestionCount(): Flow<Int> = flow {
        ensureInitialized()
        emitAll(questionDao.getQuestionCount())
    }

    override suspend fun getQuestionCountDirect(): Int {
        ensureInitialized()
        return questionDao.getQuestionCountDirect()
    }

    override fun get65_20Count(): Flow<Int> = flow {
        ensureInitialized()
        emitAll(questionDao.get65_20Count())
    }

    override fun getDynamicCount(): Flow<Int> = flow {
        ensureInitialized()
        emitAll(questionDao.getDynamicCount())
    }

    override fun getAllCategories(): Flow<List<String>> = flow {
        ensureInitialized()
        emitAll(questionDao.getAllCategories())
    }

    override fun getSubcategoriesByCategory(category: String): Flow<List<String>> = flow {
        ensureInitialized()
        emitAll(questionDao.getSubcategoriesByCategory(category))
    }

    override suspend fun ensureInitialized(): DatabaseInitializer.InitializationResult {
        return databaseInitializer.initializeIfNeeded()
    }

    override fun getAllStudyQuestions(): Flow<List<StudyQuestionItem>> = flow {
        ensureInitialized()
        val combinedFlow = combine(
            questionDao.getAllQuestionsWithAnswers(),
            questionProgressDao.getAllProgress()
        ) { questionsWithAnswers, progressList ->
            val progressMap = progressList.associateBy { it.questionId }
            val studyList = questionsWithAnswers.map { qwa ->
                val progress = progressMap[qwa.question.id]
                StudyQuestionItem.from(
                    qwa = qwa,
                    isFavorite = progress?.isFavorite ?: false,
                    isStudied = progress?.isStudied ?: false,
                    needsPractice = progress?.needsPractice ?: false,
                    isMastered = progress?.isMastered ?: false,
                    lastViewed = progress?.lastViewed ?: 0L,
                    viewCount = progress?.viewCount ?: 0,
                    correctCount = progress?.correctCount ?: 0,
                    incorrectCount = progress?.incorrectCount ?: 0,
                    lastResult = progress?.lastResult
                )
            }
            Log.i(TAG, "[REPOSITORY] questions = ${studyList.size}")
            studyList
        }
        emitAll(combinedFlow)
    }

    override fun getStudyQuestionById(id: String): Flow<StudyQuestionItem?> = flow {
        ensureInitialized()
        val flow = combine(
            questionDao.getQuestionWithAnswersById(id),
            questionProgressDao.getProgressForQuestion(id)
        ) { qwa, progress ->
            qwa?.let {
                StudyQuestionItem.from(
                    qwa = it,
                    isFavorite = progress?.isFavorite ?: false,
                    isStudied = progress?.isStudied ?: false,
                    needsPractice = progress?.needsPractice ?: false,
                    isMastered = progress?.isMastered ?: false,
                    lastViewed = progress?.lastViewed ?: 0L,
                    viewCount = progress?.viewCount ?: 0,
                    correctCount = progress?.correctCount ?: 0,
                    incorrectCount = progress?.incorrectCount ?: 0,
                    lastResult = progress?.lastResult
                )
            }
        }
        emitAll(flow)
    }

    override suspend fun getStudyQuestionByIdDirect(id: String): StudyQuestionItem? {
        ensureInitialized()
        val qwa = questionDao.getQuestionWithAnswersByIdDirect(id) ?: return null
        val progress = questionProgressDao.getProgressForQuestionDirect(id)

        return StudyQuestionItem.from(
            qwa = qwa,
            isFavorite = progress?.isFavorite ?: false,
            isStudied = progress?.isStudied ?: false,
            needsPractice = progress?.needsPractice ?: false,
            isMastered = progress?.isMastered ?: false,
            lastViewed = progress?.lastViewed ?: 0L,
            viewCount = progress?.viewCount ?: 0,
            correctCount = progress?.correctCount ?: 0,
            incorrectCount = progress?.incorrectCount ?: 0,
            lastResult = progress?.lastResult
        )
    }

    override suspend fun toggleFavorite(questionId: String): Boolean {
        ensureInitialized()
        return questionProgressDao.toggleFavorite(questionId)
    }

    override suspend fun setFavorite(questionId: String, isFavorite: Boolean) {
        ensureInitialized()
        questionProgressDao.setFavorite(questionId, isFavorite)
    }

    override suspend fun setStudied(questionId: String, isStudied: Boolean) {
        ensureInitialized()
        questionProgressDao.setStudied(questionId, isStudied)
        if (isStudied) {
            val qNum = questionId.removePrefix("Q").toIntOrNull() ?: 1
            recordStudyActivity("STUDY_QUESTION", "Studied Question $qNum")
        }
    }

    override suspend fun recordQuestionView(questionId: String) {
        ensureInitialized()
        questionProgressDao.recordQuestionView(questionId)
        val qNum = questionId.removePrefix("Q").toIntOrNull() ?: 1
        recordStudyActivity("STUDY_QUESTION", "Studied Question $qNum")
    }

    override suspend fun recordQuestionEvaluation(questionId: String, isCorrect: Boolean) {
        ensureInitialized()
        questionProgressDao.recordEvaluation(questionId, isCorrect)
        val qNum = questionId.removePrefix("Q").toIntOrNull() ?: 1
        val detail = if (isCorrect) "Answered Question $qNum correctly" else "Missed Question $qNum"
        recordStudyActivity("STUDY_QUESTION", detail)
    }

    override fun getStudiedCount(): Flow<Int> = flow {
        ensureInitialized()
        emitAll(questionProgressDao.getStudiedCount())
    }

    override fun getFavoriteCount(): Flow<Int> = flow {
        ensureInitialized()
        emitAll(questionProgressDao.getFavoriteCount())
    }

    override fun getNeedsPracticeCount(): Flow<Int> = flow {
        ensureInitialized()
        emitAll(questionProgressDao.getNeedsPracticeCount())
    }

    override fun getMostRecentViewedQuestionId(): Flow<String?> = flow {
        ensureInitialized()
        emitAll(questionProgressDao.getMostRecentViewedQuestionId())
    }

    override suspend fun resetLearningProgress() {
        questionProgressDao.resetLearningProgress()
        studyActivityDao.clearActivities()
        vocabularyDao?.resetVocabularyLearningProgress()
    }

    override suspend fun resetAllProgress() {
        resetLearningProgress()
    }

    // --- Study Activity & Streak Methods ---

    override fun getAllActivityDates(): Flow<List<String>> = flow {
        emitAll(studyActivityDao.getAllActivityDates())
    }

    override fun getRecentActivities(limit: Int): Flow<List<StudyActivityEntity>> = flow {
        emitAll(studyActivityDao.getRecentActivities(limit))
    }

    override fun getTodayActivityCount(): Flow<Int> = flow {
        val todayStr = StudyStreakCalculator.getTodayDateString()
        emitAll(studyActivityDao.getActivityCountForDate(todayStr))
    }

    override suspend fun recordStudyActivity(activityType: String, details: String) {
        try {
            val todayStr = StudyStreakCalculator.getTodayDateString()
            studyActivityDao.insertActivity(
                StudyActivityEntity(
                    date = todayStr,
                    activityType = activityType,
                    details = details,
                    createdAt = System.currentTimeMillis()
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "[StudyActivity] Failed to insert activity", e)
        }
    }

    override suspend fun clearStudyActivities() {
        studyActivityDao.clearActivities()
    }

    // --- Practice Selection Engine ---

    override suspend fun selectQuestionsForPractice(
        type: PracticeType,
        topic: String?,
        requestedCount: Int
    ): List<StudyQuestionItem> {
        ensureInitialized()
        val allQuestions = getAllStudyQuestions().first()

        val candidateQuestions: List<StudyQuestionItem> = when (type) {
            PracticeType.QUICK -> allQuestions
            PracticeType.FULL -> allQuestions
            PracticeType.RANDOM -> allQuestions
            PracticeType.TOPIC -> {
                if (!topic.isNullOrBlank()) {
                    allQuestions.filter { it.category.equals(topic, ignoreCase = true) }
                } else {
                    allQuestions
                }
            }
            PracticeType.FAVORITES -> allQuestions.filter { it.isFavorite }
            PracticeType.NEEDS_PRACTICE -> {
                val weakOnes = allQuestions.filter { it.needsPractice || WeaknessCalculator.isWeakQuestion(it) }
                if (weakOnes.isNotEmpty()) weakOnes else allQuestions
            }
            PracticeType.WEAK_AREAS -> {
                val rankedWeak = WeaknessCalculator.rankWeakQuestions(allQuestions)
                if (rankedWeak.isNotEmpty()) {
                    if (rankedWeak.size >= requestedCount) {
                        rankedWeak.take(requestedCount)
                    } else {
                        // Supplement with unstudied questions so the test has enough questions
                        val remaining = allQuestions.filterNot { rankedWeak.contains(it) }.shuffled()
                        rankedWeak + remaining.take(requestedCount - rankedWeak.size)
                    }
                } else {
                    allQuestions
                }
            }
            PracticeType.SENIOR_65_20 -> {
                val senior = allQuestions.filter { it.is65_20 }
                when (topic?.uppercase()) {
                    "NEEDS_PRACTICE" -> {
                        val weakSenior = senior.filter { it.needsPractice || WeaknessCalculator.isWeakQuestion(it) }
                        if (weakSenior.isNotEmpty()) weakSenior else senior
                    }
                    "FAVORITES" -> {
                        val favSenior = senior.filter { it.isFavorite }
                        if (favSenior.isNotEmpty()) favSenior else senior
                    }
                    else -> senior
                }
            }
            PracticeType.SPEAKING -> {
                if (!topic.isNullOrBlank()) {
                    if (topic.equals("SENIOR_65_20", ignoreCase = true) || topic.equals("65_20", ignoreCase = true)) {
                        allQuestions.filter { it.is65_20 }
                    } else {
                        allQuestions.filter { it.category.equals(topic, ignoreCase = true) }
                    }
                } else {
                    allQuestions
                }
            }
        }

        // Randomize order unless it was strictly ranked for WEAK_AREAS
        val finalCount = requestedCount.coerceAtLeast(1).coerceAtMost(candidateQuestions.size)
        return if (type == PracticeType.WEAK_AREAS) {
            candidateQuestions.take(finalCount)
        } else {
            candidateQuestions.shuffled().take(finalCount)
        }
    }

    // --- Practice Results & Test History ---

    override fun getAllPracticeResults(): Flow<List<PracticeResultEntity>> = flow {
        ensureInitialized()
        emitAll(practiceResultDao.getAllResults())
    }

    override fun getRecentPracticeResults(limit: Int): Flow<List<PracticeResultEntity>> = flow {
        ensureInitialized()
        emitAll(practiceResultDao.getRecentResults(limit))
    }

    override fun getLatestPracticeResult(): Flow<PracticeResultEntity?> = flow {
        ensureInitialized()
        emitAll(practiceResultDao.getLatestResult())
    }

    override fun getPracticeResultById(id: String): Flow<PracticeResultEntity?> = flow {
        ensureInitialized()
        emitAll(practiceResultDao.getResultById(id))
    }

    override suspend fun getPracticeResultByIdDirect(id: String): PracticeResultEntity? {
        ensureInitialized()
        return practiceResultDao.getResultByIdDirect(id)
    }

    override fun getPracticeResultCount(): Flow<Int> = flow {
        ensureInitialized()
        emitAll(practiceResultDao.getResultCount())
    }

    override suspend fun insertPracticeResult(result: PracticeResultEntity) {
        ensureInitialized()
        practiceResultDao.insertResult(result)
        // Record meaningful study activity
        val statusText = if (result.passed) "Passed" else "Needs Review"
        recordStudyActivity(
            activityType = "PRACTICE_TEST",
            details = "${result.title} — ${result.correctAnswers}/${result.totalQuestions} ($statusText)"
        )
    }

    override suspend fun deletePracticeResult(id: String) {
        ensureInitialized()
        practiceResultDao.deleteResult(id)
    }

    override suspend fun clearPracticeHistory() {
        ensureInitialized()
        practiceResultDao.clearHistory()
    }

    // --- Civics Vocabulary Implementation ---

    override fun getAllVocabulary(): Flow<List<VocabularyItem>> = flow {
        ensureInitialized()
        val dao = vocabularyDao
        if (dao == null) {
            emit(emptyList())
        } else {
            emitAll(
                dao.getAllVocabularyWithProgress().map { list ->
                    list.map { VocabularyItem.fromRelation(it) }
                }
            )
        }
    }

    override fun getVocabularyById(id: String): Flow<VocabularyItem?> = flow {
        ensureInitialized()
        val dao = vocabularyDao
        if (dao == null) {
            emit(null)
        } else {
            emitAll(
                dao.getVocabularyById(id).map { rel ->
                    rel?.let { VocabularyItem.fromRelation(it) }
                }
            )
        }
    }

    override suspend fun getVocabularyByIdDirect(id: String): VocabularyItem? {
        ensureInitialized()
        return vocabularyDao?.getVocabularyByIdDirect(id)?.let { VocabularyItem.fromRelation(it) }
    }

    override fun getVocabularyCount(): Flow<Int> = flow {
        ensureInitialized()
        val dao = vocabularyDao
        if (dao == null) {
            emit(0)
        } else {
            emitAll(dao.getVocabularyCount())
        }
    }

    override suspend fun toggleVocabularyFavorite(vocabId: String): Boolean {
        ensureInitialized()
        return vocabularyDao?.toggleFavorite(vocabId) ?: false
    }

    override suspend fun updateVocabularyStatus(vocabId: String, status: VocabularyStatus) {
        ensureInitialized()
        vocabularyDao?.updateStatus(vocabId, status.name)
    }
}
