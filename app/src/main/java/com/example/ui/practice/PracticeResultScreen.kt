package com.example.ui.practice

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AppCard
import com.example.ui.components.AppTopBar
import com.example.ui.components.StatCard
import com.example.ui.theme.AppAnimation
import com.example.ui.theme.appColors
import com.example.ui.theme.appSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeResultScreen(
    uiState: PracticeResultUiState,
    onReviewAnswers: (resultId: String) -> Unit,
    onPracticeAgain: (type: String) -> Unit,
    onBackToPractice: () -> Unit,
    onReviewMistakes: (resultId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.testTag("practice_result_screen"),
        topBar = {
            AppTopBar(
                title = "Practice Result",
                subtitle = "Performance summary",
                showCivicEmblem = false,
                showBackButton = true,
                onBackClick = onBackToPractice,
                backButtonTestTag = "result_back_button"
            )
        }
    ) { innerPadding ->
        AnimatedVisibility(
            visible = true,
            enter = AppAnimation.screenEnter()
        ) {
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
                                .testTag("result_loading_indicator"),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    uiState.result != null -> {
                        val result = uiState.result
                        val scrollState = rememberScrollState()

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState)
                                .padding(horizontal = 20.dp, vertical = 12.dp)
                                .widthIn(max = 680.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Practice Complete",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = result.title,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // 1. Score Hero Card
                            AppCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("result_score_card"),
                                shape = RoundedCornerShape(18.dp),
                                containerColor = if (uiState.isPassed) {
                                    MaterialTheme.appColors.successContainer.copy(alpha = 0.45f)
                                } else {
                                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
                                },
                                border = BorderStroke(
                                    1.dp,
                                    if (uiState.isPassed) MaterialTheme.appColors.success.copy(alpha = 0.3f)
                                    else MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
                                ),
                                elevation = 1.dp
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (uiState.isPassed) MaterialTheme.appColors.success
                                                else MaterialTheme.colorScheme.error
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (uiState.isPassed) Icons.Default.CheckCircle
                                            else Icons.Default.ErrorOutline,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = "${uiState.correctCount} / ${uiState.totalCount}",
                                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.testTag("result_score_text")
                                    )

                                    Text(
                                        text = "${uiState.percentage}%",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (uiState.isPassed) MaterialTheme.appColors.success
                                        else MaterialTheme.colorScheme.error
                                    ) {
                                        Text(
                                            text = if (uiState.isFullPractice) {
                                                if (uiState.isPassed) "PASS" else "KEEP PRACTICING"
                                            } else {
                                                if (uiState.isPassed) "GREAT JOB" else "KEEP PRACTICING"
                                            },
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White,
                                            modifier = Modifier
                                                .padding(horizontal = 12.dp, vertical = 4.dp)
                                                .testTag("result_pass_badge")
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = if (uiState.isFullPractice) {
                                            if (uiState.isPassed) {
                                                "Practice simulation result. You met the 12/20 passing threshold."
                                            } else {
                                                "Practice simulation result. 12 correct answers needed to reach passing threshold."
                                            }
                                        } else {
                                            "Practice simulation result. Self-evaluated oral civics questions."
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // 2. Metrics Breakdown Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                StatCard(
                                    label = "Correct",
                                    count = uiState.correctCount,
                                    color = MaterialTheme.appColors.success,
                                    modifier = Modifier.weight(1f)
                                )
                                StatCard(
                                    label = "Incorrect",
                                    count = uiState.incorrectCount,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.weight(1f)
                                )
                                StatCard(
                                    label = "Total Qs",
                                    count = uiState.totalCount,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // 3. Action Buttons Section
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Primary Action: Review Answers
                                Button(
                                    onClick = { onReviewAnswers(result.id) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("result_review_answers_button"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Assignment,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Review Answers",
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                                    )
                                }

                                if (uiState.hasMistakes) {
                                    OutlinedButton(
                                        onClick = { onReviewMistakes(result.id) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .testTag("result_review_mistakes_button"),
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.error)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.WarningAmber,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Review Mistakes (${uiState.incorrectCount})",
                                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }

                                OutlinedButton(
                                    onClick = { onPracticeAgain(result.practiceType) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("result_practice_again_button"),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Practice Again",
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                }

                                Button(
                                    onClick = onBackToPractice,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("result_back_to_practice_button"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                ) {
                                    Text(
                                        text = "Return to Practice Hub",
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                    else -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                                .align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Result not found",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onBackToPractice,
                                modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                            ) {
                                Text("Back to Practice")
                            }
                        }
                    }
                }
            }
        }
    }
}
