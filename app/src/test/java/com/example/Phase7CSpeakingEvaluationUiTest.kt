package com.example

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.example.data.model.PracticeQuestionItem
import com.example.data.model.UserEvaluation
import com.example.speech.AnswerMatcher
import com.example.speech.MatchStatus
import com.example.speech.SpeechMatchResult
import com.example.speech.SpeechRecognitionState
import com.example.ui.speaking.SpeakingMicZone
import com.example.ui.speaking.SpeakingPracticeScreen
import com.example.ui.speaking.SpeakingPracticeUiState
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
class Phase7CSpeakingEvaluationUiTest {

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
        allAnswers = listOf("the Constitution", "Constitution"),
        explanation = "The Constitution is the supreme law of the United States.",
        explanationSourceType = "CURATED_EDUCATIONAL",
        explanationSourceLabel = "Educational explanation",
        isFavorite = false,
        isStudied = false,
        evaluation = UserEvaluation.UNANSWERED,
        isAnswerRevealed = false
    )

    private val sampleSeniorQuestion = PracticeQuestionItem(
        questionId = "Q013",
        questionNumber = 13,
        questionText = "Name one branch or part of the government.",
        category = "American Government",
        subcategory = "System of Government",
        is65_20 = true,
        primaryAnswer = "Congress",
        allAnswers = listOf("Congress", "legislative", "President", "executive", "the courts", "judicial"),
        explanation = "The Constitution creates three separate branches of government.",
        explanationSourceType = "CURATED_EDUCATIONAL",
        explanationSourceLabel = "Educational explanation",
        isFavorite = false,
        isStudied = false,
        evaluation = UserEvaluation.UNANSWERED,
        isAnswerRevealed = false
    )

    // 1. Speaking screen renders
    @Test
    fun testSpeakingScreenRenders() {
        val state = SpeakingPracticeUiState(
            isLoading = false,
            questions = listOf(sampleQuestion),
            currentIndex = 0
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                SpeakingPracticeScreen(
                    uiState = state,
                    onStartListening = {},
                    onStopListening = {},
                    onHearQuestion = {},
                    onHearAnswer = {},
                    onRevealAnswer = {},
                    onManualOverride = {},
                    onTryAgain = {},
                    onNextQuestion = {},
                    onToggleFavorite = {},
                    onShowLeaveConfirmation = {},
                    onNavigateBack = {},
                    onViewResults = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("speaking_practice_screen").assertExists()
        composeTestRule.onNodeWithTag("speaking_question_card").assertExists()
        composeTestRule.onNodeWithTag("speaking_progress_bar").assertExists()
        composeTestRule.onNodeWithTag("speaking_mic_zone").assertExists()
        composeTestRule.onNodeWithText("What is the supreme law of the land?").assertExists()
    }

    // 2. Microphone idle state
    @Test
    fun testMicrophoneIdleState() {
        composeTestRule.setContent {
            MyApplicationTheme {
                SpeakingMicZone(
                    speechState = SpeechRecognitionState.Idle,
                    lastSpokenText = null,
                    matchResult = null,
                    currentEvaluation = UserEvaluation.UNANSWERED,
                    permissionDenied = false,
                    onMicTap = {},
                    onStopListening = {},
                    onManualOverride = {},
                    onTryAgain = {},
                    primaryAnswer = sampleQuestion.primaryAnswer,
                    allAnswers = sampleQuestion.allAnswers
                )
            }
        }

        composeTestRule.onNodeWithText("Tap the microphone to answer").assertExists()
        composeTestRule.onNodeWithText("Speak your answer clearly into the microphone").assertExists()
        composeTestRule.onNodeWithContentDescription("Microphone ready. Tap to speak.").assertExists()
        composeTestRule.onNodeWithTag("speaking_mic_button").assertExists().assertHasClickAction()
    }

    // 3. Starting microphone state
    @Test
    fun testStartingMicrophoneState() {
        composeTestRule.setContent {
            MyApplicationTheme {
                SpeakingMicZone(
                    speechState = SpeechRecognitionState.Starting,
                    lastSpokenText = null,
                    matchResult = null,
                    currentEvaluation = UserEvaluation.UNANSWERED,
                    permissionDenied = false,
                    onMicTap = {},
                    onStopListening = {},
                    onManualOverride = {},
                    onTryAgain = {},
                    primaryAnswer = sampleQuestion.primaryAnswer,
                    allAnswers = sampleQuestion.allAnswers
                )
            }
        }

        composeTestRule.onNodeWithText("Starting microphone...").assertExists()
        composeTestRule.onNodeWithContentDescription("Starting microphone.").assertExists()
    }

    // 4. Listening state
    @Test
    fun testListeningState() {
        composeTestRule.setContent {
            MyApplicationTheme {
                SpeakingMicZone(
                    speechState = SpeechRecognitionState.Listening(partialText = "", rmsLevel = 0.5f),
                    lastSpokenText = null,
                    matchResult = null,
                    currentEvaluation = UserEvaluation.UNANSWERED,
                    permissionDenied = false,
                    onMicTap = {},
                    onStopListening = {},
                    onManualOverride = {},
                    onTryAgain = {},
                    primaryAnswer = sampleQuestion.primaryAnswer,
                    allAnswers = sampleQuestion.allAnswers
                )
            }
        }

        composeTestRule.onNodeWithTag("speaking_listening_status").assertExists()
        composeTestRule.onNodeWithText("Listening").assertExists()
        composeTestRule.onNodeWithText("Speak now").assertExists()
        composeTestRule.onNodeWithTag("speaking_listening_timer").assertExists()
        composeTestRule.onNodeWithTag("speaking_stop_button").assertExists()
        composeTestRule.onNodeWithContentDescription("Listening for your answer.").assertExists()
    }

    // 5. Voice-reactive animation state
    @Test
    fun testVoiceReactiveAnimationState() {
        composeTestRule.setContent {
            MyApplicationTheme {
                SpeakingMicZone(
                    speechState = SpeechRecognitionState.Listening(partialText = "", rmsLevel = 0.75f),
                    lastSpokenText = null,
                    matchResult = null,
                    currentEvaluation = UserEvaluation.UNANSWERED,
                    permissionDenied = false,
                    onMicTap = {},
                    onStopListening = {},
                    onManualOverride = {},
                    onTryAgain = {},
                    primaryAnswer = sampleQuestion.primaryAnswer,
                    allAnswers = sampleQuestion.allAnswers
                )
            }
        }

        composeTestRule.onNodeWithTag("speaking_voice_reactive_canvas").assertExists()
    }

    // 6. Partial transcription
    @Test
    fun testPartialTranscription() {
        composeTestRule.setContent {
            MyApplicationTheme {
                SpeakingMicZone(
                    speechState = SpeechRecognitionState.Listening(partialText = "the const", rmsLevel = 0.6f),
                    lastSpokenText = null,
                    matchResult = null,
                    currentEvaluation = UserEvaluation.UNANSWERED,
                    permissionDenied = false,
                    onMicTap = {},
                    onStopListening = {},
                    onManualOverride = {},
                    onTryAgain = {},
                    primaryAnswer = sampleQuestion.primaryAnswer,
                    allAnswers = sampleQuestion.allAnswers
                )
            }
        }

        composeTestRule.onNodeWithTag("speaking_live_transcript").assertExists()
        composeTestRule.onNodeWithText("\"the const\"").assertExists()
    }

    // 7. Processing state
    @Test
    fun testProcessingState() {
        composeTestRule.setContent {
            MyApplicationTheme {
                SpeakingMicZone(
                    speechState = SpeechRecognitionState.Processing,
                    lastSpokenText = null,
                    matchResult = null,
                    currentEvaluation = UserEvaluation.UNANSWERED,
                    permissionDenied = false,
                    onMicTap = {},
                    onStopListening = {},
                    onManualOverride = {},
                    onTryAgain = {},
                    primaryAnswer = sampleQuestion.primaryAnswer,
                    allAnswers = sampleQuestion.allAnswers
                )
            }
        }

        composeTestRule.onNodeWithText("Processing...").assertExists()
        composeTestRule.onNodeWithContentDescription("Processing your spoken answer.").assertExists()
    }

    // 8. Correct evaluation state
    @Test
    fun testCorrectEvaluationState() {
        val transcript = "The Constitution"
        val matchResult = SpeechMatchResult(
            status = MatchStatus.CORRECT,
            userSpokenText = transcript,
            matchedAnswer = "the Constitution",
            explanation = "Your answer matches an accepted USCIS answer."
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                SpeakingMicZone(
                    speechState = SpeechRecognitionState.Success(transcript),
                    lastSpokenText = transcript,
                    matchResult = matchResult,
                    currentEvaluation = UserEvaluation.GOT_IT,
                    permissionDenied = false,
                    onMicTap = {},
                    onStopListening = {},
                    onManualOverride = {},
                    onTryAgain = {},
                    primaryAnswer = sampleQuestion.primaryAnswer,
                    allAnswers = sampleQuestion.allAnswers,
                    onNextQuestion = {}
                )
            }
        }

        // Green checkmark
        composeTestRule.onNodeWithText("✓").assertExists()

        // Title and supporting message
        composeTestRule.onNodeWithTag("speaking_evaluation_title").assertExists()
        composeTestRule.onNodeWithText("Correct").assertExists()
        composeTestRule.onNodeWithText("Your answer matches an accepted USCIS answer.").assertExists()

        // Content description for TalkBack
        composeTestRule.onNodeWithContentDescription("Answer correct. Your spoken answer matches an accepted answer.").assertExists()

        // Recognized transcript
        composeTestRule.onNodeWithText("Your answer:").assertExists()
        composeTestRule.onNodeWithText("\"The Constitution\"").assertExists()
        composeTestRule.onNodeWithTag("speaking_your_answer_text").assertExists()

        // Accepted answer
        composeTestRule.onNodeWithText("Matched accepted answer:").assertExists()

        // Primary actions
        composeTestRule.onNodeWithTag("speaking_next_button").assertExists()
        composeTestRule.onNodeWithTag("speaking_try_again_button").assertExists()
    }

    // 9. Incorrect evaluation state
    @Test
    fun testIncorrectEvaluationState() {
        val transcript = "The Declaration of Independence"
        val matchResult = SpeechMatchResult(
            status = MatchStatus.NO_MATCH,
            userSpokenText = transcript,
            matchedAnswer = null,
            explanation = "The response did not match any accepted USCIS answers."
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                SpeakingMicZone(
                    speechState = SpeechRecognitionState.Success(transcript),
                    lastSpokenText = transcript,
                    matchResult = matchResult,
                    currentEvaluation = UserEvaluation.MISSED_IT,
                    permissionDenied = false,
                    onMicTap = {},
                    onStopListening = {},
                    onManualOverride = {},
                    onTryAgain = {},
                    primaryAnswer = sampleQuestion.primaryAnswer,
                    allAnswers = sampleQuestion.allAnswers,
                    onNextQuestion = {}
                )
            }
        }

        // Red circular cross
        composeTestRule.onNodeWithText("✕").assertExists()

        // Title: Not Correct
        composeTestRule.onNodeWithTag("speaking_evaluation_title").assertExists()
        composeTestRule.onNodeWithText("Not Correct").assertExists()

        // Accessibility content description
        composeTestRule.onNodeWithContentDescription("Answer not correct. Review the accepted answer.").assertExists()

        // Transcript
        composeTestRule.onNodeWithText("Your answer:").assertExists()
        composeTestRule.onNodeWithText("\"The Declaration of Independence\"").assertExists()

        // Correct answer display
        composeTestRule.onNodeWithText("Correct answer:").assertExists()
        composeTestRule.onNodeWithTag("speaking_correct_answer_text").assertExists()
        composeTestRule.onNodeWithText("\"the Constitution\"").assertExists()

        // Expandable accepted answers button
        composeTestRule.onNodeWithTag("speaking_view_accepted_answers_button").assertExists()

        // Try Again and Next
        composeTestRule.onNodeWithTag("speaking_try_again_button").assertExists()
        composeTestRule.onNodeWithTag("speaking_next_button").assertExists()
        composeTestRule.onNodeWithTag("speaking_mark_correct_button").assertExists()
    }

    // 10. Likely-correct and needs-review states
    @Test
    fun testLikelyCorrectAndNeedsReviewStates() {
        val transcript = "Constitution"
        val matchResult = SpeechMatchResult(
            status = MatchStatus.LIKELY_CORRECT,
            userSpokenText = transcript,
            matchedAnswer = "the Constitution",
            explanation = "Matches core answer words."
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                SpeakingMicZone(
                    speechState = SpeechRecognitionState.Success(transcript),
                    lastSpokenText = transcript,
                    matchResult = matchResult,
                    currentEvaluation = UserEvaluation.GOT_IT,
                    permissionDenied = false,
                    onMicTap = {},
                    onStopListening = {},
                    onManualOverride = {},
                    onTryAgain = {},
                    primaryAnswer = sampleSeniorQuestion.primaryAnswer,
                    allAnswers = sampleSeniorQuestion.allAnswers
                )
            }
        }

        composeTestRule.onNodeWithTag("speaking_evaluation_title").assertExists()
        composeTestRule.onNodeWithText("Likely Correct").assertExists()
        composeTestRule.onNodeWithText("Review the accepted answer to confirm.").assertExists()
        composeTestRule.onNodeWithContentDescription("Answer likely correct. Review accepted answer.").assertExists()

        composeTestRule.onNodeWithText("Your answer:").assertExists()
        composeTestRule.onNodeWithText("\"Constitution\"").assertExists()

        // Manual grading buttons
        composeTestRule.onNodeWithTag("speaking_mark_correct_button").assertExists()
        composeTestRule.onNodeWithTag("speaking_mark_incorrect_button").assertExists()
        composeTestRule.onNodeWithTag("speaking_try_again_button").assertExists()
    }

    // 11. Try Again action
    @Test
    fun testTryAgainAction() {
        var tryAgainClicked = false
        val transcript = "The Declaration"
        val matchResult = SpeechMatchResult(
            status = MatchStatus.NO_MATCH,
            userSpokenText = transcript,
            matchedAnswer = null,
            explanation = "No match"
        )

        val state = SpeakingPracticeUiState(
            isLoading = false,
            questions = listOf(sampleQuestion.copy(isAnswerRevealed = true, evaluation = UserEvaluation.MISSED_IT)),
            currentIndex = 0,
            isAnswerRevealed = true,
            speechRecognitionState = SpeechRecognitionState.Success(transcript),
            lastSpokenText = transcript,
            speechMatchResult = matchResult
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                SpeakingPracticeScreen(
                    uiState = state,
                    onStartListening = {},
                    onStopListening = {},
                    onHearQuestion = {},
                    onHearAnswer = {},
                    onRevealAnswer = {},
                    onManualOverride = {},
                    onTryAgain = { tryAgainClicked = true },
                    onNextQuestion = {},
                    onToggleFavorite = {},
                    onShowLeaveConfirmation = {},
                    onNavigateBack = {},
                    onViewResults = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("speaking_try_again_button").performScrollTo().performClick()
        assertTrue(tryAgainClicked)
    }

    // 12. Next Question action
    @Test
    fun testNextQuestionAction() {
        var nextClicked = false
        val transcript = "The Constitution"
        val matchResult = SpeechMatchResult(
            status = MatchStatus.CORRECT,
            userSpokenText = transcript,
            matchedAnswer = "the Constitution",
            explanation = "Matches"
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                SpeakingMicZone(
                    speechState = SpeechRecognitionState.Success(transcript),
                    lastSpokenText = transcript,
                    matchResult = matchResult,
                    currentEvaluation = UserEvaluation.GOT_IT,
                    permissionDenied = false,
                    onMicTap = {},
                    onStopListening = {},
                    onManualOverride = {},
                    onTryAgain = {},
                    primaryAnswer = sampleQuestion.primaryAnswer,
                    allAnswers = sampleQuestion.allAnswers,
                    onNextQuestion = { nextClicked = true }
                )
            }
        }

        composeTestRule.onNodeWithTag("speaking_next_button").performClick()
        assertTrue(nextClicked)
    }

    // 13. 65/20 speaking mode
    @Test
    fun testSenior6520SpeakingMode() {
        val transcript = "Congress"
        val matchResult = AnswerMatcher.matchAnswer(transcript, sampleSeniorQuestion.allAnswers)
        assertEquals(MatchStatus.CORRECT, matchResult.status)

        val state = SpeakingPracticeUiState(
            isLoading = false,
            questions = listOf(sampleSeniorQuestion.copy(isAnswerRevealed = true, evaluation = UserEvaluation.GOT_IT)),
            currentIndex = 0,
            isAnswerRevealed = true,
            speechRecognitionState = SpeechRecognitionState.Success(transcript),
            lastSpokenText = transcript,
            speechMatchResult = matchResult
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                SpeakingPracticeScreen(
                    uiState = state,
                    onStartListening = {},
                    onStopListening = {},
                    onHearQuestion = {},
                    onHearAnswer = {},
                    onRevealAnswer = {},
                    onManualOverride = {},
                    onTryAgain = {},
                    onNextQuestion = {},
                    onToggleFavorite = {},
                    onShowLeaveConfirmation = {},
                    onNavigateBack = {},
                    onViewResults = {}
                )
            }
        }

        composeTestRule.onNodeWithText("65/20 Special").assertExists()
        composeTestRule.onNodeWithText("Correct").assertExists()
        composeTestRule.onNodeWithText("✓").assertExists()
        composeTestRule.onNodeWithTag("speaking_your_answer_text").assertExists()
    }

    // 14. Dark mode
    @Test
    fun testDarkModeRendering() {
        val transcript = "The Constitution"
        val matchResult = SpeechMatchResult(
            status = MatchStatus.CORRECT,
            userSpokenText = transcript,
            matchedAnswer = "the Constitution",
            explanation = "Matches accepted answer."
        )

        val state = SpeakingPracticeUiState(
            isLoading = false,
            questions = listOf(sampleQuestion.copy(isAnswerRevealed = true, evaluation = UserEvaluation.GOT_IT)),
            currentIndex = 0,
            isAnswerRevealed = true,
            speechRecognitionState = SpeechRecognitionState.Success(transcript),
            lastSpokenText = transcript,
            speechMatchResult = matchResult
        )

        composeTestRule.setContent {
            MyApplicationTheme(darkTheme = true) {
                SpeakingPracticeScreen(
                    uiState = state,
                    onStartListening = {},
                    onStopListening = {},
                    onHearQuestion = {},
                    onHearAnswer = {},
                    onRevealAnswer = {},
                    onManualOverride = {},
                    onTryAgain = {},
                    onNextQuestion = {},
                    onToggleFavorite = {},
                    onShowLeaveConfirmation = {},
                    onNavigateBack = {},
                    onViewResults = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("speaking_practice_screen").assertExists()
        composeTestRule.onNodeWithText("Correct").assertExists()
    }

    // 15. Accessibility and content descriptions
    @Test
    fun testAccessibilityAndContentDescriptions() {
        composeTestRule.setContent {
            MyApplicationTheme {
                SpeakingMicZone(
                    speechState = SpeechRecognitionState.Idle,
                    lastSpokenText = null,
                    matchResult = null,
                    currentEvaluation = UserEvaluation.UNANSWERED,
                    permissionDenied = false,
                    onMicTap = {},
                    onStopListening = {},
                    onManualOverride = {},
                    onTryAgain = {},
                    primaryAnswer = sampleQuestion.primaryAnswer,
                    allAnswers = sampleQuestion.allAnswers
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Microphone ready. Tap to speak.").assertExists()
    }

    // 16. Main speaking layout does not require scrolling for normal answer flow
    @Test
    fun testMainSpeakingLayoutNoScrollRequirement() {
        val transcript = "the Constitution"
        val matchResult = AnswerMatcher.matchAnswer(transcript, sampleQuestion.allAnswers)
        val state = SpeakingPracticeUiState(
            isLoading = false,
            questions = listOf(sampleQuestion.copy(isAnswerRevealed = true, evaluation = UserEvaluation.GOT_IT)),
            currentIndex = 0,
            isAnswerRevealed = true,
            speechRecognitionState = SpeechRecognitionState.Success(transcript),
            lastSpokenText = transcript,
            speechMatchResult = matchResult
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                SpeakingPracticeScreen(
                    uiState = state,
                    onStartListening = {},
                    onStopListening = {},
                    onHearQuestion = {},
                    onHearAnswer = {},
                    onRevealAnswer = {},
                    onManualOverride = {},
                    onTryAgain = {},
                    onNextQuestion = {},
                    onToggleFavorite = {},
                    onShowLeaveConfirmation = {},
                    onNavigateBack = {},
                    onViewResults = {}
                )
            }
        }

        // All key interaction nodes are present on screen simultaneously without scroll requirement
        composeTestRule.onNodeWithTag("speaking_question_card").assertExists()
        composeTestRule.onNodeWithTag("speaking_mic_button").assertExists()
        composeTestRule.onNodeWithTag("speaking_your_answer_text").assertExists()
        composeTestRule.onNodeWithTag("speaking_next_button").assertExists()
        composeTestRule.onNodeWithTag("speaking_try_again_button").assertExists()
    }

    @Test
    fun testViewAcceptedAnswersExpandsListOnWrongAnswer() {
        val transcript = "Congress"
        val matchResult = SpeechMatchResult(
            status = MatchStatus.NO_MATCH,
            userSpokenText = transcript,
            matchedAnswer = null,
            explanation = "Does not match accepted answers."
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                SpeakingMicZone(
                    speechState = SpeechRecognitionState.Success(transcript),
                    lastSpokenText = transcript,
                    matchResult = matchResult,
                    currentEvaluation = UserEvaluation.MISSED_IT,
                    permissionDenied = false,
                    onMicTap = {},
                    onStopListening = {},
                    onManualOverride = {},
                    onTryAgain = {},
                    primaryAnswer = sampleQuestion.primaryAnswer,
                    allAnswers = sampleQuestion.allAnswers
                )
            }
        }

        // Click "View accepted answers"
        composeTestRule.onNodeWithTag("speaking_view_accepted_answers_button").performClick()
        composeTestRule.onNodeWithTag("speaking_all_accepted_answers").assertExists()
        composeTestRule.onNodeWithText("Hide accepted answers").assertExists()
    }
}
