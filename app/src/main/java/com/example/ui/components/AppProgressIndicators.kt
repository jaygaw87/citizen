package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.appColors

/**
 * Semantic Progress Status Type
 */
enum class ProgressStatus {
    Normal, Completed, Warning, Error
}

/**
 * Animated Linear Progress Bar with semantic color states
 */
@Composable
fun AppLinearProgressBar(
    progress: Float, // 0.0f to 1.0f
    modifier: Modifier = Modifier,
    status: ProgressStatus = ProgressStatus.Normal,
    height: Dp = 8.dp,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 600),
        label = "linear_progress_animation"
    )

    val progressColor = when (status) {
        ProgressStatus.Normal -> MaterialTheme.colorScheme.primary
        ProgressStatus.Completed -> MaterialTheme.appColors.success
        ProgressStatus.Warning -> MaterialTheme.appColors.warning
        ProgressStatus.Error -> MaterialTheme.appColors.error
    }

    LinearProgressIndicator(
        progress = { animatedProgress },
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(height / 2)),
        color = progressColor,
        trackColor = trackColor,
        strokeCap = StrokeCap.Round
    )
}

/**
 * Animated Circular Progress Ring with centered label
 */
@Composable
fun AppCircularProgressRing(
    progress: Float, // 0.0f to 1.0f
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    strokeWidth: Dp = 10.dp,
    status: ProgressStatus = ProgressStatus.Normal,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    centerContent: (@Composable () -> Unit)? = null
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 800),
        label = "circular_progress_animation"
    )

    val progressColor = when (status) {
        ProgressStatus.Normal -> MaterialTheme.colorScheme.primary
        ProgressStatus.Completed -> MaterialTheme.appColors.success
        ProgressStatus.Warning -> MaterialTheme.appColors.warning
        ProgressStatus.Error -> MaterialTheme.appColors.error
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokePx = strokeWidth.toPx()
            val pad = strokePx / 2f
            val arcSize = Size(this.size.width - strokePx, this.size.height - strokePx)

            // Background Track
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(pad, pad),
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Progress Arc
            if (animatedProgress > 0f) {
                drawArc(
                    color = progressColor,
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    topLeft = Offset(pad, pad),
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }
        }

        centerContent?.invoke()
    }
}

/**
 * Consistent X / Y counter for questions, items, and steps
 */
@Composable
fun AppProgressCounter(
    current: Int,
    total: Int,
    modifier: Modifier = Modifier,
    label: String? = null
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.Start
    ) {
        Text(
            text = "$current",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = " / $total",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (label != null) {
            Spacer(modifier = Modifier.size(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
