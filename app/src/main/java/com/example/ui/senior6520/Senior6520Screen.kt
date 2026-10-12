package com.example.ui.senior6520

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.learning.MasteryStatus
import com.example.data.model.StudyQuestionItem
import com.example.ui.components.AppCard
import com.example.ui.components.AppLinearProgressBar
import com.example.ui.components.AppStatusChip
import com.example.ui.components.AppTopBar
import com.example.ui.components.EmptyState
import com.example.ui.components.ProgressStatus
import com.example.ui.components.QuestionStudyStatus
import com.example.ui.components.SectionHeader
import com.example.ui.components.StudyActionCard
import com.example.ui.theme.appColors

private val ColorMastered = Color(0xFF2E7D32)
private val ColorNeedsPractice = Color(0xFFD32F2F)
private val ColorLearning = Color(0xFF1565C0)
private val ColorNotStarted = Color(0xFF757575)

@Composable
fun Senior6520Screen(
    uiState: Senior6520UiState,
    onBackClick: () -> Unit,
    onFilterSelected: (SeniorFilter) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onQuestionClick: (String) -> Unit,
    onStudyClick: () -> Unit,
    onPracticeClick: (mode: String, count: Int) -> Unit,
    onFlashcardsClick: () -> Unit,
    onSpeakingClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showDisclaimerDialog by remember { mutableStateOf(false) }
    var showPracticeDialog by remember { mutableStateOf(false) }

    if (showPracticeDialog) {
        AlertDialog(
            onDismissRequest = { showPracticeDialog = false },
            title = {
                Text(
                    text = "65/20 Practice Options",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Select practice format for the 20 designated civics questions:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showPracticeDialog = false
                                onPracticeClick("FULL", 20)
                            },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Full 65/20 Practice (20 Questions)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Practice all 20 designated senior exemption questions",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showPracticeDialog = false
                                onPracticeClick("QUICK", 10)
                            },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Quick 10 Practice (10 Questions)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Simulate the 10-question USCIS oral interview format (pass with 6/10)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPracticeDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showDisclaimerDialog) {
        AlertDialog(
            onDismissRequest = { showDisclaimerDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("65/20 Exemption Eligibility")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "According to USCIS naturalization policy, you qualify for the 65/20 special consideration if:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "• You are age 65 or older at the time of filing Form N-400\n" +
                            "• You have lived in the United States as a lawful permanent resident (Green Card holder) for at least 20 years",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Under this exemption, you study only the 20 designated civics questions marked with an asterisk (*). You are asked up to 10 questions and must answer 6 correctly to pass. You may also take the civics test in your native language with an interpreter.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://www.uscis.gov/citizenship/find-study-materials-and-resources/study-for-the-test")
                        )
                        try {
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                        showDisclaimerDialog = false
                    }
                ) {
                    Icon(imageVector = Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Official USCIS Website")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDisclaimerDialog = false }) {
                    Text("Got It")
                }
            }
        )
    }

    Scaffold(
        modifier = modifier.testTag("senior_6520_screen"),
        topBar = {
            AppTopBar(
                title = "65/20 Special Consideration",
                subtitle = "20 designated civics questions",
                showCivicEmblem = false,
                showBackButton = true,
                onBackClick = onBackClick,
                actions = {
                    IconButton(
                        onClick = { showDisclaimerDialog = true },
                        modifier = Modifier
                            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                            .testTag("senior_disclaimer_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "USCIS Eligibility Info",
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("senior_question_list"),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 680.dp)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 1. TOP: ELIGIBILITY EXPLANATION BANNER
                    EligibilityBanner(onOpenDisclaimer = { showDisclaimerDialog = true })

                    // 2. PRIMARY CARD: 65/20 READINESS METRICS
                    SeniorProgressPrimaryCard(progress = uiState.progress)

                    // 3. 65/20 ACTION GRID (Study, Practice, Flashcards, Speaking)
                    SectionHeader(
                        title = "Study & Practice Modes",
                        subtitle = "Prepare the 20 designated questions"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StudyActionCard(
                            title = "Study 65/20",
                            description = "Read 20 questions",
                            icon = Icons.AutoMirrored.Filled.MenuBook,
                            onClick = onStudyClick,
                            accentColor = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.weight(1f),
                            testTag = "action_study_6520"
                        )
                        StudyActionCard(
                            title = "Practice Test",
                            description = "Full or Quick test",
                            icon = Icons.Default.FitnessCenter,
                            onClick = { showPracticeDialog = true },
                            accentColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f),
                            testTag = "action_practice_6520"
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StudyActionCard(
                            title = "Flashcards",
                            description = "20 senior cards",
                            icon = Icons.Default.Style,
                            onClick = onFlashcardsClick,
                            accentColor = ColorMastered,
                            modifier = Modifier.weight(1f),
                            testTag = "action_flashcards_6520"
                        )
                        StudyActionCard(
                            title = "Speaking",
                            description = "Oral practice",
                            icon = Icons.Default.Mic,
                            onClick = onSpeakingClick,
                            accentColor = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.weight(1f),
                            testTag = "action_speaking_6520"
                        )
                    }

                    // 4. REVIEW SUMMARY CHIPS (Favorites & Needs Practice live counts)
                    SeniorReviewSummaryRow(
                        questions = uiState.allQuestions,
                        selectedFilter = uiState.selectedFilter,
                        onFilterSelected = onFilterSelected
                    )

                    // 5. SEARCH FIELD
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = onSearchQueryChanged,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("senior_search_field"),
                        placeholder = { Text("Search 20 senior questions...", style = MaterialTheme.typography.bodyMedium) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                        },
                        trailingIcon = {
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchQueryChanged("") }) {
                                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear search", modifier = Modifier.size(18.dp))
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )

                    // 6. FILTER CHIPS ROW
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = uiState.selectedFilter == SeniorFilter.ALL,
                            onClick = { onFilterSelected(SeniorFilter.ALL) },
                            label = { Text("All 20", fontSize = 12.sp) },
                            modifier = Modifier.testTag("senior_filter_all"),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        )
                        FilterChip(
                            selected = uiState.selectedFilter == SeniorFilter.NEEDS_PRACTICE,
                            onClick = { onFilterSelected(SeniorFilter.NEEDS_PRACTICE) },
                            label = { Text("Needs Practice", fontSize = 12.sp) },
                            modifier = Modifier.testTag("senior_filter_needs_practice"),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer
                            )
                        )
                        FilterChip(
                            selected = uiState.selectedFilter == SeniorFilter.MASTERED,
                            onClick = { onFilterSelected(SeniorFilter.MASTERED) },
                            label = { Text("Mastered", fontSize = 12.sp) },
                            modifier = Modifier.testTag("senior_filter_mastered")
                        )
                        FilterChip(
                            selected = uiState.selectedFilter == SeniorFilter.FAVORITES,
                            onClick = { onFilterSelected(SeniorFilter.FAVORITES) },
                            label = { Text("Favorites", fontSize = 12.sp) },
                            modifier = Modifier.testTag("senior_filter_favorites")
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))
                }
            }

            // 7. QUESTION LIST
            if (uiState.filteredQuestions.isEmpty()) {
                item {
                    Box(modifier = Modifier.padding(16.dp)) {
                        EmptyState(
                            title = "No Questions Match",
                            message = if (uiState.searchQuery.isNotBlank()) {
                                "No 65/20 questions match \"${uiState.searchQuery}\"."
                            } else {
                                "No questions in this filter category."
                            },
                            testTag = "senior_empty_state"
                        )
                    }
                }
            } else {
                items(
                    items = uiState.filteredQuestions,
                    key = { it.id }
                ) { question ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 680.dp)
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        SeniorQuestionRow(
                            question = question,
                            onClick = { onQuestionClick(question.id) },
                            onToggleFavorite = { onToggleFavorite(question.id) }
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun EligibilityBanner(onOpenDisclaimer: () -> Unit) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.40f)),
        elevation = 0.5.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Applicants age 65+ with 20+ years as permanent residents study only 20 designated civics questions.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
            }

            TextButton(
                onClick = onOpenDisclaimer,
                modifier = Modifier.height(36.dp)
            ) {
                Text(
                    text = "Details",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

@Composable
private fun SeniorProgressPrimaryCard(progress: com.example.domain.usecase.LearningProgressOverview) {
    val studied = progress.senior6520Studied
    val total = progress.senior6520Total // 20
    val percentage = if (total > 0) ((studied.toFloat() / total) * 100).toInt() else 0
    val mastered = progress.senior6520Mastered
    val needsPractice = progress.senior6520NeedsPractice
    val notStarted = (total - studied).coerceAtLeast(0)

    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("senior_progress_card"),
        shape = RoundedCornerShape(16.dp),
        elevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "65/20 Readiness",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$studied / $total Questions Studied",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer
                ) {
                    Text(
                        text = "$percentage%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val animProgress by animateFloatAsState(
                targetValue = (percentage / 100f).coerceIn(0f, 1f),
                animationSpec = tween(500),
                label = "seniorProgress"
            )

            AppLinearProgressBar(
                progress = animProgress,
                status = if (percentage >= 60) ProgressStatus.Completed else ProgressStatus.Normal,
                height = 8.dp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SeniorStatPill(label = "Studied", count = studied, color = MaterialTheme.colorScheme.tertiary)
                SeniorStatPill(label = "Mastered", count = mastered, color = ColorMastered)
                SeniorStatPill(label = "Needs Practice", count = needsPractice, color = ColorNeedsPractice)
                SeniorStatPill(label = "Not Started", count = notStarted, color = ColorNotStarted)
            }
        }
    }
}

