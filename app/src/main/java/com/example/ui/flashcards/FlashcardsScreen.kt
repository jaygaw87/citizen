package com.example.ui.flashcards

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StudyQuestionItem
import com.example.ui.components.AppCategoryChip
import com.example.ui.components.AppTopBar
import com.example.ui.theme.appColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardsScreen(
    uiState: FlashcardsUiState,
    onFilterSelected: (FlashcardFilter) -> Unit,
    onFlipCard: () -> Unit,
    onNextCard: () -> Unit,
    onPreviousCard: () -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleStudied: () -> Unit,
    onHearQuestion: () -> Unit = {},
    onHearAnswer: () -> Unit = {},
    onStopAudio: () -> Unit = {},
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    DisposableEffect(Unit) {
        onDispose {
            onStopAudio()
        }
    }

    val rotation by animateFloatAsState(
        targetValue = if (uiState.isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 350),
        label = "card_flip_rotation"
    )

    var totalDragOffset by remember { mutableFloatStateOf(0f) }

    Scaffold(
        modifier = modifier.testTag("flashcards_screen"),
        topBar = {
            AppTopBar(
                title = "Civics Flashcards",
                subtitle = if (uiState.totalCount > 0) "Card ${uiState.currentIndex + 1} of ${uiState.totalCount}" else null,
                showCivicEmblem = false,
                showBackButton = true,
                onBackClick = onBackClick,
                backButtonTestTag = "flashcards_back_button",
                actions = {
                    // Visually clear Shuffle toggle
                    Surface(
                        onClick = onToggleShuffle,
                        shape = RoundedCornerShape(20.dp),
                        color = if (uiState.isShuffled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(
                            1.dp,
                            if (uiState.isShuffled) MaterialTheme.colorScheme.primary else Color.Transparent
                        ),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                            .testTag("flashcards_shuffle_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shuffle,
                                contentDescription = if (uiState.isShuffled) "Shuffle is On. Tap to turn off." else "Shuffle is Off. Tap to turn on.",
                                tint = if (uiState.isShuffled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (uiState.isShuffled) "Shuffle On" else "Shuffle",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (uiState.isShuffled) FontWeight.Bold else FontWeight.Medium,
                                color = if (uiState.isShuffled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (uiState.totalCount > 0) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    tonalElevation = 3.dp,
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Previous Button (disabled on first card)
                        OutlinedButton(
                            onClick = onPreviousCard,
                            enabled = uiState.currentIndex > 0,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .defaultMinSize(minHeight = 48.dp)
                                .weight(1f)
                                .testTag("flashcards_prev_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Previous card",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Previous")
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Flip Button
                        FilledTonalButton(
                            onClick = onFlipCard,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .defaultMinSize(minHeight = 48.dp)
                                .weight(1.2f)
                                .testTag("flashcards_flip_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Flip,
                                contentDescription = if (uiState.isFlipped) "Show question" else "Reveal answer",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (uiState.isFlipped) "Question" else "Reveal")
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Next Button (dominant action, disabled on final card)
                        Button(
                            onClick = onNextCard,
                            enabled = uiState.currentIndex < uiState.totalCount - 1,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier
                                .defaultMinSize(minHeight = 48.dp)
                                .weight(1f)
                                .testTag("flashcards_next_button")
                        ) {
                            Text("Next", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Next card",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Filter / Mode Chips Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FlashcardFilter.entries.forEach { filter ->
                    val isSelected = uiState.selectedFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { onFilterSelected(filter) },
                        label = {
                            Text(
                                text = filter.label,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        leadingIcon = if (isSelected) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier
                            .defaultMinSize(minHeight = 36.dp)
                            .testTag("flashcard_filter_${filter.name}")
                    )
                }
            }

            // Card Counter & Progress Indicator
            if (uiState.totalCount > 0) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (uiState.selectedFilter == FlashcardFilter.SENIOR_65_20) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.appColors.seniorExemptionContainer,
                                border = BorderStroke(1.dp, MaterialTheme.appColors.seniorExemption.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = MaterialTheme.appColors.seniorExemption,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "65/20 Special Consideration",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.appColors.seniorExemption
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = uiState.selectedFilter.label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Compact counter
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "${uiState.currentIndex + 1} / ${uiState.totalCount}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    LinearProgressIndicator(
                        progress = { uiState.progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }

            // Main Flashcard Area
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                when {
                    uiState.isLoading -> {
                        CircularProgressIndicator(modifier = Modifier.testTag("flashcards_loading_indicator"))
                    }
                    uiState.displayQuestions.isEmpty() -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No cards available in this category.",
                                style = MaterialTheme.typography.titleMedium,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = when (uiState.selectedFilter) {
                                    FlashcardFilter.FAVORITES -> "Tap the heart icon on any question to add it to your Favorites."
                                    FlashcardFilter.NEEDS_PRACTICE -> "Questions you miss during practice sessions will appear here."
                                    else -> "Try selecting 'All 128' to review all questions."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { onFilterSelected(FlashcardFilter.ALL) }) {
                                Text("Show All Cards")
                            }
                        }
                    }
                    uiState.currentCard != null -> {
                        val card = uiState.currentCard!!

                        // Swipe gesture detector for next/prev
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(Unit) {
                                    detectHorizontalDragGestures(
                                        onDragStart = { totalDragOffset = 0f },
                                        onDragEnd = {
                                            if (totalDragOffset < -120f) {
                                                if (uiState.currentIndex < uiState.totalCount - 1) {
                                                    onNextCard()
                                                }
                                            } else if (totalDragOffset > 120f) {
                                                if (uiState.currentIndex > 0) {
                                                    onPreviousCard()
                                                }
                                            }
                                            totalDragOffset = 0f
                                        },
                                        onHorizontalDrag = { _, dragAmount ->
                                            totalDragOffset += dragAmount
                                        }
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            // 3D Flip Card Container
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .widthIn(max = 560.dp)
                                    .graphicsLayer {
                                        rotationY = rotation
                                        cameraDistance = 12f * density
                                    }
                                    .clickable { onFlipCard() }
                                    .testTag("flashcard_container")
                            ) {
                                if (rotation <= 90f) {
                                    // Front Side (Question)
                                    FlashcardFront(
                                        card = card,
                                        isTtsSpeaking = uiState.isTtsSpeaking && uiState.ttsMode == com.example.ui.speaking.TtsMode.QUESTION,
                                        onHearQuestion = onHearQuestion,
                                        onToggleFavorite = onToggleFavorite,
                                        onToggleStudied = onToggleStudied
                                    )
                                } else {
                                    // Back Side (Answer & Educational Explanation) - rotated back 180 so it's readable
                                    Box(
                                        modifier = Modifier.graphicsLayer {
                                            rotationY = 180f
                                        }
                                    ) {
                                        FlashcardBack(
                                            card = card,
                                            isTtsSpeaking = uiState.isTtsSpeaking && uiState.ttsMode == com.example.ui.speaking.TtsMode.ANSWER,
                                            onHearAnswer = onHearAnswer,
                                            onFlipCard = onFlipCard,
                                            onToggleFavorite = onToggleFavorite,
                                            onToggleStudied = onToggleStudied
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
}

@Composable
fun FlashcardFront(
    card: StudyQuestionItem,
    isTtsSpeaking: Boolean,
    onHearQuestion: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleStudied: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("flashcard_front"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Header Row: Topic Badge, 65/20 badge, Studied & Favorite controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AppCategoryChip(category = card.category)

                    if (card.is65_20) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.appColors.seniorExemptionContainer,
                            border = BorderStroke(1.dp, MaterialTheme.appColors.seniorExemption.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = MaterialTheme.appColors.seniorExemption,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "65/20",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.appColors.seniorExemption
                                )
                            }
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Mark as Studied control
                    Surface(
                        onClick = onToggleStudied,
                        shape = RoundedCornerShape(8.dp),
                        color = if (card.isStudied) MaterialTheme.appColors.successContainer else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                            .padding(end = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (card.isStudied) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = if (card.isStudied) "Studied. Tap to unmark." else "Mark as Studied",
                                tint = if (card.isStudied) MaterialTheme.appColors.onSuccessContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (card.isStudied) "Studied ✓" else "Mark Studied",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (card.isStudied) MaterialTheme.appColors.onSuccessContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Favorite control (≥48dp touch target)
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier
                            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                            .size(48.dp)
                    ) {
                        Icon(
                            imageVector = if (card.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (card.isFavorite) "Remove from favorites" else "Add to favorites",
                            tint = if (card.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Question numbering
            Text(
                text = "QUESTION #${card.questionNumber}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Dominant Question Text
            Text(
                text = card.questionText,
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp, lineHeight = 28.sp),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Hear Question Audio Action
            FilledTonalButton(
                onClick = onHearQuestion,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .defaultMinSize(minHeight = 48.dp)
                    .testTag("flashcard_hear_question_button")
            ) {
                Icon(
                    imageVector = if (isTtsSpeaking) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = if (isTtsSpeaking) "Stop audio" else "Play question audio",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isTtsSpeaking) "Stop Audio" else "Hear Question",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Flip hint
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.TouchApp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Tap card to flip",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun FlashcardBack(
    card: StudyQuestionItem,
    isTtsSpeaking: Boolean,
    onHearAnswer: () -> Unit,
    onFlipCard: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleStudied: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("flashcard_back"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Row: Topic Badge & Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                ) {
                    Text(
                        text = "Official Answer • #${card.questionNumber}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Mark as Studied control
                    Surface(
                        onClick = onToggleStudied,
                        shape = RoundedCornerShape(8.dp),
                        color = if (card.isStudied) MaterialTheme.appColors.successContainer else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                            .padding(end = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (card.isStudied) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = if (card.isStudied) "Studied. Tap to unmark." else "Mark as Studied",
                                tint = if (card.isStudied) MaterialTheme.appColors.onSuccessContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (card.isStudied) "Studied ✓" else "Mark Studied",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (card.isStudied) MaterialTheme.appColors.onSuccessContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Favorite control (≥48dp touch target)
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier
                            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                            .size(48.dp)
                    ) {
                        Icon(
                            imageVector = if (card.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (card.isFavorite) "Remove from favorites" else "Add to favorites",
                            tint = if (card.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // USCIS Accepted Answers Header with Audio Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "USCIS ACCEPTED ANSWERS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                FilledTonalButton(
                    onClick = onHearAnswer,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier
                        .defaultMinSize(minHeight = 40.dp)
                        .testTag("flashcard_hear_answer_button")
                ) {
                    Icon(
                        imageVector = if (isTtsSpeaking) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = if (isTtsSpeaking) "Stop audio" else "Play answer audio",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isTtsSpeaking) "Stop" else "Hear Answer",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Accepted Answers List (exact items with bullet points)
            card.answerTexts.forEach { answer ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = answer,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Educational Explanation (curated)
            if (card.explanation.isNotBlank()) {
                Spacer(modifier = Modifier.height(14.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(18.dp)
                                .padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Educational Explanation",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = card.explanation,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onFlipCard,
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 48.dp)
                    .testTag("flashcard_flip_back_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Flip, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Flip Back to Question")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.TouchApp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Tap card to flip",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}
