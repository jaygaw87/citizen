package com.example.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class AppElevation(
    val none: Dp = 0.dp,
    val subtle: Dp = 1.dp,
    val low: Dp = 2.dp,
    val medium: Dp = 4.dp,
    val high: Dp = 8.dp,

    // Component semantics
    val cardDefault: Dp = 1.dp,
    val cardRaised: Dp = 3.dp,
    val dialog: Dp = 6.dp,
    val bottomBar: Dp = 4.dp,
    val topBar: Dp = 0.dp
)

val LocalAppElevation = staticCompositionLocalOf { AppElevation() }
