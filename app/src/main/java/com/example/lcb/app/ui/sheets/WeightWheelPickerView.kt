package com.example.lcb.app.ui.sheets

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.TypedValue
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.view.animation.DecelerateInterpolator
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sign

class WeightWheelPickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    var value: Int = 0
        private set

    var onValueChanged: ((Int) -> Unit)? = null

    private var minValue = 0
    private var maxValue = 0
    private var position = 0f
    private var lastTouchY = 0f
    private var velocityTracker: VelocityTracker? = null
    private var snapAnimator: ValueAnimator? = null

    private val rowHeight = 44f.dp
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private var isDragging = false

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
        textSize = 26f.sp
    }

    init {
        isHapticFeedbackEnabled = true
    }

    fun setRange(min: Int, max: Int) {
        minValue = min
        maxValue = max.coerceAtLeast(min)
        setValue(value.coerceIn(minValue, maxValue), notify = false)
    }

    fun setValue(newValue: Int, notify: Boolean = true) {
        val coerced = newValue.coerceIn(minValue, maxValue)
        val changed = coerced != value
        value = coerced
        position = (coerced - minValue).toFloat()
        invalidate()
        if (notify && changed) onValueChanged?.invoke(coerced)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = 92f.dp.roundToInt()
        val desiredHeight = 140f.dp.roundToInt()
        setMeasuredDimension(resolveSize(desiredWidth, widthMeasureSpec), resolveSize(desiredHeight, heightMeasureSpec))
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val centerY = height / 2f
        val centerX = width / 2f

        val baseIndex = position.roundToInt()
        for (index in (baseIndex - 3)..(baseIndex + 3)) {
            val itemValue = minValue + index
            if (itemValue !in minValue..maxValue) continue
            val distance = index - position
            val easedDistance = sign(distance) * abs(distance).pow(0.92f)
            val y = centerY + easedDistance * rowHeight
            if (y < -rowHeight || y > height + rowHeight) continue

            val closeness = (1f - min(1f, abs(distance))).coerceIn(0f, 1f)
            val easedCloseness = smoothStep(closeness)
            textPaint.textSize = lerp(13f.sp, 28f.sp, easedCloseness)
            textPaint.alpha = lerp(82f, 255f, easedCloseness).roundToInt()
            textPaint.typeface = if (easedCloseness > 0.42f) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            val textBaseline = y - (textPaint.ascent() + textPaint.descent()) / 2f
            canvas.drawText(itemValue.toString(), centerX, textBaseline, textPaint)
        }
        textPaint.alpha = 255
        textPaint.textSize = 26f.sp
        textPaint.typeface = Typeface.DEFAULT_BOLD
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                parent.requestDisallowInterceptTouchEvent(true)
                snapAnimator?.cancel()
                velocityTracker = VelocityTracker.obtain().also { it.addMovement(event) }
                lastTouchY = event.y
                isDragging = false
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                velocityTracker?.addMovement(event)
                val dy = lastTouchY - event.y
                if (!isDragging && abs(dy) > touchSlop) isDragging = true
                if (isDragging) {
                    updatePosition(position + dy / rowHeight, haptic = true)
                    lastTouchY = event.y
                }
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                velocityTracker?.addMovement(event)
                velocityTracker?.computeCurrentVelocity(1000)
                val velocityY = velocityTracker?.yVelocity ?: 0f
                velocityTracker?.recycle()
                velocityTracker = null
                val flingOffset = (-velocityY / rowHeight) * 0.12f
                animateToNearest(position + flingOffset)
                parent.requestDisallowInterceptTouchEvent(false)
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun updatePosition(newPosition: Float, haptic: Boolean) {
        val oldValue = value
        position = newPosition.coerceIn(0f, (maxValue - minValue).toFloat())
        val roundedValue = minValue + position.roundToInt()
        if (roundedValue != oldValue) {
            value = roundedValue.coerceIn(minValue, maxValue)
            onValueChanged?.invoke(value)
            if (haptic) performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
        }
        invalidate()
    }

    private fun animateToNearest(targetPosition: Float) {
        val target = targetPosition
            .coerceIn(0f, (maxValue - minValue).toFloat())
            .roundToInt()
            .toFloat()
        snapAnimator?.cancel()
        snapAnimator = ValueAnimator.ofFloat(position, target).apply {
            duration = max(120L, min(260L, (abs(target - position) * 58L).roundToInt().toLong() + 120L))
            interpolator = DecelerateInterpolator(1.8f)
            addUpdateListener { updatePosition(it.animatedValue as Float, haptic = true) }
            start()
        }
    }

    private val Float.dp: Float get() = this * resources.displayMetrics.density
    private val Float.sp: Float get() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this, resources.displayMetrics)

    private fun lerp(start: Float, end: Float, fraction: Float): Float = start + (end - start) * fraction

    private fun smoothStep(value: Float): Float {
        val x = value.coerceIn(0f, 1f)
        return x * x * (3f - 2f * x)
    }
}
