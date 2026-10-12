package com.example.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

// Primary deep navy palette
val Navy900 = Color(0xFF0A192F)
val Navy800 = Color(0xFF0F2445)
val Navy700 = Color(0xFF14325C)
val Navy600 = Color(0xFF1B3D6D)
val Navy500 = Color(0xFF244D85)
val Navy100 = Color(0xFFD8E4F8)
val Navy50 = Color(0xFFEDF3FC)

// Secondary slate / civic blue
val SlateBlue700 = Color(0xFF1E3A56)
val SlateBlue600 = Color(0xFF2A577E)
val SlateBlue500 = Color(0xFF456B94)
val SlateBlue100 = Color(0xFFDCE6F2)
val SlateBlue50 = Color(0xFFF0F4F9)

// Civic Red / Historic Burgundy
val CivicRed800 = Color(0xFF7F1D1D)
val CivicRed700 = Color(0xFF991B1B)
val CivicRed600 = Color(0xFFC53030)
val CivicRed500 = Color(0xFFDC2626)
val CivicRed100 = Color(0xFFFEE2E2)
val CivicRed50 = Color(0xFFFEF2F2)

// Soft Civic Gold / Seal Accent
val CivicGold800 = Color(0xFF78350F)
val CivicGold700 = Color(0xFFB45309)
val CivicGold600 = Color(0xFFD97706)
val CivicGold500 = Color(0xFFF59E0B)
val CivicGold100 = Color(0xFFFEF3C7)
val CivicGold50 = Color(0xFFFFFBEB)

// Federal / Success Green
val FederalGreen800 = Color(0xFF14532D)
val FederalGreen700 = Color(0xFF1B5E20)
val FederalGreen600 = Color(0xFF2E7D32)
val FederalGreen500 = Color(0xFF388E3C)
val FederalGreen100 = Color(0xFFE8F5E9)
val FederalGreen50 = Color(0xFFF1F8F2)

// Info / Slate Blue
val InfoBlue700 = Color(0xFF1D4ED8)
val InfoBlue600 = Color(0xFF2563EB)
val InfoBlue100 = Color(0xFFDBEAFE)
val InfoBlue50 = Color(0xFFEFF6FF)

// Semantic status alias constants
val AmberGold700 = CivicGold700
val AmberGold600 = CivicGold600
val AmberGold500 = CivicGold500
val AmberGold100 = CivicGold100
val AmberGold50 = CivicGold50

val BrickRed700 = CivicRed700
val BrickRed600 = CivicRed600
val BrickRed500 = CivicRed500
val BrickRed100 = CivicRed100
val BrickRed50 = CivicRed50

// Light Theme Tokens (Refined M3 Palette)
val LightPrimary = Color(0xFF0A192F)
val LightOnPrimary = Color(0xFFFFFFFF)
val LightPrimaryContainer = Color(0xFFDCE6F2)
val LightOnPrimaryContainer = Color(0xFF0A192F)

val LightSecondary = Color(0xFF2A577E)
val LightOnSecondary = Color(0xFFFFFFFF)
val LightSecondaryContainer = Color(0xFFDCE8F4)
val LightOnSecondaryContainer = Color(0xFF0F2445)

val LightTertiary = Color(0xFFB45309)
val LightOnTertiary = Color(0xFFFFFFFF)
val LightTertiaryContainer = Color(0xFFFEF3C7)
val LightOnTertiaryContainer = Color(0xFF78350F)

val LightError = Color(0xFFC53030)
val LightOnError = Color(0xFFFFFFFF)
val LightErrorContainer = Color(0xFFFEE2E2)
val LightOnErrorContainer = Color(0xFF991B1B)

val LightBackground = Color(0xFFF8F9FA)
val LightOnBackground = Color(0xFF0F172A)
val LightSurface = Color(0xFFFFFFFF)
val LightOnSurface = Color(0xFF0F172A)
val LightSurfaceVariant = Color(0xFFF1F5F9)
val LightOnSurfaceVariant = Color(0xFF475569)
val LightOutline = Color(0xFF94A3B8)
val LightOutlineVariant = Color(0xFFE2E8F0)

// Dark Theme Tokens (Refined M3 Deep Navy Palette)
val DarkPrimary = Color(0xFF90B4E2)
val DarkOnPrimary = Color(0xFF071424)
val DarkPrimaryContainer = Color(0xFF1B3252)
val DarkOnPrimaryContainer = Color(0xFFD4E3F7)

