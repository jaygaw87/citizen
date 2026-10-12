package com.example.ui.practice

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserEvaluation
import com.example.ui.components.AppCard
import com.example.ui.components.AppCategoryChip
import com.example.ui.components.AppEligibilityChip
import com.example.ui.components.AppTopBar
import com.example.ui.theme.appColors
import com.example.ui.theme.appSpacing
import com.example.ui.theme.studyTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeQuestionScreen(
    uiState: PracticeSessionUiState,
    onRevealAnswer: () -> Unit,
    onEvaluateAnswer: (Boolean) -> Unit,
    onNextQuestion: () -> Unit,
    onPreviousQuestion: () -> Unit,
    onToggleFavorite: () -> Unit,
    onRequestLeave: (Boolean) -> Unit,
    onConfirmLeave: () -> Unit,
    onSessionCompleted: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Navigate automatically when session finishes
    LaunchedEffect(uiState.completedResultId) {
        val resultId = uiState.completedResultId
        if (resultId != null) {
            onSessionCompleted(resultId)
        }
    }

    BackHandler {
        onRequestLeave(true)
    }

    if (uiState.showLeaveDialog) {
        AlertDialog(
            onDismissRequest = { onRequestLeave(false) },
            title = {
                Text(
                    text = "Leave Practice?",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = "Your progress in this practice session will not be saved if you leave now.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRequestLeave(false)
                        onConfirmLeave()
                    },
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Leave")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { onRequestLeave(false) },
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                ) {
                    Text("Stay")
                }
            }
        )
    }

    Scaffold(
        modifier = modifier.testTag("practice_question_screen"),
        topBar = {
            AppTopBar(
                title = uiState.title,
                subtitle = if (uiState.totalCount > 0) "Question ${uiState.currentIndex + 1} of ${uiState.totalCount}" else null,
                showCivicEmblem = false,
                showBackButton = true,
                onBackClick = { onRequestLeave(true) },
                backButtonTestTag = "practice_back_button",
                actions = {
                    if (uiState.currentQuestion != null) {
                        IconButton(
                            onClick = onToggleFavorite,
                            modifier = Modifier
                                .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                                .testTag("practice_favorite_button")
                        ) {
                            Icon(
                                imageVector = if (uiState.currentQuestion!!.isFavorite) Icons.Default.Bookmark
                                else Icons.Default.BookmarkBorder,
                                contentDescription = if (uiState.currentQuestion!!.isFavorite) "Remove from Favorites"
                                else "Add to Favorites",
                                tint = if (uiState.currentQuestion!!.isFavorite) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (uiState.currentQuestion != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    tonalElevation = 3.dp,
                    shadowElevation = 6.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onPreviousQuestion,
                            enabled = uiState.currentIndex > 0,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("practice_prev_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Previous",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Button(
                            onClick = onNextQuestion,
                            enabled = uiState.isCurrentQuestionAnswered,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("practice_next_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (uiState.isLastQuestion) "Finish Test" else "Next",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .testTag("practice_loading_indicator"),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                uiState.emptyMessage != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 500.dp)
                            .padding(24.dp)
                            .align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = uiState.emptyMessage,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.testTag("practice_empty_message")
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = onConfirmLeave,
                            modifier = Modifier
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("practice_empty_back_button")
                        ) {
                            Text("Back to Practice")
                        }
                    }
                }
                uiState.currentQuestion != null -> {
                    val question = uiState.currentQuestion!!
                    val scrollState = rememberScrollState()

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                            .widthIn(max = 680.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 1. Progress Bar & Score Stats
                        LinearProgressIndicator(
                            progress = { uiState.progressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .testTag("practice_progress_bar"),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                            strokeCap = StrokeCap.Round
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Correct / Incorrect counters
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Question ${uiState.currentIndex + 1} of ${uiState.totalCount}",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(9.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.appColors.success)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "${uiState.correctCount}",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.appColors.success,
                                        modifier = Modifier.testTag("practice_correct_count")
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(9.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.error)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "${uiState.incorrectCount}",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.testTag("practice_incorrect_count")
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // 2. Question Card
                        AppCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("practice_question_card"),
                            shape = RoundedCornerShape(16.dp),
                            containerColor = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            elevation = 0.5.dp
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        AppCategoryChip(category = question.category)
                                        if (question.is65_20) {
                                            AppEligibilityChip()
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                                ) {
                                    Text(
                                        text = "Question ${question.questionNumber}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = question.questionText,
                                    style = MaterialTheme.studyTypography.questionText.copy(
                                        fontSize = 18.sp,
                                        lineHeight = 24.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // 3. Oral-style Answer Reveal & Evaluation Section
                        if (!question.isAnswerRevealed) {
                            AppCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("practice_reveal_card"),
                                shape = RoundedCornerShape(16.dp),
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                elevation = 0.dp
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Oral Interview Simulation",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Say your answer aloud, then tap Reveal to compare.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = onRevealAnswer,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .testTag("practice_reveal_button"),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Visibility,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Reveal Answer",
                                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                }
                            }
                        } else {
                            // Revealed Answer Section with smooth entrance
                            AnimatedVisibility(
                                visible = question.isAnswerRevealed,
                                enter = fadeIn(animationSpec = tween(300)),
                                exit = fadeOut()
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Accepted Answers Card
                                    AppCard(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("practice_answer_card"),
                                        shape = RoundedCornerShape(16.dp),
                                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                                        elevation = 0.5.dp
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 14.dp, vertical = 10.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(bottom = 6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Outlined.VerifiedUser,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "USCIS ACCEPTED ANSWERS",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        letterSpacing = 0.8.sp
                                                    ),
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }

                                            question.allAnswers.forEach { ans ->
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 2.dp),
                                                    verticalAlignment = Alignment.Top
                                                ) {
                                                    Text(
                                                        text = "•",
                                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                        color = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.padding(end = 6.dp)
                                                    )
                                                    Text(
                                                        text = ans,
                                                        style = MaterialTheme.studyTypography.answerText.copy(
                                                            fontSize = 15.sp,
                                                            fontWeight = FontWeight.SemiBold
                                                        ),
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Curated Educational Explanation
                                    if (question.explanation.isNotBlank()) {
                                        AppCard(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(14.dp),
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                            elevation = 0.dp
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.Top
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Lightbulb,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.secondary,
                                                    modifier = Modifier
                                                        .size(18.dp)
                                                        .padding(top = 2.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column {
                                                    Text(
                                                        text = "Educational Explanation",
                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                        color = MaterialTheme.colorScheme.secondary
                                                    )
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = question.explanation,
                                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp),
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Self-Evaluation Card
                                    AppCard(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("practice_evaluation_card"),
                                        shape = RoundedCornerShape(16.dp),
                                        containerColor = MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                        elevation = 0.5.dp
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 14.dp, vertical = 10.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "How did you do?",
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                // I Got It Action
                                                val isGotItSelected = question.evaluation == UserEvaluation.GOT_IT
                                                Button(
                                                    onClick = { onEvaluateAnswer(true) },
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(48.dp)
                                                        .testTag("practice_button_got_it"),
                                                    shape = RoundedCornerShape(12.dp),
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = if (isGotItSelected) MaterialTheme.appColors.success
                                                        else MaterialTheme.appColors.successContainer.copy(alpha = 0.4f),
                                                        contentColor = if (isGotItSelected) Color.White
                                                        else MaterialTheme.appColors.success
                                                    ),
                                                    border = if (isGotItSelected) null
                                                    else BorderStroke(1.5.dp, MaterialTheme.appColors.success)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "I Got It",
                                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                                                    )
                                                }

                                                // I Missed It Action
                                                val isMissedSelected = question.evaluation == UserEvaluation.MISSED_IT
                                                Button(
                                                    onClick = { onEvaluateAnswer(false) },
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(48.dp)
                                                        .testTag("practice_button_missed_it"),
                                                    shape = RoundedCornerShape(12.dp),
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = if (isMissedSelected) MaterialTheme.colorScheme.error
                                                        else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f),
                                                        contentColor = if (isMissedSelected) MaterialTheme.colorScheme.onError
                                                        else MaterialTheme.colorScheme.error
                                                    ),
                                                    border = if (isMissedSelected) null
                                                    else BorderStroke(1.5.dp, MaterialTheme.colorScheme.error)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Close,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "I Missed It",
                                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}
