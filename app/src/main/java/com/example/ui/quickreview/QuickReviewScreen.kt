package com.example.ui.quickreview

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.learning.MasteryStatus
import com.example.data.model.StudyQuestionItem
import com.example.ui.components.AppTopBar

private val ColorMastered = Color(0xFF2E7D32)
private val ColorNeedsPractice = Color(0xFFE65100)
private val ColorLearning = Color(0xFF1565C0)
private val ColorNotStarted = Color(0xFF757575)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickReviewScreen(
    uiState: QuickReviewUiState,
    onBackClick: () -> Unit,
    onSectionSelected: (QuickReviewSection) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onQuestionClick: (String) -> Unit,
    onPracticeNeedsWork: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("quick_review_screen"),
        topBar = {
            AppTopBar(
                title = "Quick Review",
                subtitle = "Targeted review & focused refresh",
                showCivicEmblem = false,
                showBackButton = true,
                onBackClick = onBackClick,
                backButtonTestTag = "quick_review_back_button"
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Summary Stat Tiles
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ReviewStatTile(
                        icon = "⚠️",
                        title = "Needs Work",
                        count = uiState.needsPracticeQuestions.size,
                        isSelected = uiState.selectedSection == QuickReviewSection.NEEDS_PRACTICE,
                        onClick = { onSectionSelected(QuickReviewSection.NEEDS_PRACTICE) },
                        modifier = Modifier.weight(1f)
                    )
                    ReviewStatTile(
                        icon = "❤️",
                        title = "Favorites",
                        count = uiState.favoriteQuestions.size,
                        isSelected = uiState.selectedSection == QuickReviewSection.FAVORITES,
                        onClick = { onSectionSelected(QuickReviewSection.FAVORITES) },
                        modifier = Modifier.weight(1f)
                    )
                    ReviewStatTile(
                        icon = "🧠",
                        title = "Missed",
                        count = uiState.recentlyMissedQuestions.size,
                        isSelected = uiState.selectedSection == QuickReviewSection.RECENTLY_MISSED,
                        onClick = { onSectionSelected(QuickReviewSection.RECENTLY_MISSED) },
                        modifier = Modifier.weight(1f)
                    )
                    ReviewStatTile(
                        icon = "🏆",
                        title = "Mastered",
                        count = uiState.masteredQuestions.size,
                        isSelected = uiState.selectedSection == QuickReviewSection.MASTERED,
                        onClick = { onSectionSelected(QuickReviewSection.MASTERED) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Quick Action if there are questions that need practice
            if (uiState.needsPracticeQuestions.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Practice Weak Areas",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Text(
                                    text = "${uiState.needsPracticeQuestions.size} questions need extra practice",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Button(
                                onClick = onPracticeNeedsWork,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("quick_review_practice_weak_button")
                            ) {
                                Text("Practice Now")
                            }
                        }
                    }
                }
            }

            // Section Filter Chips
            item {
                val scrollState = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    QuickReviewSection.entries.forEach { section ->
                        val count = when (section) {
                            QuickReviewSection.ALL -> 128
                            QuickReviewSection.NEEDS_PRACTICE -> uiState.needsPracticeQuestions.size
                            QuickReviewSection.FAVORITES -> uiState.favoriteQuestions.size
                            QuickReviewSection.RECENTLY_MISSED -> uiState.recentlyMissedQuestions.size
                            QuickReviewSection.MASTERED -> uiState.masteredQuestions.size
                            QuickReviewSection.RECENTLY_VIEWED -> uiState.recentlyViewedQuestions.size
                        }
                        val isSelected = uiState.selectedSection == section
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSectionSelected(section) },
                            label = { Text("${section.icon} ${section.displayName} ($count)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.testTag("quick_review_filter_${section.name.lowercase()}")
                        )
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = onSearchQueryChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("quick_review_search_input"),
                    placeholder = { Text("Search questions in ${uiState.selectedSection.displayName}...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChanged("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )
            }

            // Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${uiState.selectedSection.displayName} (${uiState.currentDisplayQuestions.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Official Civics Questions",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Empty State
            if (uiState.currentDisplayQuestions.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = uiState.selectedSection.icon,
                                style = MaterialTheme.typography.headlineMedium
                            )
                            Text(
                                text = when (uiState.selectedSection) {
                                    QuickReviewSection.NEEDS_PRACTICE -> "No questions need practice yet. Complete a practice session and missed questions will appear here."
                                    QuickReviewSection.FAVORITES -> "You haven't saved any questions yet."
                                    QuickReviewSection.MASTERED -> "Keep practicing. Your mastered questions will appear here."
                                    QuickReviewSection.RECENTLY_MISSED -> "No missed questions. Great job on your practice sessions!"
                                    QuickReviewSection.RECENTLY_VIEWED -> "No recently viewed questions. Start studying to review them here."
                                    QuickReviewSection.ALL -> "No questions found matching your search."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(
                    items = uiState.currentDisplayQuestions,
                    key = { it.id }
                ) { question ->
                    QuickReviewQuestionCard(
                        question = question,
                        onClick = { onQuestionClick(question.id) },
                        onToggleFavorite = { onToggleFavorite(question.id) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ReviewStatTile(
    icon: String,
    title: String,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(text = icon, style = MaterialTheme.typography.titleMedium)
            Text(
                text = "$count",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun QuickReviewQuestionCard(
    question: StudyQuestionItem,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag("quick_review_card_${question.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "#${question.questionNumber}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (question.is65_20) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer
                        ) {
                            Text(
                                text = "65/20",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = question.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (question.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (question.isFavorite) "Remove favorite" else "Save favorite",
                        tint = if (question.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Text(
                text = question.questionText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val (icon, label, color) = when (question.masteryStatus) {
                    MasteryStatus.MASTERED -> Triple(Icons.Default.CheckCircle, "Mastered", ColorMastered)
                    MasteryStatus.NEEDS_PRACTICE -> Triple(Icons.Default.Warning, "Needs Practice", ColorNeedsPractice)
                    MasteryStatus.LEARNING -> Triple(Icons.Default.Psychology, "Learning", ColorLearning)
                    MasteryStatus.NOT_STARTED -> Triple(Icons.Default.RadioButtonUnchecked, "Not Started", ColorNotStarted)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = color
                    )
                }

                if (question.incorrectCount > 0) {
                    Text(
                        text = "Missed ${question.incorrectCount}x",
                        style = MaterialTheme.typography.labelSmall,
                        color = ColorNeedsPractice
                    )
                }
            }
        }
    }
}
