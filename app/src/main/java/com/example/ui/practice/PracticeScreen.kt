package com.example.ui.practice

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Rule
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.PracticeResultEntity
import com.example.ui.components.AppCard
import com.example.ui.components.AppPrimaryButton
import com.example.ui.components.AppTopBar
import com.example.ui.components.SectionHeader
import com.example.ui.theme.AppAnimation
import com.example.ui.theme.appColors
import com.example.ui.theme.appSpacing
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PracticeScreen(
    uiState: PracticeHomeUiState,
    onStartPractice: (type: String, topic: String, count: Int) -> Unit,
    onNavigateToResult: (String) -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToFlashcards: () -> Unit,
    onNavigateToMockInterview: () -> Unit,
    onNavigateToSpeaking: () -> Unit = {},
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showRandomDialog by remember { mutableStateOf(false) }
    var showTopicDialog by remember { mutableStateOf(false) }
    var selectedRandomCount by remember { mutableIntStateOf(20) }

    Scaffold(
        modifier = modifier.testTag("practice_screen"),
        topBar = {
            AppTopBar(
                title = "Practice",
                subtitle = "Civics exam practice & simulations",
                showCivicEmblem = true,
                showBackButton = false,
                showSettingsButton = true,
                onSettingsClick = onNavigateToSettings
            )
        }
    ) { innerPadding ->
        AnimatedVisibility(
            visible = true,
            enter = AppAnimation.screenEnter()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 680.dp)
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        // 1. Flashcards Quick Action Card
                        AppCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToFlashcards() }
                                .testTag("practice_flashcards_card"),
                            shape = RoundedCornerShape(16.dp),
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
                            elevation = 0.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Style,
                                            contentDescription = "Flashcards",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Civics Flashcards",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Flip and review cards across all 128 questions",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Open Flashcards",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // 2. Section Header: Practice Modes
                        SectionHeader(
                            title = "Practice Modes",
                            subtitle = "Prepare with exam simulations and targeted drills"
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Full Practice Test — DOMINANT Primary Action
                        PracticeActionCard(
                            title = "Full Practice Test",
                            description = "Official simulation: 20 questions, 12 correct needed to pass.",
                            subtitleNotice = "Official 2026 Test Simulation",
                            badge = "20 Qs",
                            icon = Icons.AutoMirrored.Filled.Rule,
                            buttonText = "Start Full Test",
                            highlighted = true,
                            testTag = "practice_card_full",
                            onButtonClick = {
                                onStartPractice("FULL", "", 20)
                            }
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Quick Practice — 10 Qs
                        PracticeActionCard(
                            title = "Quick Practice",
                            description = "Rapid 10-question practice session to test retention.",
                            badge = "10 Qs",
                            icon = Icons.Default.Speed,
                            buttonText = "Start Quick Practice",
                            testTag = "practice_card_quick",
                            onButtonClick = {
                                onStartPractice("QUICK", "", 10)
                            }
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Practice by Topic
                        PracticeActionCard(
                            title = "Study by Topic",
                            description = "Focus on one civics category (${uiState.governmentCount} Gov, ${uiState.historyCount} Hist, ${uiState.civicsCount} Civics).",
                            badge = "3 Topics",
                            icon = Icons.Default.Category,
                            buttonText = "Choose Category",
                            testTag = "practice_card_topic",
                            onButtonClick = {
                                showTopicDialog = true
                            }
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // 65/20 Special Consideration Practice
                        PracticeActionCard(
                            title = "65/20 Practice",
                            description = "Practice the 20 designated questions for applicants 65+ with 20+ years LPR.",
                            subtitleNotice = "Senior Exemption",
                            badge = "20 Qs",
                            icon = Icons.Default.Star,
                            buttonText = "Start 65/20 Practice",
                            testTag = "practice_card_65_20",
                            onButtonClick = {
                                onStartPractice("SENIOR_65_20", "", 20)
                            }
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Favorites Practice
                        PracticeActionCard(
                            title = "Favorites Practice",
                            description = if (uiState.favoriteCount > 0) {
                                "${uiState.favoriteCount} questions saved in your favorites list."
                            } else {
                                "Tap the heart on questions to practice them in this mode."
                            },
                            badge = "${uiState.favoriteCount} Qs",
                            icon = Icons.Default.Bookmark,
                            buttonText = "Practice Favorites",
                            enabled = uiState.favoriteCount > 0,
                            testTag = "practice_card_favorites",
                            onButtonClick = {
                                onStartPractice("FAVORITES", "", uiState.favoriteCount)
                            }
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Needs Practice (Mistakes)
                        PracticeActionCard(
                            title = "Needs Practice",
                            description = if (uiState.needsPracticeCount > 0) {
                                "${uiState.needsPracticeCount} questions missed in practice tests."
                            } else {
                                "No questions currently flagged for extra practice."
                            },
                            badge = "${uiState.needsPracticeCount} Qs",
                            icon = Icons.Default.WarningAmber,
                            buttonText = "Review Missed Questions",
                            enabled = uiState.needsPracticeCount > 0,
                            testTag = "practice_card_needs_practice",
                            onButtonClick = {
                                onStartPractice("NEEDS_PRACTICE", "", uiState.needsPracticeCount)
                            }
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Speaking Practice — Oral Interview Simulation
                        PracticeActionCard(
                            title = "Speaking Practice",
                            description = "Practice questions aloud with voice recognition.",
                            subtitleNotice = "Oral Civics Interview",
                            badge = "Voice Mode",
                            icon = Icons.Default.Mic,
                            buttonText = "Start Speaking Practice",
                            highlighted = false,
                            testTag = "practice_card_speaking",
                            onButtonClick = onNavigateToSpeaking
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Random Practice
                        PracticeActionCard(
                            title = "Random Practice",
                            description = "Customize number of randomized questions from all categories.",
                            badge = "Custom",
                            icon = Icons.Default.Shuffle,
                            buttonText = "Configure & Start",
                            testTag = "practice_card_random",
                            onButtonClick = {
                                showRandomDialog = true
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // 3. Practice History Section
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Recent Test History",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (uiState.recentResults.isNotEmpty()) {
                                TextButton(
                                    onClick = onNavigateToHistory,
                                    modifier = Modifier
                                        .defaultMinSize(minHeight = 48.dp)
                                        .testTag("practice_view_all_history")
                                ) {
                                    Text(
                                        text = "View All",
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (uiState.recentResults.isEmpty()) {
                            AppCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("practice_history_empty_card"),
                                shape = RoundedCornerShape(16.dp),
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                elevation = 0.dp
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "No practice tests completed yet",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Take a Full Test or Quick Practice session to record your scores.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                uiState.recentResults.take(3).forEach { result ->
                                    HistoryItemCard(
                                        result = result,
                                        onClick = { onNavigateToResult(result.id) }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }

    // Random Count Selector Dialog
    if (showRandomDialog) {
        AlertDialog(
            onDismissRequest = { showRandomDialog = false },
            title = {
                Text(
                    text = "Random Practice",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column {
                    Text(
                        text = "Select number of questions for this session:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    listOf(10, 20, 30, 50).forEach { count ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { selectedRandomCount = count }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedRandomCount == count,
                                onClick = { selectedRandomCount = count }
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "$count questions",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRandomDialog = false
                        onStartPractice("RANDOM", "", selectedRandomCount)
                    },
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                ) {
                    Text("Start")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showRandomDialog = false },
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Topic Selector Dialog
    if (showTopicDialog) {
        AlertDialog(
            onDismissRequest = { showTopicDialog = false },
            title = {
                Text(
                    text = "Choose Civics Topic",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    TopicSelectionRow(
                        title = "American Government",
                        count = uiState.governmentCount,
                        onClick = {
                            showTopicDialog = false
                            onStartPractice("TOPIC", "American Government", 20)
                        }
                    )
                    TopicSelectionRow(
                        title = "American History",
                        count = uiState.historyCount,
                        onClick = {
                            showTopicDialog = false
                            onStartPractice("TOPIC", "American History", 20)
                        }
                    )
                    TopicSelectionRow(
                        title = "Integrated Civics",
                        count = uiState.civicsCount,
                        onClick = {
                            showTopicDialog = false
                            onStartPractice("TOPIC", "Integrated Civics", 10)
                        }
                    )
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(
                    onClick = { showTopicDialog = false },
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun PracticeActionCard(
    title: String,
    description: String,
    badge: String,
    icon: ImageVector,
    buttonText: String,
    onButtonClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitleNotice: String? = null,
    enabled: Boolean = true,
    highlighted: Boolean = false,
    testTag: String = ""
) {
    AppCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        containerColor = if (highlighted) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        border = BorderStroke(
            1.dp,
            if (highlighted) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            }
        ),
        elevation = if (highlighted) 1.dp else 0.5.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (highlighted) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (highlighted) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (subtitleNotice != null) {
                            Text(
                                text = subtitleNotice,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                ) {
                    Text(
                        text = badge,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 15.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(6.dp))

            Button(
                onClick = onButtonClick,
                enabled = enabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 44.dp)
                    .testTag("${testTag}_button"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (highlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                )
            ) {
                Text(
                    text = buttonText,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
fun TopicSelectionRow(
    title: String,
    count: Int,
    onClick: () -> Unit
) {
    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        elevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "$count official questions",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun HistoryItemCard(
    result: PracticeResultEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault()) }
    val formattedDate = remember(result.completedAt) {
        dateFormat.format(Date(result.completedAt))
    }

    AppCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("practice_history_item_${result.id}"),
        shape = RoundedCornerShape(16.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = 0.5.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = result.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${result.correctAnswers} / ${result.totalQuestions}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${result.percentage}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (result.passed) MaterialTheme.appColors.successContainer
                    else MaterialTheme.appColors.warningContainer
                ) {
                    Text(
                        text = if (result.passed) "PASSED" else "REVIEW",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (result.passed) MaterialTheme.appColors.onSuccessContainer
                        else MaterialTheme.appColors.onWarningContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}
