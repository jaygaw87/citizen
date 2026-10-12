package com.example.ui.study

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.learning.MasteryStatus
import com.example.data.model.StudyQuestionItem
import com.example.ui.components.AppCard
import com.example.ui.components.AppCategoryChip
import com.example.ui.components.AppStatusChip
import com.example.ui.components.AppTopBar
import com.example.ui.components.CivicEmblem
import com.example.ui.components.QuestionStudyStatus
import com.example.ui.theme.AppAnimation
import com.example.ui.theme.appColors
import com.example.ui.theme.studyTypography

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun StudyScreen(
    uiState: StudyScreenUiState,
    onFilterSelected: (StudyFilter) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onQuestionClick: (String, StudyFilter, String) -> Unit,
    onNavigateToSettings: (() -> Unit)? = null,
    onBackClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Log.i("CivicsDiagnostic", "[UI] questions displayed = ${uiState.questions.size}")

    Scaffold(
        modifier = modifier.testTag("study_screen"),
        topBar = {
            AppTopBar(
                title = "Study Civics Questions",
                subtitle = "Learn the official questions and accepted answers.",
                showCivicEmblem = onBackClick == null,
                showBackButton = onBackClick != null,
                onBackClick = onBackClick,
                backButtonTestTag = "study_back_button",
                showSettingsButton = onNavigateToSettings != null,
                onSettingsClick = onNavigateToSettings,
                settingsButtonTestTag = "study_settings_button"
            )
        }
    ) { innerPadding ->
        AnimatedVisibility(
            visible = true,
            enter = AppAnimation.screenEnter()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 720.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        // 1. Compact Study Progress Header Card (Requirement 3)
                        item {
                            StudyHeaderSection(
                                totalQuestions = uiState.totalCount,
                                studiedCount = uiState.studiedCount,
                                progressPercentage = uiState.progressPercentage,
                                masteredCount = uiState.masteredCount,
                                needsPracticeCount = uiState.needsPracticeCount,
                                notStartedCount = uiState.notStartedCount
                            )
                        }

                        // 2. Sticky Search Field and Filter Bar (Requirements 4 & 5)
                        stickyHeader {
                            Surface(
                                color = MaterialTheme.colorScheme.background,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column {
                                    StudySearchField(
                                        query = uiState.searchQuery,
                                        onQueryChanged = onSearchQueryChanged,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                                    )
                                    StudyFilterBar(
                                        selectedFilter = uiState.selectedFilter,
                                        favoriteCount = uiState.favoriteCount,
                                        needsPracticeCount = uiState.needsPracticeCount,
                                        onFilterSelected = onFilterSelected,
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    )
                                }
                            }
                        }

                        // 3. 65/20 Informational Notice when 65/20 filter is selected
                        if (uiState.selectedFilter == StudyFilter.SENIOR_65_20) {
                            item {
                                SeniorNoticeBanner(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // 4. Loading State
                        if (uiState.isLoading) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(48.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        } else if (uiState.questions.isEmpty()) {
                            // 5. Empty States (Requirement 18)
                            item {
                                StudyEmptyState(
                                    filter = uiState.selectedFilter,
                                    searchQuery = uiState.searchQuery,
                                    onClearFilter = {
                                        onFilterSelected(StudyFilter.ALL)
                                        onSearchQueryChanged("")
                                    }
                                )
                            }
                        } else {
                            // 6. Compact Question List Items (Requirement 6)
                            items(
                                items = uiState.questions,
                                key = { it.id }
                            ) { questionItem ->
                                StudyQuestionCard(
                                    item = questionItem,
                                    onClick = {
                                        onQuestionClick(
                                            questionItem.id,
                                            uiState.selectedFilter,
                                            uiState.searchQuery
                                        )
                                    },
                                    onToggleFavorite = { onToggleFavorite(questionItem.id) },
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Compact Study Progress Header Section (Phase 7C.6 Requirement 3)
 */
@Composable
private fun StudyHeaderSection(
    totalQuestions: Int,
    studiedCount: Int,
    progressPercentage: Int,
    masteredCount: Int,
    needsPracticeCount: Int,
    notStartedCount: Int,
    modifier: Modifier = Modifier
) {
    AppCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("study_progress_card"),
        shape = RoundedCornerShape(16.dp),
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        elevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Studied",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$studiedCount / $totalQuestions",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                ) {
                    Text(
                        text = "$progressPercentage%",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val progressFraction = (progressPercentage / 100f).coerceIn(0f, 1f)
            val animatedProgress by animateFloatAsState(
                targetValue = progressFraction,
                animationSpec = tween(500),
                label = "study_progress"
            )

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                strokeCap = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Compact metrics below progress bar: ✓ Mastered, ⚠ Needs Practice, ○ Not Started
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mastered
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "✓",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.appColors.success
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$masteredCount Mastered",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Needs Practice
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "⚠",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.appColors.warning
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$needsPracticeCount Needs Practice",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Not Started
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "○",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$notStartedCount Not Started",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Compact Material 3 Search Field (Phase 7C.6 Requirement 4)
 */
@Composable
private fun StudySearchField(
    query: String,
    onQueryChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChanged,
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
            .testTag("study_search_field"),
        placeholder = {
            Text(
                text = "Search questions or answers",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(
                    onClick = { onQueryChanged("") },
                    modifier = Modifier
                        .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                        .testTag("study_clear_search_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}

/**
 * Horizontal Filter Chips Bar (Phase 7C.6 Requirement 5)
 */
@Composable
private fun StudyFilterBar(
    selectedFilter: StudyFilter,
    favoriteCount: Int,
    needsPracticeCount: Int,
    onFilterSelected: (StudyFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StudyFilter.entries.forEach { filter ->
            val isSelected = selectedFilter == filter
            val label = when (filter) {
                StudyFilter.FAVORITES -> if (favoriteCount > 0) "Favorites ($favoriteCount)" else "Favorites"
                StudyFilter.NEEDS_PRACTICE -> if (needsPracticeCount > 0) "Needs Practice ($needsPracticeCount)" else "Needs Practice"
                StudyFilter.SENIOR_65_20 -> "65/20"
                else -> filter.displayName
            }

            FilterChip(
                selected = isSelected,
                onClick = { onFilterSelected(filter) },
                label = {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                },
                leadingIcon = if (filter == StudyFilter.FAVORITES) {
                    {
                        Icon(
                            imageVector = if (isSelected) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else if (filter == StudyFilter.SENIOR_65_20) {
                    {
                        Icon(
                            imageVector = Icons.Outlined.Star,
                            contentDescription = null,
                            tint = if (isSelected) MaterialTheme.appColors.onWarningContainer else MaterialTheme.appColors.warning,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = if (filter == StudyFilter.SENIOR_65_20) {
                        MaterialTheme.appColors.warningContainer
                    } else {
                        MaterialTheme.colorScheme.primaryContainer
                    },
                    selectedLabelColor = if (filter == StudyFilter.SENIOR_65_20) {
                        MaterialTheme.appColors.onWarningContainer
                    } else {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    },
                    containerColor = MaterialTheme.colorScheme.surface,
                    labelColor = MaterialTheme.colorScheme.onSurface
                ),
                border = BorderStroke(
                    1.dp,
                    if (isSelected) {
                        if (filter == StudyFilter.SENIOR_65_20) MaterialTheme.appColors.warning else MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    }
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .defaultMinSize(minHeight = 48.dp)
                    .testTag("filter_chip_${filter.name.lowercase()}")
            )
        }
    }
}

/**
 * 65/20 Informational Notice Banner
 */
@Composable
private fun SeniorNoticeBanner(
    modifier: Modifier = Modifier
) {
    AppCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        containerColor = MaterialTheme.appColors.seniorExemptionContainer.copy(alpha = 0.4f),
        border = BorderStroke(1.dp, MaterialTheme.appColors.seniorExemption.copy(alpha = 0.35f)),
        elevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.appColors.seniorExemption,
                modifier = Modifier
                    .size(18.dp)
                    .padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = "65/20 Special Consideration Questions",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Applicants age 65 or older who have been permanent residents for at least 20 years study only these 20 designated questions.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

/**
 * Compact, Scannable Question Card in Question List (Phase 7C.6 Requirements 6-9)
 *
 * Structure:
 * ┌─────────────────────────────┐
 * │ #12       Government   ♥    │
 * │                             │
 * │ What is the supreme law...  │
 * │                             │
 * │ ✓ Studied       65/20       │
 * └─────────────────────────────┘
 */
@Composable
fun StudyQuestionCard(
    item: StudyQuestionItem,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Accessibility TalkBack description (Requirement 20)
    val statusDesc = when {
        item.masteryStatus == MasteryStatus.MASTERED -> "mastered"
        item.masteryStatus == MasteryStatus.NEEDS_PRACTICE -> "needs practice"
        item.isStudied -> "studied"
        else -> "not started"
    }
    val cardContentDescription = "Question ${item.questionNumber}, ${item.category}${if (item.is65_20) ", 65/20" else ""}, $statusDesc"

    AppCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("question_card_${item.questionNumber}")
            .semantics { contentDescription = cardContentDescription },
        shape = RoundedCornerShape(14.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = 0.5.dp,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            // Row 1: #12, Category, Favorite button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // Question Number (#12)
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                    ) {
                        Text(
                            text = "#${item.questionNumber}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Category Chip
                    AppCategoryChip(category = item.category)
                }

                // Favorite button (accessible content descriptions & min 48dp touch target)
                val scale by animateFloatAsState(
                    targetValue = if (item.isFavorite) 1.15f else 1.0f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "fav_scale"
                )
                val tint by animateColorAsState(
                    targetValue = if (item.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    label = "fav_tint"
                )

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                        .scale(scale)
                        .testTag("fav_button_${item.questionNumber}")
                ) {
                    Icon(
                        imageVector = if (item.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (item.isFavorite) "Remove from favorites" else "Add to favorites",
                        tint = tint,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Row 2: Question Text (16sp, Semi-bold, comfortable line height)
            Text(
                text = item.questionText,
                style = MaterialTheme.studyTypography.questionText.copy(
                    fontSize = 16.sp,
                    lineHeight = 22.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Row 3: Status chip + 65/20 small badge + dynamic answer tag if applicable
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val statusChip = when (item.masteryStatus) {
                        MasteryStatus.MASTERED -> QuestionStudyStatus.Mastered
                        MasteryStatus.NEEDS_PRACTICE -> QuestionStudyStatus.NeedsPractice
                        MasteryStatus.LEARNING -> QuestionStudyStatus.Learning
                        MasteryStatus.NOT_STARTED -> if (item.isStudied) QuestionStudyStatus.Learning else QuestionStudyStatus.NotStarted
                    }
                    AppStatusChip(status = statusChip)

                    // Small amber 65/20 badge (Requirement 9)
                    if (item.is65_20) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.appColors.warningContainer,
                            border = BorderStroke(1.dp, MaterialTheme.appColors.warning.copy(alpha = 0.35f)),
                            modifier = Modifier.testTag("app_eligibility_chip")
                        ) {
                            Text(
                                text = "65/20",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.appColors.onWarningContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                if (item.isDynamicAnswer) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = "Dynamic",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Compact Empty State for Search or Filters (Phase 7C.6 Requirement 18)
 */
@Composable
private fun StudyEmptyState(
    filter: StudyFilter,
    searchQuery: String,
    onClearFilter: () -> Unit,
    modifier: Modifier = Modifier
) {
    val icon = when {
        searchQuery.isNotBlank() -> Icons.Outlined.SearchOff
        filter == StudyFilter.FAVORITES -> Icons.Outlined.BookmarkBorder
        filter == StudyFilter.NEEDS_PRACTICE -> Icons.Outlined.CheckCircleOutline
        else -> Icons.Outlined.SearchOff
    }

    val title = when {
        searchQuery.isNotBlank() -> "No questions found"
        filter == StudyFilter.FAVORITES -> "No favorite questions yet"
        filter == StudyFilter.NEEDS_PRACTICE -> "No questions need extra practice yet."
        else -> "No questions found"
    }

    val description = when {
        searchQuery.isNotBlank() -> "Try another search or filter."
        filter == StudyFilter.FAVORITES -> "Tap the heart on any question to save it for later review."
        filter == StudyFilter.NEEDS_PRACTICE -> "Questions you answer incorrectly in practice tests will appear here."
        else -> "Try another search or filter."
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 40.dp)
            .testTag("study_empty_state"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            modifier = Modifier.size(64.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        if (searchQuery.isNotBlank() || filter != StudyFilter.ALL) {
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(
                onClick = onClearFilter,
                modifier = Modifier
                    .defaultMinSize(minHeight = 48.dp)
                    .testTag("clear_filter_button")
            ) {
                Text(
                    text = "Clear Filters",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}
