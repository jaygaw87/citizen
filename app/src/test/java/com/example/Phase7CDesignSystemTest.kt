package com.example

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.ui.components.AppCategoryChip
import com.example.ui.components.AppConfirmationDialog
import com.example.ui.components.AppDestructiveButton
import com.example.ui.components.AppDestructiveConfirmationDialog
import com.example.ui.components.AppEligibilityChip
import com.example.ui.components.AppFeatureCard
import com.example.ui.components.AppFilterChip
import com.example.ui.components.AppLinearProgressBar
import com.example.ui.components.AppPrimaryButton
import com.example.ui.components.AppProgressCounter
import com.example.ui.components.AppProgressRing
import com.example.ui.components.AppQuestionCard
import com.example.ui.components.AppSecondaryButton
import com.example.ui.components.AppStatusCard
import com.example.ui.components.AppStatusChip
import com.example.ui.components.AppTertiaryButton
import com.example.ui.components.ProgressStatus
import com.example.ui.components.QuestionStudyStatus
import com.example.ui.components.StatusType
import com.example.ui.theme.AppAnimation
import com.example.ui.theme.AppElevation
import com.example.ui.theme.AppShapes
import com.example.ui.theme.AppSpacing
import com.example.ui.theme.DarkSemanticColors
import com.example.ui.theme.LightSemanticColors
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.Typography
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Phase7CDesignSystemTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testSemanticColorsTokens() {
        // Civic Primary Navy
        assertEquals(androidx.compose.ui.graphics.Color(0xFF0A192F), com.example.ui.theme.Navy900)
        // Civic palette assertions
        assertNotNull(LightSemanticColors.success)
        assertNotNull(LightSemanticColors.warning)
        assertNotNull(LightSemanticColors.error)
        assertNotNull(LightSemanticColors.info)
        assertNotNull(LightSemanticColors.govCategory)
        assertNotNull(LightSemanticColors.historyCategory)
        assertNotNull(LightSemanticColors.civicsCategory)
        assertNotNull(LightSemanticColors.seniorExemption)

        assertNotNull(DarkSemanticColors.success)
        assertNotNull(DarkSemanticColors.warning)
        assertNotNull(DarkSemanticColors.error)
        assertNotNull(DarkSemanticColors.info)
        assertNotNull(DarkSemanticColors.govCategory)
        assertNotNull(DarkSemanticColors.historyCategory)
        assertNotNull(DarkSemanticColors.civicsCategory)
        assertNotNull(DarkSemanticColors.seniorExemption)
    }

    @Test
    fun testSpacingAndElevationSystem() {
        val spacing = AppSpacing()
        assertEquals(16f, spacing.screenHorizontalPadding.value)
        assertEquals(52f, spacing.buttonHeight.value)
        assertEquals(48f, spacing.minTouchTarget.value)
        assertTrue(spacing.xs.value < spacing.sm.value)
        assertTrue(spacing.sm.value < spacing.md.value)
        assertTrue(spacing.md.value < spacing.lg.value)

        val elevation = AppElevation()
        assertEquals(0f, elevation.none.value)
        assertEquals(1f, elevation.subtle.value)
        assertEquals(2f, elevation.low.value)
        assertEquals(4f, elevation.medium.value)
    }

    @Test
    fun testShapesSystem() {
        assertNotNull(AppShapes.extraSmall)
        assertNotNull(AppShapes.small)
        assertNotNull(AppShapes.medium)
        assertNotNull(AppShapes.large)
        assertNotNull(AppShapes.extraLarge)
    }

    @Test
    fun testTypographySystem() {
        assertNotNull(Typography.displayLarge)
        assertNotNull(Typography.headlineMedium)
        assertNotNull(Typography.titleMedium)
        assertNotNull(Typography.bodyLarge)
        assertNotNull(Typography.labelMedium)
    }

    @Test
    fun testAnimationConstants() {
        assertEquals(150, AppAnimation.DurationFast)
        assertEquals(250, AppAnimation.DurationNormal)
        assertEquals(350, AppAnimation.DurationSlow)
        assertEquals(600, AppAnimation.DurationProgress)
        assertNotNull(AppAnimation.screenEnter())
        assertNotNull(AppAnimation.screenExit())
    }

    @Test
    fun testButtonSystemRenderingAndClicks() {
        var primaryClicked = false
        var secondaryClicked = false
        var tertiaryClicked = false
        var destructiveClicked = false

        composeTestRule.setContent {
            MyApplicationTheme {
                Column {
                    AppPrimaryButton(
                        text = "Continue Studying",
                        onClick = { primaryClicked = true },
                        testTag = "test_primary"
                    )
                    AppSecondaryButton(
                        text = "Review Answers",
                        onClick = { secondaryClicked = true },
                        testTag = "test_secondary"
                    )
                    AppTertiaryButton(
                        text = "Dismiss",
                        onClick = { tertiaryClicked = true },
                        testTag = "test_tertiary"
                    )
                    AppDestructiveButton(
                        text = "Reset Progress",
                        onClick = { destructiveClicked = true },
                        testTag = "test_destructive"
                    )
                }
            }
        }

        composeTestRule.onNodeWithTag("test_primary").performClick()
        assertTrue(primaryClicked)

        composeTestRule.onNodeWithTag("test_secondary").performClick()
        assertTrue(secondaryClicked)

        composeTestRule.onNodeWithTag("test_tertiary").performClick()
        assertTrue(tertiaryClicked)

        composeTestRule.onNodeWithTag("test_destructive").performClick()
        assertTrue(destructiveClicked)
    }

    @Test
    fun testChipSystemRendering() {
        var filterClicked = false

        composeTestRule.setContent {
            MyApplicationTheme {
                Column {
                    AppCategoryChip(category = "American Government", testTag = "gov_chip")
                    AppCategoryChip(category = "American History", testTag = "hist_chip")
                    AppEligibilityChip(testTag = "senior_chip")
                    AppStatusChip(status = QuestionStudyStatus.Mastered, testTag = "mastered_chip")
                    AppFilterChip(
                        text = "All Questions",
                        selected = true,
                        onClick = { filterClicked = true },
                        testTag = "filter_chip"
                    )
                }
            }
        }

        composeTestRule.onNodeWithTag("gov_chip").assertExists()
        composeTestRule.onNodeWithTag("hist_chip").assertExists()
        composeTestRule.onNodeWithTag("senior_chip").assertExists()
        composeTestRule.onNodeWithTag("mastered_chip").assertExists()
        composeTestRule.onNodeWithTag("filter_chip").performClick()
        assertTrue(filterClicked)
    }

    @Test
    fun testCardSystemRendering() {
        var featureClicked = false
        var questionClicked = false

        composeTestRule.setContent {
            MyApplicationTheme {
                Column {
                    AppFeatureCard(
                        title = "Flashcards",
                        description = "Master all 128 questions",
                        icon = androidx.compose.material.icons.Icons.Filled.Home,
                        onClick = { featureClicked = true },
                        testTag = "feature_card_test"
                    )
                    AppQuestionCard(
                        questionNumber = 1,
                        questionText = "What is the supreme law of the land?",
                        onClick = { questionClicked = true },
                        testTag = "question_card_test"
                    )
                    AppStatusCard(
                        message = "All progress saved",
                        type = StatusType.Success
                    )
                }
            }
        }

        composeTestRule.onNodeWithTag("feature_card_test").performClick()
        assertTrue(featureClicked)

        composeTestRule.onNodeWithTag("question_card_test").performClick()
        assertTrue(questionClicked)

        composeTestRule.onNodeWithText("What is the supreme law of the land?").assertExists()
        composeTestRule.onNodeWithText("All progress saved").assertExists()
    }

    @Test
    fun testProgressIndicatorsRendering() {
        composeTestRule.setContent {
            MyApplicationTheme {
                AppLinearProgressBar(progress = 0.75f, status = ProgressStatus.Normal)
                AppProgressRing(progress = 0.5f, percentageText = "50%")
                AppProgressCounter(current = 10, total = 20, label = "Questions")
            }
        }

        composeTestRule.onNodeWithText("50%").assertExists()
        composeTestRule.onNodeWithText("10").assertExists()
        composeTestRule.onNodeWithText(" / 20").assertExists()
    }

    @Test
    fun testDialogSystemRendering() {
        var dialogConfirmed = false

        composeTestRule.setContent {
            MyApplicationTheme {
                AppConfirmationDialog(
                    onDismissRequest = {},
                    title = "Confirm Action",
                    message = "Are you sure you want to proceed?",
                    onConfirm = { dialogConfirmed = true },
                    testTag = "confirm_dialog"
                )
            }
        }

        composeTestRule.onNodeWithTag("confirm_dialog").assertExists()
        composeTestRule.onNodeWithText("Confirm Action").assertExists()
        composeTestRule.onNodeWithText("Confirm").performClick()
        assertTrue(dialogConfirmed)
    }

    @Test
    fun testDestructiveDialogSystemRendering() {
        var resetConfirmed = false

        composeTestRule.setContent {
            MyApplicationTheme {
                AppDestructiveConfirmationDialog(
                    onDismissRequest = {},
                    title = "Reset Learning Progress",
                    message = "This will erase all question history.",
                    onConfirm = { resetConfirmed = true },
                    testTag = "destructive_dialog"
                )
            }
        }

        composeTestRule.onNodeWithTag("destructive_dialog").assertExists()
        composeTestRule.onNodeWithText("Reset Learning Progress").assertExists()
        composeTestRule.onNodeWithText("Reset Everything").performClick()
        assertTrue(resetConfirmed)
    }
}
