package com.example.ui.vocabulary

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.data.model.VocabularyItem
import com.example.data.model.VocabularyStatus
import com.example.ui.components.AppTopBar

private val ColorLearned = Color(0xFF2E7D32)
private val ColorNeedsReview = Color(0xFFE65100)
private val ColorLearning = Color(0xFF1565C0)
private val ColorNotStarted = Color(0xFF757575)

private fun getCategoryEmoji(category: String): String = when (category) {
    "American Government" -> "🏛 "
    "Rights & Responsibilities" -> "🇺🇸 "
    "American History" -> "📜 "
    "Geography & Integrated Civics", "Integrated Civics", "Geography & Civics" -> "🗺 "
    else -> ""
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VocabularyScreen(
    uiState: VocabularyUiState,
    onBackClick: () -> Unit,
    onFilterSelected: (VocabFilter) -> Unit,
    onCategorySelected: (String) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onVocabularyClick: (String) -> Unit,
    onQuickReviewClick: (String) -> Unit = {},
    onFlashcardsClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("vocabulary_screen"),
        topBar = {
            AppTopBar(
                title = "Civics Vocabulary",
                subtitle = "Educational Supplement for Test Terms",
                showCivicEmblem = false,
                showBackButton = true,
                onBackClick = onBackClick,
                backButtonTestTag = "vocab_back_button"
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
            // Educational Notice Banner
            item {
                VocabularyEducationalBanner()
            }

            // Vocabulary Progress Card
            item {
                VocabularyProgressCard(progress = uiState.progress)
            }

            // Quick Review & Flashcards Action Section
            item {
                VocabularyQuickReviewSection(
                    onContinueLearning = { onQuickReviewClick("ALL") },
                    onReviewFavorites = { onQuickReviewClick("FAVORITES") },
                    onReviewNeedsReview = { onQuickReviewClick("NEEDS_REVIEW") },
                    onOpenFlashcards = { onFlashcardsClick("ALL") },
                    favoritesCount = uiState.progress.favoriteCount,
                    needsReviewCount = uiState.progress.needsReviewCount
                )
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = onSearchQueryChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("vocab_search_input"),
                    placeholder = { Text("Search words, definitions, or topics...") },
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

            // Status Filter Chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = uiState.selectedFilter == VocabFilter.ALL,
                        onClick = { onFilterSelected(VocabFilter.ALL) },
                        label = { Text("All (${uiState.progress.totalCount})") },
                        modifier = Modifier.testTag("vocab_filter_all")
                    )
                    FilterChip(
                        selected = uiState.selectedFilter == VocabFilter.FAVORITES,
                        onClick = { onFilterSelected(VocabFilter.FAVORITES) },
                        label = { Text("Saved (${uiState.progress.favoriteCount})") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Favorite,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                        },
                        modifier = Modifier.testTag("vocab_filter_favorites")
                    )
                    FilterChip(
                        selected = uiState.selectedFilter == VocabFilter.NEEDS_REVIEW,
                        onClick = { onFilterSelected(VocabFilter.NEEDS_REVIEW) },
                        label = { Text("Review (${uiState.progress.needsReviewCount})") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = ColorNeedsReview
                            )
                        },
                        modifier = Modifier.testTag("vocab_filter_needs_review")
                    )
                    FilterChip(
                        selected = uiState.selectedFilter == VocabFilter.LEARNED,
                        onClick = { onFilterSelected(VocabFilter.LEARNED) },
                        label = { Text("Learned (${uiState.progress.learnedCount})") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = ColorLearned
                            )
                        },
                        modifier = Modifier.testTag("vocab_filter_learned")
                    )
                }
            }

            // Category Chips (Horizontal Scroll)
            item {
                val scrollState = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    uiState.categories.forEach { cat ->
                        val isSelected = uiState.selectedCategory.equals(cat, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = { onCategorySelected(cat) },
                            label = { Text(if (cat == "ALL") "All Categories" else "${getCategoryEmoji(cat)}$cat") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }

            // Section Count Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Vocabulary Words (${uiState.filteredItems.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Alphabetical Order",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Empty state
            if (uiState.filteredItems.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Translate,
                                contentDescription = null,
                                modifier = Modifier.size(36.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "No vocabulary items found",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = when (uiState.selectedFilter) {
                                    VocabFilter.FAVORITES -> "You haven't saved any vocabulary terms to favorites yet."
                                    VocabFilter.NEEDS_REVIEW -> "No terms currently marked for review."
                                    VocabFilter.LEARNED -> "No terms marked as learned yet. Study and mark words as learned!"
                                    else -> "No terms match the selected filters or search text."
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
                    items = uiState.filteredItems,
                    key = { it.id }
                ) { item ->
                    VocabularyCard(
                        item = item,
                        onClick = { onVocabularyClick(item.id) },
                        onToggleFavorite = { onToggleFavorite(item.id) }
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
private fun VocabularyEducationalBanner(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Column {
                Text(
                    text = "Educational Civics Vocabulary",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "This vocabulary is an educational supplement to assist applicants in understanding citizenship concepts. Definitions are simplified for learners and are distinct from official USCIS question text.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun VocabularyProgressCard(
    progress: VocabularyProgressOverview,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Vocabulary Mastery",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${progress.learnedCount} of ${progress.totalCount} Words Learned",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = "${progress.progressPercentage}%",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            LinearProgressIndicator(
                progress = { (progress.progressPercentage / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                VocabMetric(label = "Total", count = progress.totalCount, color = MaterialTheme.colorScheme.primary)
                VocabMetric(label = "Learned", count = progress.learnedCount, color = ColorLearned)
                VocabMetric(label = "Review", count = progress.needsReviewCount, color = ColorNeedsReview)
                VocabMetric(label = "Learning", count = progress.learningCount, color = ColorLearning)
                VocabMetric(label = "Not Started", count = progress.notStartedCount, color = ColorNotStarted)
            }
        }
    }
}

@Composable
private fun VocabMetric(
    label: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "$count",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun VocabularyCard(
    item: VocabularyItem,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag("vocab_card_${item.id}"),
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
            // Header Row: Word, Pronunciation, Favorite
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = item.word.uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (item.pronunciation.isNotBlank()) {
                        Text(
                            text = item.pronunciation,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (item.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (item.isFavorite) "Remove from favorites" else "Add to favorites",
                        tint = if (item.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Simple Definition
            Text(
                text = item.simpleDefinition,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Badges Row: Category, Related questions count, Status
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
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = item.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (item.relatedQuestionIds.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = "${item.relatedQuestionIds.size} Qs",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                VocabStatusBadge(status = item.status)
            }
        }
    }
}

@Composable
private fun VocabStatusBadge(
    status: VocabularyStatus,
    modifier: Modifier = Modifier
) {
    val (icon, label, color) = when (status) {
        VocabularyStatus.LEARNED -> Triple(Icons.Default.CheckCircle, "Learned", ColorLearned)
        VocabularyStatus.NEEDS_REVIEW -> Triple(Icons.Default.Warning, "Review", ColorNeedsReview)
        VocabularyStatus.LEARNING -> Triple(Icons.Default.Psychology, "Learning", ColorLearning)
        VocabularyStatus.NOT_STARTED -> Triple(Icons.Default.RadioButtonUnchecked, "Not Started", ColorNotStarted)
    }

    Row(
        modifier = modifier,
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
}

@Composable
private fun VocabularyQuickReviewSection(
    onContinueLearning: () -> Unit,
    onReviewFavorites: () -> Unit,
    onReviewNeedsReview: () -> Unit,
    onOpenFlashcards: () -> Unit,
    favoritesCount: Int,
    needsReviewCount: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("vocab_quick_review_section"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quick Review",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "10 terms / session",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onContinueLearning,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("vocab_continue_learning_button")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("10-Term Review")
                }

                FilledTonalButton(
                    onClick = onOpenFlashcards,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("vocab_flashcards_button")
                ) {
                    Icon(Icons.Default.Style, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Flashcards")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onReviewFavorites,
                    shape = RoundedCornerShape(10.dp),
                    enabled = favoritesCount > 0,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("vocab_review_favorites_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Favorites ($favoritesCount)", style = MaterialTheme.typography.labelMedium)
                }

                OutlinedButton(
                    onClick = onReviewNeedsReview,
                    shape = RoundedCornerShape(10.dp),
                    enabled = needsReviewCount > 0,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("vocab_review_needs_review_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = ColorNeedsReview
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Needs Review ($needsReviewCount)", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}
