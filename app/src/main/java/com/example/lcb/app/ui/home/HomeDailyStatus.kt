package com.example.lcb.app.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.lcb.app.R
import java.time.DayOfWeek

@Composable
internal fun DailyStatusIcon(
    value: Int,
    goal: Int,
    modifier: Modifier = Modifier.size(20.dp),
) {
    val state = DailyStatusState.from(value = value, goal = goal)
    val animatedStateProgress = animatedProgress(
        target = when (state) {
            DailyStatusState.Empty -> 0f
            DailyStatusState.Complete -> 1f
            is DailyStatusState.Progress -> state.progress
        },
        label = "HomeDailyStatusProgress",
    )
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension / 2f
        when (state) {
            is DailyStatusState.Empty -> {
                drawCircle(color = ReportEmptyFill, radius = radius, center = center)
                val stroke = 1.35.dp.toPx()
                val markInset = 6.5.dp.toPx()
                drawLine(
                    color = ReportEmptyMark,
                    start = Offset(markInset, markInset),
                    end = Offset(size.width - markInset, size.height - markInset),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round,
                )
                drawLine(
                    color = ReportEmptyMark,
                    start = Offset(size.width - markInset, markInset),
                    end = Offset(markInset, size.height - markInset),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round,
                )
            }

            is DailyStatusState.Progress -> {
                val stroke = 3.dp.toPx()
                val arcRadius = radius - stroke / 2f
                val sweepAngle = (360f * animatedStateProgress).coerceAtLeast(8f)
                drawCircle(
                    color = ReportProgressTrack,
                    radius = arcRadius,
                    center = center,
                    style = Stroke(stroke, cap = StrokeCap.Round),
                )
                drawArc(
                    color = LinkBlue,
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(stroke, cap = StrokeCap.Round),
                )
            }

            is DailyStatusState.Complete -> {
                drawCircle(color = LinkBlue, radius = radius, center = center)
                val stroke = 1.8.dp.toPx()
                drawLine(
                    color = Color.White,
                    start = Offset(5.2.dp.toPx(), 10.3.dp.toPx()),
                    end = Offset(8.3.dp.toPx(), 13.1.dp.toPx()),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round,
                )
                drawLine(
                    color = Color.White,
                    start = Offset(8.3.dp.toPx(), 13.1.dp.toPx()),
                    end = Offset(14.8.dp.toPx(), 6.9.dp.toPx()),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round,
                )
            }
        }
    }
}

private sealed interface DailyStatusState {
    data object Empty : DailyStatusState
    data object Complete : DailyStatusState
    data class Progress(val progress: Float) : DailyStatusState

    companion object {
        fun from(value: Int, goal: Int): DailyStatusState {
            val safeGoal = goal.coerceAtLeast(1)
            return when {
                value <= 0 -> Empty
                value >= safeGoal -> Complete
                else -> Progress((value.toFloat() / safeGoal).coerceIn(0f, 1f))
            }
        }
    }
}

@Composable
internal fun weekdayLabel(dayOfWeek: DayOfWeek): String {
    return when (dayOfWeek) {
        DayOfWeek.SUNDAY -> stringResource(R.string.weekday_sun)
        DayOfWeek.MONDAY -> stringResource(R.string.weekday_mon)
        DayOfWeek.TUESDAY -> stringResource(R.string.weekday_tue)
        DayOfWeek.WEDNESDAY -> stringResource(R.string.weekday_wed)
        DayOfWeek.THURSDAY -> stringResource(R.string.weekday_thu)
        DayOfWeek.FRIDAY -> stringResource(R.string.weekday_fri)
        DayOfWeek.SATURDAY -> stringResource(R.string.weekday_sat)
    }
}
