package com.example.lcb.app.ui.hydrate

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator

internal class WaterDropWaveView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {
    private val density = resources.displayMetrics.density
    private val dropPath = Path()
    private val frontWavePath = Path()
    private val backWavePath = Path()
    private val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0x99000000.toInt()
        style = Paint.Style.FILL
        maskFilter = BlurMaskFilter(18f * density, BlurMaskFilter.Blur.NORMAL)
    }
    private val wavePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val backWavePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val frostPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val rimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.2f * density
        color = 0x18FFFFFF
    }
    private val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 9f * density
    }
    private val highlightPath = Path()
    private var phase = 0f
    private var progress = 0f
    private val animator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 2600L
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()
        addUpdateListener {
            phase = it.animatedValue as Float
            invalidate()
        }
    }

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    fun setProgress(value: Float) {
        progress = value.coerceIn(0f, 1f)
        if (progress > 0f && isAttachedToWindow && !animator.isStarted) {
            animator.start()
        } else if (progress <= 0f && animator.isStarted) {
            animator.cancel()
        }
        invalidate()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (progress > 0f && !animator.isStarted) animator.start()
    }

    override fun onDetachedFromWindow() {
        animator.cancel()
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        buildDropPath(width.toFloat(), height.toFloat())
        canvas.save()
        canvas.translate(0f, 8f * density)
        canvas.drawPath(dropPath, shadowPaint)
        canvas.restore()

        bodyPaint.shader = RadialGradient(
            width * 0.46f,
            height * 0.44f,
            width * 0.56f,
            intArrayOf(0xFF333336.toInt(), 0xFF202023.toInt(), 0xFF121214.toInt()),
            floatArrayOf(0f, 0.62f, 1f),
            Shader.TileMode.CLAMP,
        )
        canvas.drawPath(dropPath, bodyPaint)
        bodyPaint.shader = null
        drawFrostedShell(canvas)

        if (progress > 0f) {
            canvas.save()
            canvas.clipPath(dropPath)
            drawWave(canvas)
            canvas.restore()
        }
        canvas.drawPath(dropPath, rimPaint)
        drawHighlight(canvas)
    }

    private fun buildDropPath(w: Float, h: Float) {
        dropPath.reset()
        dropPath.moveTo(w * 0.445f, h * 0.095f)
        dropPath.cubicTo(w * 0.462f, h * 0.055f, w * 0.538f, h * 0.055f, w * 0.555f, h * 0.095f)
        dropPath.cubicTo(w * 0.765f, h * 0.335f, w * 0.88f, h * 0.56f, w * 0.88f, h * 0.68f)
        dropPath.cubicTo(w * 0.88f, h * 0.88f, w * 0.7f, h * 0.98f, w * 0.5f, h * 0.98f)
        dropPath.cubicTo(w * 0.3f, h * 0.98f, w * 0.12f, h * 0.88f, w * 0.12f, h * 0.68f)
        dropPath.cubicTo(w * 0.12f, h * 0.56f, w * 0.235f, h * 0.335f, w * 0.445f, h * 0.095f)
        dropPath.close()
    }

    private fun drawWave(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        val frontAmplitude = 11f * density
        val backAmplitude = 8f * density
        val waterTop = waterSurfaceY(
            height = h,
            progress = progress,
            amplitude = frontAmplitude,
        )
        val wavelength = w * 0.58f
        val phaseShift = phase * wavelength

        buildBezierWave(
            path = backWavePath,
            width = w,
            height = h,
            centerY = waterTop + 9f * density,
            amplitude = backAmplitude,
            wavelength = wavelength * 0.92f,
            phaseShift = -phaseShift * 0.65f,
        )
        backWavePaint.shader = LinearGradient(
            0f,
            waterTop,
            w,
            h,
            intArrayOf(0x5538B7FF, 0x885DF1E6.toInt()),
            null,
            Shader.TileMode.CLAMP,
        )
        canvas.drawPath(backWavePath, backWavePaint)
        backWavePaint.shader = null

        buildBezierWave(
            path = frontWavePath,
            width = w,
            height = h,
            centerY = waterTop,
            amplitude = frontAmplitude,
            wavelength = wavelength,
            phaseShift = phaseShift,
        )
        wavePaint.shader = LinearGradient(
            0f,
            waterTop - frontAmplitude,
            w,
            h,
            intArrayOf(0xFF52A7FF.toInt(), 0xFF65DDEB.toInt(), 0xFF5DE2DD.toInt()),
            floatArrayOf(0f, 0.55f, 1f),
            Shader.TileMode.CLAMP,
        )
        canvas.drawPath(frontWavePath, wavePaint)
        wavePaint.shader = null
    }

    private fun waterSurfaceY(height: Float, progress: Float, amplitude: Float): Float {
        val safeProgress = progress.coerceIn(0f, 1f)
        val emptySurface = height * 0.98f
        val fullSurface = height * 0.055f
        if (safeProgress >= 0.999f) {
            return -amplitude * 2f
        }
        // Map the real hydration ratio to the whole droplet height. The previous
        // 34%-92% range intentionally left the top unfilled, so full progress
        // could never visually reach the rounded tip.
        return emptySurface + (fullSurface - emptySurface) * safeProgress
    }

    private fun buildBezierWave(
        path: Path,
        width: Float,
        height: Float,
        centerY: Float,
        amplitude: Float,
        wavelength: Float,
        phaseShift: Float,
    ) {
        path.reset()
        var x = -wavelength * 2f - phaseShift
        path.moveTo(x, height)
        path.lineTo(x, centerY)
        // Cubic Bezier half-waves make the water edge visibly curved and allow smooth horizontal motion.
        while (x < width + wavelength * 2f) {
            path.cubicTo(
                x + wavelength * 0.25f,
                centerY - amplitude,
                x + wavelength * 0.25f,
                centerY - amplitude,
                x + wavelength * 0.5f,
                centerY,
            )
            path.cubicTo(
                x + wavelength * 0.75f,
                centerY + amplitude,
                x + wavelength * 0.75f,
                centerY + amplitude,
                x + wavelength,
                centerY,
            )
            x += wavelength
        }
        path.lineTo(x, height)
        path.close()
    }

    private fun drawFrostedShell(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        canvas.save()
        canvas.clipPath(dropPath)
        frostPaint.shader = LinearGradient(
            0f,
            0f,
            w,
            h,
            intArrayOf(0x30FFFFFF, 0x05000000, 0x2AFFFFFF, 0x33000000),
            floatArrayOf(0f, 0.34f, 0.68f, 1f),
            Shader.TileMode.CLAMP,
        )
        canvas.drawRect(0f, 0f, w, h, frostPaint)
        frostPaint.shader = RadialGradient(
            w * 0.36f,
            h * 0.35f,
            w * 0.36f,
            intArrayOf(0x22FFFFFF, 0x08000000, Color.TRANSPARENT),
            floatArrayOf(0f, 0.58f, 1f),
            Shader.TileMode.CLAMP,
        )
        canvas.drawCircle(w * 0.36f, h * 0.35f, w * 0.36f, frostPaint)
        frostPaint.shader = RadialGradient(
            w * 0.7f,
            h * 0.82f,
            w * 0.42f,
            intArrayOf(0x11000000, 0x33000000, Color.TRANSPARENT),
            floatArrayOf(0f, 0.58f, 1f),
            Shader.TileMode.CLAMP,
        )
        canvas.drawCircle(w * 0.7f, h * 0.82f, w * 0.42f, frostPaint)
        frostPaint.shader = null
        canvas.restore()
    }

    private fun drawHighlight(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        highlightPath.reset()
        highlightPath.addArc(
            RectF(w * 0.56f, h * 0.42f, w * 0.78f, h * 0.78f),
            8f,
            76f,
        )
        highlightPaint.alpha = if (progress > 0f) 255 else 95
        canvas.drawPath(highlightPath, highlightPaint)
    }
}
