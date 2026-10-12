package com.example

import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.example.data.local.entity.AnswerEntity
import com.example.data.local.entity.QuestionEntity
import com.example.data.model.StudyQuestionItem
import com.example.ui.flashcards.FlashcardFilter
import com.example.ui.flashcards.FlashcardsScreen
import com.example.ui.flashcards.FlashcardsUiState
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
class Phase7CFlashcardsUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun createTestQuestion(
        id: String,
        number: Int,
        text: String,
        category: String,
        subcategory: String,
        answers: List<String>,
        explanation: String,
        is65_20: Boolean = false,
        isFav: Boolean = false,
        isStudied: Boolean = false
    ): StudyQuestionItem {
        val q = QuestionEntity(
            id = id,
            questionNumber = number,
            category = category,
            subcategory = subcategory,
            questionText = text,
            explanation = explanation,
            is65_20 = is65_20,
            isDynamicAnswer = false
        )
        val answerEntities = answers.mapIndexed { idx, ans ->
            AnswerEntity(
                id = (number * 10 + idx).toLong(),
                questionId = id,
                answerText = ans,
                isPrimary = idx == 0,
                orderIndex = idx
            )
        }
        return StudyQuestionItem(
            question = q,
            answers = answerEntities,
            isFavorite = isFav,
            isStudied = isStudied
        )
    }

    private val sampleQuestion1 = createTestQuestion(
        id = "Q001",
        number = 1,
        text = "What is the supreme law of the land?",
        category = "American Government",
        subcategory = "Principles of American Democracy",
        answers = listOf("the Constitution"),
        explanation = "The Constitution is the supreme law of the United States.",
        is65_20 = false,
        isFav = false,
        isStudied = false
    )

    private val sampleQuestion2Senior = createTestQuestion(
        id = "Q013",
        number = 13,
        text = "Name one branch or part of the government.",
        category = "American Government",
        subcategory = "System of Government",
        answers = listOf("Congress", "legislative", "President", "executive", "the courts", "judicial"),
        explanation = "The government is divided into three branches.",
        is65_20 = true,
        isFav = true,
        isStudied = true
    )

    @Test
    fun testFlashcardFrontDisplaysQuestionDetailsAndControls() {
        var flipClicked = false
        var hearQuestionClicked = false
        var favoriteClicked = false
        var studiedClicked = false

        val state = FlashcardsUiState(
            allQuestions = listOf(sampleQuestion1, sampleQuestion2Senior),
            displayQuestions = listOf(sampleQuestion1, sampleQuestion2Senior),
            currentIndex = 0,
            isFlipped = false,
            isLoading = false
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                FlashcardsScreen(
                    uiState = state,
                    onFilterSelected = {},
                    onFlipCard = { flipClicked = true },
                    onNextCard = {},
                    onPreviousCard = {},
                    onToggleShuffle = {},
                    onToggleFavorite = { favoriteClicked = true },
                    onToggleStudied = { studiedClicked = true },
                    onHearQuestion = { hearQuestionClicked = true },
                    onHearAnswer = {},
                    onBackClick = {}
                )
            }
        }

        // Header and counter
        composeTestRule.onNodeWithText("Civics Flashcards").assertExists()
        composeTestRule.onNodeWithText("1 / 2").assertExists()

        // Question details on Front
        composeTestRule.onNodeWithTag("flashcard_front", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithText("QUESTION #1", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithText("What is the supreme law of the land?", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithText("Tap card to flip", useUnmergedTree = true).assertExists()

        // Action controls
        composeTestRule.onNodeWithText("Mark Studied", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithTag("flashcard_hear_question_button", useUnmergedTree = true).assertExists()

        // Test flip callback on card container click
        composeTestRule.onNodeWithTag("flashcard_container").performClick()
        assertTrue(flipClicked)

        // Test hear question
        composeTestRule.onNodeWithTag("flashcard_hear_question_button", useUnmergedTree = true).performClick()
        assertTrue(hearQuestionClicked)

        // Previous button disabled at index 0
        composeTestRule.onNodeWithTag("flashcards_prev_button").assertIsNotEnabled()

        // Next button enabled at index 0 of 2
        composeTestRule.onNodeWithTag("flashcards_next_button").assertIsEnabled()
    }

    @Test
    fun testFlashcardBackDisplaysAcceptedAnswersAndExplanation() {
        var flipBackClicked = false
        var hearAnswerClicked = false

        val state = FlashcardsUiState(
            allQuestions = listOf(sampleQuestion2Senior),
            displayQuestions = listOf(sampleQuestion2Senior),
            currentIndex = 0,
            isFlipped = true,
            isLoading = false
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                FlashcardsScreen(
                    uiState = state,
                    onFilterSelected = {},
                    onFlipCard = { flipBackClicked = true },
                    onNextCard = {},
                    onPreviousCard = {},
                    onToggleShuffle = {},
                    onToggleFavorite = {},
                    onToggleStudied = {},
                    onHearQuestion = {},
                    onHearAnswer = { hearAnswerClicked = true },
                    onBackClick = {}
                )
            }
        }

        // Advance clock for 3D flip animation to finish
        composeTestRule.mainClock.advanceTimeBy(500)

        // Answer card exists
        composeTestRule.onNodeWithTag("flashcard_back", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithText("Official Answer • #13", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithText("USCIS ACCEPTED ANSWERS", useUnmergedTree = true).assertExists()

        // Multiple bullet items exist
        composeTestRule.onNodeWithText("Congress", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithText("legislative", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithText("President", useUnmergedTree = true).assertExists()

        // Educational explanation container exists
        composeTestRule.onNodeWithText("Educational Explanation", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithText("The government is divided into three branches.", useUnmergedTree = true).assertExists()

        // Studied state is marked
        composeTestRule.onNodeWithText("Studied ✓", useUnmergedTree = true).assertExists()

        // Hear answer button
        composeTestRule.onNodeWithTag("flashcard_hear_answer_button", useUnmergedTree = true).assertExists().performClick()
        assertTrue(hearAnswerClicked)

        // Flip back button
        composeTestRule.onNodeWithTag("flashcard_flip_back_button", useUnmergedTree = true).assertExists().performScrollTo().performClick()
        assertTrue(flipBackClicked)
    }

    @Test
    fun testSenior6520ModeDisplaysBadgeAndFilter() {
        val state = FlashcardsUiState(
            allQuestions = listOf(sampleQuestion2Senior),
            displayQuestions = listOf(sampleQuestion2Senior),
            currentIndex = 0,
            selectedFilter = FlashcardFilter.SENIOR_65_20,
            isLoading = false
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                FlashcardsScreen(
                    uiState = state,
                    onFilterSelected = {},
                    onFlipCard = {},
                    onNextCard = {},
                    onPreviousCard = {},
                    onToggleShuffle = {},
                    onToggleFavorite = {},
                    onToggleStudied = {},
                    onHearQuestion = {},
                    onHearAnswer = {},
                    onBackClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("65/20 Special Consideration").assertExists()
        composeTestRule.onNodeWithText("1 / 1").assertExists()
        composeTestRule.onNodeWithText("65/20").assertExists()
    }

    @Test
    fun testShuffleButtonToggle() {
        var shuffleToggled = false
        val state = FlashcardsUiState(
            allQuestions = listOf(sampleQuestion1),
            displayQuestions = listOf(sampleQuestion1),
            currentIndex = 0,
            isShuffled = true,
            isLoading = false
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                FlashcardsScreen(
                    uiState = state,
                    onFilterSelected = {},
                    onFlipCard = {},
                    onNextCard = {},
                    onPreviousCard = {},
                    onToggleShuffle = { shuffleToggled = true },
                    onToggleFavorite = {},
                    onToggleStudied = {},
                    onHearQuestion = {},
                    onHearAnswer = {},
                    onBackClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Shuffle On").assertExists()
        composeTestRule.onNodeWithTag("flashcards_shuffle_button").performClick()
        assertTrue(shuffleToggled)
    }

    @Test
    fun testFilterSelectionCallback() {
        var selectedFilter: FlashcardFilter? = null
        val state = FlashcardsUiState(
            allQuestions = listOf(sampleQuestion1),
            displayQuestions = listOf(sampleQuestion1),
            currentIndex = 0,
            isLoading = false
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                FlashcardsScreen(
                    uiState = state,
                    onFilterSelected = { selectedFilter = it },
                    onFlipCard = {},
                    onNextCard = {},
                    onPreviousCard = {},
                    onToggleShuffle = {},
                    onToggleFavorite = {},
                    onToggleStudied = {},
                    onHearQuestion = {},
                    onHearAnswer = {},
                    onBackClick = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("flashcard_filter_GOVERNMENT").performClick()
        assertEquals(FlashcardFilter.GOVERNMENT, selectedFilter)
    }
}
