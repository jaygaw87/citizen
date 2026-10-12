package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

val LocalAppSemanticColors = staticCompositionLocalOf { LightSemanticColors }

val MaterialTheme.appColors: AppSemanticColors
    @Composable
    @ReadOnlyComposable
    get() = LocalAppSemanticColors.current

val MaterialTheme.appSpacing: AppSpacing
    @Composable
    @ReadOnlyComposable
    get() = LocalAppSpacing.current

val MaterialTheme.appElevation: AppElevation
    @Composable
    @ReadOnlyComposable
    get() = LocalAppElevation.current

val MaterialTheme.studyTypography: AppStudyTypography
    @Composable
    @ReadOnlyComposable
    get() = LocalAppStudyTypography.current
