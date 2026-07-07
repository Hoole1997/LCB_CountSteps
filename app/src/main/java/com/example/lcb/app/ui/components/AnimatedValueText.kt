package com.example.lcb.app.ui.components

import android.provider.Settings
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@Composable
fun String.AnimatedValueText(
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = 20.sp,
    lineHeight: TextUnit = TextUnit.Unspecified,
    fontWeight: FontWeight? = FontWeight.SemiBold,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    label: String = "AnimatedValueText",
) {
    val context = LocalContext.current
    val animationsEnabled = remember {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f,
        ) > 0f
    }

    if (!animationsEnabled) {
        Text(
            text = this,
            modifier = modifier,
            color = color,
            fontSize = fontSize,
            lineHeight = lineHeight,
            fontWeight = fontWeight,
            textAlign = textAlign,
            maxLines = maxLines,
        )
        return
    }

    AnimatedContent(
        targetState = this,
        modifier = modifier,
        transitionSpec = {
            val direction = if (targetState.numericValueOrNull() >= initialState.numericValueOrNull()) {
                1
            } else {
                -1
            }
            (
                slideInVertically(animationSpec = tween(180)) { height -> height / 3 * direction } +
                    fadeIn(animationSpec = tween(140))
                ).togetherWith(
                slideOutVertically(animationSpec = tween(120)) { height -> -height / 3 * direction } +
                    fadeOut(animationSpec = tween(100)),
            )
        },
        label = label,
    ) { target ->
        Text(
            text = target,
            color = color,
            fontSize = fontSize,
            lineHeight = lineHeight,
            fontWeight = fontWeight,
            textAlign = textAlign,
            maxLines = maxLines,
        )
    }
}

@Composable
fun AnnotatedString.AnimatedValueText(
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    lineHeight: TextUnit = TextUnit.Unspecified,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    label: String = "AnimatedAnnotatedValueText",
) {
    val context = LocalContext.current
    val animationsEnabled = remember {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f,
        ) > 0f
    }

    if (!animationsEnabled) {
        Text(
            text = this,
            modifier = modifier,
            color = color,
            fontSize = fontSize,
            lineHeight = lineHeight,
            fontWeight = fontWeight,
            textAlign = textAlign,
            maxLines = maxLines,
        )
        return
    }

    AnimatedContent(
        targetState = this,
        modifier = modifier,
        transitionSpec = {
            val direction = if (targetState.text.numericValueOrNull() >= initialState.text.numericValueOrNull()) {
                1
            } else {
                -1
            }
            (
                slideInVertically(animationSpec = tween(180)) { height -> height / 3 * direction } +
                    fadeIn(animationSpec = tween(140))
                ).togetherWith(
                slideOutVertically(animationSpec = tween(120)) { height -> -height / 3 * direction } +
                    fadeOut(animationSpec = tween(100)),
            )
        },
        label = label,
    ) { target ->
        Text(
            text = target,
            color = color,
            fontSize = fontSize,
            lineHeight = lineHeight,
            fontWeight = fontWeight,
            textAlign = textAlign,
            maxLines = maxLines,
        )
    }
}

internal fun String.numericValueOrNull(): Double {
    return Regex("-?\\d+(?:\\.\\d+)?")
        .find(this)
        ?.value
        ?.toDoubleOrNull()
        ?: 0.0
}
