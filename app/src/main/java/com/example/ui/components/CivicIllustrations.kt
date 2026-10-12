package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun CivicTrustIllustration(
    modifier: Modifier = Modifier,
    size: Dp = 180.dp
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val containerColor = MaterialTheme.colorScheme.primaryContainer

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height

            // Background soft circular shield
            drawCircle(
                color = containerColor.copy(alpha = 0.5f),
                radius = w * 0.44f,
                center = Offset(w * 0.5f, h * 0.5f)
            )
            drawCircle(
                color = secondaryColor.copy(alpha = 0.2f),
                radius = w * 0.46f,
                center = Offset(w * 0.5f, h * 0.5f),
                style = Stroke(width = 3.dp.toPx())
            )

            // Classical Capitol Dome outline
            val domePath = Path().apply {
                moveTo(w * 0.5f, h * 0.22f)
                cubicTo(w * 0.35f, h * 0.24f, w * 0.30f, h * 0.40f, w * 0.28f, h * 0.52f)
                lineTo(w * 0.72f, h * 0.52f)
                cubicTo(w * 0.70f, h * 0.40f, w * 0.65f, h * 0.24f, w * 0.5f, h * 0.22f)
                close()
            }
            drawPath(path = domePath, color = primaryColor)

            // Cupola / Top Lantern
            drawRoundRect(
                color = tertiaryColor,
                topLeft = Offset(w * 0.46f, h * 0.16f),
                size = Size(w * 0.08f, h * 0.07f),
                cornerRadius = CornerRadius(4.dp.toPx())
            )

            // Open book at base
            val bookLeft = Path().apply {
                moveTo(w * 0.5f, h * 0.64f)
                quadraticTo(w * 0.32f, h * 0.61f, w * 0.20f, h * 0.63f)
                lineTo(w * 0.20f, h * 0.82f)
                quadraticTo(w * 0.32f, h * 0.80f, w * 0.5f, h * 0.84f)
                close()
            }
            val bookRight = Path().apply {
                moveTo(w * 0.5f, h * 0.64f)
                quadraticTo(w * 0.68f, h * 0.61f, w * 0.80f, h * 0.63f)
                lineTo(w * 0.80f, h * 0.82f)
                quadraticTo(w * 0.68f, h * 0.80f, w * 0.5f, h * 0.84f)
                close()
            }
            drawPath(path = bookLeft, color = primaryColor)
            drawPath(path = bookRight, color = secondaryColor)

            // Page text lines on open book
            drawLine(
                color = Color.White.copy(alpha = 0.8f),
                start = Offset(w * 0.26f, h * 0.70f),
                end = Offset(w * 0.44f, h * 0.70f),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color.White.copy(alpha = 0.8f),
                start = Offset(w * 0.26f, h * 0.75f),
                end = Offset(w * 0.42f, h * 0.75f),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color.White.copy(alpha = 0.8f),
                start = Offset(w * 0.56f, h * 0.70f),
                end = Offset(w * 0.74f, h * 0.70f),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color.White.copy(alpha = 0.8f),
                start = Offset(w * 0.56f, h * 0.75f),
                end = Offset(w * 0.72f, h * 0.75f),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun StudyCardsIllustration(
    modifier: Modifier = Modifier,
    size: Dp = 180.dp
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val containerColor = MaterialTheme.colorScheme.primaryContainer

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height

            // Background soft circle
            drawCircle(
                color = containerColor.copy(alpha = 0.45f),
                radius = w * 0.44f,
                center = Offset(w * 0.5f, h * 0.5f)
            )

            // Back tilted card
            val backCard = Path().apply {
                moveTo(w * 0.22f, h * 0.22f)
                lineTo(w * 0.74f, h * 0.16f)
                lineTo(w * 0.82f, h * 0.62f)
                lineTo(w * 0.30f, h * 0.68f)
                close()
            }
            drawPath(path = backCard, color = secondaryColor.copy(alpha = 0.45f))

            // Main foreground card
            drawRoundRect(
                color = primaryColor,
                topLeft = Offset(w * 0.18f, h * 0.26f),
                size = Size(w * 0.64f, h * 0.54f),
                cornerRadius = CornerRadius(16.dp.toPx())
            )

            // Question badge / Q mark
            drawCircle(
                color = tertiaryColor,
                radius = w * 0.08f,
                center = Offset(w * 0.34f, h * 0.42f)
            )
            drawLine(
                color = Color.White,
                start = Offset(w * 0.48f, h * 0.42f),
                end = Offset(w * 0.72f, h * 0.42f),
                strokeWidth = 4.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Answer lines
            drawLine(
                color = Color.White.copy(alpha = 0.7f),
                start = Offset(w * 0.26f, h * 0.57f),
                end = Offset(w * 0.74f, h * 0.57f),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color.White.copy(alpha = 0.7f),
                start = Offset(w * 0.26f, h * 0.65f),
                end = Offset(w * 0.62f, h * 0.65f),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color.White.copy(alpha = 0.7f),
                start = Offset(w * 0.26f, h * 0.72f),
                end = Offset(w * 0.48f, h * 0.72f),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun InterviewMicIllustration(
    modifier: Modifier = Modifier,
    size: Dp = 180.dp
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val containerColor = MaterialTheme.colorScheme.primaryContainer

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height

            // Background soft circle
            drawCircle(
                color = containerColor.copy(alpha = 0.5f),
                radius = w * 0.44f,
                center = Offset(w * 0.5f, h * 0.5f)
            )

            // Acoustic sound wave rings
            drawCircle(
                color = secondaryColor.copy(alpha = 0.25f),
                radius = w * 0.32f,
                center = Offset(w * 0.5f, h * 0.38f),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
            drawCircle(
                color = secondaryColor.copy(alpha = 0.15f),
                radius = w * 0.40f,
                center = Offset(w * 0.5f, h * 0.38f),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            // Microphone capsule
            drawRoundRect(
                color = primaryColor,
                topLeft = Offset(w * 0.41f, h * 0.20f),
                size = Size(w * 0.18f, h * 0.32f),
                cornerRadius = CornerRadius(18.dp.toPx())
            )

            // Mic grille highlight
            drawLine(
                color = Color.White.copy(alpha = 0.4f),
                start = Offset(w * 0.44f, h * 0.28f),
                end = Offset(w * 0.56f, h * 0.28f),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color.White.copy(alpha = 0.4f),
                start = Offset(w * 0.44f, h * 0.34f),
                end = Offset(w * 0.56f, h * 0.34f),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Mic U-cradle
            val cradle = Path().apply {
                moveTo(w * 0.34f, h * 0.36f)
                lineTo(w * 0.34f, h * 0.46f)
                cubicTo(w * 0.34f, h * 0.58f, w * 0.66f, h * 0.58f, w * 0.66f, h * 0.46f)
                lineTo(w * 0.66f, h * 0.36f)
            }
            drawPath(
                path = cradle,
                color = tertiaryColor,
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
            )

            // Mic stand stem
            drawLine(
                color = tertiaryColor,
                start = Offset(w * 0.5f, h * 0.58f),
                end = Offset(w * 0.5f, h * 0.72f),
                strokeWidth = 5.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Mic stand circular base
            drawLine(
                color = primaryColor,
                start = Offset(w * 0.32f, h * 0.73f),
                end = Offset(w * 0.68f, h * 0.73f),
                strokeWidth = 6.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun ProgressDashboardIllustration(
    modifier: Modifier = Modifier,
    size: Dp = 180.dp
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val containerColor = MaterialTheme.colorScheme.primaryContainer

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height

            // Background circle
            drawCircle(
                color = containerColor.copy(alpha = 0.5f),
                radius = w * 0.44f,
                center = Offset(w * 0.5f, h * 0.5f)
            )

            // Progress gauge track
            drawArc(
                color = secondaryColor.copy(alpha = 0.25f),
                startAngle = 140f,
                sweepAngle = 260f,
                useCenter = false,
                topLeft = Offset(w * 0.18f, h * 0.18f),
                size = Size(w * 0.64f, h * 0.64f),
                style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
            )

            // Active gauge sweep
            drawArc(
                color = primaryColor,
                startAngle = 140f,
                sweepAngle = 190f,
                useCenter = false,
                topLeft = Offset(w * 0.18f, h * 0.18f),
                size = Size(w * 0.64f, h * 0.64f),
                style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
            )

            // Star or checkpoint at center
            drawCircle(
                color = tertiaryColor,
                radius = w * 0.06f,
                center = Offset(w * 0.5f, h * 0.5f)
            )

            // Success checkmark badge
            drawCircle(
                color = primaryColor,
                radius = w * 0.14f,
                center = Offset(w * 0.5f, h * 0.5f)
            )
            val checkPath = Path().apply {
                moveTo(w * 0.43f, h * 0.50f)
                lineTo(w * 0.48f, h * 0.55f)
                lineTo(w * 0.58f, h * 0.44f)
            }
            drawPath(
                path = checkPath,
                color = Color.White,
                style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}
