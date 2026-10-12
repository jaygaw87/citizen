package com.example.ui.study

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Button
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AppCard
import com.example.ui.components.AppCategoryChip
import com.example.ui.components.AppPrimaryButton
import com.example.ui.components.AppStatusChip
import com.example.ui.components.AppTopBar
import com.example.ui.components.QuestionStudyStatus
import com.example.ui.theme.AppAnimation
import com.example.ui.theme.appColors
import com.example.ui.theme.studyTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionDetailScreen(
    uiState: QuestionDetailUiState,
    onBackClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onMarkStudied: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Error State
    if (uiState.errorMessage != null || (!uiState.isLoading && uiState.question == null)) {
        Scaffold(
            topBar = {
                AppTopBar(
                    title = "Question Detail",
                    showCivicEmblem = false,
                    showBackButton = true,
                    onBackClick = onBackClick,
                    backButtonTestTag = "detail_back_button"
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .testTag("question_detail_error"),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Question unavailable",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "The requested question could not be loaded.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    AppPrimaryButton(
                        text = "Return to Study",
                        onClick = onBackClick,
                        modifier = Modifier
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("return_to_study_button")
                    )
                }
            }
        }
        return
    }

    if (uiState.isLoading || uiState.question == null) {
        Scaffold(
            topBar = {
                AppTopBar(
                    title = "Question Detail",
                    subtitle = "Loading question...",
                    showCivicEmblem = false,
                    showBackButton = true,
                    onBackClick = onBackClick,
                    backButtonTestTag = "detail_back_button"
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
        return
    }

    val item = uiState.question

    // Horizontal swipe gesture support (Requirement 16)
    var totalDragX by remember { mutableFloatStateOf(0f) }

    Scaffold(
        modifier = modifier
            .testTag("question_detail_screen")
            .pointerInput(item.id) {
                detectHorizontalDragGestures(
                    onDragStart = { totalDragX = 0f },
                    onDragEnd = {
                        if (totalDragX > 140f && uiState.hasPrevious) {
                            onPreviousClick()
                        } else if (totalDragX < -140f && uiState.hasNext) {
                            onNextClick()
                        }
                        totalDragX = 0f
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        totalDragX += dragAmount
                    }
                )
            },
        topBar = {
            AppTopBar(
                title = uiState.contextTitle,
                showCivicEmblem = false,
                showBackButton = true,
                onBackClick = onBackClick,
                backButtonTestTag = "detail_back_button",
                actions = {
                    val scale by animateFloatAsState(
                        targetValue = if (item.isFavorite) 1.2f else 1.0f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                        label = "fav_scale_detail"
                    )
                    val tint by animateColorAsState(
                        targetValue = if (item.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        label = "fav_tint_detail"
                    )
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier
                            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                            .scale(scale)
                            .testTag("detail_favorite_button")
                    ) {
                        Icon(
                            imageVector = if (item.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = if (item.isFavorite) "Remove from favorites" else "Add to favorites",
                            tint = tint,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            )
        },
        bottomBar = {
            // Accessible Contextual Navigation Bottom Bar (Requirement 15)
            Surface(
                tonalElevation = 2.dp,
                shadowElevation = 4.dp,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onPreviousClick,
                        enabled = uiState.hasPrevious,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("previous_question_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Previous",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = onNextClick,
                        enabled = uiState.hasNext,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("next_question_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Next",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        AnimatedVisibility(
            visible = true,
            enter = AppAnimation.screenEnter()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 720.dp)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 1. Top Badges & Studied Action Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            AppCategoryChip(category = item.category)

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
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        // Studied Toggle Action
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (item.isStudied) MaterialTheme.appColors.successContainer else MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(
                                1.dp,
                                if (item.isStudied) MaterialTheme.appColors.success.copy(alpha = 0.35f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .defaultMinSize(minHeight = 44.dp)
                                .clickable { onMarkStudied(!item.isStudied) }
                                .testTag("mark_studied_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = if (item.isStudied) Icons.Default.CheckCircle else Icons.Outlined.CheckCircleOutline,
                                    contentDescription = null,
                                    tint = if (item.isStudied) MaterialTheme.appColors.success else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (item.isStudied) "Studied" else "Mark Studied",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = if (item.isStudied) MaterialTheme.appColors.onSuccessContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // 2. Question Text (18sp, semi-bold, comfortable line height - Requirement 11)
                    Text(
                        text = item.questionText,
                        style = MaterialTheme.studyTypography.questionText.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 26.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // 3. Dynamic Answer Notice (Requirement 14)
                    if (item.isDynamicAnswer) {
                        AppCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            containerColor = MaterialTheme.appColors.warningContainer.copy(alpha = 0.35f),
                            border = BorderStroke(1.dp, MaterialTheme.appColors.warning.copy(alpha = 0.35f)),
                            elevation = 0.dp
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = MaterialTheme.appColors.warning,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Current answer may change",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.appColors.onWarningContainer
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Verify the current official answer before your interview date.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.appColors.onWarningContainer.copy(alpha = 0.9f)
                                )
                                if (!item.answerLastVerified.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Last verified: ${item.answerLastVerified}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.appColors.onWarningContainer.copy(alpha = 0.75f)
                                    )
                                }
                            }
                        }
                    }

                    // 4. USCIS Accepted Answers Section (Requirement 12)
                    AppCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("official_answers_card"),
                        shape = RoundedCornerShape(14.dp),
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                        elevation = 0.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.VerifiedUser,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (item.answerTexts.size > 1) "USCIS Accepted Answers" else "USCIS Accepted Answer",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    ),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            if (item.answerTexts.size <= 1) {
                                Text(
                                    text = item.primaryAnswerText,
                                    style = MaterialTheme.studyTypography.answerText.copy(
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Normal,
                                        lineHeight = 24.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    item.answerTexts.forEach { answer ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Text(
                                                text = "•",
                                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(end = 8.dp)
                                            )
                                            Text(
                                                text = answer,
                                                style = MaterialTheme.studyTypography.answerText.copy(
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Normal,
                                                    lineHeight = 24.sp
                                                ),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 5. Educational Explanation Section (Requirement 13)
                    AppCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("curated_explanation_card"),
                        shape = RoundedCornerShape(14.dp),
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                        elevation = 0.dp
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
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Educational Explanation",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    ),
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = item.explanation,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 22.sp
                            )
                        }
                    }

                    // 6. Official Source Section
                    AppCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        containerColor = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                        elevation = 0.dp
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
                                    text = "Official Source",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = item.sourceName.ifBlank { "USCIS Civics Test Guide" },
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            OutlinedButton(
                                onClick = {
                                    val targetUrl = item.answerSourceUrl?.takeIf { it.isNotBlank() }
                                        ?: item.sourceUrl.takeIf { it.isNotBlank() }
                                        ?: "https://www.uscis.gov/citizenship"
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl))
                                        context.startActivity(intent)
                                    } catch (_: Exception) {
                                    }
                                },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .defaultMinSize(minHeight = 44.dp)
                                    .testTag("view_official_source_button")
                            ) {
                                Text(
                                    text = "Official Source",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}
