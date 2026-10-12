package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Clean civic emblem representing classical democratic architecture (Capitol/Courthouse pediment + columns + civic star).
 */
@Composable
fun CivicEmblem(
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
    tint: Color = MaterialTheme.colorScheme.primary,
    accentTint: Color = MaterialTheme.colorScheme.tertiary
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height

            // Outer decorative ring
            drawCircle(
                color = tint.copy(alpha = 0.15f),
                radius = w * 0.48f,
                style = Stroke(width = w * 0.04f)
            )

            // Pediment (Triangle Roof)
            val pediment = Path().apply {
                moveTo(w * 0.5f, h * 0.22f)
                lineTo(w * 0.22f, h * 0.40f)
                lineTo(w * 0.78f, h * 0.40f)
                close()
            }
            drawPath(path = pediment, color = tint)

            // Architrave (Roof base beam)
            drawRect(
                color = tint,
                topLeft = Offset(w * 0.20f, h * 0.42f),
                size = Size(w * 0.60f, h * 0.05f)
            )

            // 4 Classical Columns
            val colWidth = w * 0.08f
            val colHeight = h * 0.22f
            val colY = h * 0.49f
            val colSpacing = (w * 0.60f - 4 * colWidth) / 3f

            for (i in 0 until 4) {
                val colX = w * 0.20f + i * (colWidth + colSpacing)
                drawRect(
                    color = tint,
                    topLeft = Offset(colX, colY),
                    size = Size(colWidth, colHeight)
                )
            }

            // Base Steps
            drawRect(
                color = tint,
                topLeft = Offset(w * 0.18f, h * 0.73f),
                size = Size(w * 0.64f, h * 0.05f)
            )
            drawRect(
                color = tint,
                topLeft = Offset(w * 0.14f, h * 0.80f),
                size = Size(w * 0.72f, h * 0.05f)
            )

            // Star or central civic dot inside pediment
            drawCircle(
                color = accentTint,
                radius = w * 0.045f,
                center = Offset(w * 0.5f, h * 0.32f)
            )
        }
    }
}
