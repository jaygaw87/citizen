package com.example.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class AppSpacing(
    val xxs: Dp = 2.dp,
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 12.dp,
    val lg: Dp = 16.dp,
    val xl: Dp = 20.dp,
    val xxl: Dp = 24.dp,
    val xxxl: Dp = 32.dp,
    val huge: Dp = 40.dp,
    val massive: Dp = 48.dp,

    // Semantic layout spacing
    val screenHorizontalPadding: Dp = 16.dp,
    val screenVerticalPadding: Dp = 16.dp,
    val cardPadding: Dp = 16.dp,
    val cardPaddingLarge: Dp = 20.dp,
    val sectionSpacing: Dp = 20.dp,
    val itemSpacing: Dp = 12.dp,
    val buttonSpacing: Dp = 12.dp,
    val buttonHeight: Dp = 52.dp,
    val minTouchTarget: Dp = 48.dp,
    val iconSmall: Dp = 16.dp,
    val iconMedium: Dp = 20.dp,
    val iconLarge: Dp = 24.dp,
    val iconExtraLarge: Dp = 32.dp
)

val LocalAppSpacing = staticCompositionLocalOf { AppSpacing() }
