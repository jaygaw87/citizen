package com.example.ui.practice

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ReviewAnswerItem
import com.example.ui.components.AppCard
import com.example.ui.components.AppCategoryChip
import com.example.ui.components.AppTopBar
import com.example.ui.theme.AppAnimation
import com.example.ui.theme.appColors
import com.example.ui.theme.appSpacing
import com.example.ui.theme.studyTypography

enum class ReviewFilter {
    ALL,
    CORRECT,
    MISTAKES
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewAnswersScreen(
    uiState: PracticeResultUiState,
    initialFilter: ReviewFilter = ReviewFilter.ALL,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf(initialFilter) }
    var expandedQuestionId by remember { mutableStateOf<String?>(null) }

    val filteredItems = remember(uiState.reviewItems, selectedFilter) {
        when (selectedFilter) {
            ReviewFilter.ALL -> uiState.reviewItems
            ReviewFilter.CORRECT -> uiState.reviewItems.filter { it.isCorrect }
            ReviewFilter.MISTAKES -> uiState.reviewItems.filter { !it.isCorrect }
        }
    }

    Scaffold(
        modifier = modifier.testTag("review_answers_screen"),
        topBar = {
            AppTopBar(
                title = "Review Answers",
                subtitle = "Practice test detailed breakdown",
                showCivicEmblem = false,
                showBackButton = true,
                onBackClick = onBackClick,
                backButtonTestTag = "review_back_button"
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
                            .testTag("review_loading_indicator"),
                        color = MaterialTheme.colorScheme.primary
                    )
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
                            Column(modifier = Modifier.fillMaxWidth()) {
                                // Filter chips row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    FilterChip(
                                        selected = selectedFilter == ReviewFilter.ALL,
                                        onClick = { selectedFilter = ReviewFilter.ALL },
                                        label = {
                                            Text(
                                                text = "All (${uiState.reviewItems.size})",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = if (selectedFilter == ReviewFilter.ALL) FontWeight.Bold else FontWeight.Medium
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .defaultMinSize(minHeight = 48.dp)
                                            .testTag("review_filter_all")
                                    )
                                    FilterChip(
                                        selected = selectedFilter == ReviewFilter.CORRECT,
                                        onClick = { selectedFilter = ReviewFilter.CORRECT },
                                        label = {
                                            Text(
                                                text = "Correct (${uiState.correctCount})",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = if (selectedFilter == ReviewFilter.CORRECT) FontWeight.Bold else FontWeight.Medium
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.appColors.successContainer,
                                            selectedLabelColor = MaterialTheme.appColors.onSuccessContainer
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .defaultMinSize(minHeight = 48.dp)
                                            .testTag("review_filter_correct")
                                    )
                                    FilterChip(
                                        selected = selectedFilter == ReviewFilter.MISTAKES,
                                        onClick = { selectedFilter = ReviewFilter.MISTAKES },
                                        label = {
                                            Text(
                                                text = "Needs Review (${uiState.incorrectCount})",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = if (selectedFilter == ReviewFilter.MISTAKES) FontWeight.Bold else FontWeight.Medium
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                                            selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .defaultMinSize(minHeight = 48.dp)
                                            .testTag("review_filter_mistakes")
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }

                        if (filteredItems.isEmpty()) {
                            item {
                                AppCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                    elevation = 0.dp
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (selectedFilter == ReviewFilter.MISTAKES) {
                                                "No mistakes to review in this session!"
                                            } else {
                                                "No questions found for this filter."
                                            },
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        } else {
                            items(filteredItems, key = { it.questionId }) { item ->
                                ReviewItemCard(
                                    item = item,
                                    isExpanded = expandedQuestionId == item.questionId,
                                    onToggleExpand = {
                                        expandedQuestionId = if (expandedQuestionId == item.questionId) null else item.questionId
                                    }
                                )
                            }
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

@Composable
fun ReviewItemCard(
    item: ReviewAnswerItem,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleExpand() }
            .testTag("review_item_${item.questionId}"),
        shape = RoundedCornerShape(18.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.dp,
            if (item.isCorrect) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.error.copy(alpha = 0.35f)
        ),
        elevation = 0.5.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
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
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                if (item.isCorrect) MaterialTheme.appColors.successContainer
                                else MaterialTheme.colorScheme.errorContainer
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (item.isCorrect) Icons.Default.Check else Icons.Default.Close,
                            contentDescription = if (item.isCorrect) "Correct" else "Needs Review",
                            tint = if (item.isCorrect) MaterialTheme.appColors.onSuccessContainer else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = "Question ${item.questionNumber}",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (item.isCorrect) MaterialTheme.appColors.successContainer
                    else MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        text = if (item.isCorrect) "Correct" else "Needs Review",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (item.isCorrect) MaterialTheme.appColors.onSuccessContainer else MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = item.questionText,
                style = MaterialTheme.studyTypography.questionText.copy(fontSize = 17.sp, lineHeight = 24.sp),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppCategoryChip(category = item.category)

                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(animationSpec = tween(250)),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Accepted Answers Box
                    AppCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                        elevation = 0.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.VerifiedUser,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "USCIS ACCEPTED ANSWERS",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            item.allAnswers.forEach { ans ->
                                Text(
                                    text = "• $ans",
                                    style = MaterialTheme.studyTypography.answerText.copy(fontSize = 15.sp, lineHeight = 20.sp),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }
                    }

                    // Explanation Box
                    if (item.explanation.isNotBlank()) {
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
                                    .padding(14.dp),
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
                                        text = item.explanation,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