val DarkSecondary = Color(0xFF7DB1D9)
val DarkOnSecondary = Color(0xFF06233B)
val DarkSecondaryContainer = Color(0xFF1E3A56)
val DarkOnSecondaryContainer = Color(0xFFCEE5FA)

val DarkTertiary = Color(0xFFF59E0B)
val DarkOnTertiary = Color(0xFF451A03)
val DarkTertiaryContainer = Color(0xFF78350F)
val DarkOnTertiaryContainer = Color(0xFFFEF3C7)

val DarkError = Color(0xFFF87171)
val DarkOnError = Color(0xFF450A0A)
val DarkErrorContainer = Color(0xFF7F1D1D)
val DarkOnErrorContainer = Color(0xFFFECACA)

val DarkBackground = Color(0xFF0B131E)
val DarkOnBackground = Color(0xFFF1F5F9)
val DarkSurface = Color(0xFF131D2C)
val DarkOnSurface = Color(0xFFF1F5F9)
val DarkSurfaceVariant = Color(0xFF1C2A3D)
val DarkOnSurfaceVariant = Color(0xFF94A3B8)
val DarkOutline = Color(0xFF64748B)
val DarkOutlineVariant = Color(0xFF28394E)

@Immutable
data class AppSemanticColors(
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
    val error: Color,
    val onError: Color,
    val errorContainer: Color,
    val onErrorContainer: Color,
    val info: Color,
    val onInfo: Color,
    val infoContainer: Color,
    val onInfoContainer: Color,
    val cardBorder: Color,
    val divider: Color,
    val govCategory: Color,
    val govCategoryContainer: Color,
    val historyCategory: Color,
    val historyCategoryContainer: Color,
    val civicsCategory: Color,
    val civicsCategoryContainer: Color,
    val seniorExemption: Color,
    val seniorExemptionContainer: Color,
    val starGold: Color
)

val LightSemanticColors = AppSemanticColors(
    success = FederalGreen600,
    onSuccess = Color.White,
    successContainer = FederalGreen100,
    onSuccessContainer = FederalGreen800,
    warning = CivicGold600,
    onWarning = Color.White,
    warningContainer = CivicGold100,
    onWarningContainer = CivicGold800,
    error = CivicRed600,
    onError = Color.White,
    errorContainer = CivicRed100,
    onErrorContainer = CivicRed800,
    info = InfoBlue600,
    onInfo = Color.White,
    infoContainer = InfoBlue100,
    onInfoContainer = InfoBlue700,
    cardBorder = Color(0xFFE2E8F0),
    divider = Color(0xFFE2E8F0),
    govCategory = Color(0xFF0A192F),
    govCategoryContainer = Color(0xFFE2E8F0),
    historyCategory = Color(0xFF991B1B),
    historyCategoryContainer = Color(0xFFFEE2E2),
    civicsCategory = Color(0xFF2A577E),
    civicsCategoryContainer = Color(0xFFDCE8F4),
    seniorExemption = Color(0xFFB45309),
    seniorExemptionContainer = Color(0xFFFEF3C7),
    starGold = Color(0xFFD97706)
)

val DarkSemanticColors = AppSemanticColors(
    success = Color(0xFF4ADE80),
    onSuccess = Color(0xFF052E16),
    successContainer = Color(0xFF14532D),
    onSuccessContainer = Color(0xFFDCFCE7),
    warning = Color(0xFFFBBF24),
    onWarning = Color(0xFF451A03),
    warningContainer = Color(0xFF78350F),
    onWarningContainer = Color(0xFFFEF3C7),
    error = Color(0xFFF87171),
    onError = Color(0xFF450A0A),
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFECACA),
    info = Color(0xFF60A5FA),
    onInfo = Color(0xFF082F49),
    infoContainer = Color(0xFF1E3A8A),
    onInfoContainer = Color(0xFFDBEAFE),
    cardBorder = Color(0xFF28394E),
    divider = Color(0xFF1E293B),
    govCategory = Color(0xFF90B4E2),
    govCategoryContainer = Color(0xFF1B3252),
    historyCategory = Color(0xFFF87171),
    historyCategoryContainer = Color(0xFF7F1D1D),
    civicsCategory = Color(0xFF7DB1D9),
    civicsCategoryContainer = Color(0xFF1E3A56),
    seniorExemption = Color(0xFFFBBF24),
    seniorExemptionContainer = Color(0xFF78350F),
    starGold = Color(0xFFF59E0B)
)
