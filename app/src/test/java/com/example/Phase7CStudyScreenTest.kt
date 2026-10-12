package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.example.data.local.entity.AnswerEntity
import com.example.data.local.entity.QuestionEntity
import com.example.data.model.StudyQuestionItem
import com.example.ui.study.QuestionDetailScreen
import com.example.ui.study.QuestionDetailUiState
import com.example.ui.study.StudyFilter
import com.example.ui.study.StudyScreen
import com.example.ui.study.StudyScreenUiState
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
class Phase7CStudyScreenTest {

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
        isDynamic: Boolean = false,
        isFav: Boolean = false,
        isStudied: Boolean = false,
        correctCount: Int = 0,
        incorrectCount: Int = 0
    ): StudyQuestionItem {
        val q = QuestionEntity(
            id = id,
            questionNumber = number,
            category = category,
            subcategory = subcategory,
            questionText = text,
            explanation = explanation,
            is65_20 = is65_20,
            isDynamicAnswer = isDynamic,
            answerLastVerified = if (isDynamic) "2026" else null,
            answerSourceName = if (isDynamic) "US Senate Roster" else null
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
            isStudied = isStudied,
            isMastered = correctCount >= 3 && incorrectCount == 0,
            needsPractice = incorrectCount > 0,
            correctCount = correctCount,
            incorrectCount = incorrectCount
        )
    }

    private val sampleQuestion1 = createTestQuestion(
        id = "Q001",
        number = 1,
        text = "What is the supreme law of the land?",
        category = "American Government",
        subcategory = "Principles of American Democracy",
        answers = listOf("the Constitution"),
        explanation = "The Constitution is the highest law in the United States.",
        is65_20 = false,
        isDynamic = false,
        isFav = false,
        isStudied = true,
        correctCount = 5,
        incorrectCount = 0
    )

    private val sampleQuestion2 = createTestQuestion(
        id = "Q020",
        number = 20,
        text = "Who is one of your state's U.S. Senators now?",
        category = "American Government",
        subcategory = "System of Government",
        answers = listOf("Answers will vary by state"),
        explanation = "Each state elects two senators to represent the entire state in Congress.",
        is65_20 = true,
        isDynamic = true,
        isFav = true,
        isStudied = false,
        correctCount = 1,
        incorrectCount = 2
    )

    @Test
    fun testStudyScreenHeaderFiltersAndCardsRender() {
        var filterSelected: StudyFilter? = null
        var favoriteToggledId: String? = null
        var questionClickedId: String? = null

        val state = StudyScreenUiState(
            questions = listOf(sampleQuestion1, sampleQuestion2),
            totalCount = 128,
            studiedCount = 24,
            favoriteCount = 5,
            needsPracticeCount = 3,
            selectedFilter = StudyFilter.ALL,
            isLoading = false
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                StudyScreen(
                    uiState = state,
                    onFilterSelected = { filterSelected = it },
                    onSearchQueryChanged = {},
                    onToggleFavorite = { favoriteToggledId = it },
                    onQuestionClick = { id, _, _ -> questionClickedId = id }
                )
            }
        }

        // Header and progress
        composeTestRule.onNodeWithText("Study Civics Questions").assertExists()
        composeTestRule.onNodeWithText("24 / 128").assertExists()
        composeTestRule.onNodeWithText("18%").assertExists()

        // Filter chips
        composeTestRule.onNodeWithTag("filter_chip_all").assertExists()
        composeTestRule.onNodeWithTag("filter_chip_government").assertExists()
        composeTestRule.onNodeWithTag("filter_chip_senior_65_20").assertExists()
        composeTestRule.onNodeWithTag("filter_chip_favorites").assertExists()

        // Question cards
        composeTestRule.onNodeWithTag("question_card_1").assertExists()
        composeTestRule.onNodeWithText("What is the supreme law of the land?").assertExists()
        composeTestRule.onNodeWithText("#1").assertExists()

        // Click question card
        composeTestRule.onNodeWithTag("question_card_1").performClick()
        assertEquals("Q001", questionClickedId)

        // Click favorite toggle
        composeTestRule.onNodeWithTag("fav_button_1").performClick()
        assertEquals("Q001", favoriteToggledId)

        // Click filter chip
        composeTestRule.onNodeWithTag("filter_chip_senior_65_20").performClick()
        assertEquals(StudyFilter.SENIOR_65_20, filterSelected)
    }

    @Test
    fun testStudyScreenSearchAndEmptyState() {
        var queryChanged = ""
        var cleared = false

        val emptyState = StudyScreenUiState(
            questions = emptyList(),
            totalCount = 128,
            studiedCount = 0,
            searchQuery = "UnknownQuery",
            isLoading = false
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                StudyScreen(
                    uiState = emptyState,
                    onFilterSelected = { cleared = true },
                    onSearchQueryChanged = { queryChanged = it },
                    onToggleFavorite = {},
                    onQuestionClick = { _, _, _ -> }
                )
            }
        }

        // Search field exists
        composeTestRule.onNodeWithTag("study_search_field").assertExists()
        composeTestRule.onNodeWithTag("study_clear_search_button").performClick()
        assertEquals("", queryChanged)

        // Empty state exists
        composeTestRule.onNodeWithTag("study_empty_state").assertExists()
        composeTestRule.onNodeWithText("No questions found").assertExists()

        // Clear filter button clicks
        composeTestRule.onNodeWithTag("clear_filter_button").performScrollTo().performClick()
        assertTrue(cleared)
    }

    @Test
    fun testQuestionDetailScreenRendersAllSections() {
        var previousClicked = false
        var nextClicked = false
        var favoriteToggled = false
        var markStudiedValue: Boolean? = null
        var backClicked = false

        val detailState = QuestionDetailUiState(
            isLoading = false,
            question = sampleQuestion2,
            currentIndex = 19,
            totalInContext = 128,
            contextTitle = "Question 20 of 128",
            hasPrevious = true,
            hasNext = true
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                QuestionDetailScreen(
                    uiState = detailState,
                    onBackClick = { backClicked = true },
                    onPreviousClick = { previousClicked = true },
                    onNextClick = { nextClicked = true },
                    onToggleFavorite = { favoriteToggled = true },
                    onMarkStudied = { markStudiedValue = it }
                )
            }
        }

        // Title and Back
        composeTestRule.onNodeWithText("Question 20 of 128").assertExists()
        composeTestRule.onNodeWithTag("detail_back_button").performClick()
        assertTrue(backClicked)

        // Favorite toggle
        composeTestRule.onNodeWithTag("detail_favorite_button").performClick()
        assertTrue(favoriteToggled)

        // Question text
        composeTestRule.onNodeWithText("Who is one of your state's U.S. Senators now?").assertExists()

        // Dynamic notice
        composeTestRule.onNodeWithText("Current answer may change").assertExists()

        // Official answers card
        composeTestRule.onNodeWithTag("official_answers_card").assertExists()
        composeTestRule.onNodeWithText("Answers will vary by state").assertExists()

        // Curated explanation card
        composeTestRule.onNodeWithTag("curated_explanation_card").assertExists()
        composeTestRule.onNodeWithText("Each state elects two senators to represent the entire state in Congress.").assertExists()

        // Mark studied toggle
        composeTestRule.onNodeWithTag("mark_studied_button").performClick()
        assertEquals(true, markStudiedValue)

        // Previous and Next buttons
        composeTestRule.onNodeWithTag("previous_question_button").performClick()
        assertTrue(previousClicked)

        composeTestRule.onNodeWithTag("next_question_button").performClick()
        assertTrue(nextClicked)
    }

    @Test
    fun testDarkModeRendering() {
        val detailState = QuestionDetailUiState(
            isLoading = false,
            question = sampleQuestion1,
            currentIndex = 0,
            totalInContext = 128,
            contextTitle = "Question 1 of 128",
            hasPrevious = false,
            hasNext = true
        )

        composeTestRule.setContent {
            MyApplicationTheme(darkTheme = true) {
                QuestionDetailScreen(
                    uiState = detailState,
                    onBackClick = {},
                    onPreviousClick = {},
                    onNextClick = {},
                    onToggleFavorite = {},
                    onMarkStudied = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("question_detail_screen").assertExists()
        composeTestRule.onNodeWithTag("official_answers_card").assertExists()
        composeTestRule.onNodeWithTag("curated_explanation_card").assertExists()
    }
}
