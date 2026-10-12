package com.example.ui.navigation

import java.net.URLEncoder

sealed class NavRoutes(val route: String) {
    // Top-level / Primary tabs
    object Onboarding : NavRoutes("onboarding")
    object Home : NavRoutes("home")
    object Study : NavRoutes("study?filter={filter}") {
        fun createRoute(filter: String = "ALL") = "study?filter=$filter"
    }
    object Practice : NavRoutes("practice")
    object Progress : NavRoutes("progress")
    object Settings : NavRoutes("settings")

    // Study Feature Routes
    object Questions : NavRoutes("questions")
    object QuestionDetail : NavRoutes("question_detail/{questionId}?filter={filter}&search={search}") {
        fun createRoute(questionId: String, filter: String = "ALL", search: String = ""): String {
            val encodedSearch = try {
                URLEncoder.encode(search, "UTF-8")
            } catch (_: Exception) {
                search
            }
            return "question_detail/$questionId?filter=$filter&search=$encodedSearch"
        }
        fun createRoute(questionId: Int, filter: String = "ALL", search: String = ""): String {
            val qId = String.format("Q%03d", questionId)
            return createRoute(qId, filter, search)
        }
    }
    object Topics : NavRoutes("topics")
    object Favorites : NavRoutes("favorites")
    object Mistakes : NavRoutes("mistakes")
    object Flashcards : NavRoutes("flashcards?filter={filter}") {
        fun createRoute(filter: String = "ALL") = "flashcards?filter=$filter"
    }
    object SpecialConsideration65_20 : NavRoutes("special_consideration_65_20?filter={filter}") {
        fun createRoute(filter: String = "ALL") = "special_consideration_65_20?filter=$filter"
    }
    object Vocabulary : NavRoutes("vocabulary")
    object VocabularyDetail : NavRoutes("vocabulary_detail/{vocabId}") {
        fun createRoute(vocabId: String) = "vocabulary_detail/$vocabId"
    }
    object VocabularyQuickReview : NavRoutes("vocabulary_quick_review?filter={filter}") {
        fun createRoute(filter: String = "ALL") = "vocabulary_quick_review?filter=$filter"
    }
    object VocabularyFlashcards : NavRoutes("vocabulary_flashcards?category={category}&filter={filter}") {
        fun createRoute(category: String = "ALL", filter: String = "ALL") = "vocabulary_flashcards?category=$category&filter=$filter"
    }
    object QuickReview : NavRoutes("quick_review?section={section}") {
        fun createRoute(section: String = "ALL") = "quick_review?section=$section"
    }
    object AudioPractice : NavRoutes("audio_practice")

    // Practice Feature Routes
    object PracticeTest : NavRoutes("practice_session/{testType}?topic={topic}&count={count}") {
        fun createRoute(testType: String, topic: String = "", count: Int = 0): String {
            val encodedTopic = try {
                URLEncoder.encode(topic, "UTF-8")
            } catch (_: Exception) {
                topic
            }
            return "practice_session/$testType?topic=$encodedTopic&count=$count"
        }
    }
    object TestResult : NavRoutes("test_result/{resultId}") {
        fun createRoute(resultId: String) = "test_result/$resultId"
    }
    object ReviewAnswers : NavRoutes("review_answers/{resultId}") {
        fun createRoute(resultId: String) = "review_answers/$resultId"
    }
    object PracticeHistory : NavRoutes("practice_history")
    object MockInterview : NavRoutes("mock_interview")
    object SpeakingPractice : NavRoutes("speaking_practice?topic={topic}&count={count}") {
        fun createRoute(topic: String = "", count: Int = 20): String {
            val encodedTopic = try {
                URLEncoder.encode(topic, "UTF-8")
            } catch (_: Exception) {
                topic
            }
            return "speaking_practice?topic=$encodedTopic&count=$count"
        }
    }

    // Progress Sub-Routes
    object RecentTests : NavRoutes("recent_tests")
    object QuestionsToReview : NavRoutes("questions_to_review")
    object StudyActivity : NavRoutes("study_activity")
    object CategoryProgress : NavRoutes("category_progress/{categoryName}") {
        fun createRoute(categoryName: String): String {
            val encodedName = try {
                URLEncoder.encode(categoryName, "UTF-8")
            } catch (_: Exception) {
                categoryName
            }
            return "category_progress/$encodedName"
        }
    }
    object WeakAreas : NavRoutes("weak_areas")

    // Info & Legal
    object About : NavRoutes("about")
    object Disclaimer : NavRoutes("disclaimer")
}
