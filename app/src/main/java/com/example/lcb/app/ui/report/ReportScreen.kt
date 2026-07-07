package com.example.lcb.app.ui.report

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lcb.app.R
import com.example.lcb.app.data.ReportData
import com.example.lcb.app.data.StepDailyRecord
import com.example.lcb.app.data.StepMetrics
import com.example.lcb.app.data.TrendBucket
import com.example.lcb.app.ui.components.AnimatedValueText
import com.example.lcb.app.ui.components.AppBottomBar
import com.example.lcb.app.ui.components.NativeAdSlot
import com.example.lcb.app.ui.components.ScreenFrame
import com.example.lcb.app.ui.components.TabDestination
import com.example.lcb.app.ui.currentAppLocale
import com.example.lcb.app.ui.theme.LcbDarkPage
import com.example.lcb.app.ui.theme.LcbDarkTextPrimary
import com.example.lcb.app.ui.theme.LcbDarkTextSecondary
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun ReportScreen(
    data: ReportData,
    active: Boolean = true,
    showBottomBar: Boolean = true,
    onHome: () -> Unit,
    onData: () -> Unit,
    onMetricClick: (ReportMetricType) -> Unit = {},
) {
    var selectedTab by remember { mutableStateOf(ReportTab.Day) }
    val today = LocalDate.parse(data.today)
    var selectedDate by remember(data.today) { mutableStateOf(today) }
    val locale = currentAppLocale()
    val history = remember(data.stepHistory, data.todaySteps, data.stepGoal, data.today) {
        mergeToday(data)
    }
    val selectedData = remember(data, selectedDate) { data.forReportDate(selectedDate) }
    val period = remember(selectedTab, selectedDate, today, data, selectedData, history) {
        selectedTab.toPeriodTrend(selectedDate, today, data, selectedData, history)
    }

    ScreenFrame(
        background = LcbDarkPage,
        bottomBar = if (showBottomBar) {
            {
                AppBottomBar(
                    selected = TabDestination.Data,
                    onHome = onHome,
                    onData = onData,
                    dark = true,
                    dataLabel = stringResource(R.string.nav_trends),
                )
            }
        } else {
            null
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 96.dp)
                .statusBarsPadding(),
        ) {
            Text(
                text = stringResource(R.string.nav_trends),
                modifier = Modifier.padding(start = 16.dp, top = 24.dp),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = LcbDarkTextPrimary,
            )
            ReportTabRow(
                selected = selectedTab,
                onSelected = { selectedTab = it },
                modifier = Modifier.padding(start = 16.dp, top = 19.dp, end = 16.dp),
            )
            Spacer(Modifier.height(24.dp))
            PeriodSwitcher(
                tab = selectedTab,
                selectedDate = selectedDate,
                maxDate = today,
                locale = locale,
                onDateChange = { selectedDate = it },
            )
            NativeAdSlot(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                active = active,
            )
            Spacer(Modifier.height(20.dp))
            PeriodMetricCards(
                period = period,
                locale = locale,
                onMetricClick = onMetricClick,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
    }
}

@Composable
private fun ReportTabRow(
    selected: ReportTab,
    onSelected: (ReportTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ReportTab.values().forEach { tab ->
            val active = selected == tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(32.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(if (active) Color.White else Color(0xFF232227))
                    .clickable { onSelected(tab) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(tab.labelRes),
                    color = if (active) Color(0xFF222222) else Color(0xFF999999),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun PeriodSwitcher(
    tab: ReportTab,
    selectedDate: LocalDate,
    maxDate: LocalDate,
    locale: Locale,
    onDateChange: (LocalDate) -> Unit,
) {
    val nextDate = tab.moveDate(selectedDate, 1)
    val canGoNext = !nextDate.isAfter(maxDate)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PeriodArrow(
            direction = ArrowDirection.Left,
            enabled = true,
            onClick = { onDateChange(tab.moveDate(selectedDate, -1)) },
        )
        Text(
            text = tab.formatPeriod(selectedDate, locale),
            modifier = Modifier.padding(horizontal = 16.dp),
            color = LcbDarkTextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
        )
        PeriodArrow(
            direction = ArrowDirection.Right,
            enabled = canGoNext,
            onClick = { onDateChange(nextDate) },
        )
    }
}

@Composable
private fun PeriodArrow(
    direction: ArrowDirection,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val color = Color.White.copy(alpha = if (enabled) 0.72f else 0.25f)
    Canvas(
        modifier = Modifier
            .size(12.dp)
            .then(if (enabled) Modifier.clickable { onClick() } else Modifier),
    ) {
        val left = size.width * 0.35f
        val right = size.width * 0.65f
        val top = size.height * 0.2f
        val middle = size.height * 0.5f
        val bottom = size.height * 0.8f
        if (direction == ArrowDirection.Left) {
            drawLine(color, Offset(right, top), Offset(left, middle), strokeWidth = 1.8.dp.toPx(), cap = StrokeCap.Round)
            drawLine(color, Offset(left, middle), Offset(right, bottom), strokeWidth = 1.8.dp.toPx(), cap = StrokeCap.Round)
        } else {
            drawLine(color, Offset(left, top), Offset(right, middle), strokeWidth = 1.8.dp.toPx(), cap = StrokeCap.Round)
            drawLine(color, Offset(right, middle), Offset(left, bottom), strokeWidth = 1.8.dp.toPx(), cap = StrokeCap.Round)
        }
    }
}

private enum class ArrowDirection {
    Left,
    Right,
}

@Composable
private fun PeriodMetricCards(
    period: LocalPeriodTrend,
    locale: Locale,
    onMetricClick: (ReportMetricType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        TrendMetricCard(
            title = stringResource(R.string.report_metric_step),
            value = period.steps.toString(),
            unit = stringResource(R.string.home_steps_unit),
            color = Color(0xFF10CEAC),
            icon = TrendIcon.Step,
            values = period.buckets.map { it.steps.toDouble() },
            chartStartLabel = period.chartStartLabel,
            chartEndLabel = period.chartEndLabel,
            onClick = { onMetricClick(ReportMetricType.Step) },
        )
        TrendMetricCard(
            title = stringResource(R.string.report_metric_calorie),
            value = period.caloriesKcal.roundToInt().toString(),
            unit = stringResource(R.string.unit_kcal),
            color = Color(0xFFDA9A43),
            icon = TrendIcon.Calorie,
            values = period.buckets.map { it.caloriesKcal },
            chartStartLabel = period.chartStartLabel,
            chartEndLabel = period.chartEndLabel,
            onClick = { onMetricClick(ReportMetricType.Calorie) },
        )
        TrendMetricCard(
            title = stringResource(R.string.report_metric_distance),
            value = formatDistance(period.distanceKm, locale),
            unit = stringResource(R.string.unit_km_title),
            color = Color(0xFF4B7EF2),
            icon = TrendIcon.Distance,
            values = period.buckets.map { it.distanceKm },
            chartStartLabel = period.chartStartLabel,
            chartEndLabel = period.chartEndLabel,
            onClick = { onMetricClick(ReportMetricType.Distance) },
        )
        TrendMetricCard(
            title = stringResource(R.string.report_metric_time),
            value = formatExerciseDuration(period.exerciseMinutes),
            unit = stringResource(R.string.report_metric_exercise_time),
            color = Color(0xFF43DA61),
            icon = TrendIcon.Time,
            values = period.buckets.map { it.exerciseMinutes },
            chartStartLabel = period.chartStartLabel,
            chartEndLabel = period.chartEndLabel,
            onClick = { onMetricClick(ReportMetricType.Time) },
        )
    }
}

@Composable
private fun TrendMetricCard(
    title: String,
    value: String,
    unit: String,
    color: Color,
    icon: TrendIcon,
    values: List<Double>,
    chartStartLabel: String,
    chartEndLabel: String,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1C1C22))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TrendIconView(icon = icon, color = color)
                Text(
                    text = title,
                    color = LcbDarkTextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
            Canvas(modifier = Modifier.size(18.dp)) {
                drawLine(
                    color = LcbDarkTextSecondary,
                    start = Offset(size.width * 0.38f, size.height * 0.22f),
                    end = Offset(size.width * 0.64f, size.height * 0.5f),
                    strokeWidth = 2.2f,
                    cap = StrokeCap.Round,
                )
                drawLine(
                    color = LcbDarkTextSecondary,
                    start = Offset(size.width * 0.64f, size.height * 0.5f),
                    end = Offset(size.width * 0.38f, size.height * 0.78f),
                    strokeWidth = 2.2f,
                    cap = StrokeCap.Round,
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Column(
                modifier = Modifier.width(94.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                value.AnimatedValueText(
                    color = LcbDarkTextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    label = "TrendMetricValue:$title",
                )
                Text(
                    text = unit,
                    color = LcbDarkTextSecondary,
                    fontSize = 12.sp,
                )
            }
            MiniTrendChart(
                values = values,
                color = color,
                startLabel = chartStartLabel,
                endLabel = chartEndLabel,
                modifier = Modifier
                    .width(132.dp)
                    .height(58.dp),
            )
        }
    }
}

@Composable
private fun TrendIconView(icon: TrendIcon, color: Color) {
    Canvas(modifier = Modifier.size(18.dp)) {
        when (icon) {
            TrendIcon.Step -> {
                drawCircle(color, radius = size.minDimension * 0.12f, center = Offset(size.width * 0.58f, size.height * 0.18f))
                drawLine(color, Offset(size.width * 0.48f, size.height * 0.34f), Offset(size.width * 0.36f, size.height * 0.60f), strokeWidth = 2.2f, cap = StrokeCap.Round)
                drawLine(color, Offset(size.width * 0.48f, size.height * 0.34f), Offset(size.width * 0.70f, size.height * 0.45f), strokeWidth = 2.2f, cap = StrokeCap.Round)
                drawLine(color, Offset(size.width * 0.38f, size.height * 0.60f), Offset(size.width * 0.26f, size.height * 0.86f), strokeWidth = 2.2f, cap = StrokeCap.Round)
                drawLine(color, Offset(size.width * 0.38f, size.height * 0.60f), Offset(size.width * 0.58f, size.height * 0.82f), strokeWidth = 2.2f, cap = StrokeCap.Round)
            }
            TrendIcon.Calorie -> {
                val path = Path().apply {
                    moveTo(size.width * 0.5f, size.height * 0.08f)
                    cubicTo(size.width * 0.82f, size.height * 0.38f, size.width * 0.78f, size.height * 0.88f, size.width * 0.5f, size.height * 0.92f)
                    cubicTo(size.width * 0.18f, size.height * 0.88f, size.width * 0.16f, size.height * 0.43f, size.width * 0.5f, size.height * 0.08f)
                    close()
                }
                drawPath(path, color, style = Fill)
                drawCircle(Color(0xFF1C1C22), radius = size.minDimension * 0.15f, center = Offset(size.width * 0.5f, size.height * 0.61f))
            }
            TrendIcon.Distance -> {
                drawCircle(color, radius = size.minDimension * 0.34f, center = Offset(size.width * 0.42f, size.height * 0.44f), style = Stroke(width = 2.6f, cap = StrokeCap.Round))
                drawCircle(color, radius = size.minDimension * 0.10f, center = Offset(size.width * 0.42f, size.height * 0.44f))
                drawLine(color, Offset(size.width * 0.62f, size.height * 0.66f), Offset(size.width * 0.84f, size.height * 0.82f), strokeWidth = 2.6f, cap = StrokeCap.Round)
            }
            TrendIcon.Time -> {
                drawCircle(color, radius = size.minDimension * 0.42f, center = center)
                drawLine(Color(0xFF1C1C22), center, Offset(size.width * 0.5f, size.height * 0.28f), strokeWidth = 2f, cap = StrokeCap.Round)
                drawLine(Color(0xFF1C1C22), center, Offset(size.width * 0.68f, size.height * 0.5f), strokeWidth = 2f, cap = StrokeCap.Round)
            }
        }
    }
}

@Composable
private fun MiniTrendChart(
    values: List<Double>,
    color: Color,
    startLabel: String,
    endLabel: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            val safeValues = values.ifEmpty { listOf(0.0) }.map { it.coerceAtLeast(0.0) }
            val bucketCount = safeValues.size.coerceAtLeast(1)
            val maxValue = max(safeValues.maxOrNull() ?: 0.0, 1.0)
            val preferredBarWidth = when {
                bucketCount <= 7 -> 8.dp.toPx()
                bucketCount <= 14 -> 6.dp.toPx()
                else -> 4.dp.toPx()
            }
            val barWidth = preferredBarWidth.coerceAtMost(size.width / (bucketCount * 1.15f))
            val gap = if (bucketCount > 1) {
                ((size.width - barWidth * bucketCount) / (bucketCount - 1)).coerceAtLeast(0f)
            } else {
                0f
            }
            val totalWidth = barWidth * bucketCount + gap * (bucketCount - 1)
            val startX = ((size.width - totalWidth) / 2f).coerceAtLeast(0f)
            val minHeight = 2.dp.toPx()
            safeValues.forEachIndexed { index, value ->
                val left = startX + index * (barWidth + gap)
                val height = if (value <= 0.0) {
                    minHeight
                } else {
                    (size.height * (value / maxValue).toFloat()).coerceAtLeast(8.dp.toPx())
                }
                drawRoundRect(
                    color = if (value <= 0.0) Color(0xFF333333) else color,
                    topLeft = Offset(left, size.height - height),
                    size = Size(barWidth, height),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(startLabel, color = Color(0xFF666666), fontSize = 11.sp)
            Text(endLabel, color = Color(0xFF666666), fontSize = 11.sp)
        }
    }
}

private fun ReportTab.moveDate(date: LocalDate, amount: Int): LocalDate {
    return when (this) {
        ReportTab.Day -> date.plusDays(amount.toLong())
        ReportTab.Week -> date.plusWeeks(amount.toLong())
        ReportTab.Month -> date.plusMonths(amount.toLong())
    }
}

private fun ReportTab.formatPeriod(date: LocalDate, locale: Locale): String {
    return when (this) {
        ReportTab.Day -> date.format(DateTimeFormatter.ofPattern("MMM d", locale))
        ReportTab.Week -> {
            val days = weekDays(date)
            "${days.first().format(DateTimeFormatter.ofPattern("MMM d", locale))}-${days.last().format(DateTimeFormatter.ofPattern("MMM d", locale))}"
        }
        ReportTab.Month -> date.format(DateTimeFormatter.ofPattern("MMM yyyy", locale))
    }
}

private fun ReportTab.toPeriodTrend(
    selectedDate: LocalDate,
    today: LocalDate,
    currentData: ReportData,
    selectedData: ReportData,
    history: List<StepDailyRecord>,
): LocalPeriodTrend {
    return when (this) {
        ReportTab.Day -> localDayTrendData(selectedData)
        ReportTab.Week -> {
            val days = weekDays(selectedDate)
            localRangeTrendData(days, history)
        }
        ReportTab.Month -> {
            val days = monthDays(selectedDate, today)
            localRangeTrendData(days, history.ifEmpty { mergeToday(currentData) })
        }
    }
}

private fun ReportData.forReportDate(date: LocalDate): ReportData {
    val dateText = date.toString()
    if (dateText == today) return this

    val record = stepHistory.firstOrNull { it.date == dateText }
    return copy(
        today = dateText,
        todaySteps = record?.steps ?: 0,
        stepGoal = record?.goal ?: stepGoal,
        trendBuckets = emptyList(),
    )
}

private fun localDayTrendData(data: ReportData): LocalPeriodTrend {
    val metrics = StepMetrics(data.todaySteps, data.stepGoal)
    val buckets = localTrendBuckets(data, metrics)
    return LocalPeriodTrend(
        steps = metrics.steps.toLong(),
        caloriesKcal = metrics.calories.toDouble(),
        distanceKm = metrics.distanceKm,
        exerciseMinutes = metrics.steps / 70.0,
        buckets = buckets,
        chartStartLabel = "00:00",
        chartEndLabel = "23:59",
    )
}

private fun localRangeTrendData(
    days: List<LocalDate>,
    history: List<StepDailyRecord>,
): LocalPeriodTrend {
    val buckets = days.mapIndexed { index, day ->
        val steps = history.firstOrNull { it.date == day.toString() }?.steps ?: 0
        trendBucketFromSteps(index, steps)
    }
    val first = days.firstOrNull()
    val last = days.lastOrNull()
    return LocalPeriodTrend(
        steps = buckets.sumOf { it.steps },
        caloriesKcal = buckets.sumOf { it.caloriesKcal },
        distanceKm = buckets.sumOf { it.distanceKm },
        exerciseMinutes = buckets.sumOf { it.exerciseMinutes },
        buckets = buckets,
        chartStartLabel = first?.format(axisDateFormatter) ?: "",
        chartEndLabel = last?.format(axisDateFormatter) ?: "",
    )
}

private fun localTrendBuckets(data: ReportData, metrics: StepMetrics): List<TrendBucket> {
    val bucketsByHour = data.trendBuckets
        .filter { bucket -> bucket.steps > 0L }
        .associateBy { bucket -> bucket.hour.coerceIn(0, 23) }

    if (bucketsByHour.isNotEmpty()) {
        return (0..23).map { hour ->
            val steps = bucketsByHour[hour]?.steps?.toInt()?.coerceAtLeast(0) ?: 0
            trendBucketFromSteps(hour, steps)
        }
    }

    // Upgraded installs may have a daily total before hourly tracking exists.
    // Keep the chart visible without inventing a fake distribution.
    val currentHour = if (data.today == LocalDate.now().toString()) {
        LocalTime.now().hour.coerceIn(0, 23)
    } else {
        12
    }
    return (0..23).map { hour ->
        val steps = if (hour == currentHour) metrics.steps else 0
        trendBucketFromSteps(hour, steps)
    }
}

private fun trendBucketFromSteps(hour: Int, steps: Int): TrendBucket {
    val safeSteps = steps.coerceAtLeast(0)
    return TrendBucket(
        hour = hour,
        steps = safeSteps.toLong(),
        caloriesKcal = safeSteps * 0.071,
        distanceKm = safeSteps * 0.00057,
        exerciseMinutes = safeSteps / 70.0,
    )
}

private fun weekDays(date: LocalDate): List<LocalDate> {
    val start = date.minusDays((date.dayOfWeek.value - 1).toLong())
    return (0..6).map { start.plusDays(it.toLong()) }
}

private fun monthDays(date: LocalDate, today: LocalDate): List<LocalDate> {
    val lastDay = if (date.year == today.year && date.month == today.month) {
        today.dayOfMonth
    } else {
        date.lengthOfMonth()
    }
    return (1..lastDay).map { date.withDayOfMonth(it) }
}

private val axisDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d")

private fun mergeToday(data: ReportData): List<StepDailyRecord> {
    val todayRecord = StepDailyRecord(data.today, data.todaySteps, data.stepGoal)
    return (data.stepHistory.filterNot { it.date == data.today } + todayRecord).sortedBy { it.date }
}

private fun formatDistance(distanceKm: Double, locale: Locale): String {
    val pattern = if (distanceKm > 0.0 && distanceKm < 1.0) "%.2f" else "%.1f"
    return String.format(locale, pattern, distanceKm)
}

private fun formatExerciseDuration(minutes: Double): String {
    val roundedMinutes = minutes.roundToInt().coerceAtLeast(0)
    val hours = roundedMinutes / 60
    val remainingMinutes = roundedMinutes % 60
    return if (hours > 0) "${hours}h${remainingMinutes}m" else "${remainingMinutes}m"
}
