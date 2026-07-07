package com.example.lcb.app.ui.hydrate

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View

internal class HydrateHeaderBackgroundView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.TRANSPARENT)
        fillPaint.shader = RadialGradient(
            width * 0.5f,
            -height * 0.1f,
            height * 0.82f,
            intArrayOf(0x6632D4B5, 0x221D6BF2, Color.TRANSPARENT),
            floatArrayOf(0f, 0.42f, 1f),
            Shader.TileMode.CLAMP,
        )
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), fillPaint)

        drawGlow(canvas, width * 0.88f, -height * 0.02f, width * 0.9f, height * 0.38f, 0x4432D4B5)
        drawGlow(canvas, width * 0.16f, height * 0.02f, width * 0.82f, height * 0.32f, 0x331D6BF2)
        fillPaint.shader = null
    }

    private fun drawGlow(canvas: Canvas, cx: Float, cy: Float, w: Float, h: Float, color: Int) {
        glowPaint.shader = RadialGradient(cx, cy, w.coerceAtLeast(h) * 0.55f, color, Color.TRANSPARENT, Shader.TileMode.CLAMP)
        canvas.drawOval(RectF(cx - w / 2f, cy - h / 2f, cx + w / 2f, cy + h / 2f), glowPaint)
        glowPaint.shader = null
    }
}

