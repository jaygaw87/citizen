package com.example.ui.vocabulary

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VocabularyItem
import com.example.data.model.VocabularyStatus
import com.example.ui.components.AppTopBar

private val ColorLearned = Color(0xFF2E7D32)
private val ColorNeedsReview = Color(0xFFE65100)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VocabularyFlashcardsScreen(
    uiState: VocabularyFlashcardsUiState,
    onBackClick: () -> Unit,
    onFlipCard: () -> Unit,
    onNextCard: () -> Unit,
    onPreviousCard: () -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleFavorite: () -> Unit,
    onMarkLearned: () -> Unit,
    onMarkNeedsReview: () -> Unit,
    onCategorySelected: (String) -> Unit,
    onFilterSelected: (String) -> Unit,
    onHearWord: () -> Unit,
    onHearDefinition: () -> Unit,
    onStopAudio: () -> Unit,
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
        label = "vocab_card_rotation"
    )

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("vocabulary_flashcards_screen"),
        topBar = {
            AppTopBar(
                title = "Vocabulary Flashcards",
                subtitle = if (uiState.totalCount > 0) "Card ${uiState.currentIndex + 1} of ${uiState.totalCount}" else null,
                showCivicEmblem = false,
                showBackButton = true,
                onBackClick = onBackClick,
                backButtonTestTag = "vocab_flashcards_back_button",
                actions = {
                    IconButton(
                        onClick = onToggleShuffle,
                        modifier = Modifier
                            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                            .testTag("vocab_flashcards_shuffle_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = "Shuffle",
                            tint = if (uiState.isShuffle) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        },
        bottomBar = {
            if (uiState.totalCount > 0) {
                Surface(
                    tonalElevation = 3.dp,
                    shadowElevation = 6.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onPreviousCard,
                            modifier = Modifier.testTag("vocab_flashcards_prev_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Prev")
                        }

                        FilledTonalButton(
                            onClick = onFlipCard,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("vocab_flashcards_flip_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Flip,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (uiState.isFlipped) "Show Word" else "Flip Card")
                        }

                        Button(
                            onClick = onNextCard,
                            modifier = Modifier.testTag("vocab_flashcards_next_button")
                        ) {
                            Text("Next")
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LinearProgressIndicator(
                progress = { uiState.progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            // Category Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                uiState.categories.forEach { cat ->
                    val isSelected = uiState.selectedCategory.equals(cat, ignoreCase = true)
                    FilterChip(
                        selected = isSelected,
                        onClick = { onCategorySelected(cat) },
                        label = { Text(if (cat == "ALL") "All Categories" else cat) }
                    )
                }
            }

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No vocabulary cards match this filter.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                val item = uiState.currentItem
                if (item != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Flashcard container with 3D Flip
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(380.dp)
                                .graphicsLayer {
                                    rotationY = rotation
                                    cameraDistance = 12f * density
                                }
                                .clickable(onClick = onFlipCard)
                                .testTag("vocab_flashcard_flipper"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (rotation <= 90f) {
                                // Front of Flashcard
                                VocabCardFront(
                                    item = item,
                                    isSpeaking = uiState.isSpeaking,
                                    onHearWord = onHearWord,
                                    onStopAudio = onStopAudio,
                                    onToggleFavorite = onToggleFavorite,
                                    onFlip = onFlipCard
                                )
                            } else {
                                // Back of Flashcard (counter-rotated to not be mirrored)
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .graphicsLayer { rotationY = 180f }
                                ) {
                                    VocabCardBack(
                                        item = item,
                                        isSpeaking = uiState.isSpeaking,
                                        onHearDefinition = onHearDefinition,
                                        onStopAudio = onStopAudio,
                                        onMarkLearned = onMarkLearned,
                                        onMarkNeedsReview = onMarkNeedsReview,
                                        onFlip = onFlipCard
                                    )
                                }
                            }
                        }

                        // Tap instruction hint
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.TouchApp,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (uiState.isFlipped) "Tap card to see word" else "Tap card to flip for definition",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun VocabCardFront(
    item: VocabularyItem,
    isSpeaking: Boolean,
    onHearWord: () -> Unit,
    onStopAudio: () -> Unit,
    onToggleFavorite: () -> Unit,
    onFlip: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxSize(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = item.category,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (item.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (item.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Word Center
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = item.word.uppercase(),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 32.sp
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )

                if (item.pronunciation.isNotBlank()) {
                    Text(
                        text = item.pronunciation,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Bottom Audio & Action
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onHearWord,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Hear Word")
                }

                if (isSpeaking) {
                    IconButton(
                        onClick = onStopAudio,
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(8.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Stop",
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VocabCardBack(
    item: VocabularyItem,
    isSpeaking: Boolean,
    onHearDefinition: () -> Unit,
    onStopAudio: () -> Unit,
    onMarkLearned: () -> Unit,
    onMarkNeedsReview: () -> Unit,
    onFlip: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxSize(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: Word Name small
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.word.uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Button(
                        onClick = onHearDefinition,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Definition", style = MaterialTheme.typography.labelSmall)
                    }

                    if (isSpeaking) {
                        IconButton(
                            onClick = onStopAudio,
                            modifier = Modifier
                                .size(32.dp)
                                .background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(8.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Stop",
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Content
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = item.simpleDefinition,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (item.exampleSentence.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "\"${item.exampleSentence}\"",
                            style = MaterialTheme.typography.bodyMedium,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }

            // Bottom Study Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val isLearned = item.status == VocabularyStatus.LEARNED
                Button(
                    onClick = onMarkLearned,
                    colors = if (isLearned) ButtonDefaults.buttonColors(containerColor = ColorLearned) else ButtonDefaults.outlinedButtonColors(),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isLearned) "Learned ✓" else "Mark Learned", style = MaterialTheme.typography.labelSmall)
                }

                val isReview = item.status == VocabularyStatus.NEEDS_REVIEW
                Button(
                    onClick = onMarkNeedsReview,
                    colors = if (isReview) ButtonDefaults.buttonColors(containerColor = ColorNeedsReview) else ButtonDefaults.outlinedButtonColors(),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isReview) "Needs Review ⚠️" else "Need Review", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
