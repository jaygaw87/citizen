package com.example.ui.speaking

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.UserEvaluation
import com.example.speech.MatchStatus
import com.example.speech.SpeechMatchResult
import com.example.speech.SpeechRecognitionState
import com.example.ui.components.AppTopBar
import com.example.ui.theme.appColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeakingPracticeScreen(
    uiState: SpeakingPracticeUiState,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    onHearQuestion: () -> Unit,
    onHearAnswer: () -> Unit,
    onRevealAnswer: () -> Unit,
    onManualOverride: (Boolean) -> Unit,
    onTryAgain: () -> Unit,
    onNextQuestion: () -> Unit,
    onToggleFavorite: () -> Unit,
    onShowLeaveConfirmation: (Boolean) -> Unit,
    onNavigateBack: () -> Unit,
    onViewResults: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var permissionDeniedNotice by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            onStopListening()
        }
    }

    BackHandler {
        if (!uiState.isSessionComplete) {
            onShowLeaveConfirmation(true)
        } else {
            onNavigateBack()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onStartListening()
            permissionDeniedNotice = false
        } else {
            permissionDeniedNotice = true
        }
    }

    fun handleMicTap() {
        if (uiState.speechRecognitionState is SpeechRecognitionState.Listening ||
            uiState.speechRecognitionState is SpeechRecognitionState.Starting
        ) {
            onStopListening()
        } else {
            val permissionCheck = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            )
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                onStartListening()
                permissionDeniedNotice = false
            } else {
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    Scaffold(
        modifier = modifier.testTag("speaking_practice_screen"),
        topBar = {
            AppTopBar(
                title = "Speaking Practice",
                subtitle = if (uiState.totalCount > 0 && !uiState.isSessionComplete) "Question ${uiState.currentIndex + 1} of ${uiState.totalCount}" else null,
                showCivicEmblem = false,
                showBackButton = true,
                onBackClick = {
                    if (!uiState.isSessionComplete) {
                        onShowLeaveConfirmation(true)
                    } else {
                        onNavigateBack()
                    }
                },
                backButtonTestTag = "speaking_back_button",
                actions = {
                    val currentQ = uiState.currentQuestion
                    if (currentQ != null && !uiState.isSessionComplete) {
                        IconButton(
                            onClick = onToggleFavorite,
                            modifier = Modifier
                                .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                                .testTag("speaking_favorite_button")
                        ) {
                            Icon(
                                imageVector = if (currentQ.isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = if (currentQ.isFavorite) "Remove from favorites" else "Add to favorites",
                                tint = if (currentQ.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.isSessionComplete && uiState.savedResultId != null) {
                SpeakingCompletionView(
                    uiState = uiState,
                    onViewResults = { onViewResults(uiState.savedResultId) },
                    onNavigateBack = onNavigateBack
                )
            } else {
                val currentQuestion = uiState.currentQuestion
                if (currentQuestion != null) {
                    val scrollState = rememberScrollState()
                    val isLastQuestion = uiState.currentIndex + 1 >= uiState.totalCount

                    // Compact, No-Scroll layout fitting standard phone viewports
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .widthIn(max = 600.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 1. TOP: Progress Indicator
                        LinearProgressIndicator(
                            progress = { uiState.progressPercentage },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .testTag("speaking_progress_bar"),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // 2. TOP: Category & 65/20 Badge Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                            ) {
                                Text(
                                    text = currentQuestion.category,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }

                            if (currentQuestion.is65_20) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.tertiaryContainer
                                ) {
                                    Text(
                                        text = "65/20 Special",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // 3. MIDDLE: Question Card (Compact & Clear Hierarchy)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("speaking_question_card"),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Question ${currentQuestion.questionNumber}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )

                                    FilledTonalButton(
                                        onClick = onHearQuestion,
                                        modifier = Modifier
                                            .height(34.dp)
                                            .testTag("speaking_hear_question_button"),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = if (uiState.isTtsSpeaking && uiState.ttsMode == TtsMode.QUESTION) {
                                                MaterialTheme.colorScheme.errorContainer
                                            } else {
                                                MaterialTheme.colorScheme.primaryContainer
                                            },
                                            contentColor = if (uiState.isTtsSpeaking && uiState.ttsMode == TtsMode.QUESTION) {
                                                MaterialTheme.colorScheme.onErrorContainer
                                            } else {
                                                MaterialTheme.colorScheme.onPrimaryContainer
                                            }
                                        )
                                    ) {
                                        Icon(
                                            imageVector = if (uiState.isTtsSpeaking && uiState.ttsMode == TtsMode.QUESTION) {
                                                Icons.Default.Stop
                                            } else {
                                                Icons.AutoMirrored.Filled.VolumeUp
                                            },
                                            contentDescription = null,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (uiState.isTtsSpeaking && uiState.ttsMode == TtsMode.QUESTION) {
                                                "Stop Audio"
                                            } else {
                                                "Hear Question"
                                            },
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = currentQuestion.questionText,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 15.sp,
                                    lineHeight = 20.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // 4. MIDDLE & BOTTOM: Speaking Microphone + Evaluation Result Area
                        SpeakingMicZone(
                            speechState = uiState.speechRecognitionState,
                            lastSpokenText = uiState.lastSpokenText,
                            matchResult = uiState.speechMatchResult,
                            currentEvaluation = currentQuestion.evaluation,
                            permissionDenied = permissionDeniedNotice,
                            onMicTap = { handleMicTap() },
                            onStopListening = onStopListening,
                            onManualOverride = onManualOverride,
                            onTryAgain = onTryAgain,
                            primaryAnswer = currentQuestion.primaryAnswer,
                            allAnswers = currentQuestion.allAnswers,
                            explanation = currentQuestion.explanation,
                            onNextQuestion = onNextQuestion,
                            isLastQuestion = isLastQuestion
                        )

                        // 5. Pre-evaluation controls (Reveal Answer & Next/Skip Question)
                        if (uiState.speechMatchResult == null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            if (!uiState.isAnswerRevealed) {
                                OutlinedButton(
                                    onClick = onRevealAnswer,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(42.dp)
                                        .testTag("speaking_reveal_button"),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Visibility,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Reveal Accepted Answer",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                }
                            } else {
                                AnswerRevealedCard(
                                    answers = currentQuestion.allAnswers,
                                    explanation = currentQuestion.explanation,
                                    isTtsSpeakingAnswer = uiState.isTtsSpeaking && uiState.ttsMode == TtsMode.ANSWER,
                                    onHearAnswer = onHearAnswer
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Button(
                                onClick = onNextQuestion,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .testTag("speaking_next_button"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text(
                                    text = if (isLastQuestion) "Complete Practice" else "Next Question",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }
        }
    }

    // Leave Confirmation Dialog
    if (uiState.showLeaveDialog) {
        AlertDialog(
            onDismissRequest = { onShowLeaveConfirmation(false) },
            title = { Text("Leave Speaking Practice?") },
            text = { Text("Your speaking practice session progress will not be saved if you leave now.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onShowLeaveConfirmation(false)
                        onNavigateBack()
                    }
                ) {
                    Text("Leave", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { onShowLeaveConfirmation(false) }) {
                    Text("Continue Practice")
                }
            }
        )
    }
}

@Composable
fun SpeakingMicZone(
    speechState: SpeechRecognitionState,
    lastSpokenText: String?,
    matchResult: SpeechMatchResult?,
    currentEvaluation: UserEvaluation,
    permissionDenied: Boolean,
    onMicTap: () -> Unit,
    onStopListening: () -> Unit,
    onManualOverride: (Boolean) -> Unit,
    onTryAgain: () -> Unit,
    primaryAnswer: String = "",
    allAnswers: List<String> = emptyList(),
    explanation: String = "",
    onNextQuestion: (() -> Unit)? = null,
    isLastQuestion: Boolean = false,
    modifier: Modifier = Modifier
) {
    val isListening = speechState is SpeechRecognitionState.Listening
    val isStarting = speechState is SpeechRecognitionState.Starting
    val isProcessing = speechState is SpeechRecognitionState.Processing
    val isSuccess = speechState is SpeechRecognitionState.Success
    val isError = speechState is SpeechRecognitionState.Error

    val haptic = LocalHapticFeedback.current
    LaunchedEffect(isListening) {
        if (isListening) {
            try {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            } catch (_: Exception) {}
        }
    }

    var listeningSeconds by remember { mutableIntStateOf(0) }

    LaunchedEffect(isListening) {
        if (isListening) {
            listeningSeconds = 0
            val startTime = System.currentTimeMillis()
            while (isActive) {
                delay(300L)
                val elapsed = ((System.currentTimeMillis() - startTime) / 1000L).toInt()
                listeningSeconds = elapsed
            }
        } else {
            listeningSeconds = 0
        }
    }

    val rawRms = (speechState as? SpeechRecognitionState.Listening)?.rmsLevel ?: 0f
    val smoothRms by animateFloatAsState(
        targetValue = if (isListening) rawRms.coerceIn(0f, 1f) else 0f,
        animationSpec = tween(durationMillis = 85, easing = LinearOutSlowInEasing),
        label = "smoothRms"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "starting_pulse")
    val startingPulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "startingPulse"
    )

    val centerCircleScale = when {
        matchResult != null -> 1.0f
        isListening -> (1.0f + (smoothRms * 0.12f)).coerceIn(1.0f, 1.14f)
        isStarting -> startingPulseScale
        else -> 1.0f
    }

    // Meaningful TalkBack semantics for accessibility
    val semanticsDescription = when {
        matchResult != null -> when (matchResult.status) {
            MatchStatus.CORRECT -> "Answer correct. Your spoken answer matches an accepted answer."
            MatchStatus.NO_MATCH -> "Answer not correct. Review the accepted answer."
            MatchStatus.LIKELY_CORRECT -> "Answer likely correct. Review accepted answer."
            MatchStatus.NEEDS_REVIEW -> "Answer needs review."
        }
        isListening -> "Listening for your answer."
        isStarting -> "Starting microphone."
        isProcessing -> "Processing your spoken answer."
        isSuccess -> "Speech received."
        isError -> "Speech recognition error. Try again."
        else -> "Microphone ready. Tap to speak."
    }

    val targetCircleBgColor = when {
        matchResult != null -> when (matchResult.status) {
            MatchStatus.CORRECT -> MaterialTheme.appColors.success
            MatchStatus.NO_MATCH -> MaterialTheme.colorScheme.error
            MatchStatus.LIKELY_CORRECT -> MaterialTheme.appColors.warning
            MatchStatus.NEEDS_REVIEW -> MaterialTheme.appColors.warning
        }
        isListening -> MaterialTheme.colorScheme.primary
        isProcessing -> MaterialTheme.colorScheme.secondaryContainer
        isSuccess -> MaterialTheme.colorScheme.primaryContainer
        isError -> MaterialTheme.colorScheme.errorContainer
        isStarting -> MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
        else -> MaterialTheme.colorScheme.primaryContainer
    }

    val animatedCircleBgColor by animateColorAsState(
        targetValue = targetCircleBgColor,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "circleBgColor"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("speaking_mic_zone"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Live Listening Timer
            if (isListening) {
                val timerText = String.format(Locale.US, "%02d:%02d", listeningSeconds / 60, listeningSeconds % 60)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    modifier = Modifier.testTag("speaking_listening_timer")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFD32F2F))
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "LIVE $timerText",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Central Listening Area with Voice-Reactive Circular Animation
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(if (isListening) 170.dp else 84.dp)
            ) {
                // Voice-reactive concentric rings only active during Listening
                if (isListening) {
                    VoiceReactiveCircleAnimation(
                        rmsLevel = smoothRms,
                        primaryColor = MaterialTheme.colorScheme.primary
                    )
                }

                // Processing Spinner around center
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(82.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 3.dp
                    )
                }

                // Central Circular Surface
                Surface(
                    modifier = Modifier
                        .size(76.dp)
                        .scale(centerCircleScale)
                        .clip(CircleShape)
                        .semantics { contentDescription = semanticsDescription }
                        .clickable {
                            if (isListening || isStarting) {
                                onStopListening()
                            } else if (matchResult != null) {
                                onTryAgain()
                            } else {
                                onMicTap()
                            }
                        }
                        .testTag("speaking_mic_button"),
                    shape = CircleShape,
                    border = if (isListening) BorderStroke(
                        width = (2f + (smoothRms * 2.5f)).dp,
                        color = Color.White.copy(alpha = (0.5f + smoothRms * 0.5f).coerceIn(0.5f, 1f))
                    ) else null,
                    color = animatedCircleBgColor,
                    shadowElevation = if (isListening) (4f + smoothRms * 6f).dp else 2.dp
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        AnimatedContent(
                            targetState = when {
                                matchResult?.status == MatchStatus.CORRECT -> "CORRECT"
                                matchResult?.status == MatchStatus.NO_MATCH -> "NO_MATCH"
                                matchResult?.status == MatchStatus.LIKELY_CORRECT -> "LIKELY"
                                matchResult?.status == MatchStatus.NEEDS_REVIEW -> "REVIEW"
                                isError -> "ERROR"
                                else -> "MIC"
                            },
                            transitionSpec = {
                                fadeIn(animationSpec = tween(180)) togetherWith fadeOut(animationSpec = tween(120))
                            },
                            label = "micIconTransition"
                        ) { iconState ->
                            when (iconState) {
                                "CORRECT" -> Text(
                                    text = "✓",
                                    fontSize = 38.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.appColors.onSuccess,
                                    textAlign = TextAlign.Center
                                )
                                "NO_MATCH" -> Text(
                                    text = "✕",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.appColors.onError,
                                    textAlign = TextAlign.Center
                                )
                                "LIKELY", "REVIEW" -> Icon(
                                    imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.appColors.onWarning,
                                    modifier = Modifier.size(34.dp)
                                )
                                "ERROR" -> Icon(
                                    imageVector = Icons.Default.MicOff,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(32.dp)
                                )
                                else -> Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = null,
                                    tint = if (isListening || isStarting) Color.White else MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // State Title and Supporting Text
            if (matchResult != null) {
                when (matchResult.status) {
                    MatchStatus.CORRECT -> {
                        Text(
                            text = "Correct",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.appColors.success,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.testTag("speaking_evaluation_title")
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Your answer matches an accepted USCIS answer.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                    MatchStatus.NO_MATCH -> {
                        Text(
                            text = "Not Correct",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.testTag("speaking_evaluation_title")
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Your answer did not match the accepted answers.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                    MatchStatus.LIKELY_CORRECT -> {
                        Text(
                            text = "Likely Correct",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.appColors.warning,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.testTag("speaking_evaluation_title")
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Review the accepted answer to confirm.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                    MatchStatus.NEEDS_REVIEW -> {
                        Text(
                            text = "Needs Review",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.appColors.warning,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.testTag("speaking_evaluation_title")
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Review the official accepted answers.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                when (speechState) {
                    is SpeechRecognitionState.Idle -> {
                        Text(
                            text = "Tap the microphone to answer",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Speak your answer clearly into the microphone",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                    is SpeechRecognitionState.Starting -> {
                        Text(
                            text = "Starting microphone...",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                    }
                    is SpeechRecognitionState.Listening -> {
                        Text(
                            text = "Listening",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.testTag("speaking_listening_status")
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Speak now",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                    is SpeechRecognitionState.Processing -> {
                        Text(
                            text = "Processing...",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Evaluating answer...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                    is SpeechRecognitionState.Success -> {
                        Text(
                            text = "Speech received",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.testTag("speaking_speech_received_status")
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Evaluating answer...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                    is SpeechRecognitionState.Error -> {
                        Text(
                            text = "Couldn't hear your answer",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = speechState.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                    is SpeechRecognitionState.Unavailable -> {
                        Text(
                            text = "Speech Recognition Unavailable",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Live Partial Transcription during Listening
            if (isListening) {
                val partialText = (speechState as? SpeechRecognitionState.Listening)?.partialText.orEmpty()
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("speaking_live_transcript")
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (partialText.isNotBlank()) "\"$partialText\"" else "Speak your answer",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = if (partialText.isNotBlank()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                OutlinedButton(
                    onClick = onStopListening,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .testTag("speaking_stop_button")
                        .height(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Tap to finish speaking",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            // Error Try Again Button
            if (isError) {
                Spacer(modifier = Modifier.height(6.dp))
                FilledTonalButton(
                    onClick = onTryAgain,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .testTag("speaking_try_again_button")
                        .height(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Try Again",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            if (permissionDenied) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Microphone permission is required to recognize spoken answers.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }

            // Recognized Answer Transcript Card
            if (!lastSpokenText.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("speaking_success_transcript")
                ) {
                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                        Text(
                            text = "Your answer:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(
                            text = "\"$lastSpokenText\"",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.testTag("speaking_your_answer_text")
                        )
                    }
                }
            }

            // Evaluation Details & USCIS Accepted Answers
            if (matchResult != null) {
                val answersToShow = if (allAnswers.isNotEmpty()) allAnswers else listOf(primaryAnswer)

                when (matchResult.status) {
                    MatchStatus.CORRECT -> {
                        val matchedText = matchResult.matchedAnswer ?: primaryAnswer
                        if (matchedText.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.appColors.success.copy(alpha = 0.10f),
                                border = BorderStroke(1.dp, MaterialTheme.appColors.success.copy(alpha = 0.35f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                                    Text(
                                        text = "Matched accepted answer:",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.appColors.success
                                    )
                                    Spacer(modifier = Modifier.height(1.dp))
                                    Text(
                                        text = "\"$matchedText\"",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    MatchStatus.NO_MATCH, MatchStatus.LIKELY_CORRECT, MatchStatus.NEEDS_REVIEW -> {
                        Spacer(modifier = Modifier.height(6.dp))
                        val isNoMatch = matchResult.status == MatchStatus.NO_MATCH
                        val cardContainerColor = if (isNoMatch) {
                            MaterialTheme.appColors.errorContainer.copy(alpha = 0.25f)
                        } else {
                            MaterialTheme.appColors.warningContainer.copy(alpha = 0.25f)
                        }
                        val cardBorderColor = if (isNoMatch) {
                            MaterialTheme.appColors.error.copy(alpha = 0.35f)
                        } else {
                            MaterialTheme.appColors.warning.copy(alpha = 0.35f)
                        }
                        val cardTag = when (matchResult.status) {
                            MatchStatus.NO_MATCH -> "speaking_wrong_answer_card"
                            MatchStatus.LIKELY_CORRECT -> "speaking_likely_correct_card"
                            else -> "speaking_needs_review_card"
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = cardContainerColor,
                            border = BorderStroke(1.dp, cardBorderColor),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag(cardTag)
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (answersToShow.size > 1) "USCIS Accepted Answers:" else "USCIS Accepted Answer:",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isNoMatch) MaterialTheme.appColors.error else MaterialTheme.appColors.warning
                                    )
                                    Text(
                                        text = "Correct answer:",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "\"$primaryAnswer\"",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.testTag("speaking_correct_answer_text")
                                )

                                if (answersToShow.size > 1) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    var showAllAnswers by remember(lastSpokenText) { mutableStateOf(false) }

                                    TextButton(
                                        onClick = { showAllAnswers = !showAllAnswers },
                                        modifier = Modifier
                                            .testTag("speaking_view_accepted_answers_button")
                                            .testTag("speaking_view_accepted_answer_button")
                                            .heightIn(min = 36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Visibility,
                                            contentDescription = null,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (showAllAnswers) "Hide accepted answers" else "View accepted answers",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp
                                        )
                                    }

                                    AnimatedVisibility(visible = showAllAnswers) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 2.dp)
                                                .testTag("speaking_all_accepted_answers"),
                                            verticalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            answersToShow.forEach { ans ->
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.Top
                                                ) {
                                                    Text(
                                                        text = "• ",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                    Text(
                                                        text = ans,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        fontWeight = FontWeight.Medium,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Manual Override Buttons
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onManualOverride(true) },
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 40.dp)
                                    .testTag("speaking_mark_correct_button"),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.appColors.success,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Mark Correct",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp
                                )
                            }

                            if (matchResult.status == MatchStatus.LIKELY_CORRECT || matchResult.status == MatchStatus.NEEDS_REVIEW) {
                                OutlinedButton(
                                    onClick = { onManualOverride(false) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 40.dp)
                                        .testTag("speaking_mark_incorrect_button"),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Mark Missed",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Educational Explanation (Visually Secondary, Collapsed/Compact)
                if (explanation.isNotBlank()) {
                    var showExplanation by remember(explanation) { mutableStateOf(false) }
                    Spacer(modifier = Modifier.height(4.dp))
                    TextButton(
                        onClick = { showExplanation = !showExplanation },
                        modifier = Modifier.heightIn(min = 32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (showExplanation) "Hide context" else "Study context",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    AnimatedVisibility(visible = showExplanation) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = explanation,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp,
                                lineHeight = 15.sp,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }

                // Action Buttons Hierarchy (Requirement 8 & 10)
                Spacer(modifier = Modifier.height(8.dp))
                if (matchResult.status == MatchStatus.CORRECT) {
                    if (onNextQuestion != null) {
                        Button(
                            onClick = onNextQuestion,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("speaking_next_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(
                                text = if (isLastQuestion) "Complete Practice" else "Next Question",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    OutlinedButton(
                        onClick = onTryAgain,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 40.dp)
                            .testTag("speaking_try_again_button"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Practice Question Again",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    if (onNextQuestion != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilledTonalButton(
                                onClick = onTryAgain,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("speaking_try_again_button"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Try Again",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }

                            Button(
                                onClick = onNextQuestion,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("speaking_next_button"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text(
                                    text = if (isLastQuestion) "Complete" else "Next Question",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    } else {
                        FilledTonalButton(
                            onClick = onTryAgain,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("speaking_try_again_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Try Again",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VoiceReactiveCircleAnimation(
    rmsLevel: Float,
    primaryColor: Color,
    modifier: Modifier = Modifier
) {
    // Subtle continuous breathing cycle (2.2s period) for organic life even in silence
    val infiniteTransition = rememberInfiniteTransition(label = "voice_rings_breathing")
    val breathingPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathingPhase"
    )

    Canvas(
        modifier = modifier
            .size(170.dp)
            .testTag("speaking_voice_reactive_canvas")
    ) {
        val centerPoint = Offset(size.width / 2f, size.height / 2f)

        // Center mic button is 76dp -> radius 38dp
        val baseRadiusPx = 38.dp.toPx()

        // Subtle breathing offset in silence: 0dp to 2dp (settles as RMS takes over)
        val breathingPx = (breathingPhase * 2.dp.toPx()) * (1f - (rmsLevel * 0.7f))

        // Ring 3: Outer Ring - Responds smoothly to louder speech
        val ring3Expansion = (rmsLevel * 28.dp.toPx()) + breathingPx
        val ring3Radius = baseRadiusPx + 22.dp.toPx() + ring3Expansion
        val ring3Alpha = (0.08f + rmsLevel * 0.22f).coerceIn(0f, 0.35f)
        drawCircle(
            color = primaryColor.copy(alpha = ring3Alpha * 0.35f),
            radius = ring3Radius,
            center = centerPoint
        )
        drawCircle(
            color = primaryColor.copy(alpha = ring3Alpha),
            radius = ring3Radius,
            center = centerPoint,
            style = Stroke(width = (1.5f + rmsLevel * 1.5f).dp.toPx())
        )

        // Ring 2: Medium Ring - Responds moderately
        val ring2Expansion = (rmsLevel * 18.dp.toPx()) + (breathingPx * 1.2f)
        val ring2Radius = baseRadiusPx + 13.dp.toPx() + ring2Expansion
        val ring2Alpha = (0.15f + rmsLevel * 0.30f).coerceIn(0f, 0.50f)
        drawCircle(
            color = primaryColor.copy(alpha = ring2Alpha * 0.35f),
            radius = ring2Radius,
            center = centerPoint
        )
        drawCircle(
            color = primaryColor.copy(alpha = ring2Alpha),
            radius = ring2Radius,
            center = centerPoint,
            style = Stroke(width = (2f + rmsLevel * 1.8f).dp.toPx())
        )

        // Ring 1: Closest Ring - Responds quickly to voice
        val ring1Expansion = (rmsLevel * 9.dp.toPx()) + (breathingPx * 1.4f)
        val ring1Radius = baseRadiusPx + 5.dp.toPx() + ring1Expansion
        val ring1Alpha = (0.24f + rmsLevel * 0.40f).coerceIn(0f, 0.70f)
        drawCircle(
            color = primaryColor.copy(alpha = ring1Alpha * 0.35f),
            radius = ring1Radius,
            center = centerPoint
        )
        drawCircle(
            color = primaryColor.copy(alpha = ring1Alpha),
            radius = ring1Radius,
            center = centerPoint,
            style = Stroke(width = (2.5f + rmsLevel * 2f).dp.toPx())
        )
    }
}

@Composable
fun AnswerRevealedCard(
    answers: List<String>,
    explanation: String,
    isTtsSpeakingAnswer: Boolean,
    onHearAnswer: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("speaking_revealed_answer_card"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Official Accepted Answers",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                FilledTonalButton(
                    onClick = onHearAnswer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("speaking_hear_answer_button")
                ) {
                    Icon(
                        imageVector = if (isTtsSpeakingAnswer) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isTtsSpeakingAnswer) "Stop" else "Hear Answer",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                answers.forEach { ans ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "• ",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = ans,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            if (explanation.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Study Context",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = explanation,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SpeakingCompletionView(
    uiState: SpeakingPracticeUiState,
    onViewResults: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .widthIn(max = 500.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(76.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(42.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Speaking Practice Complete",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Great job practicing aloud! Speaking civics questions and answers prepares you directly for the USCIS interview.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${uiState.correctCount}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                    Text(
                        text = "Correct",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${uiState.needsReviewCount}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE65100)
                    )
                    Text(
                        text = "Reviewed",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${uiState.incorrectCount}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "Missed",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onViewResults,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("speaking_view_results_button"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "View Detailed Results & Answers",
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("speaking_back_home_button"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Back to Practice",
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
