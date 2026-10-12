package com.example.ui.practice

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
fun PracticeHistoryScreen(
    uiState: PracticeHistoryUiState,
    onResultClick: (resultId: String) -> Unit,
    onClearHistory: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = {
                Text(
                    text = "Clear Practice History?",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = "This will permanently remove all past practice test scores and reviews.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showClearConfirmDialog = false
                        onClearHistory()
                    },
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showClearConfirmDialog = false },
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        modifier = modifier.testTag("practice_history_screen"),
        topBar = {
            AppTopBar(
                title = "Practice History",
                subtitle = "Past test sessions & performance",
                showCivicEmblem = false,
                showBackButton = true,
                onBackClick = onBackClick,
                backButtonTestTag = "history_back_button",
                actions = {
                    if (uiState.results.isNotEmpty()) {
                        IconButton(
                            onClick = { showClearConfirmDialog = true },
                            modifier = Modifier
                                .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                                .testTag("history_clear_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Clear History",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
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
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .testTag("history_loading_indicator"),
                        color = MaterialTheme.colorScheme.primary
                    )
                } else if (uiState.results.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 500.dp)
                            .padding(32.dp)
                            .align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            modifier = Modifier.size(72.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Practice History",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Complete a practice session or full test to view your scores and review mistakes here.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = onBackClick,
                            modifier = Modifier
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("history_empty_back_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Start Practice",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .widthIn(max = 680.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            // Summary Stats Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                StatCard(
                                    label = "Tests Taken",
                                    count = uiState.totalTests,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.weight(1f)
                                )
                                StatCard(
                                    label = "Passed",
                                    count = uiState.passedTests,
                                    color = MaterialTheme.appColors.success,
                                    modifier = Modifier.weight(1f)
                                )
                                StatCard(
                                    label = "Avg Score",
                                    count = uiState.averageScore,
                                    color = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        items(uiState.results, key = { it.id }) { result ->
                            HistoryItemCard(
                                result = result,
                                onClick = { onResultClick(result.id) }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(20.dp))
                        }
                    }
                }
            }
        }
    }
}
