package com.example.data.model

import org.json.JSONArray
import org.json.JSONObject

enum class PracticeType(
    val title: String,
    val subtitle: String,
    val defaultCount: Int
) {
    QUICK(
        title = "Quick Practice",
        subtitle = "10 questions • Fast review session",
        defaultCount = 10
    ),
    FULL(
        title = "Full Practice Test",
        subtitle = "20 questions • 12 correct to pass",
        defaultCount = 20
    ),
    RANDOM(
        title = "Random Practice",
        subtitle = "Practice questions from all topics",
        defaultCount = 20
    ),
    TOPIC(
        title = "Study by Topic",
        subtitle = "Focus on one civics topic",
        defaultCount = 20
    ),
    FAVORITES(
        title = "Favorites",
        subtitle = "Practice questions you've saved",
        defaultCount = 10
    ),
    NEEDS_PRACTICE(
        title = "Needs Practice",
        subtitle = "Review questions you answered incorrectly",
        defaultCount = 10
    ),
    SENIOR_65_20(
        title = "65/20 Practice",
        subtitle = "20 questions for applicants 65+ with 20+ years of residency",
        defaultCount = 20
    ),
    WEAK_AREAS(
        title = "Practice Weak Areas",
        subtitle = "Questions with low accuracy or recent mistakes",
        defaultCount = 10
    ),
    SPEAKING(
        title = "Speaking Practice",
        subtitle = "Practice questions aloud like an interview",
        defaultCount = 20
    );

    companion object {
        fun fromString(value: String): PracticeType {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: QUICK
        }
    }
}

enum class UserEvaluation {
    UNANSWERED,
    GOT_IT,
    MISSED_IT
}

data class PracticeQuestionItem(
    val questionId: String,
    val questionNumber: Int,
    val questionText: String,
    val category: String,
    val subcategory: String,
    val is65_20: Boolean,
    val primaryAnswer: String,
    val allAnswers: List<String>,
    val explanation: String,
    val explanationSourceType: String,
    val explanationSourceLabel: String,
    val isFavorite: Boolean = false,
    val isStudied: Boolean = false,
    val evaluation: UserEvaluation = UserEvaluation.UNANSWERED,
    val isAnswerRevealed: Boolean = false
) {
    val isAnswered: Boolean
        get() = evaluation != UserEvaluation.UNANSWERED
}

data class ReviewAnswerItem(
    val questionId: String,
    val questionNumber: Int,
    val questionText: String,
    val category: String,
    val primaryAnswer: String,
    val allAnswers: List<String>,
    val explanation: String,
    val isCorrect: Boolean
) {
    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("questionId", questionId)
        json.put("questionNumber", questionNumber)
        json.put("questionText", questionText)
        json.put("category", category)
        json.put("primaryAnswer", primaryAnswer)
        val arr = JSONArray()
        allAnswers.forEach { arr.put(it) }
        json.put("allAnswers", arr)
        json.put("explanation", explanation)
        json.put("isCorrect", isCorrect)
        return json
    }

    companion object {
        fun fromJson(json: JSONObject): ReviewAnswerItem {
            val answersList = mutableListOf<String>()
            val arr = json.optJSONArray("allAnswers")
            if (arr != null) {
                for (i in 0 until arr.length()) {
                    answersList.add(arr.getString(i))
                }
            }
            return ReviewAnswerItem(
                questionId = json.optString("questionId"),
                questionNumber = json.optInt("questionNumber"),
                questionText = json.optString("questionText"),
                category = json.optString("category"),
                primaryAnswer = json.optString("primaryAnswer"),
                allAnswers = answersList,
                explanation = json.optString("explanation"),
                isCorrect = json.optBoolean("isCorrect")
            )
        }

        fun listToJsonString(items: List<ReviewAnswerItem>): String {
            val arr = JSONArray()
            items.forEach { arr.put(it.toJson()) }
            return arr.toString()
        }

        fun listFromJsonString(jsonString: String): List<ReviewAnswerItem> {
            val list = mutableListOf<ReviewAnswerItem>()
            if (jsonString.isBlank()) return list
            try {
                val arr = JSONArray(jsonString)
                for (i in 0 until arr.length()) {
                    list.add(fromJson(arr.getJSONObject(i)))
                }
            } catch (_: Exception) {
            }
            return list
        }
    }
}