@Composable
private fun SeniorReviewSummaryRow(
    questions: List<StudyQuestionItem>,
    selectedFilter: SeniorFilter,
    onFilterSelected: (SeniorFilter) -> Unit
) {
    val favoritesCount = questions.count { it.isFavorite }
    val needsPracticeCount = questions.count { it.needsPractice || it.masteryStatus == MasteryStatus.NEEDS_PRACTICE }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Card(
            modifier = Modifier
                .weight(1f)
                .clickable { onFilterSelected(SeniorFilter.FAVORITES) },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (selectedFilter == SeniorFilter.FAVORITES) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                } else {
                    MaterialTheme.colorScheme.surface
                }
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Bookmark,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "$favoritesCount Favorites",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (favoritesCount > 0) "Review saved" else "None saved",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Card(
            modifier = Modifier
                .weight(1f)
                .clickable { onFilterSelected(SeniorFilter.NEEDS_PRACTICE) },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (selectedFilter == SeniorFilter.NEEDS_PRACTICE) {
                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                } else {
                    MaterialTheme.colorScheme.surface
                }
            ),
            border = BorderStroke(
                1.dp,
                if (needsPracticeCount > 0) MaterialTheme.colorScheme.error.copy(alpha = 0.35f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (needsPracticeCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "$needsPracticeCount Needs Practice",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (needsPracticeCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (needsPracticeCount > 0) "Missed questions" else "All clear",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SeniorQuestionRow(
    question: StudyQuestionItem,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    AppCard(
        modifier = Modifier.fillMaxWidth(),
        testTag = "senior_question_${question.questionNumber}",
        shape = RoundedCornerShape(14.dp),
        onClick = onClick,
        elevation = 0.5.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 65/20 question number badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "${question.questionNumber}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = question.questionText,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = if (expanded) 6 else 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val status = when (question.masteryStatus) {
                            MasteryStatus.MASTERED -> QuestionStudyStatus.Mastered
                            MasteryStatus.NEEDS_PRACTICE -> QuestionStudyStatus.NeedsPractice
                            MasteryStatus.LEARNING -> QuestionStudyStatus.Learning
                            else -> QuestionStudyStatus.NotStarted
                        }
                        AppStatusChip(status = status)

                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${question.answers.size} accepted answers",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { expanded = !expanded },
                        modifier = Modifier
                            .size(40.dp)
                            .defaultMinSize(minWidth = 40.dp, minHeight = 40.dp)
                    ) {
                        Icon(
                            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (expanded) "Collapse answers" else "Expand answers",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier
                            .size(44.dp)
                            .defaultMinSize(minWidth = 44.dp, minHeight = 44.dp)
                            .testTag("senior_fav_${question.questionNumber}")
                    ) {
                        Icon(
                            imageVector = if (question.isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = if (question.isFavorite) "Remove from favorites" else "Add to favorites",
                            tint = if (question.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, start = 48.dp)
                ) {
                    Text(
                        text = "Official Accepted Answers:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    question.answers.forEach { ans ->
                        Row(
                            modifier = Modifier.padding(vertical = 2.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "• ",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = ans.answerText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SeniorStatPill(
    label: String,
    count: Int,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "$count",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp
        )
    }
}
