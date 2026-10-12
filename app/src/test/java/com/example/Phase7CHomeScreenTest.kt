package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.example.data.model.CategoryOverviewItem
import com.example.data.model.HomeUiState
import com.example.ui.home.HomeScreen
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
class Phase7CHomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sampleUiState = HomeUiState(
        isReady = true,
        studiedCount = 42,
        totalCount = 128,
        favoriteCount = 7,
        needsPracticeCount = 3,
        senior6520Count = 20,
        senior6520Studied = 8,
        continueQuestionText = "Continue Question 14",
        continueQuestionNumber = 14,
        isFirstSession = false,
        testsCompleted = 4,
        averageScore = 85,
        studyStreakDays = 5,
        dailyStudyGoal = 15,
        todayQuestionsCount = 10,
        recommendedTitle = "Review your recent mistakes",
        recommendedSubtitle = "3 questions need practice to achieve mastery.",
        recommendedActionType = "MISTAKES",
        categories = listOf(
            CategoryOverviewItem(
                name = "American Government",
                totalCount = 72,
                studiedCount = 25,
                description = "Principles, system & rights"
            ),
            CategoryOverviewItem(
                name = "American History",
                totalCount = 40,
                studiedCount = 12,
                description = "Colonial, 1800s & modern"
            ),
            CategoryOverviewItem(
                name = "Integrated Civics",
                totalCount = 16,
                studiedCount = 5,
                description = "Geography, symbols & holidays"
            )
        ),
        vocabularyLearnedCount = 35,
        vocabularyTotalCount = 115
    )

    @Test
    fun testHomeScreenHeaderAndCardsRender() {
        composeTestRule.setContent {
            MyApplicationTheme {
                HomeScreen(
                    uiState = sampleUiState,
                    onNavigateToSettings = {},
                    onNavigateToQuestions = {},
                    onNavigateToPracticeTest = {},
                    onNavigateToFlashcards = {},
                    onNavigateToMockInterview = {},
                    onNavigateToAudioPractice = {},
                    onNavigateToFavorites = {},
                    onNavigateToMistakes = {},
                    onNavigateToSpecialConsideration = {},
                    onNavigateToVocabulary = {}
                )
            }
        }

        // Header assertions
        composeTestRule.onNodeWithText("U.S. Citizenship Test").assertExists()
        composeTestRule.onNodeWithText("Prepare. Practice. Master your civics test.").assertExists()

        // Daily Goal and Streak
        composeTestRule.onNodeWithText("Daily Goal: 10 / 15 questions").assertExists()
        composeTestRule.onNodeWithText("5 days").assertExists()

        // Level 1 Continue Card
        composeTestRule.onNodeWithTag("continue_studying_card").assertExists()
        composeTestRule.onNodeWithText("Continue Question 14").assertExists()

        // Level 2 Progress Card
        composeTestRule.onNodeWithTag("home_progress_card").assertExists()
        composeTestRule.onNodeWithText("32%").assertExists()

        // Metrics row
        composeTestRule.onNodeWithText("Need Work").assertExists()
        composeTestRule.onNodeWithText("Saved").assertExists()

        // 65/20 card
        composeTestRule.onNodeWithTag("feature_65_20").assertExists()
        composeTestRule.onNodeWithText("65/20 Special Consideration").assertExists()
        composeTestRule.onNodeWithText("20 Questions • 8 / 20 studied").assertExists()

        // Category breakdown
        composeTestRule.onNodeWithText("Civics Topics").assertExists()
        composeTestRule.onNodeWithText("American Government").assertExists()
        composeTestRule.onNodeWithText("American History").assertExists()
        composeTestRule.onNodeWithText("Integrated Civics").assertExists()
    }

    @Test
    fun testContinueStudyingActionDispatched() {
        var continueClicked = false

        composeTestRule.setContent {
            MyApplicationTheme {
                HomeScreen(
                    uiState = sampleUiState,
                    onNavigateToSettings = {},
                    onNavigateToQuestions = {},
                    onNavigateToPracticeTest = {},
                    onNavigateToFlashcards = {},
                    onNavigateToMockInterview = {},
                    onNavigateToAudioPractice = {},
                    onNavigateToFavorites = {},
                    onNavigateToMistakes = {},
                    onNavigateToSpecialConsideration = {},
                    onNavigateToVocabulary = {},
                    onNavigateToContinue = { continueClicked = true }
                )
            }
        }

        composeTestRule.onNodeWithTag("continue_start_button").performClick()
        assertTrue(continueClicked)
    }

    @Test
    fun testQuickActionsAndExploreCallbacksDispatched() {
        var questionsClicked = false
        var practiceClickedType: String? = null
        var speakingClicked = false
        var flashcardsClicked = false
        var favoritesClicked = false
        var progressClicked = false
        var audioClicked = false
        var quickReviewClicked = false
        var seniorClicked = false
        var vocabClicked = false

        composeTestRule.setContent {
            MyApplicationTheme {
                HomeScreen(
                    uiState = sampleUiState,
                    onNavigateToSettings = {},
                    onNavigateToQuestions = { questionsClicked = true },
                    onNavigateToPracticeTest = { practiceClickedType = it },
                    onNavigateToFlashcards = { flashcardsClicked = true },
                    onNavigateToMockInterview = { speakingClicked = true },
                    onNavigateToSpeakingPractice = { speakingClicked = true },
                    onNavigateToAudioPractice = { audioClicked = true },
                    onNavigateToFavorites = { favoritesClicked = true },
                    onNavigateToProgress = { progressClicked = true },
                    onNavigateToMistakes = {},
                    onNavigateToQuickReview = { quickReviewClicked = true },
                    onNavigateToSpecialConsideration = { seniorClicked = true },
                    onNavigateToVocabulary = { vocabClicked = true }
                )
            }
        }

        // Test Quick Actions
        composeTestRule.onNodeWithTag("action_study_questions").performScrollTo().performClick()
        assertTrue(questionsClicked)

        composeTestRule.onNodeWithTag("action_practice_test").performScrollTo().performClick()
        assertEquals("full", practiceClickedType)

        composeTestRule.onNodeWithTag("action_speaking_practice").performScrollTo().performClick()
        assertTrue(speakingClicked)

        composeTestRule.onNodeWithTag("action_flashcards").performScrollTo().performClick()
        assertTrue(flashcardsClicked)

        composeTestRule.onNodeWithTag("action_favorites").performScrollTo().performClick()
        assertTrue(favoritesClicked)

        composeTestRule.onNodeWithTag("action_progress").performScrollTo().performClick()
        assertTrue(progressClicked)

        // Test 65/20 button
        composeTestRule.onNodeWithTag("senior_home_continue_button").performScrollTo().performClick()
        assertTrue(seniorClicked)

        // Test Explore features
        composeTestRule.onNodeWithTag("feature_vocabulary").performScrollTo().performClick()
        assertTrue(vocabClicked)

        composeTestRule.onNodeWithTag("feature_audio_practice").performScrollTo().performClick()
        assertTrue(audioClicked)

        composeTestRule.onNodeWithTag("feature_quick_review").performScrollTo().performClick()
        assertTrue(quickReviewClicked)
    }

    @Test
    fun testDarkModeRendering() {
        composeTestRule.setContent {
            MyApplicationTheme(darkTheme = true) {
                HomeScreen(
                    uiState = sampleUiState,
                    onNavigateToSettings = {},
                    onNavigateToQuestions = {},
                    onNavigateToPracticeTest = {},
                    onNavigateToFlashcards = {},
                    onNavigateToMockInterview = {},
                    onNavigateToAudioPractice = {},
                    onNavigateToFavorites = {},
                    onNavigateToMistakes = {},
                    onNavigateToSpecialConsideration = {},
                    onNavigateToVocabulary = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("home_screen").assertExists()
        composeTestRule.onNodeWithTag("continue_studying_card").assertExists()
        composeTestRule.onNodeWithTag("home_progress_card").assertExists()
        composeTestRule.onNodeWithTag("feature_65_20").assertExists()
    }
}
