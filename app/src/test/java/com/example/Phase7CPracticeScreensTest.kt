package com.example

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.example.data.local.entity.PracticeResultEntity
import com.example.data.model.PracticeQuestionItem
import com.example.data.model.PracticeType
import com.example.data.model.ReviewAnswerItem
import com.example.data.model.UserEvaluation
import com.example.ui.practice.PracticeHistoryScreen
import com.example.ui.practice.PracticeHistoryUiState
import com.example.ui.practice.PracticeHomeUiState
import com.example.ui.practice.PracticeQuestionScreen
import com.example.ui.practice.PracticeResultScreen
import com.example.ui.practice.PracticeResultUiState
import com.example.ui.practice.PracticeScreen
import com.example.ui.practice.PracticeSessionUiState
import com.example.ui.practice.ReviewAnswersScreen
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
class Phase7CPracticeScreensTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sampleQuestion = PracticeQuestionItem(
        questionId = "Q001",
        questionNumber = 1,
        questionText = "What is the supreme law of the land?",
        category = "American Government",
        subcategory = "Principles of American Democracy",
        is65_20 = false,
        primaryAnswer = "the Constitution",
        allAnswers = listOf("the Constitution"),
        explanation = "The Constitution is the highest law in the nation.",
        explanationSourceType = "CURATED_EDUCATIONAL",
        explanationSourceLabel = "Educational explanation",
        isFavorite = false,
        isStudied = false,
        evaluation = UserEvaluation.UNANSWERED,
        isAnswerRevealed = false
    )

    private val sampleResultEntity = PracticeResultEntity(
        id = "result_1",
        practiceType = "FULL",
        title = "Full Practice Test",
        totalQuestions = 20,
        correctAnswers = 16,
        incorrectAnswers = 4,
        skippedAnswers = 0,
        percentage = 80,
        passed = true,
        startedAt = System.currentTimeMillis() - 120000,
        completedAt = System.currentTimeMillis(),
        questionResultsJson = "[]"
    )

    private val sampleReviewItems = listOf(
        ReviewAnswerItem(
            questionId = "Q001",
            questionNumber = 1,
            questionText = "What is the supreme law of the land?",
            category = "American Government",
            primaryAnswer = "the Constitution",
            allAnswers = listOf("the Constitution"),
            explanation = "Highest law",
            isCorrect = true
        ),
        ReviewAnswerItem(
            questionId = "Q002",
            questionNumber = 2,
            questionText = "What does the Constitution do?",
            category = "American Government",
            primaryAnswer = "sets up the government",
            allAnswers = listOf("sets up the government", "defines the government"),
            explanation = "Defines framework",
            isCorrect = false
        )
    )

    @Test
    fun testPracticeHomeScreenRendersAllModesAndDispatchesClicks() {
        var startPracticeType = ""
        var flashcardsClicked = false

        val state = PracticeHomeUiState(
            favoriteCount = 3,
            needsPracticeCount = 2,
            totalCount = 128,
            governmentCount = 75,
            historyCount = 43,
            civicsCount = 10,
            recentResults = listOf(sampleResultEntity),
            isLoading = false
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                PracticeScreen(
                    uiState = state,
                    onStartPractice = { type, _, _ -> startPracticeType = type },
                    onNavigateToResult = {},
                    onNavigateToHistory = {},
                    onNavigateToFlashcards = { flashcardsClicked = true },
                    onNavigateToMockInterview = {},
                    onNavigateToSpeaking = {},
                    onNavigateToSettings = {}
                )
            }
        }

        // Header and Flashcards card
        composeTestRule.onNodeWithText("Civics Flashcards").assertExists()
        composeTestRule.onNodeWithTag("practice_flashcards_card").performClick()
        assertTrue(flashcardsClicked)

        // Full practice action card click
        composeTestRule.onNodeWithTag("practice_card_full").assertExists()
        composeTestRule.onNodeWithTag("practice_card_full_button").performScrollTo().performClick()
        assertEquals("FULL", startPracticeType)

        // Quick practice action card
        composeTestRule.onNodeWithTag("practice_card_quick_button").performScrollTo().performClick()
        assertEquals("QUICK", startPracticeType)

        // Recent history item card
        composeTestRule.onNodeWithTag("practice_history_item_result_1").assertExists()
    }

    @Test
    fun testPracticeQuestionScreenRevealAndEvaluation() {
        var revealed = false
        var evaluatedGotIt: Boolean? = null
        var nextClicked = false

        val unrevealedState = PracticeSessionUiState(
            practiceType = PracticeType.FULL,
            title = "Full Practice Test",
            questions = listOf(sampleQuestion),
            currentIndex = 0,
            isLoading = false
        )

        var sessionState by mutableStateOf(unrevealedState)

        composeTestRule.setContent {
            MyApplicationTheme {
                PracticeQuestionScreen(
                    uiState = sessionState,
                    onRevealAnswer = {
                        revealed = true
                        sessionState = sessionState.copy(
                            questions = listOf(
                                sampleQuestion.copy(
                                    isAnswerRevealed = true,
                                    evaluation = UserEvaluation.GOT_IT
                                )
                            )
                        )
                    },
                    onEvaluateAnswer = { evaluatedGotIt = it },
                    onNextQuestion = { nextClicked = true },
                    onPreviousQuestion = {},
                    onToggleFavorite = {},
                    onRequestLeave = {},
                    onConfirmLeave = {},
                    onSessionCompleted = {}
                )
            }
        }

        // Question displayed
        composeTestRule.onNodeWithText("What is the supreme law of the land?").assertExists()
        composeTestRule.onNodeWithTag("practice_reveal_button").performScrollTo().performClick()
        assertTrue(revealed)

        // Answer and self evaluation card displayed
        composeTestRule.onNodeWithTag("practice_answer_card").assertExists()
        composeTestRule.onNodeWithText("the Constitution").assertExists()

        // Click I Got It button
        composeTestRule.onNodeWithTag("practice_button_got_it").performScrollTo().performClick()
        assertEquals(true, evaluatedGotIt)

        // Next button is enabled and clicks
        composeTestRule.onNodeWithTag("practice_next_button").performClick()
        assertTrue(nextClicked)
    }

    @Test
    fun testPracticeResultScreenRendersScoreAndActions() {
        var reviewAnswersClicked = false
        var practiceAgainType = ""

        val resultState = PracticeResultUiState(
            result = sampleResultEntity,
            reviewItems = sampleReviewItems,
            isLoading = false
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                PracticeResultScreen(
                    uiState = resultState,
                    onReviewAnswers = { reviewAnswersClicked = true },
                    onPracticeAgain = { practiceAgainType = it },
                    onBackToPractice = {},
                    onReviewMistakes = {}
                )
            }
        }

        // Score card
        composeTestRule.onNodeWithTag("result_score_card").assertExists()
        composeTestRule.onNodeWithTag("result_score_text").assertExists()
        composeTestRule.onNodeWithText("16 / 20").assertExists()
        composeTestRule.onNodeWithText("80%").assertExists()
        composeTestRule.onNodeWithTag("result_pass_badge").assertExists()

        // Review Answers Button
        composeTestRule.onNodeWithTag("result_review_answers_button").performScrollTo().performClick()
        assertTrue(reviewAnswersClicked)

        // Practice Again Button
        composeTestRule.onNodeWithTag("result_practice_again_button").performScrollTo().performClick()
        assertEquals("FULL", practiceAgainType)
    }

    @Test
    fun testReviewAnswersScreenFiltering() {
        var backClicked = false
        val resultState = PracticeResultUiState(
            result = sampleResultEntity,
            reviewItems = sampleReviewItems,
            isLoading = false
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                ReviewAnswersScreen(
                    uiState = resultState,
                    onBackClick = { backClicked = true }
                )
            }
        }

        composeTestRule.onNodeWithTag("review_filter_all").assertExists()
        composeTestRule.onNodeWithTag("review_filter_correct").assertExists()
        composeTestRule.onNodeWithTag("review_filter_mistakes").assertExists()

        // Check review items
        composeTestRule.onNodeWithTag("review_item_Q001").assertExists()
        composeTestRule.onNodeWithTag("review_item_Q002").assertExists()

        // Click mistakes filter
        composeTestRule.onNodeWithTag("review_filter_mistakes").performClick()

        // Back button
        composeTestRule.onNodeWithTag("review_back_button").performClick()
        assertTrue(backClicked)
    }

    @Test
    fun testPracticeHistoryScreenWithResults() {
        var selectedResultId = ""

        val historyState = PracticeHistoryUiState(
            results = listOf(sampleResultEntity),
            isLoading = false
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                PracticeHistoryScreen(
                    uiState = historyState,
                    onResultClick = { selectedResultId = it },
                    onClearHistory = {},
                    onBackClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Tests Taken").assertExists()
        composeTestRule.onNodeWithText("Passed").assertExists()
        composeTestRule.onNodeWithTag("practice_history_item_result_1").performClick()
        assertEquals("result_1", selectedResultId)
    }

    @Test
    fun testPracticeHistoryScreenEmpty() {
        var backClicked = false

        val emptyState = PracticeHistoryUiState(
            results = emptyList(),
            isLoading = false
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                PracticeHistoryScreen(
                    uiState = emptyState,
                    onResultClick = {},
                    onClearHistory = {},
                    onBackClick = { backClicked = true }
                )
            }
        }

        composeTestRule.onNodeWithText("No Practice History").assertExists()
        composeTestRule.onNodeWithTag("history_empty_back_button").performClick()
        assertTrue(backClicked)
    }

    @Test
    fun testDarkModeRenderingAcrossPracticeScreens() {
        val resultState = PracticeResultUiState(
            result = sampleResultEntity,
            reviewItems = sampleReviewItems,
            isLoading = false
        )

        composeTestRule.setContent {
            MyApplicationTheme(darkTheme = true) {
                PracticeResultScreen(
                    uiState = resultState,
                    onReviewAnswers = {},
                    onPracticeAgain = {},
                    onBackToPractice = {},
                    onReviewMistakes = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("practice_result_screen").assertExists()
        composeTestRule.onNodeWithTag("result_score_card").assertExists()
    }
}
