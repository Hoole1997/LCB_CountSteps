package com.example.lcb.app.ui.hydrate

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View

internal class AddWaterIconView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = HydrateVisualTokens.TextPrimary
        style = Paint.Style.FILL
    }
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = HydrateVisualTokens.AccentBlue
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeWidth = resources.displayMetrics.density * 1.4f
    }
    private val dropPath = Path()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        dropPath.reset()
        dropPath.moveTo(w * 0.48f, h * 0.06f)
        dropPath.cubicTo(w * 0.16f, h * 0.38f, w * 0.1f, h * 0.58f, w * 0.1f, h * 0.72f)
        dropPath.cubicTo(w * 0.1f, h * 0.94f, w * 0.3f, h, w * 0.48f, h)
        dropPath.cubicTo(w * 0.66f, h, w * 0.86f, h * 0.94f, w * 0.86f, h * 0.72f)
        dropPath.cubicTo(w * 0.86f, h * 0.58f, w * 0.78f, h * 0.38f, w * 0.48f, h * 0.06f)
        canvas.drawPath(dropPath, paint)

        val cx = w * 0.72f
        val cy = h * 0.72f
        canvas.drawLine(cx - w * 0.12f, cy, cx + w * 0.12f, cy, strokePaint)
        canvas.drawLine(cx, cy - h * 0.12f, cx, cy + h * 0.12f, strokePaint)
    }
}

