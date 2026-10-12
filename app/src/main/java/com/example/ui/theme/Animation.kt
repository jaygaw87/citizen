package com.example.ui.theme

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically

object AppAnimation {
    const val DurationFast = 150
    const val DurationNormal = 250
    const val DurationSlow = 350
    const val DurationProgress = 600

    val fastTween: AnimationSpec<Float> = tween(durationMillis = DurationFast, easing = FastOutSlowInEasing)
    val normalTween: AnimationSpec<Float> = tween(durationMillis = DurationNormal, easing = FastOutSlowInEasing)
    val slowTween: AnimationSpec<Float> = tween(durationMillis = DurationSlow, easing = FastOutSlowInEasing)
    val progressTween: AnimationSpec<Float> = tween(durationMillis = DurationProgress, easing = FastOutSlowInEasing)

    val bouncySpring = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )

    val gentleSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

    fun screenEnter(): EnterTransition {
        return fadeIn(
            animationSpec = tween(durationMillis = DurationNormal, easing = LinearOutSlowInEasing)
        ) + slideInVertically(
            animationSpec = tween(durationMillis = DurationNormal, easing = FastOutSlowInEasing),
            initialOffsetY = { fullHeight -> (fullHeight * 0.04f).toInt() }
        )
    }

    fun screenExit(): ExitTransition {
        return fadeOut(
            animationSpec = tween(durationMillis = DurationFast, easing = FastOutLinearInEasing)
        ) + slideOutVertically(
            animationSpec = tween(durationMillis = DurationFast, easing = FastOutLinearInEasing),
            targetOffsetY = { fullHeight -> -(fullHeight * 0.02f).toInt() }
        )
    }
}
