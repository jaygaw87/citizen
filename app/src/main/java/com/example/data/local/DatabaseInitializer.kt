package com.example.data.local

import android.content.Context
import android.util.Log
import com.example.data.local.dao.QuestionDao
import com.example.data.local.entity.AnswerEntity
import com.example.data.local.entity.QuestionEntity
import com.example.data.local.entity.QuestionSourceTypes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * DatabaseInitializer safely seeds the 128 official USCIS civics questions and answers
 * into the local Room database from bundled asset JSON.
 *
 * Runs strictly on [Dispatchers.IO] and executes within a database transaction and Mutex lock.
 */
class DatabaseInitializer(
    private val context: Context,
    private val questionDao: QuestionDao,
    private val vocabularyDao: com.example.data.local.dao.VocabularyDao? = null
) {
    companion object {
        private const val TAG = "CivicsDiagnostic"
        private const val SEED_FILE = "civics_questions.json"
        private const val VOCAB_SEED_FILE = "civics_vocabulary.json"
        const val EXPECTED_QUESTION_COUNT = 128
        const val EXPECTED_65_20_COUNT = 20
        const val EXPECTED_DYNAMIC_COUNT = 8
        const val EXPECTED_VOCABULARY_COUNT = 115
        private val mutex = Mutex()
    }

    suspend fun initializeIfNeeded(): InitializationResult = withContext(Dispatchers.IO) {
        mutex.withLock {
            // Check vocabulary initialization
            initializeVocabularyIfNeeded()

            val existingCount = questionDao.getQuestionCountDirect()
            if (existingCount >= EXPECTED_QUESTION_COUNT) {
                val seniorCount = questionDao.get65_20CountDirect()
                val govCount = questionDao.getCategoryCountDirect("American Government")
                val histCount = questionDao.getCategoryCountDirect("American History")
                val civicsCount = questionDao.getCategoryCountDirect("Integrated Civics")

                Log.i(TAG, "[DB] Database initialized: true")
                Log.i(TAG, "[DB] Total questions: $existingCount")
                Log.i(TAG, "[DB] 65/20 questions: $seniorCount")
                Log.i(TAG, "[DB] American Government: $govCount")
                Log.i(TAG, "[DB] American History: $histCount")
                Log.i(TAG, "[DB] Integrated Civics: $civicsCount")
                Log.i(TAG, "[DB] Seed required = false")
                return@withContext InitializationResult.AlreadyInitialized(existingCount)
            }

            Log.i(TAG, "[DB] Database initialized: false")
            Log.i(TAG, "[DB] Total questions in DB: $existingCount (expected $EXPECTED_QUESTION_COUNT)")
            Log.i(TAG, "[DB] Seed required = true")

            if (existingCount in 1 until EXPECTED_QUESTION_COUNT) {
                Log.w(TAG, "[DB] Incomplete questions table detected ($existingCount rows). Resetting before full seed...")
                questionDao.deleteAllAnswers()
                questionDao.deleteAllQuestions()
            }

            Log.i(TAG, "[DB] Reading and parsing seed file: $SEED_FILE...")
            try {
                val jsonString = readAsset(SEED_FILE)
                val jsonObject = JSONObject(jsonString)
                val questionsArray = jsonObject.getJSONArray("questions")
                val jsonRecordCount = questionsArray.length()
                Log.i(TAG, "[DB] JSON records = $jsonRecordCount")

                val questionEntities = mutableListOf<QuestionEntity>()
                val answerEntities = mutableListOf<AnswerEntity>()

                val seenIds = mutableSetOf<String>()
                val seenNumbers = mutableSetOf<Int>()

                for (i in 0 until questionsArray.length()) {
                    val qObj = questionsArray.getJSONObject(i)

                    val id = qObj.getString("id")
                    val number = qObj.getInt("questionNumber")
                    val category = qObj.getString("category")
                    val subcategory = qObj.getString("subcategory")
                    val questionText = qObj.getString("questionText")
                    val explanation = qObj.optString("explanation", "")
                    val is65_20 = qObj.optBoolean("is65_20", false)
                    val isDynamic = qObj.optBoolean("isDynamicAnswer", false)
                    val dynamicType = if (qObj.has("dynamicAnswerType") && !qObj.isNull("dynamicAnswerType")) {
                        qObj.getString("dynamicAnswerType")
                    } else null
                    val currentAnswer = if (qObj.has("currentAnswer") && !qObj.isNull("currentAnswer")) {
                        qObj.getString("currentAnswer")
                    } else null
                    val answerLastVerified = if (qObj.has("answerLastVerified") && !qObj.isNull("answerLastVerified")) {
                        qObj.getString("answerLastVerified")
                    } else null
                    val answerSourceName = if (qObj.has("answerSourceName") && !qObj.isNull("answerSourceName")) {
                        qObj.getString("answerSourceName")
                    } else null
                    val answerSourceUrl = if (qObj.has("answerSourceUrl") && !qObj.isNull("answerSourceUrl")) {
                        qObj.getString("answerSourceUrl")
                    } else null

                    val sourceType = qObj.optString("sourceType", QuestionSourceTypes.USCIS_OFFICIAL)
                    val explanationSourceType = qObj.optString("explanationSourceType", QuestionSourceTypes.CURATED_EDUCATIONAL)
                    val explanationSourceLabel = qObj.optString("explanationSourceLabel", "Educational explanation")
                    val sourceName = qObj.optString("sourceName", "USCIS 2025 Civics Test (Form M-1778)")
                    val sourceUrl = qObj.optString("sourceUrl", "https://www.uscis.gov/citizenship/")
                    val version = qObj.optInt("version", 1)

                    require(id.isNotBlank()) { "Question at index $i has blank id" }
                    require(!seenIds.contains(id)) { "Duplicate question ID: $id" }
                    require(!seenNumbers.contains(number)) { "Duplicate question number: $number" }
                    require(questionText.isNotBlank()) { "Blank question text in $id" }
                    if (isDynamic) {
                        require(!dynamicType.isNullOrBlank()) { "Dynamic question $id missing dynamicAnswerType" }
                        require(!currentAnswer.isNullOrBlank()) { "Dynamic question $id missing currentAnswer" }
                        require(!answerLastVerified.isNullOrBlank()) { "Dynamic question $id missing answerLastVerified" }
                        require(!answerSourceName.isNullOrBlank()) { "Dynamic question $id missing answerSourceName" }
                        require(!answerSourceUrl.isNullOrBlank()) { "Dynamic question $id missing answerSourceUrl" }
                    }

                    seenIds.add(id)
                    seenNumbers.add(number)

                    questionEntities.add(
                        QuestionEntity(
                            id = id,
                            questionNumber = number,
                            category = category,
                            subcategory = subcategory,
                            questionText = questionText,
                            sourceType = sourceType,
                            explanation = explanation,
                            explanationSourceType = explanationSourceType,
                            explanationSourceLabel = explanationSourceLabel,
                            is65_20 = is65_20,
                            isDynamicAnswer = isDynamic,
                            dynamicAnswerType = dynamicType,
                            currentAnswer = currentAnswer,
                            answerLastVerified = answerLastVerified,
                            answerSourceName = answerSourceName,
                            answerSourceUrl = answerSourceUrl,
                            sourceName = sourceName,
                            sourceUrl = sourceUrl,
                            version = version,
                            lastUpdated = System.currentTimeMillis()
                        )
                    )

                    val answersArray = qObj.getJSONArray("answers")
                    require(answersArray.length() > 0) { "Question $id must have at least one answer" }

                    for (a in 0 until answersArray.length()) {
                        val answerText = answersArray.getString(a)
                        require(answerText.isNotBlank()) { "Question $id has blank answer at index $a" }

                        answerEntities.add(
                            AnswerEntity(
                                questionId = id,
                                answerText = answerText,
                                sourceType = QuestionSourceTypes.USCIS_OFFICIAL,
                                sourceName = sourceName,
                                isPrimary = (a == 0),
                                isAcceptable = true,
                                orderIndex = a
                            )
                        )
                    }
                }

                // Insert into Room using transaction
                questionDao.insertQuestionsAndAnswers(questionEntities, answerEntities)

                // Post-insertion verification
                val actualCount = questionDao.getQuestionCountDirect()
                val actual6520 = questionDao.get65_20CountDirect()
                val actualDynamic = questionDao.getDynamicCountDirect()
                val govCount = questionDao.getCategoryCountDirect("American Government")
                val histCount = questionDao.getCategoryCountDirect("American History")
                val civicsCount = questionDao.getCategoryCountDirect("Integrated Civics")
                val categories = questionDao.getAllCategoriesDirect()

                Log.i(TAG, "[DB] Inserted records = $actualCount")
                Log.i(TAG, "[DB] Database initialized: true")
                Log.i(TAG, "[DB] Total questions: $actualCount")
                Log.i(TAG, "[DB] 65/20 questions: $actual6520")
                Log.i(TAG, "[DB] American Government: $govCount")
                Log.i(TAG, "[DB] American History: $histCount")
                Log.i(TAG, "[DB] Integrated Civics: $civicsCount")

                check(actualCount == EXPECTED_QUESTION_COUNT) {
                    "Database seed mismatch: expected $EXPECTED_QUESTION_COUNT questions, got $actualCount"
                }
                check(actual6520 == EXPECTED_65_20_COUNT) {
                    "Database seed mismatch: expected $EXPECTED_65_20_COUNT 65/20 questions, got $actual6520"
                }
                check(actualDynamic == EXPECTED_DYNAMIC_COUNT) {
                    "Database seed mismatch: expected $EXPECTED_DYNAMIC_COUNT dynamic questions, got $actualDynamic"
                }

                InitializationResult.Success(
                    totalQuestions = actualCount,
                    seniorQuestions = actual6520,
                    dynamicQuestions = actualDynamic,
                    categories = categories
                )
            } catch (e: Exception) {
                Log.e(TAG, "[DB] Failed to initialize USCIS civics database", e)
                InitializationResult.Error(e)
            }
        }
    }

    suspend fun initializeVocabularyIfNeeded(): Int = withContext(Dispatchers.IO) {
        val dao = vocabularyDao ?: return@withContext 0
        try {
            val existing = dao.getVocabularyCountDirect()
            if (existing >= EXPECTED_VOCABULARY_COUNT) {
                Log.i(TAG, "[DB] Vocabulary already seeded: $existing items")
                return@withContext existing
            }
            Log.i(TAG, "[DB] Seeding civics vocabulary from $VOCAB_SEED_FILE...")
            val jsonString = readAsset(VOCAB_SEED_FILE)
            val jsonArray = org.json.JSONArray(jsonString)
            val entities = mutableListOf<com.example.data.local.entity.VocabularyEntity>()
            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(i)
                val relArray = item.optJSONArray("relatedQuestionIds")
                val relIds = mutableListOf<String>()
                if (relArray != null) {
                    for (j in 0 until relArray.length()) {
                        relIds.add(relArray.getString(j))
                    }
                }
                entities.add(
                    com.example.data.local.entity.VocabularyEntity(
                        id = item.getString("id"),
                        word = item.getString("word"),
                        pronunciation = item.optString("pronunciation", ""),
                        simpleDefinition = item.getString("simpleDefinition"),
                        detailedDefinition = item.getString("detailedDefinition"),
                        exampleSentence = item.optString("exampleSentence", ""),
                        relatedQuestionIds = relIds.joinToString(","),
                        category = item.getString("category")
                    )
                )
            }
            dao.insertAll(entities)
            Log.i(TAG, "[DB] Successfully seeded ${entities.size} vocabulary items")
            entities.size
        } catch (e: Exception) {
            Log.e(TAG, "[DB] Error seeding vocabulary", e)
            0
        }
    }

    private fun readAsset(fileName: String): String {
        return context.assets.open(fileName).use { inputStream ->
            BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
                reader.readText()
            }
        }
    }

    sealed interface InitializationResult {
        data class Success(
            val totalQuestions: Int,
            val seniorQuestions: Int,
            val dynamicQuestions: Int,
            val categories: List<String>
        ) : InitializationResult

        data class AlreadyInitialized(val totalQuestions: Int) : InitializationResult
        data class Error(val throwable: Throwable) : InitializationResult
    }
}
