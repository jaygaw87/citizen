package com.example.ui.progress

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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.StudyActivityEntity
import com.example.data.model.CategoryProgressData
import com.example.data.model.ProgressUiState
import com.example.data.model.ScoreTrendPoint
import com.example.ui.components.AppCard
import com.example.ui.components.AppCategoryChip
import com.example.ui.components.AppLinearProgressBar
import com.example.ui.components.AppTopBar
import com.example.ui.components.EmptyState
import com.example.ui.components.ProgressStatus
import com.example.ui.components.SectionHeader
import com.example.ui.theme.appColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val ColorMastered = Color(0xFF2E7D32)
private val ColorNeedsPractice = Color(0xFFD32F2F)

@Composable
fun ProgressScreen(
    uiState: ProgressUiState,
    onNavigateToCategoryProgress: (String) -> Unit,
    onNavigateToWeakAreas: () -> Unit,
    onNavigateToPracticeWeakAreas: () -> Unit,
    onNavigateTo6520Practice: () -> Unit,
    onNavigateToFavoritesPractice: () -> Unit,
    onNavigateToFlashcards: () -> Unit,
    onNavigateToSpeakingPractice: () -> Unit,
    onNavigateToPracticeHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.testTag("progress_screen"),
        topBar = {
            AppTopBar(
                title = "Progress",
                subtitle = "USCIS civics readiness tracking",
                showCivicEmblem = true,
                showBackButton = false,
                showSettingsButton = true,
                onSettingsClick = onNavigateToSettings
            )
        }
    ) { innerPadding ->
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
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 1. PRIMARY PROGRESS CARD ("How am I doing?")
                    OverallProgressCard(uiState = uiState)

                    // 2. STUDY STREAK & DAILY GOAL (Compact Row)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StudyStreakCard(
                            streakDays = uiState.studyStreakDays,
                            modifier = Modifier.weight(1f)
                        )
                        DailyGoalCard(
                            goal = uiState.dailyStudyGoal,
                            todayCount = uiState.todayActivityCount,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // 3. CIVICS CATEGORIES (3 Official Topic Cards)
                    SectionHeader(
                        title = "Civics Categories",
                        subtitle = "Official 128 questions divided by topic"
                    )

                    uiState.categories.forEach { category ->
                        CategoryProgressCard(
                            data = category,
                            onClick = { onNavigateToCategoryProgress(category.categoryName) }
                        )
                    }

                    // 4. MASTERY & WEAK AREAS (Actionable Next Steps)
                    WeakAreasCard(
                        weakCount = uiState.weakAreasCount,
                        onViewWeakAreas = onNavigateToWeakAreas,
                        onPracticeWeakAreas = onNavigateToPracticeWeakAreas
                    )

                    // 5. 65/20 SPECIAL CONSIDERATION PROGRESS
                    Senior6520ProgressCard(
                        uiState = uiState,
                        onPracticeClick = onNavigateTo6520Practice
                    )

                    // 6. PRACTICE & SPEAKING PERFORMANCE STATS
                    PracticePerformanceCard(uiState = uiState)

                    // 7. SCORE TREND (Recent test history)
                    if (uiState.scoreTrendPoints.isNotEmpty()) {
                        ScoreTrendCard(points = uiState.scoreTrendPoints)
                    }

                    // 8. STUDY MODES BREAKDOWN
                    LearningModesProgressSection(
                        uiState = uiState,
                        onFavoritesClick = onNavigateToFavoritesPractice,
                        onFlashcardsClick = onNavigateToFlashcards,
                        onSpeakingClick = onNavigateToSpeakingPractice
                    )

                    // 9. RECENT ACTIVITY FEED
                    RecentActivityFeedSection(
                        activities = uiState.recentActivities,
                        onViewAllHistory = onNavigateToPracticeHistory
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
private fun OverallProgressCard(uiState: ProgressUiState) {
    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("progress_overall_card"),
        shape = RoundedCornerShape(16.dp),
        elevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Readiness Progress",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${uiState.studiedCount} of ${uiState.totalQuestions} questions studied",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
                ) {
                    Text(
                        text = "${uiState.progressPercentage}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Smooth linear progress bar
            val progressFrac = (uiState.progressPercentage / 100f).coerceIn(0f, 1f)
            AppLinearProgressBar(
                progress = progressFrac,
                status = when {
                    uiState.progressPercentage >= 80 -> ProgressStatus.Completed
                    uiState.progressPercentage >= 40 -> ProgressStatus.Normal
                    else -> ProgressStatus.Normal
                },
                height = 8.dp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 4 Semantic Metrics Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatPill(
                    label = "Studied",
                    count = uiState.studiedCount,
                    color = MaterialTheme.colorScheme.primary
                )
                StatPill(
                    label = "Mastered",
                    count = uiState.masteredCount,
                    color = ColorMastered
                )
                StatPill(
                    label = "Needs Practice",
                    count = uiState.needsPracticeCount,
                    color = ColorNeedsPractice
                )
                StatPill(
                    label = "Not Started",
                    count = uiState.notStartedCount,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Explanatory note
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Studied: viewed or practiced. Mastered: 2+ correct answers with no unresolved errors.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun StudyStreakCard(
    streakDays: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (streakDays > 0) Color(0xFFFFF3E0) else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Study Streak",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = null,
                    tint = if (streakDays > 0) Color(0xFFE65100) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp)
                )
            }

            Column {
                Text(
                    text = if (streakDays > 0) "🔥 $streakDays Day${if (streakDays > 1) "s" else ""}" else "Start today",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (streakDays > 0) Color(0xFFE65100) else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (streakDays > 0) "Keep up the momentum!" else "Study daily to build streak",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun DailyGoalCard(
    goal: Int,
    todayCount: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Daily Goal",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = if (goal > 0 && todayCount >= goal) ColorMastered else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }

            Column {
                if (goal > 0) {
                    val progress = (todayCount.toFloat() / goal).coerceIn(0f, 1f)
                    Text(
                        text = "$todayCount / $goal Qs",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = if (todayCount >= goal) ColorMastered else MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                } else {
                    Text(
                        text = "Goal Inactive",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Set a goal in Settings",
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
private fun CategoryProgressCard(
    data: CategoryProgressData,
    onClick: () -> Unit
) {
    val categoryTag = "category_progress_${data.categoryName.replace(" ", "_").lowercase()}"

    AppCard(
        modifier = Modifier.fillMaxWidth(),
        testTag = categoryTag,
        shape = RoundedCornerShape(14.dp),
        onClick = onClick,
        elevation = 1.dp
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
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AppCategoryChip(category = data.categoryName)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${data.progressPercentage}%",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${data.studiedCount} of ${data.totalCount} questions studied",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (data.masteredCount > 0) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = ColorMastered.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "${data.masteredCount} Mastered",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = ColorMastered,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "View category",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            val animProgress by animateFloatAsState(
                targetValue = (data.progressPercentage / 100f).coerceIn(0f, 1f),
                animationSpec = tween(500),
                label = "categoryProgress"
            )

            LinearProgressIndicator(
                progress = { animProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round
            )
        }
    }
}

@Composable
private fun WeakAreasCard(
    weakCount: Int,
    onViewWeakAreas: () -> Unit,
    onPracticeWeakAreas: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("progress_weak_areas_card"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (weakCount > 0) {
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = BorderStroke(
            1.dp,
            if (weakCount > 0) MaterialTheme.colorScheme.error.copy(alpha = 0.35f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (weakCount > 0) Icons.Default.Warning else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (weakCount > 0) MaterialTheme.colorScheme.error else ColorMastered,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (weakCount > 0) "Needs Practice ($weakCount)" else "Weak Areas",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (weakCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                }

                if (weakCount > 0) {
                    TextButton(
                        onClick = onViewWeakAreas,
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text(
                            text = "View List",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (weakCount > 0) {
                    "You have $weakCount questions that need extra review based on missed answers or practice errors."
                } else {
                    "No weak areas identified yet. Questions with missed answers will appear here for targeted review."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (weakCount > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onPracticeWeakAreas,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .testTag("practice_weak_areas_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Practice Weak Questions",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun Senior6520ProgressCard(
    uiState: ProgressUiState,
    onPracticeClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("progress_6520_card"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "65/20 Special Consideration",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer
                ) {
                    Text(
                        text = "${uiState.senior6520Studied} / ${uiState.senior6520Total}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Track the 20 designated questions for applicants aged 65+ with 20+ years of permanent residency.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            val seniorPercent = if (uiState.senior6520Total > 0) {
                ((uiState.senior6520Studied.toFloat() / uiState.senior6520Total) * 100).toInt()
            } else 0

            LinearProgressIndicator(
                progress = { (seniorPercent / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = MaterialTheme.colorScheme.tertiary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onPracticeClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .testTag("progress_practice_6520_button"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp),
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Practice 65/20 Questions",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun PracticePerformanceCard(uiState: ProgressUiState) {
    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("progress_performance_card"),
        shape = RoundedCornerShape(14.dp),
        elevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "Practice Performance",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                PerformanceMetric(
                    label = "Tests Taken",
                    value = "${uiState.testsCompleted}"
                )
                PerformanceMetric(
                    label = "Average Score",
                    value = if (uiState.testsCompleted > 0) "${uiState.averageScore}%" else "--"
                )
                PerformanceMetric(
                    label = "Pass Rate",
                    value = if (uiState.testsCompleted > 0) "${uiState.passRate}%" else "--",
                    color = if (uiState.passRate >= 60) ColorMastered else MaterialTheme.colorScheme.error
                )
                PerformanceMetric(
                    label = "Questions",
                    value = "${uiState.totalQuestionsPracticed}"
                )
            }

            if (uiState.speakingSessionsCompleted > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.RecordVoiceOver,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Speaking Practice Accuracy",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            text = "${uiState.speakingAccuracy}% (${uiState.speakingSessionsCompleted} sessions)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScoreTrendCard(points: List<ScoreTrendPoint>) {
    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("progress_score_trend_card"),
        shape = RoundedCornerShape(14.dp),
        elevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Score History",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Passing: 60%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                points.forEach { point ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Text(
                            text = "${point.percentage}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (point.passed) ColorMastered else MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .width(20.dp)
                                .height((point.percentage * 0.40f).coerceAtLeast(4f).dp)
                                .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                                .background(
                                    if (point.passed) ColorMastered else MaterialTheme.colorScheme.error
                                )
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = point.dateLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LearningModesProgressSection(
    uiState: ProgressUiState,
    onFavoritesClick: () -> Unit,
    onFlashcardsClick: () -> Unit,
    onSpeakingClick: () -> Unit
) {
    SectionHeader(
        title = "Study Formats",
        subtitle = "Practice modes progress"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        LearningModePill(
            title = "Flashcards",
            subtitle = "${uiState.flashcardsStudied} studied",
            icon = Icons.Default.Style,
            onClick = onFlashcardsClick,
            modifier = Modifier.weight(1f)
        )
        LearningModePill(
            title = "Speaking",
            subtitle = "${uiState.speakingSessionsCompleted} sessions",
            icon = Icons.Default.Mic,
            onClick = onSpeakingClick,
            modifier = Modifier.weight(1f)
        )
        LearningModePill(
            title = "Favorites",
            subtitle = "${uiState.favoritesTotal} saved",
            icon = Icons.Default.Bookmark,
            onClick = onFavoritesClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun LearningModePill(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppCard(
        modifier = modifier.testTag("mode_${title.lowercase()}"),
        shape = RoundedCornerShape(12.dp),
        onClick = onClick,
        elevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun RecentActivityFeedSection(
    activities: List<StudyActivityEntity>,
    onViewAllHistory: () -> Unit
) {
    SectionHeader(
        title = "Recent Activity",
        subtitle = "Your latest civics study history",
        actionText = if (activities.isNotEmpty()) "View All" else null,
        onActionClick = if (activities.isNotEmpty()) onViewAllHistory else null
    )

    if (activities.isEmpty()) {
        EmptyState(
            title = "No Practice Activity Yet",
            message = "Complete flashcards, speaking practice, or mock tests to see your activity history here.",
            icon = Icons.Outlined.History,
            testTag = "recent_activity_empty"
        )
    } else {
        AppCard(
            shape = RoundedCornerShape(14.dp),
            elevation = 1.dp,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("recent_activity_list")
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                activities.take(5).forEachIndexed { index, act ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = act.activityType.replace("_", " "),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = act.details.ifBlank { SimpleDateFormat("MMM d, yyyy", Locale.US).format(Date(act.createdAt)) },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = SimpleDateFormat("MMM d", Locale.US).format(Date(act.createdAt)),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (index < activities.take(5).size - 1) {
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun StatPill(
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

@Composable
private fun PerformanceMetric(
    label: String,
    value: String,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp
        )
    }
}
