package com.example.lcb.app.ui.components

import android.provider.Settings
import android.view.View
import android.widget.TextView

fun TextView.setAnimatedValueText(value: CharSequence) {
    if (text.toString() == value.toString()) {
        return
    }

    val animationsEnabled = runCatching {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) > 0f
    }.getOrDefault(true)

    if (!animationsEnabled || !isLaidOutForValueAnimation() || visibility != View.VISIBLE) {
        text = value
        alpha = 1f
        translationY = 0f
        return
    }

    val direction = if (value.toString().numericValueOrNull() >= text.toString().numericValueOrNull()) {
        1f
    } else {
        -1f
    }
    val offset = (height.takeIf { it > 0 } ?: lineHeight).coerceAtLeast(1) * 0.28f * direction

    animate().cancel()
    animate()
        .translationY(-offset)
        .alpha(0f)
        .setDuration(90L)
        .withEndAction {
            text = value
            translationY = offset
            animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(120L)
                .start()
        }
        .start()
}

private fun View.isLaidOutForValueAnimation(): Boolean {
    return isAttachedToWindow && width > 0 && height > 0
}
