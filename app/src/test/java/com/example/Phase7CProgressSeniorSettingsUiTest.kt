package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTextInput
import com.example.data.learning.MasteryStatus
import com.example.data.local.entity.AnswerEntity
import com.example.data.local.entity.QuestionEntity
import com.example.data.local.entity.StudyActivityEntity
import com.example.data.model.CategoryProgressData
import com.example.data.model.ProgressUiState
import com.example.data.model.ScoreTrendPoint
import com.example.data.model.SettingsUiState
import com.example.data.model.StudyQuestionItem
import com.example.domain.usecase.LearningProgressOverview
import com.example.ui.progress.ProgressScreen
import com.example.ui.senior6520.Senior6520Screen
import com.example.ui.senior6520.Senior6520UiState
import com.example.ui.senior6520.SeniorFilter
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Phase7CProgressSeniorSettingsUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun createSampleQuestion(
        id: String,
        number: Int,
        text: String,
        is6520: Boolean = true,
        isFavorite: Boolean = false,
        status: MasteryStatus = MasteryStatus.NOT_STARTED,
        answers: List<String> = listOf("Accepted answer 1")
    ): StudyQuestionItem {
        val q = QuestionEntity(
            id = id,
            questionNumber = number,
            category = "American Government",
            subcategory = "Principles",
            questionText = text,
            explanation = "Sample explanation",
            is65_20 = is6520,
            isDynamicAnswer = false
        )
        val ansEntities = answers.mapIndexed { idx, a ->
            AnswerEntity(
                id = idx.toLong() + 1L,
                questionId = id,
                answerText = a,
                isPrimary = idx == 0
            )
        }
        val isMastered = status == MasteryStatus.MASTERED
        val needsPractice = status == MasteryStatus.NEEDS_PRACTICE
        val isStudied = status != MasteryStatus.NOT_STARTED
        return StudyQuestionItem(
            question = q,
            answers = ansEntities,
            isFavorite = isFavorite,
            isStudied = isStudied,
            needsPractice = needsPractice,
            isMastered = isMastered,
            correctCount = if (isMastered) 2 else 0,
            incorrectCount = if (needsPractice) 1 else 0
        )
    }

    // =========================================================================
    // PROGRESS SCREEN TESTS
    // =========================================================================

    @Test
    fun testProgressScreen_rendersOverallCardAndMetrics() {
        val uiState = ProgressUiState(
            totalQuestions = 128,
            studiedCount = 64,
            progressPercentage = 50,
            masteredCount = 30,
            needsPracticeCount = 10,
            notStartedCount = 64,
            studyStreakDays = 5,
            dailyStudyGoal = 20,
            todayActivityCount = 12
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                ProgressScreen(
                    uiState = uiState,
                    onNavigateToCategoryProgress = {},
                    onNavigateToWeakAreas = {},
                    onNavigateToPracticeWeakAreas = {},
                    onNavigateTo6520Practice = {},
                    onNavigateToFavoritesPractice = {},
                    onNavigateToFlashcards = {},
                    onNavigateToSpeakingPractice = {},
                    onNavigateToPracticeHistory = {},
                    onNavigateToSettings = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("progress_screen").assertExists()
        composeTestRule.onNodeWithTag("progress_overall_card").assertExists()
        composeTestRule.onNodeWithText("Readiness Progress").assertExists()
        composeTestRule.onNodeWithText("64 of 128 questions studied").assertExists()
        composeTestRule.onNodeWithText("50%").assertExists()
        composeTestRule.onNodeWithText("Studied").assertExists()
        composeTestRule.onNodeWithText("Mastered").assertExists()
        composeTestRule.onNodeWithText("Needs Practice").assertExists()
        composeTestRule.onNodeWithText("Not Started").assertExists()
        composeTestRule.onNodeWithText("🔥 5 Days").assertExists()
        composeTestRule.onNodeWithText("12 / 20 Qs").assertExists()
    }

    @Test
    fun testProgressScreen_categoriesDisplayAllThree() {
        val categories = listOf(
            CategoryProgressData(
                categoryName = "American Government",
                totalCount = 75,
                studiedCount = 40,
                masteredCount = 20,
                needsPracticeCount = 5,
                accuracyPercentage = 80
            ),
            CategoryProgressData(
                categoryName = "American History",
                totalCount = 43,
                studiedCount = 20,
                masteredCount = 10,
                needsPracticeCount = 2,
                accuracyPercentage = 75
            ),
            CategoryProgressData(
                categoryName = "Integrated Civics",
                totalCount = 10,
                studiedCount = 4,
                masteredCount = 2,
                needsPracticeCount = 1,
                accuracyPercentage = 70
            )
        )

        var selectedCategory = ""
        val uiState = ProgressUiState(categories = categories)

        composeTestRule.setContent {
            MyApplicationTheme {
                ProgressScreen(
                    uiState = uiState,
                    onNavigateToCategoryProgress = { cat -> selectedCategory = cat },
                    onNavigateToWeakAreas = {},
                    onNavigateToPracticeWeakAreas = {},
                    onNavigateTo6520Practice = {},
                    onNavigateToFavoritesPractice = {},
                    onNavigateToFlashcards = {},
                    onNavigateToSpeakingPractice = {},
                    onNavigateToPracticeHistory = {},
                    onNavigateToSettings = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("category_progress_american_government").assertExists()
        composeTestRule.onNodeWithTag("category_progress_american_history").assertExists()
        composeTestRule.onNodeWithTag("category_progress_integrated_civics").assertExists()

        composeTestRule.onNodeWithTag("category_progress_american_government").performScrollTo().performClick()
        assertEquals("American Government", selectedCategory)
    }

    @Test
    fun testProgressScreen_weakAreasCardWhenWeakQuestionsPresent() {
        var weakAreasClicked = false
        var practiceWeakClicked = false

        val uiState = ProgressUiState(
            weakAreasCount = 6
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                ProgressScreen(
                    uiState = uiState,
                    onNavigateToCategoryProgress = {},
                    onNavigateToWeakAreas = { weakAreasClicked = true },
                    onNavigateToPracticeWeakAreas = { practiceWeakClicked = true },
                    onNavigateTo6520Practice = {},
                    onNavigateToFavoritesPractice = {},
                    onNavigateToFlashcards = {},
                    onNavigateToSpeakingPractice = {},
                    onNavigateToPracticeHistory = {},
                    onNavigateToSettings = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("progress_weak_areas_card").performScrollTo().assertExists()
        composeTestRule.onNodeWithText("Needs Practice (6)").assertExists()
        composeTestRule.onNodeWithText("View List").performClick()
        assertTrue(weakAreasClicked)

        composeTestRule.onNodeWithTag("practice_weak_areas_button").performClick()
        assertTrue(practiceWeakClicked)
    }

    @Test
    fun testProgressScreen_senior6520CardAndPracticeButton() {
        var practice6520Clicked = false

        val uiState = ProgressUiState(
            senior6520Studied = 15,
            senior6520Total = 20
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                ProgressScreen(
                    uiState = uiState,
                    onNavigateToCategoryProgress = {},
                    onNavigateToWeakAreas = {},
                    onNavigateToPracticeWeakAreas = {},
                    onNavigateTo6520Practice = { practice6520Clicked = true },
                    onNavigateToFavoritesPractice = {},
                    onNavigateToFlashcards = {},
                    onNavigateToSpeakingPractice = {},
                    onNavigateToPracticeHistory = {},
                    onNavigateToSettings = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("progress_6520_card").performScrollTo().assertExists()
        composeTestRule.onNodeWithText("15 / 20").assertExists()
        composeTestRule.onNodeWithTag("progress_practice_6520_button").performClick()
        assertTrue(practice6520Clicked)
    }

    @Test
    fun testProgressScreen_scoreTrendAndRecentActivity() {
        val scorePoints = listOf(
            ScoreTrendPoint(testId = "1", title = "Quick Practice", percentage = 80, passed = true, dateLabel = "Sep 28"),
            ScoreTrendPoint(testId = "2", title = "Full Practice", percentage = 90, passed = true, dateLabel = "Sep 29")
        )
        val activities = listOf(
            StudyActivityEntity(
                date = "2026-09-29",
                activityType = "PRACTICE_TEST",
                details = "Scored 90% in Quick Practice",
                createdAt = System.currentTimeMillis()
            )
        )

        val uiState = ProgressUiState(
            testsCompleted = 2,
            averageScore = 85,
            passRate = 100,
            totalQuestionsPracticed = 20,
            speakingSessionsCompleted = 3,
            speakingAccuracy = 92,
            scoreTrendPoints = scorePoints,
            recentActivities = activities
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                ProgressScreen(
                    uiState = uiState,
                    onNavigateToCategoryProgress = {},
                    onNavigateToWeakAreas = {},
                    onNavigateToPracticeWeakAreas = {},
                    onNavigateTo6520Practice = {},
                    onNavigateToFavoritesPractice = {},
                    onNavigateToFlashcards = {},
                    onNavigateToSpeakingPractice = {},
                    onNavigateToPracticeHistory = {},
                    onNavigateToSettings = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("progress_performance_card").performScrollTo().assertExists()
        composeTestRule.onNodeWithText("85%").assertExists()
        composeTestRule.onNodeWithText("100%").assertExists()
        composeTestRule.onNodeWithText("Speaking Practice Accuracy").assertExists()

        composeTestRule.onNodeWithTag("progress_score_trend_card").performScrollTo().assertExists()
        composeTestRule.onNodeWithText("Passing: 60%").assertExists()

        composeTestRule.onNodeWithTag("recent_activity_list").performScrollTo().assertExists()
        composeTestRule.onNodeWithText("PRACTICE TEST").assertExists()
    }

    // =========================================================================
    // SENIOR 65/20 SCREEN TESTS
    // =========================================================================

    @Test
    fun testSenior6520Screen_rendersBannerReadinessCardAndActionGrid() {
        val q1 = createSampleQuestion("Q1", 1, "What is the supreme law of the land?", is6520 = true)
        val q2 = createSampleQuestion("Q2", 2, "What does the Constitution do?", is6520 = true)
        val progress = LearningProgressOverview(
            senior6520Studied = 12,
            senior6520Total = 20,
            senior6520Mastered = 8,
            senior6520NeedsPractice = 2
        )
        val uiState = Senior6520UiState(
            allQuestions = listOf(q1, q2),
            filteredQuestions = listOf(q1, q2),
            progress = progress,
            selectedFilter = SeniorFilter.ALL
        )

        var studyClicked = false
        var flashcardsClicked = false
        var speakingClicked = false

        composeTestRule.setContent {
            MyApplicationTheme {
                Senior6520Screen(
                    uiState = uiState,
                    onBackClick = {},
                    onFilterSelected = {},
                    onSearchQueryChanged = {},
                    onToggleFavorite = {},
                    onQuestionClick = {},
                    onStudyClick = { studyClicked = true },
                    onPracticeClick = { _, _ -> },
                    onFlashcardsClick = { flashcardsClicked = true },
                    onSpeakingClick = { speakingClicked = true }
                )
            }
        }

        composeTestRule.onNodeWithTag("senior_6520_screen").assertExists()
        composeTestRule.onNodeWithText("65/20 Special Consideration").assertExists()
        composeTestRule.onNodeWithTag("senior_progress_card").assertExists()
        composeTestRule.onNodeWithText("12 / 20 Questions Studied").assertExists()
        composeTestRule.onNodeWithText("60%").assertExists()

        composeTestRule.onNodeWithTag("action_study_6520").performScrollTo().performClick()
        assertTrue(studyClicked)

        composeTestRule.onNodeWithTag("action_flashcards_6520").performScrollTo().performClick()
        assertTrue(flashcardsClicked)

        composeTestRule.onNodeWithTag("action_speaking_6520").performScrollTo().performClick()
        assertTrue(speakingClicked)
    }

    @Test
    fun testSenior6520Screen_disclaimerDialogOpensAndDisplaysPolicy() {
        val uiState = Senior6520UiState()

        composeTestRule.setContent {
            MyApplicationTheme {
                Senior6520Screen(
                    uiState = uiState,
                    onBackClick = {},
                    onFilterSelected = {},
                    onSearchQueryChanged = {},
                    onToggleFavorite = {},
                    onQuestionClick = {},
                    onStudyClick = {},
                    onPracticeClick = { _, _ -> },
                    onFlashcardsClick = {},
                    onSpeakingClick = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("senior_disclaimer_button").performClick()
        composeTestRule.onNodeWithText("65/20 Exemption Eligibility").assertExists()
        composeTestRule.onNodeWithText("Official USCIS Website").assertExists()
        composeTestRule.onNodeWithText("Got It").performClick()
    }

    @Test
    fun testSenior6520Screen_practiceDialogOffersFullAndQuickModes() {
        val uiState = Senior6520UiState()
        var selectedMode = ""
        var selectedCount = 0

        composeTestRule.setContent {
            MyApplicationTheme {
                Senior6520Screen(
                    uiState = uiState,
                    onBackClick = {},
                    onFilterSelected = {},
                    onSearchQueryChanged = {},
                    onToggleFavorite = {},
                    onQuestionClick = {},
                    onStudyClick = {},
                    onPracticeClick = { mode, count ->
                        selectedMode = mode
                        selectedCount = count
                    },
                    onFlashcardsClick = {},
                    onSpeakingClick = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("action_practice_6520").performClick()
        composeTestRule.onNodeWithText("65/20 Practice Options").assertExists()
        composeTestRule.onNodeWithText("Full 65/20 Practice (20 Questions)").performClick()
        assertEquals("FULL", selectedMode)
        assertEquals(20, selectedCount)
    }

    @Test
    fun testSenior6520Screen_questionItemRowAndExpandAnswers() {
        val q = createSampleQuestion(
            id = "Q001",
            number = 1,
            text = "What is the supreme law of the land?",
            answers = listOf("the Constitution")
        )
        val uiState = Senior6520UiState(
            allQuestions = listOf(q),
            filteredQuestions = listOf(q)
        )

        var toggledFavoriteId = ""

        composeTestRule.setContent {
            MyApplicationTheme {
                Senior6520Screen(
                    uiState = uiState,
                    onBackClick = {},
                    onFilterSelected = {},
                    onSearchQueryChanged = {},
                    onToggleFavorite = { id -> toggledFavoriteId = id },
                    onQuestionClick = {},
                    onStudyClick = {},
                    onPracticeClick = { _, _ -> },
                    onFlashcardsClick = {},
                    onSpeakingClick = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("senior_question_list").performScrollToIndex(1)
        composeTestRule.onNodeWithTag("senior_question_1").assertExists()
        composeTestRule.onNodeWithText("What is the supreme law of the land?").assertExists()
        composeTestRule.onNodeWithText("1 accepted answers").assertExists()

        // Click bookmark icon to toggle favorite
        composeTestRule.onNodeWithTag("senior_fav_1").performClick()
        assertEquals("Q001", toggledFavoriteId)
    }

    @Test
    fun testSenior6520Screen_filterChipsAndSearch() {
        val q1 = createSampleQuestion("Q1", 1, "What is the supreme law?", isFavorite = true)
        val uiState = Senior6520UiState(
            allQuestions = listOf(q1),
            filteredQuestions = listOf(q1),
            selectedFilter = SeniorFilter.ALL,
            searchQuery = "supreme"
        )

        var chosenFilter: SeniorFilter? = null
        var newSearchText = ""

        composeTestRule.setContent {
            MyApplicationTheme {
                Senior6520Screen(
                    uiState = uiState,
                    onBackClick = {},
                    onFilterSelected = { filter -> chosenFilter = filter },
                    onSearchQueryChanged = { query -> newSearchText = query },
                    onToggleFavorite = {},
                    onQuestionClick = {},
                    onStudyClick = {},
                    onPracticeClick = { _, _ -> },
                    onFlashcardsClick = {},
                    onSpeakingClick = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("senior_filter_all").performScrollTo().assertExists()
        composeTestRule.onNodeWithTag("senior_filter_needs_practice").performScrollTo().performClick()
        assertEquals(SeniorFilter.NEEDS_PRACTICE, chosenFilter)

        composeTestRule.onNodeWithTag("senior_filter_mastered").performScrollTo().performClick()
        assertEquals(SeniorFilter.MASTERED, chosenFilter)

        composeTestRule.onNodeWithTag("senior_filter_favorites").performScrollTo().performClick()
        assertEquals(SeniorFilter.FAVORITES, chosenFilter)

        composeTestRule.onNodeWithTag("senior_search_field").performScrollTo().assertExists()
    }

    // =========================================================================
    // SETTINGS SCREEN TESTS
    // =========================================================================

    @Test
    fun testSettingsScreen_rendersAllSections() {
        val uiState = SettingsUiState(
            dailyStudyGoal = 20,
            dailyReminder = true,
            themeMode = "SYSTEM",
            playbackSpeed = 1.0f,
            questionAudio = true,
            answerAudio = true,
            audioAutoplay = false
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                SettingsScreen(
                    uiState = uiState,
                    onThemeChange = {},
                    onAudioAutoplayChange = {},
                    onPlaybackSpeedChange = {},
                    onDailyReminderChange = {},
                    onResetProgress = {},
                    onResetOnboarding = {},
                    onNavigateToAbout = {},
                    onNavigateToDisclaimer = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("settings_screen").assertExists()
        composeTestRule.onNodeWithText("Study Preferences").assertExists()
        composeTestRule.onNodeWithTag("settings_daily_goal").assertExists()
        composeTestRule.onNodeWithTag("settings_daily_reminder").assertExists()
        composeTestRule.onNodeWithText("Audio & Speech").assertExists()
        composeTestRule.onNodeWithTag("settings_question_audio").assertExists()
        composeTestRule.onNodeWithTag("settings_answer_audio").assertExists()
        composeTestRule.onNodeWithTag("settings_audio_autoplay").assertExists()
        composeTestRule.onNodeWithTag("settings_playback_speed").assertExists()
        composeTestRule.onNodeWithText("Appearance").assertExists()
        composeTestRule.onNodeWithTag("settings_theme_row").assertExists()
        composeTestRule.onNodeWithText("Data Management").assertExists()
        composeTestRule.onNodeWithTag("settings_reset_progress").assertExists()
        composeTestRule.onNodeWithTag("settings_reset_onboarding").assertExists()
        composeTestRule.onNodeWithText("About & Disclaimer").assertExists()
        composeTestRule.onNodeWithTag("settings_about").assertExists()
        composeTestRule.onNodeWithTag("settings_sources").assertExists()
        composeTestRule.onNodeWithTag("settings_disclaimer").assertExists()
    }

    @Test
    fun testSettingsScreen_themeDialogSelection() {
        var selectedTheme = ""
        val uiState = SettingsUiState(themeMode = "LIGHT")

        composeTestRule.setContent {
            MyApplicationTheme {
                SettingsScreen(
                    uiState = uiState,
                    onThemeChange = { mode -> selectedTheme = mode },
                    onAudioAutoplayChange = {},
                    onPlaybackSpeedChange = {},
                    onDailyReminderChange = {},
                    onResetProgress = {},
                    onResetOnboarding = {},
                    onNavigateToAbout = {},
                    onNavigateToDisclaimer = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("settings_theme_row").performScrollTo().performClick()
        composeTestRule.onNodeWithText("Select Theme").assertExists()
        composeTestRule.onNodeWithText("Dark Mode").performClick()
        assertEquals("DARK", selectedTheme)
    }

    @Test
    fun testSettingsScreen_dailyGoalDialog() {
        var newGoal = -1
        val uiState = SettingsUiState(dailyStudyGoal = 10)

        composeTestRule.setContent {
            MyApplicationTheme {
                SettingsScreen(
                    uiState = uiState,
                    onThemeChange = {},
                    onDailyStudyGoalChange = { goal -> newGoal = goal },
                    onAudioAutoplayChange = {},
                    onPlaybackSpeedChange = {},
                    onDailyReminderChange = {},
                    onResetProgress = {},
                    onResetOnboarding = {},
                    onNavigateToAbout = {},
                    onNavigateToDisclaimer = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("settings_daily_goal").performClick()
        composeTestRule.onNodeWithText("30 questions / day").performClick()
        assertEquals(30, newGoal)
    }

    @Test
    fun testSettingsScreen_playbackSpeedDialog() {
        var chosenSpeed = 0f
        val uiState = SettingsUiState(playbackSpeed = 1.0f)

        composeTestRule.setContent {
            MyApplicationTheme {
                SettingsScreen(
                    uiState = uiState,
                    onThemeChange = {},
                    onAudioAutoplayChange = {},
                    onPlaybackSpeedChange = { sp -> chosenSpeed = sp },
                    onDailyReminderChange = {},
                    onResetProgress = {},
                    onResetOnboarding = {},
                    onNavigateToAbout = {},
                    onNavigateToDisclaimer = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("settings_playback_speed").performScrollTo().performClick()
        composeTestRule.onNodeWithText("Fast (1.15x)").performClick()
        assertEquals(1.15f, chosenSpeed, 0.01f)
    }

    @Test
    fun testSettingsScreen_resetLearningProgressDialogConfirmation() {
        var resetProgressCalled = false
        val uiState = SettingsUiState()

        composeTestRule.setContent {
            MyApplicationTheme {
                SettingsScreen(
                    uiState = uiState,
                    onThemeChange = {},
                    onAudioAutoplayChange = {},
                    onPlaybackSpeedChange = {},
                    onDailyReminderChange = {},
                    onResetProgress = { resetProgressCalled = true },
                    onResetOnboarding = {},
                    onNavigateToAbout = {},
                    onNavigateToDisclaimer = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("settings_reset_progress").performScrollTo().performClick()
        composeTestRule.onNodeWithText("Reset Learning Progress?").assertExists()

        // Confirms preservation note is clearly visible to user
        composeTestRule.onNodeWithText(
            "This will reset your question progress, civics vocabulary learning progress, mastery, and daily streak.\n\n" +
                "Your 128 questions, saved favorites, practice history, and settings will remain preserved."
        ).assertExists()

        composeTestRule.onNodeWithTag("confirm_reset_progress_button").performClick()
        assertTrue(resetProgressCalled)
    }

    @Test
    fun testSettingsScreen_resetLearningProgressDialogCancel() {
        var resetProgressCalled = false
        val uiState = SettingsUiState()

        composeTestRule.setContent {
            MyApplicationTheme {
                SettingsScreen(
                    uiState = uiState,
                    onThemeChange = {},
                    onAudioAutoplayChange = {},
                    onPlaybackSpeedChange = {},
                    onDailyReminderChange = {},
                    onResetProgress = { resetProgressCalled = true },
                    onResetOnboarding = {},
                    onNavigateToAbout = {},
                    onNavigateToDisclaimer = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("settings_reset_progress").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("cancel_reset_progress_button").performClick()
        assertTrue(!resetProgressCalled)
    }

    @Test
    fun testSettingsScreen_sourcesDialog() {
        val uiState = SettingsUiState()

        composeTestRule.setContent {
            MyApplicationTheme {
                SettingsScreen(
                    uiState = uiState,
                    onThemeChange = {},
                    onAudioAutoplayChange = {},
                    onPlaybackSpeedChange = {},
                    onDailyReminderChange = {},
                    onResetProgress = {},
                    onResetOnboarding = {},
                    onNavigateToAbout = {},
                    onNavigateToDisclaimer = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("settings_sources").performScrollTo().performClick()
        composeTestRule.onNodeWithText("https://www.uscis.gov/citizenship", substring = true).assertExists()
        composeTestRule.onNodeWithText("Close").performClick()
    }
}
