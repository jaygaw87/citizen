package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.appColors

/**
 * Civic Category Chip: American Government, American History, Integrated Civics
 */
@Composable
fun AppCategoryChip(
    category: String,
    modifier: Modifier = Modifier,
    testTag: String = "app_category_chip"
) {
    val (bgColor, textColor) = when {
        category.contains("Government", ignoreCase = true) ->
            MaterialTheme.appColors.govCategoryContainer to MaterialTheme.appColors.govCategory
        category.contains("History", ignoreCase = true) ->
            MaterialTheme.appColors.historyCategoryContainer to MaterialTheme.appColors.historyCategory
        else ->
            MaterialTheme.appColors.civicsCategoryContainer to MaterialTheme.appColors.civicsCategory
    }

    Surface(
        modifier = modifier.testTag(testTag),
        shape = RoundedCornerShape(8.dp),
        color = bgColor
    ) {
        Text(
            text = category,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

/**
 * 65/20 Special Consideration Eligibility Chip
 */
@Composable
fun AppEligibilityChip(
    modifier: Modifier = Modifier,
    label: String = "65/20 Exempt",
    testTag: String = "app_eligibility_chip"
) {
    Surface(
        modifier = modifier.testTag(testTag),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.appColors.seniorExemptionContainer,
        border = BorderStroke(1.dp, MaterialTheme.appColors.seniorExemption.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🎯",
                fontSize = 12.sp,
                modifier = Modifier.padding(end = 4.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.appColors.seniorExemption
            )
        }
    }
}

/**
 * Status Chip: Mastered, Needs Practice, Learning, Not Started, Favorite
 */
@Composable
fun AppStatusChip(
    status: QuestionStudyStatus,
    modifier: Modifier = Modifier,
    testTag: String = "app_status_chip"
) {
    val (bgColor, textColor, label, iconEmoji) = when (status) {
        QuestionStudyStatus.Mastered -> Quadruple(
            MaterialTheme.appColors.successContainer,
            MaterialTheme.appColors.onSuccessContainer,
            "Mastered",
            "✓"
        )
        QuestionStudyStatus.NeedsPractice -> Quadruple(
            MaterialTheme.appColors.warningContainer,
            MaterialTheme.appColors.onWarningContainer,
            "Needs Practice",
            "⚠️"
        )
        QuestionStudyStatus.Learning -> Quadruple(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer,
            "Learning",
            "•"
        )
        QuestionStudyStatus.NotStarted -> Quadruple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            "Not Started",
            "○"
        )
        QuestionStudyStatus.Favorite -> Quadruple(
            MaterialTheme.appColors.errorContainer,
            MaterialTheme.appColors.onErrorContainer,
            "Favorite",
            "❤️"
        )
    }

    Surface(
        modifier = modifier.testTag(testTag),
        shape = RoundedCornerShape(8.dp),
        color = bgColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = iconEmoji,
                fontSize = 11.sp,
                color = textColor,
                modifier = Modifier.padding(end = 4.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = textColor
            )
        }
    }
}

enum class QuestionStudyStatus {
    Mastered, NeedsPractice, Learning, NotStarted, Favorite
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

/**
 * Interactive Filter Chip with smooth selection transition
 */
@Composable
fun AppFilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    count: Int? = null,
    testTag: String = "app_filter_chip"
) {
    val containerColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        label = "chip_bg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "chip_fg"
    )
    val borderColor = if (selected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }

    Surface(
        modifier = modifier
            .testTag(testTag)
            .clickable(onClick = onClick)
            .defaultMinSize(minHeight = 36.dp),
        shape = CircleShape,
        color = containerColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = contentColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium),
                color = contentColor
            )
            if (count != null) {
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    shape = CircleShape,
                    color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "$count",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = contentColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                    )
                }
            }
        }
    }
}
