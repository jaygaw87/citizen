package com.example.ui.home

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Style
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import com.example.data.model.CategoryOverviewItem
import com.example.data.model.HomeUiState
import com.example.ui.components.AppCard
import com.example.ui.components.AppCategoryChip
import com.example.ui.components.AppFeatureCard
import com.example.ui.components.AppLinearProgressBar
import com.example.ui.components.AppPrimaryButton
import com.example.ui.components.AppTopBar
import com.example.ui.components.FeatureCard
import com.example.ui.components.ProgressCard
import com.example.ui.components.ProgressStatus
import com.example.ui.components.SectionHeader
import com.example.ui.components.StudyActionCard
import com.example.ui.theme.AppAnimation
import com.example.ui.theme.appColors
import com.example.ui.theme.appSpacing
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    uiState: HomeUiState = HomeUiState(),
    onNavigateToSettings: () -> Unit,
    onNavigateToQuestions: () -> Unit,
    onNavigateToPracticeTest: (String) -> Unit,
    onNavigateToFlashcards: () -> Unit,
    onNavigateToMockInterview: () -> Unit,
    onNavigateToSpeakingPractice: () -> Unit = onNavigateToMockInterview,
    onNavigateToAudioPractice: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToProgress: () -> Unit = {},
    onNavigateToMistakes: () -> Unit,
    onNavigateToQuickReview: () -> Unit = onNavigateToMistakes,
    onNavigateToSpecialConsideration: () -> Unit,
    onNavigateToVocabulary: () -> Unit,
    onNavigateToRecommended: (actionType: String, categoryName: String?) -> Unit = { _, _ -> },
    onNavigateToContinue: () -> Unit = onNavigateToQuestions,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.testTag("home_screen"),
        topBar = {
            AppTopBar(
                title = "U.S. Citizenship Test",
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
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        // 1. Clean Civic Header
                        HomeHeaderSection()

                        Spacer(modifier = Modifier.height(8.dp))

                        // 2. Daily Study Goal & Streak Row
                        HomeDailyGoalAndStreakBanner(
                            goal = uiState.dailyStudyGoal,
                            todayCount = uiState.todayQuestionsCount,
                            streakDays = uiState.studyStreakDays
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // 3. Level 1: Primary Continue Card (Dominant Action)
                        HomePrimaryContinueCard(
                            isFirstSession = uiState.isFirstSession,
                            studiedCount = uiState.studiedCount,
                            totalQuestions = uiState.totalQuestions,
                            continueLabel = uiState.continueQuestionText,
                            onContinueClick = onNavigateToContinue
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // 4. Level 2: Overall Progress Overview Card
                        ProgressCard(
                            studiedCount = uiState.studiedCount,
                            totalQuestions = uiState.totalQuestions,
                            percentage = uiState.progressPercentage,
                            subtitle = if (uiState.isFirstSession) "Start your first study session" else "${uiState.totalQuestions - uiState.studiedCount} questions remaining",
                            onClick = onNavigateToProgress
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // 5. Level 2: 65/20 Special Consideration Card
                        Senior6520HomeCard(
                            studiedCount = uiState.senior6520Studied,
                            totalCount = uiState.senior6520Count,
                            onClick = onNavigateToSpecialConsideration
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // 6. Level 3: Quick Actions Section Header
                        SectionHeader(
                            title = "Quick Actions",
                            subtitle = "Choose your preferred study mode"
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Quick Actions 2-column Grid - Primary Study & Practice Modes
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StudyActionCard(
                                title = "Questions",
                                description = "All 128 civics questions",
                                icon = Icons.AutoMirrored.Outlined.MenuBook,
                                onClick = onNavigateToQuestions,
                                modifier = Modifier.weight(1f),
                                accentColor = MaterialTheme.colorScheme.primary,
                                testTag = "action_study_questions"
                            )
                            StudyActionCard(
                                title = "Practice",
                                description = "Full tests & simulations",
                                icon = Icons.Outlined.TrackChanges,
                                onClick = { onNavigateToPracticeTest("full") },
                                modifier = Modifier.weight(1f),
                                accentColor = MaterialTheme.colorScheme.secondary,
                                testTag = "action_practice_test"
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StudyActionCard(
                                title = "Speaking Practice",
                                description = "Practice questions aloud",
                                icon = Icons.Outlined.Mic,
                                onClick = onNavigateToSpeakingPractice,
                                modifier = Modifier.weight(1f),
                                accentColor = MaterialTheme.colorScheme.tertiary,
                                testTag = "action_speaking_practice"
                            )
                            StudyActionCard(
                                title = "Flashcards",
                                description = "Quick flip card review",
                                icon = Icons.Outlined.Style,
                                onClick = onNavigateToFlashcards,
                                modifier = Modifier.weight(1f),
                                accentColor = MaterialTheme.colorScheme.primary,
                                testTag = "action_flashcards"
                            )
                        }

                        // 7. Stat Metric Chips / Row
                        if (!uiState.isFirstSession) {
                            Spacer(modifier = Modifier.height(8.dp))
                            HomeProgressMetricsRow(
                                studiedCount = uiState.studiedCount,
                                needsPracticeCount = uiState.needsPracticeCount,
                                favoriteCount = uiState.favoriteCount,
                                testsCompleted = uiState.testsCompleted,
                                onFavoritesClick = onNavigateToFavorites,
                                onNeedsPracticeClick = onNavigateToMistakes
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StudyActionCard(
                                title = "Favorites",
                                description = "Bookmarked questions",
                                icon = Icons.Outlined.FavoriteBorder,
                                onClick = onNavigateToFavorites,
                                modifier = Modifier.weight(1f),
                                accentColor = MaterialTheme.colorScheme.secondary,
                                testTag = "action_favorites"
                            )
                            StudyActionCard(
                                title = "Progress",
                                description = "Scores, stats & activity",
                                icon = Icons.AutoMirrored.Outlined.TrendingUp,
                                onClick = onNavigateToProgress,
                                modifier = Modifier.weight(1f),
                                accentColor = MaterialTheme.colorScheme.tertiary,
                                testTag = "action_progress"
                            )
                        }

                        // 8. Category Progress Section (if categories available)
                        if (uiState.categories.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            HomeCategoryProgressSection(
                                categories = uiState.categories,
                                onCategoryClick = onNavigateToQuestions
                            )
                        }

                        // 9. Recommended For You Card (if applicable)
                        if (uiState.recommendedTitle.isNotBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            HomeRecommendedCard(
                                title = uiState.recommendedTitle,
                                subtitle = uiState.recommendedSubtitle,
                                onActionClick = {
                                    onNavigateToRecommended(uiState.recommendedActionType, uiState.recommendedCategoryName)
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 10. Level 4: Explore Learning Modes
                        SectionHeader(
                            title = "Explore Learning Modes",
                            subtitle = "Additional tools to prepare for the test"
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            CivicsVocabularyHomeCard(
                                learnedCount = uiState.vocabularyLearnedCount,
                                totalCount = uiState.vocabularyTotalCount,
                                onClick = onNavigateToVocabulary
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FeatureCard(
                                    title = "Audio Practice",
                                    description = "Listen to questions & answers",
                                    icon = Icons.Outlined.Headphones,
                                    onClick = onNavigateToAudioPractice,
                                    modifier = Modifier.weight(1f),
                                    testTag = "feature_audio_practice"
                                )
                                FeatureCard(
                                    title = "Quick Review",
                                    description = "Focus on missed & saved",
                                    icon = Icons.Outlined.Checklist,
                                    onClick = onNavigateToQuickReview,
                                    modifier = Modifier.weight(1f),
                                    testTag = "feature_quick_review"
                                )
                            }
                            FeatureCard(
                                title = "Favorites",
                                description = "Review bookmarked questions",
                                icon = Icons.Outlined.BookmarkBorder,
                                onClick = onNavigateToFavorites,
                                testTag = "feature_favorites"
                            )
                        }

                        // 11. Recent Activity Feed
                        if (uiState.recentActivities.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            SectionHeader(
                                title = "Recent Activity",
                                subtitle = "Your latest study and practice sessions"
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            HomeRecentActivityList(activities = uiState.recentActivities)
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

/**
 * Top Header Section with concise educational positioning
 */
@Composable
private fun HomeHeaderSection() {
    Column {
        Text(
            text = "U.S. Citizenship Test",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            ),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Prepare. Practice. Master your civics test.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Daily Study Goal & Streak Row with refined layout
 */
@Composable
private fun HomeDailyGoalAndStreakBanner(
    goal: Int,
    todayCount: Int,
    streakDays: Int
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        elevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (goal > 0) "Daily Goal: $todayCount / $goal questions" else "Today: $todayCount questions studied",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (goal > 0 && todayCount >= goal) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Goal reached",
                            tint = MaterialTheme.appColors.success,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                if (goal > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    val progressFraction = (todayCount.toFloat() / goal).coerceIn(0f, 1f)
                    AppLinearProgressBar(
                        progress = progressFraction,
                        status = if (todayCount >= goal) ProgressStatus.Completed else ProgressStatus.Normal,
                        height = 5.dp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Streak Badge
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (streakDays > 0) MaterialTheme.appColors.warningContainer else MaterialTheme.colorScheme.surface,
                border = BorderStroke(
                    1.dp,
                    if (streakDays > 0) MaterialTheme.appColors.warning.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outlineVariant
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Streak",
                        tint = if (streakDays > 0) MaterialTheme.appColors.warning else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (streakDays > 0) "$streakDays day${if (streakDays == 1) "" else "s"}" else "0 days",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (streakDays > 0) MaterialTheme.appColors.onWarningContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Level 1: Primary Continue Studying Card
 * Strongest visual element on screen with clear call-to-action
 */
@Composable
private fun HomePrimaryContinueCard(
    isFirstSession: Boolean,
    studiedCount: Int,
    totalQuestions: Int,
    continueLabel: String,
    onContinueClick: () -> Unit
) {
    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onContinueClick),
        shape = RoundedCornerShape(18.dp),
        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
        elevation = 1.dp,
        testTag = "continue_studying_card"
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        text = if (isFirstSession) "START STUDYING" else "CONTINUE STUDYING",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Text(
                    text = "$studiedCount / $totalQuestions Questions",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = continueLabel,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 17.sp),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = if (isFirstSession) {
                    "Begin preparing for your test with Question 1"
                } else {
                    "Pick up right where you left off in your civics study"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            AppPrimaryButton(
                text = if (isFirstSession) "Start Study" else "Continue Studying",
                onClick = onContinueClick,
                leadingIcon = Icons.Default.PlayArrow,
                testTag = "continue_start_button"
            )
        }
    }
}

/**
 * Supporting progress metrics row under Overall Progress Card
 */
@Composable
private fun HomeProgressMetricsRow(
    studiedCount: Int,
    needsPracticeCount: Int,
    favoriteCount: Int,
    testsCompleted: Int,
    onFavoritesClick: () -> Unit,
    onNeedsPracticeClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Needs practice metric
        Surface(
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onNeedsPracticeClick)
                .defaultMinSize(minHeight = 48.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⚠️",
                    fontSize = 12.sp,
                    modifier = Modifier.padding(end = 6.dp)
                )
                Column {
                    Text(
                        text = "$needsPracticeCount",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (needsPracticeCount > 0) MaterialTheme.appColors.warning else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Need Work",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Favorites metric
        Surface(
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onFavoritesClick)
                .defaultMinSize(minHeight = 48.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "❤️",
                    fontSize = 12.sp,
                    modifier = Modifier.padding(end = 6.dp)
                )
                Column {
                    Text(
                        text = "$favoriteCount",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Saved",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Tests Completed metric
        Surface(
            modifier = Modifier
                .weight(1f)
                .defaultMinSize(minHeight = 48.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📊",
                    fontSize = 12.sp,
                    modifier = Modifier.padding(end = 6.dp)
                )
                Column {
                    Text(
                        text = "$testsCompleted",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Tests",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Category Progress Section showing American Government, American History, Integrated Civics
 */
@Composable
private fun HomeCategoryProgressSection(
    categories: List<CategoryOverviewItem>,
    onCategoryClick: () -> Unit
) {
    Column {
        SectionHeader(
            title = "Civics Topics",
            subtitle = "Track progress across the 3 test categories"
        )
        Spacer(modifier = Modifier.height(10.dp))

        AppCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onCategoryClick),
            shape = RoundedCornerShape(16.dp),
            elevation = 0.5.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                categories.forEach { cat ->
                    val fraction = if (cat.totalCount > 0) (cat.studiedCount.toFloat() / cat.totalCount).coerceIn(0f, 1f) else 0f
                    val percentage = (fraction * 100).toInt()

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AppCategoryChip(category = cat.name)
                            Text(
                                text = "${cat.studiedCount}/${cat.totalCount} ($percentage%)",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        val progressColor = when {
                            cat.name.contains("Government", ignoreCase = true) -> MaterialTheme.appColors.govCategory
                            cat.name.contains("History", ignoreCase = true) -> MaterialTheme.appColors.historyCategory
                            else -> MaterialTheme.appColors.civicsCategory
                        }

                        val animatedFraction by animateFloatAsState(
                            targetValue = fraction,
                            animationSpec = tween(600),
                            label = "cat_progress"
                        )

                        LinearProgressIndicator(
                            progress = { animatedFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = progressColor,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            strokeCap = StrokeCap.Round
                        )
                    }
                }
            }
        }
    }
}

/**
 * Recommended Card with subtle styling and primary play action
 */
@Composable
private fun HomeRecommendedCard(
    title: String,
    subtitle: String,
    onActionClick: () -> Unit
) {
    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onActionClick),
        shape = RoundedCornerShape(16.dp),
        elevation = 0.5.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Recommended for you",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Start recommended action",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * 65/20 Special Consideration Card with prominent amber accent
 */
@Composable
private fun Senior6520HomeCard(
    studiedCount: Int,
    totalCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("feature_65_20"),
        shape = RoundedCornerShape(18.dp),
        containerColor = MaterialTheme.appColors.seniorExemptionContainer.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, MaterialTheme.appColors.seniorExemption.copy(alpha = 0.3f)),
        elevation = 0.5.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.appColors.seniorExemptionContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🎯",
                            fontSize = 16.sp
                        )
                    }
                    Column {
                        Text(
                            text = "65/20 Special Consideration",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$totalCount Questions • $studiedCount / $totalCount studied",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.appColors.seniorExemptionContainer
                ) {
                    Text(
                        text = "$totalCount Qs",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.appColors.seniorExemption,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            val progressFraction = if (totalCount > 0) (studiedCount.toFloat() / totalCount).coerceIn(0f, 1f) else 0f
            val animatedFraction by animateFloatAsState(
                targetValue = progressFraction,
                animationSpec = tween(600),
                label = "senior_progress"
            )

            LinearProgressIndicator(
                progress = { animatedFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = MaterialTheme.appColors.seniorExemption,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (studiedCount == totalCount && totalCount > 0) "All 20 Studied! 🎉" else "${totalCount - studiedCount} remaining",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                TextButton(
                    onClick = onClick,
                    modifier = Modifier
                        .heightIn(min = 36.dp)
                        .testTag("senior_home_continue_button")
                ) {
                    Text(
                        text = if (studiedCount > 0) "Continue" else "Start",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.appColors.seniorExemption
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.appColors.seniorExemption
                    )
                }
            }
        }
    }
}

/**
 * Civics Vocabulary Home Card
 */
@Composable
private fun CivicsVocabularyHomeCard(
    learnedCount: Int,
    totalCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progressFraction = if (totalCount > 0) (learnedCount.toFloat() / totalCount).coerceIn(0f, 1f) else 0f
    val animatedFraction by animateFloatAsState(
        targetValue = progressFraction,
        animationSpec = tween(600),
        label = "vocab_progress"
    )

    AppCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("feature_vocabulary"),
        shape = RoundedCornerShape(16.dp),
        elevation = 0.5.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Civics Vocabulary",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$learnedCount / $totalCount terms learned",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = "115 Terms",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            LinearProgressIndicator(
                progress = { animatedFraction },
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

/**
 * Recent Activity List
 */
@Composable
private fun HomeRecentActivityList(activities: List<StudyActivityEntity>) {
    val timeFormat = SimpleDateFormat("h:mm a, MMM d", Locale.US)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        activities.take(4).forEach { act ->
            AppCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = act.details.ifEmpty { "Civics Study" },
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = timeFormat.format(Date(act.createdAt)),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
