package com.example.lcb.app.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lcb.app.R
import com.example.lcb.app.data.HomeData
import com.example.lcb.app.data.StepDailyRecord
import com.example.lcb.app.data.StepMetrics
import com.example.lcb.app.ui.components.AnimatedValueText
import java.text.NumberFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import kotlin.math.roundToInt

@Composable
internal fun StepCard(
    data: HomeData,
    metrics: StepMetrics,
    locale: Locale,
    onEditGoal: () -> Unit,
    onTogglePaused: () -> Unit,
    onMore: () -> Unit,
    onDetail: () -> Unit,
) {
    val formatter = remember(locale) { NumberFormat.getIntegerInstance(locale) }
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .height(240.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(CardBg)
            .padding(horizontal = 16.dp, vertical = 20.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(49.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            formatter.format(metrics.steps).AnimatedValueText(
                color = Color.White,
                fontSize = stepFontSize(metrics.steps),
                lineHeight = 49.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                maxLines = 1,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StepActionIcon(
                    iconRes = if (data.isStepCountingPaused) R.drawable.ic_home_step_resume else R.drawable.ic_home_step_pause,
                    onClick = onTogglePaused,
                )
                StepActionIcon(
                    iconRes = R.drawable.ic_home_step_more,
                    onClick = onMore,
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                stringResource(R.string.home_goal_value, formatter.format(metrics.goal)).AnimatedValueText(
                    color = TextDim,
                    fontSize = 12.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.Medium,
                    label = "HomeGoalValue",
                )
                Spacer(Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clickable(onClick = onEditGoal),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        painter = painterResource(R.drawable.home_dark_edit),
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
            val weeklyAverageText = formatter.format(weeklyAverage(data))
            val dailyAverageTemplate = stringResource(R.string.home_daily_avg_value, weeklyAverageText)
            buildAnnotatedString {
                val valueStart = dailyAverageTemplate.indexOf(weeklyAverageText)
                if (valueStart < 0) {
                    append(dailyAverageTemplate)
                } else {
                    append(dailyAverageTemplate.substring(0, valueStart))
                    withStyle(SpanStyle(color = Color.White.copy(alpha = 0.89f))) {
                        append(weeklyAverageText)
                    }
                    append(dailyAverageTemplate.substring(valueStart + weeklyAverageText.length))
                }
            }.AnimatedValueText(
                color = TextDim,
                fontSize = 12.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.End,
                label = "HomeDailyAverage",
            )
        }
        Spacer(Modifier.height(13.dp))
        StepProgressBar(progress = metrics.progress)
        Spacer(Modifier.height(17.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(19.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.home_report),
                color = Color.White,
                fontSize = 16.sp,
                lineHeight = 19.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )
            Row(
                modifier = Modifier.clickable(onClick = onDetail),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.home_detail),
                    color = LinkBlue,
                    fontSize = 14.sp,
                    lineHeight = 16.sp,
                )
                Spacer(Modifier.width(2.dp))
                Image(painter = painterResource(R.drawable.home_dark_more_blue), contentDescription = null, modifier = Modifier.size(14.dp))
            }
        }
        Spacer(Modifier.height(16.dp))
        WeeklyReport(data = data)
    }
}

@Composable
private fun StepActionIcon(iconRes: Int, onClick: () -> Unit) {
    Image(
        painter = painterResource(iconRes),
        contentDescription = null,
        modifier = Modifier
            .size(18.dp)
            .clickable(onClick = onClick),
    )
}

@Composable
private fun StepProgressBar(progress: Float) {
    val progressValue = animatedProgress(progress, label = "HomeStepProgress")
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(16.dp),
    ) {
        val barHeight = size.height
        val corner = CornerRadius(barHeight / 2f, barHeight / 2f)
        drawRoundRect(
            color = TrackBg,
            size = size,
            cornerRadius = corner,
        )
        if (progressValue > 0f) {
            val progressWidth = (size.width * progressValue).coerceAtLeast(barHeight)
            drawRoundRect(
                brush = Brush.horizontalGradient(listOf(Color(0xFF5989ED), Color(0xFF10CEAC))),
                size = Size(progressWidth, barHeight),
                cornerRadius = corner,
            )
        }
    }
}

@Composable
private fun WeeklyReport(data: HomeData) {
    val today = remember(data.today) { runCatching { LocalDate.parse(data.today) }.getOrDefault(LocalDate.now()) }
    val days = remember(today) {
        val start = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
        (0..6).map { start.plusDays(it.toLong()) }
    }
    val records = remember(data.stepHistory, data.todaySteps, data.stepGoal, data.today) {
        (data.stepHistory + StepDailyRecord(data.today, data.todaySteps, data.stepGoal)).associateBy { it.date }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        days.forEach { day ->
            val record = records[day.toString()]
            val steps = record?.steps ?: 0
            val goal = (record?.goal ?: data.stepGoal).coerceAtLeast(1)
            val isToday = day == today
            Column(
                modifier = Modifier.width(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                DailyStatusIcon(value = steps, goal = goal)
                Spacer(Modifier.height(6.dp))
                Text(
                    text = weekdayLabel(day.dayOfWeek),
                    color = if (isToday) LinkBlue else TextMuted,
                    fontSize = 12.sp,
                    lineHeight = 14.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
            }
        }
    }
}

private fun weeklyAverage(data: HomeData): Int {
    val today = runCatching { LocalDate.parse(data.today) }.getOrDefault(LocalDate.now())
    val start = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
    val records = (data.stepHistory + StepDailyRecord(data.today, data.todaySteps, data.stepGoal)).associateBy { it.date }
    val values = (0..6).map { offset -> records[start.plusDays(offset.toLong()).toString()]?.steps ?: 0 }
    return values.average().roundToInt()
}

private fun stepFontSize(steps: Int) = when {
    steps >= 1_000_000 -> 30.sp
    steps >= 100_000 -> 34.sp
    steps >= 10_000 -> 38.sp
    else -> 42.sp
}
