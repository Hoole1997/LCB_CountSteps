package com.example.lcb.app.ui.home

import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
internal fun animatedProgress(
    target: Float,
    label: String,
): Float {
    val context = LocalContext.current
    val animationsEnabled = remember {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f,
        ) > 0f
    }
    val normalizedTarget = target.coerceIn(0f, 1f)
    if (!animationsEnabled) {
        return normalizedTarget
    }

    val progress = remember(label) { Animatable(0f) }
    LaunchedEffect(normalizedTarget, animationsEnabled) {
        progress.animateTo(
            targetValue = normalizedTarget,
            animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
        )
    }
    return progress.value
}
