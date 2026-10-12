package com.example.ui.study

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.HistoryEdu
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AppTopBar

data class TopicCardInfo(
    val filter: StudyFilter,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val questionCount: Int,
    val studiedCount: Int
) {
    val progressPercentage: Int
        get() = if (questionCount > 0) ((studiedCount * 100f) / questionCount).toInt() else 0
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicsScreen(
    categoryProgressList: List<CategoryProgress>,
    onTopicSelected: (StudyFilter) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val govProgress = categoryProgressList.firstOrNull { it.categoryName == "American Government" }
    val histProgress = categoryProgressList.firstOrNull { it.categoryName == "American History" }
    val civicsProgress = categoryProgressList.firstOrNull { it.categoryName == "Integrated Civics" }
    val seniorProgress = categoryProgressList.firstOrNull { it.categoryName.contains("65/20") }

    val topics = listOf(
        TopicCardInfo(
            filter = StudyFilter.GOVERNMENT,
            title = "American Government",
            description = "Principles of Democracy, System of Government, and Rights & Responsibilities",
            icon = Icons.Outlined.AccountBalance,
            questionCount = govProgress?.totalCount ?: 75,
            studiedCount = govProgress?.studiedCount ?: 0
        ),
        TopicCardInfo(
            filter = StudyFilter.HISTORY,
            title = "American History",
            description = "Colonial Period, 1800s, Civil War, and Recent American History",
            icon = Icons.Outlined.HistoryEdu,
            questionCount = histProgress?.totalCount ?: 43,
            studiedCount = histProgress?.studiedCount ?: 0
        ),
        TopicCardInfo(
            filter = StudyFilter.CIVICS,
            title = "Integrated Civics",
            description = "Geography, National Symbols, Flag, Anthem, and Official Holidays",
            icon = Icons.Outlined.Public,
            questionCount = civicsProgress?.totalCount ?: 10,
            studiedCount = civicsProgress?.studiedCount ?: 0
        ),
        TopicCardInfo(
            filter = StudyFilter.SENIOR_65_20,
            title = "65/20 Special Consideration",
            description = "20 designated civics questions for eligible permanent residents aged 65 or older",
            icon = Icons.Outlined.Star,
            questionCount = seniorProgress?.totalCount ?: 20,
            studiedCount = seniorProgress?.studiedCount ?: 0
        )
    )

    Scaffold(
        modifier = modifier.testTag("topics_screen"),
        topBar = {
            AppTopBar(
                title = "Civics Topics",
                subtitle = "Browse official categories",
                showCivicEmblem = false,
                showBackButton = true,
                onBackClick = onBackClick,
                backButtonTestTag = "topics_back_button"
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 720.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "Study by Category",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Select a topic to focus your study on specific civics subjects.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(
                    count = topics.size,
                    key = { topics[it].filter.name }
                ) { index ->
                    val topic = topics[index]
                    TopicItemCard(
                        topic = topic,
                        onClick = { onTopicSelected(topic.filter) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TopicItemCard(
    topic: TopicCardInfo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("topic_card_${topic.filter.name.lowercase()}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = topic.icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = topic.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${topic.questionCount} Questions • ${topic.studiedCount} Studied",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = topic.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Progress bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LinearProgressIndicator(
                    progress = { (topic.progressPercentage / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(CircleShape),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "${topic.progressPercentage}%",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
