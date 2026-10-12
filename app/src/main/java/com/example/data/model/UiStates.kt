package com.example.data.model

import com.example.data.local.entity.StudyActivityEntity

data class HomeUiState(
    val studiedCount: Int = 0,
    val totalCount: Int = TestConstants.TOTAL_CIVICS_QUESTIONS,
    val favoriteCount: Int = 0,
    val needsPracticeCount: Int = 0,
    val senior6520Count: Int = 20,
    val senior6520Studied: Int = 0,
    val recentQuestionId: String? = null,
    val recentQuestionNumber: Int? = null,
    val isReady: Boolean = false,
    val categories: List<CategoryOverviewItem> = emptyList(),
    // Phase 6 additions:
    val studyStreakDays: Int = 0,
    val dailyStudyGoal: Int = 10,
    val todayQuestionsCount: Int = 0,
    val recommendedTitle: String = "Start with your unstudied questions.",
    val recommendedSubtitle: String = "Build your knowledge base across the 128 civics questions.",
    val recommendedActionType: String = "UNSTUDIED", // UNSTUDIED, CATEGORY, MISTAKES, FULL_TEST
    val recommendedCategoryName: String? = null,
    val continueQuestionId: String? = null,
    val continueQuestionNumber: Int? = null,
    val continuePracticeType: String? = null,
    val continueQuestionText: String = "",
    val isFirstSession: Boolean = true,
    val testsCompleted: Int = 0,
    val averageScore: Int = 0,
    val recentActivities: List<StudyActivityEntity> = emptyList(),
    val vocabularyLearnedCount: Int = 0,
    val vocabularyTotalCount: Int = 115
) {
    val totalQuestions: Int get() = totalCount

    val progressPercentage: Int
        get() = if (totalCount > 0) ((studiedCount * 100f) / totalCount).toInt().coerceIn(0, 100) else 0

    val unstudiedCount: Int
        get() = (totalCount - studiedCount).coerceAtLeast(0)
}

data class CategoryOverviewItem(
    val name: String,
    val totalCount: Int,
    val studiedCount: Int,
    val description: String
) {
    val percentage: Int
        get() = if (totalCount > 0) ((studiedCount * 100f) / totalCount).toInt().coerceIn(0, 100) else 0
}

data class PracticeTestSummaryItem(
    val id: String,
    val title: String,
    val scoreText: String,
    val passed: Boolean,
    val dateText: String
)

data class PracticeModeItem(
    val id: String,
    val title: String,
    val questionCountText: String,
    val description: String,
    val badge: String? = null
)

data class PracticeDashboardUiState(
    val recentTestResults: List<PracticeTestSummaryItem> = emptyList(),
    val isReady: Boolean = false,
    val availableModes: List<PracticeModeItem> = listOf(
        PracticeModeItem("quick", "Quick Practice", "10 questions", "Focused rapid review session", "10 Qs"),
        PracticeModeItem("full", "Full Practice", "20 questions", "Simulate the standard USCIS civics test format", "Official Format"),
        PracticeModeItem("random", "Random Practice", "Variable", "Shuffle across all civics categories", null),
        PracticeModeItem("weak", "Weak Questions", "Targeted", "Practice questions you often miss or flag", "Needs Review"),
        PracticeModeItem("topics", "Topic Practice", "By Subject", "Practice questions by specific civics category", null),
        PracticeModeItem("interview", "Mock Interview", "Oral Simulation", "Practice answering spoken civics questions", "Audio Ready")
    )
)

data class CategoryProgressData(
    val categoryName: String,
    val totalCount: Int,
    val studiedCount: Int,
    val masteredCount: Int,
    val needsPracticeCount: Int,
    val accuracyPercentage: Int
) {
    val progressPercentage: Int
        get() = if (totalCount > 0) ((studiedCount * 100f) / totalCount).toInt().coerceIn(0, 100) else 0
}

data class ScoreTrendPoint(
    val testId: String,
    val title: String,
    val percentage: Int,
    val passed: Boolean,
    val dateLabel: String
)

data class ProgressUiState(
    val totalQuestions: Int = TestConstants.TOTAL_CIVICS_QUESTIONS,
    val studiedCount: Int = 0,
    val masteredCount: Int = 0,
    val needsPracticeCount: Int = 0,
    val notStartedCount: Int = TestConstants.TOTAL_CIVICS_QUESTIONS,
    val progressPercentage: Int = 0,
    val categories: List<CategoryProgressData> = emptyList(),
    // 65/20 breakdown
    val senior6520Total: Int = 20,
    val senior6520Studied: Int = 0,
    val senior6520Mastered: Int = 0,
    val senior6520NeedsPractice: Int = 0,
    val senior6520NotStarted: Int = 20,
    // Favorites breakdown
    val favoritesTotal: Int = 0,
    val favoritesStudied: Int = 0,
    val favoritesMastered: Int = 0,
    val favoritesNeedsPractice: Int = 0,
    // Flashcards breakdown
    val flashcardsTotal: Int = TestConstants.TOTAL_CIVICS_QUESTIONS,
    val flashcardsStudied: Int = 0,
    val flashcardsMastered: Int = 0,
    val flashcardsNeedReview: Int = 0,
    // Streak & Goal
    val studyStreakDays: Int = 0,
    val dailyStudyGoal: Int = 10,
    val todayActivityCount: Int = 0,
    // Practice Performance
    val testsCompleted: Int = 0,
    val averageScore: Int = 0,
    val bestScore: Int = 0,
    val passRate: Int = 0,
    val totalQuestionsPracticed: Int = 0,
    // Speaking
    val speakingSessionsCompleted: Int = 0,
    val speakingTotalQuestions: Int = 0,
    val speakingAccuracy: Int = 0,
    val recentSpeakingResult: String? = null,
    // Trend & Activities
    val scoreTrendPoints: List<ScoreTrendPoint> = emptyList(),
    val recentActivities: List<StudyActivityEntity> = emptyList(),
    val weakAreasCount: Int = 0,
    val weakQuestions: List<StudyQuestionItem> = emptyList(),
    val isLoading: Boolean = false
)

data class SettingsUiState(
    val themeMode: String = "SYSTEM", // "LIGHT", "DARK", "SYSTEM"
    val questionAudio: Boolean = true,
    val answerAudio: Boolean = true,
    val audioAutoplay: Boolean = false,
    val playbackSpeed: Float = 1.0f,
    val dailyReminder: Boolean = false,
    val dailyStudyGoal: Int = 10,
    val testDate: String? = null,
    val onboardingCompleted: Boolean = false
)
