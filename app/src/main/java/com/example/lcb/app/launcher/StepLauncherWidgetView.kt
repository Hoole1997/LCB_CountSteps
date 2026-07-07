package com.example.lcb.app.launcher

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import com.example.lcb.app.R
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.min
import kotlin.math.roundToInt

class StepLauncherWidgetView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    private var state = StepLauncherWidgetState.empty(context)

    private val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    }
    private val mutedTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(179, 255, 255, 255)
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    }
    private val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val path = Path()

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    fun update(newState: StepLauncherWidgetState) {
        state = newState
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(resolveSize(343f.dp.roundToInt(), widthMeasureSpec), resolveSize(271f.dp.roundToInt(), heightMeasureSpec))
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val scale = min(width / DesignWidth, height / DesignHeight)
        val dx = (width - DesignWidth * scale) / 2f
        val dy = (height - DesignHeight * scale) / 2f
        canvas.save()
        canvas.translate(dx, dy)
        canvas.scale(scale, scale)
        drawCard(canvas)
        drawHeader(canvas)
        drawWeeklyStatus(canvas)
        drawMetrics(canvas)
        canvas.restore()
    }

    private fun drawCard(canvas: Canvas) {
        cardPaint.shader = LinearGradient(
            0f,
            0f,
            DesignWidth,
            DesignHeight,
            intArrayOf(
                Color.argb(102, 255, 255, 255),
                Color.argb(48, 0, 0, 0),
                Color.argb(115, 0, 0, 0),
            ),
            floatArrayOf(0f, 0.48f, 1f),
            Shader.TileMode.CLAMP,
        )
        cardPaint.setShadowLayer(22f, 0f, 10f, Color.argb(56, 0, 0, 0))
        canvas.drawRoundRect(RectF(0f, 0f, DesignWidth, DesignHeight), 16f, 16f, cardPaint)
        cardPaint.shader = null
        cardPaint.clearShadowLayer()
    }

    private fun drawHeader(canvas: Canvas) {
        textPaint.color = Color.WHITE
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        textPaint.textSize = 56f
        canvas.drawText(LocalTime.now().format(TimeFormatter), 16f, 72f, textPaint)

        textPaint.textSize = 20f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(LocalDate.now().format(DateFormatter), 16f, 111f, textPaint)

        drawLogo(canvas, 271f, 25f)
    }

    private fun drawWeeklyStatus(canvas: Canvas) {
        val startX = 28f
        val centerGap = 43.8f
        val iconCenterY = 153f
        state.days.forEachIndexed { index, day ->
            val centerX = startX + index * centerGap
            drawDayIcon(canvas, centerX, iconCenterY, day)
            textPaint.color = Color.WHITE
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textPaint.textSize = 12f
            textPaint.textAlign = Paint.Align.CENTER
            canvas.drawText(day.label, centerX, 181f, textPaint)
            textPaint.textAlign = Paint.Align.LEFT
        }
    }

    private fun drawMetrics(canvas: Canvas) {
        drawMetric(canvas, x = 57f, value = state.minutes.toString(), label = context.getString(R.string.home_unit_min))
        drawMetric(canvas, x = 171.5f, value = state.calories.toString(), label = context.getString(R.string.unit_kcal))
        drawMetric(canvas, x = 286f, value = state.distanceKmText, label = context.getString(R.string.unit_km_title))
    }

    private fun drawMetric(canvas: Canvas, x: Float, value: String, label: String) {
        textPaint.color = Color.WHITE
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textSize = 18f
        textPaint.textAlign = Paint.Align.CENTER
        canvas.drawText(value, x, 223f, textPaint)

        mutedTextPaint.textSize = 12f
        mutedTextPaint.textAlign = Paint.Align.CENTER
        canvas.drawText(label, x, 249f, mutedTextPaint)
        textPaint.textAlign = Paint.Align.LEFT
        mutedTextPaint.textAlign = Paint.Align.LEFT
    }

    private fun drawDayIcon(canvas: Canvas, centerX: Float, centerY: Float, day: LauncherWidgetDayState) {
        val radius = 10f
        when {
            day.steps <= 0 -> {
                iconPaint.style = Paint.Style.FILL
                iconPaint.color = Color.argb(118, 95, 108, 91)
                canvas.drawCircle(centerX, centerY, radius, iconPaint)
                strokePaint.color = Color.WHITE
                strokePaint.strokeWidth = 1.35f
                canvas.drawLine(centerX - 3.3f, centerY - 3.3f, centerX + 3.3f, centerY + 3.3f, strokePaint)
                canvas.drawLine(centerX + 3.3f, centerY - 3.3f, centerX - 3.3f, centerY + 3.3f, strokePaint)
            }
            day.steps >= day.goal -> {
                iconPaint.style = Paint.Style.FILL
                iconPaint.color = Color.argb(218, 255, 255, 255)
                canvas.drawCircle(centerX, centerY, radius, iconPaint)
                strokePaint.color = Color.rgb(73, 173, 104)
                strokePaint.strokeWidth = 1.8f
                canvas.drawLine(centerX - 4.2f, centerY, centerX - 1f, centerY + 3.1f, strokePaint)
                canvas.drawLine(centerX - 1f, centerY + 3.1f, centerX + 5.3f, centerY - 4.3f, strokePaint)
            }
            else -> {
                val progress = (day.steps.toFloat() / day.goal.coerceAtLeast(1)).coerceIn(0f, 1f)
                strokePaint.strokeWidth = 3.4f
                strokePaint.color = Color.argb(125, 255, 255, 255)
                canvas.drawCircle(centerX, centerY, radius - 1.7f, strokePaint)
                strokePaint.color = Color.WHITE
                canvas.drawArc(
                    RectF(centerX - radius + 1.7f, centerY - radius + 1.7f, centerX + radius - 1.7f, centerY + radius - 1.7f),
                    -90f,
                    (progress * 360f).coerceAtLeast(18f),
                    false,
                    strokePaint,
                )
            }
        }
    }

    private fun drawLogo(canvas: Canvas, left: Float, top: Float) {
        iconPaint.style = Paint.Style.FILL
        iconPaint.shader = LinearGradient(left, top, left + 56f, top + 56f, Color.rgb(21, 222, 184), Color.rgb(4, 193, 163), Shader.TileMode.CLAMP)
        canvas.drawRoundRect(RectF(left, top, left + 56f, top + 56f), 16f, 16f, iconPaint)
        iconPaint.shader = null

        strokePaint.color = Color.WHITE
        strokePaint.strokeWidth = 2.1f
        strokePaint.style = Paint.Style.STROKE
        path.reset()
        path.moveTo(left + 16f, top + 34f)
        path.cubicTo(left + 24f, top + 34f, left + 32f, top + 31f, left + 37f, top + 25f)
        path.cubicTo(left + 40f, top + 29f, left + 44f, top + 31f, left + 48f, top + 32f)
        path.cubicTo(left + 45f, top + 39f, left + 36f, top + 44f, left + 24f, top + 44f)
        path.cubicTo(left + 17f, top + 44f, left + 13f, top + 40f, left + 16f, top + 34f)
        canvas.drawPath(path, strokePaint)
        canvas.drawLine(left + 31f, top + 24f, left + 31f, top + 17f, strokePaint)
        canvas.drawLine(left + 35f, top + 27f, left + 39f, top + 24f, strokePaint)
        canvas.drawLine(left + 25f, top + 37f, left + 39f, top + 37f, strokePaint)
    }

    private val Float.dp: Float get() = this * resources.displayMetrics.density
}

data class StepLauncherWidgetState(
    val days: List<LauncherWidgetDayState>,
    val minutes: Int,
    val calories: Int,
    val distanceKmText: String,
) {
    companion object {
        fun empty(context: Context): StepLauncherWidgetState = StepLauncherWidgetState(
            days = listOf(
                LauncherWidgetDayState(context.getString(R.string.weekday_sun), 0, 1),
                LauncherWidgetDayState(context.getString(R.string.weekday_mon), 0, 1),
                LauncherWidgetDayState(context.getString(R.string.weekday_tue), 0, 1),
                LauncherWidgetDayState(context.getString(R.string.weekday_wed), 0, 1),
                LauncherWidgetDayState(context.getString(R.string.weekday_thu), 0, 1),
                LauncherWidgetDayState(context.getString(R.string.weekday_fri), 0, 1),
                LauncherWidgetDayState(context.getString(R.string.weekday_sat), 0, 1),
            ),
            minutes = 0,
            calories = 0,
            distanceKmText = "0.0",
        )
    }
}

data class LauncherWidgetDayState(
    val label: String,
    val steps: Int,
    val goal: Int,
)

private val TimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.US)
private val DateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yy", Locale.US)
private const val DesignWidth = 343f
private const val DesignHeight = 271f
