package com.example.lcb.app.ui.report

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
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
import com.example.lcb.app.data.ReportData
import com.example.lcb.app.data.StepDailyRecord
import com.example.lcb.app.data.StepMetrics
import com.example.lcb.app.data.TrendBucket
import com.example.lcb.app.ui.components.AnimatedValueText
import com.example.lcb.app.ui.components.NativeAdSlot
import com.example.lcb.app.ui.currentAppLocale
import com.example.lcb.app.ui.theme.LcbDarkPage
import java.text.NumberFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun ReportDetailScreen(
    data: ReportData,
    initialMetric: ReportMetricType,
    onBack: () -> Unit,
) {
    var selectedRange by remember { mutableStateOf(ReportDetailRange.Day) }
    var selectedMetric by remember(initialMetric) { mutableStateOf(initialMetric) }
    val today = remember(data.today) { runCatching { LocalDate.parse(data.today) }.getOrDefault(LocalDate.now()) }
    var selectedDate by remember(data.today) { mutableStateOf(today) }
    val locale = currentAppLocale()
    val labels = reportDetailLabels()
    val detail = remember(data, selectedDate, selectedRange, selectedMetric, locale, labels) {
        buildReportDetail(data, selectedRange, selectedMetric, selectedDate, today, locale, labels)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LcbDarkPage)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 28.dp),
    ) {
        ReportDetailTopBar(onBack = onBack)
        Spacer(Modifier.height(15.dp))
        RangeTabs(
            selected = selectedRange,
            onSelected = { selectedRange = it },
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(18.dp))
        MetricTypeTabs(
            selected = selectedMetric,
            onSelected = { selectedMetric = it },
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(16.dp))
        NativeAdSlot(modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(Modifier.height(20.dp))
        SummaryCard(
            detail = detail,
            range = selectedRange,
            selectedDate = selectedDate,
            today = today,
            onPrevious = { selectedDate = selectedRange.moveDate(selectedDate, -1) },
            onNext = { selectedDate = selectedRange.moveDate(selectedDate, 1) },
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(22.dp))
        ReportDetailChart(
            detail = detail,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .height(245.dp),
        )
        Spacer(Modifier.height(22.dp))
        StatisticsHeader(
            title = detail.statisticsTitle,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            StatisticCard(
                stat = detail.activeStat,
                iconRes = R.drawable.home_dark_medal,
                modifier = Modifier.weight(1f),
            )
            StatisticCard(
                stat = detail.relaxingStat,
                iconRes = R.drawable.home_dark_exercise,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ReportDetailTopBar(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.report_detail_title),
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
        )
        Canvas(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 16.dp)
                .size(24.dp)
                .clickable(onClick = onBack),
        ) {
            drawLine(Color.White, Offset(size.width * 0.62f, size.height * 0.24f), Offset(size.width * 0.36f, size.height * 0.5f), 2.1.dp.toPx(), StrokeCap.Round)
            drawLine(Color.White, Offset(size.width * 0.36f, size.height * 0.5f), Offset(size.width * 0.62f, size.height * 0.76f), 2.1.dp.toPx(), StrokeCap.Round)
        }
    }
}

@Composable
private fun RangeTabs(
    selected: ReportDetailRange,
    onSelected: (ReportDetailRange) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        ReportDetailRange.values().forEach { range ->
            Column(
                modifier = Modifier
                    .width(84.dp)
                    .clickable { onSelected(range) },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                val active = range == selected
                Text(
                    text = stringResource(range.labelRes),
                    color = if (active) AccentGreen else MutedText,
                    fontSize = if (active) 16.sp else 14.sp,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(Modifier.height(5.dp))
                Box(
                    modifier = Modifier
                        .width(20.dp)
                        .height(2.dp)
                        .clip(RoundedCornerShape(100.dp))
                        .background(if (active) AccentGreen else Color.Transparent),
                )
            }
        }
    }
}

@Composable
private fun MetricTypeTabs(
    selected: ReportMetricType,
    onSelected: (ReportMetricType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ReportMetricType.values().forEach { metric ->
            val active = metric == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(32.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(if (active) Color.White else ChipBg)
                    .clickable { onSelected(metric) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(metric.labelRes),
                    color = if (active) Color(0xFF222222) else Color(0xFF999999),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun SummaryCard(
    detail: ReportDetailData,
    range: ReportDetailRange,
    selectedDate: LocalDate,
    today: LocalDate,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val canGoNext = !range.moveDate(selectedDate, 1).isAfter(today)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(128.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(CardBg)
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PeriodSwitcherRow(
            text = detail.periodTitle,
            canGoNext = canGoNext,
            onPrevious = onPrevious,
            onNext = onNext,
        )
        Spacer(Modifier.height(32.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            SummaryValue(value = detail.averageText, label = stringResource(R.string.report_detail_avg))
            SummaryValue(value = detail.totalText, label = stringResource(R.string.report_detail_total))
        }
    }
}

@Composable
private fun PeriodSwitcherRow(
    text: String,
    canGoNext: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SmallArrow(enabled = true, reverse = false, onClick = onPrevious)
        Text(text, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        SmallArrow(enabled = canGoNext, reverse = true, onClick = onNext)
    }
}

@Composable
private fun SmallArrow(enabled: Boolean, reverse: Boolean, onClick: () -> Unit) {
    val color = Color.White.copy(alpha = if (enabled) 0.58f else 0.18f)
    Canvas(
        modifier = Modifier
            .size(12.dp)
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier),
    ) {
        val left = size.width * 0.35f
        val right = size.width * 0.65f
        val top = size.height * 0.2f
        val middle = size.height * 0.5f
        val bottom = size.height * 0.8f
        if (!reverse) {
            drawLine(color, Offset(right, top), Offset(left, middle), 1.7.dp.toPx(), StrokeCap.Round)
            drawLine(color, Offset(left, middle), Offset(right, bottom), 1.7.dp.toPx(), StrokeCap.Round)
        } else {
            drawLine(color, Offset(left, top), Offset(right, middle), 1.7.dp.toPx(), StrokeCap.Round)
            drawLine(color, Offset(right, middle), Offset(left, bottom), 1.7.dp.toPx(), StrokeCap.Round)
        }
    }
}

@Composable
private fun SummaryValue(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        value.AnimatedValueText(
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            label = "ReportDetailSummary:$label",
        )
        Spacer(Modifier.height(4.dp))
        Text(label, color = MutedText, fontSize = 12.sp)
    }
}

@Composable
private fun ReportDetailChart(
    detail: ReportDetailData,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .width(32.dp)
                .height(190.dp)
                .padding(top = 16.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.End,
        ) {
            detail.yAxisLabels.forEach { label ->
                Text(label, color = Color(0xFFD9D9D9), fontSize = 14.sp, textAlign = TextAlign.End)
            }
        }
        Spacer(Modifier.width(20.dp))
        Column(modifier = Modifier.weight(1f)) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp),
            ) {
                val values = detail.values
                val count = values.size.coerceAtLeast(1)
                val chartHeight = size.height - 24.dp.toPx()
                val baseline = chartHeight
                val maxValue = detail.axisMax.coerceAtLeast(1.0)
                val gap = if (count > 1) size.width / (count - 1) else size.width
                val barWidth = when {
                    count <= 7 -> 8.dp.toPx()
                    count <= 24 -> 6.dp.toPx()
                    else -> 4.dp.toPx()
                }.coerceAtMost(size.width / (count * 1.2f))
                values.forEachIndexed { index, rawValue ->
                    val x = if (count == 1) size.width / 2f else index * gap
                    drawCircle(
                        color = Color(0xFF666666),
                        radius = 3.dp.toPx(),
                        center = Offset(x, baseline),
                    )
                    if (rawValue > 0.0) {
                        val barHeight = (chartHeight * (rawValue / maxValue).toFloat()).coerceAtLeast(8.dp.toPx())
                        drawRoundRect(
                            color = AccentGreen,
                            topLeft = Offset(x - barWidth / 2f, baseline - barHeight),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
                        )
                    }
                }

                val selectedIndex = detail.selectedIndex.coerceIn(0, values.lastIndex.coerceAtLeast(0))
                val selectedX = if (count == 1) size.width / 2f else selectedIndex * gap
                drawLine(
                    color = AccentGreen,
                    start = Offset(selectedX, 36.dp.toPx()),
                    end = Offset(selectedX, baseline - 12.dp.toPx()),
                    strokeWidth = 1.dp.toPx(),
                )
                drawTooltip(
                    centerX = selectedX,
                    value = detail.tooltipValue,
                    unit = detail.tooltipUnit,
                    label = detail.tooltipLabel,
                )
            }
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                detail.xAxisLabels.forEach { label ->
                    Text(label, color = Color(0xFF666666), fontSize = 14.sp)
                }
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawTooltip(
    centerX: Float,
    value: String,
    unit: String,
    label: String,
) {
    val tooltipWidth = 82.dp.toPx()
    val tooltipHeight = 45.dp.toPx()
    val left = (centerX - tooltipWidth / 2f).coerceIn(0f, size.width - tooltipWidth)
    drawRoundRect(
        color = AccentGreen,
        topLeft = Offset(left, 0f),
        size = Size(tooltipWidth, tooltipHeight),
        cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx()),
    )
    val trianglePath = androidx.compose.ui.graphics.Path().apply {
        moveTo(centerX - 5.dp.toPx(), tooltipHeight)
        lineTo(centerX + 5.dp.toPx(), tooltipHeight)
        lineTo(centerX, tooltipHeight + 6.dp.toPx())
        close()
    }
    drawPath(trianglePath, AccentGreen)
    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        textSize = 14.sp.toPx()
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }
    val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(207, 245, 238)
        textSize = 11.sp.toPx()
        textAlign = Paint.Align.CENTER
    }
    drawContext.canvas.nativeCanvas.drawText("$value $unit", left + tooltipWidth / 2f, 17.dp.toPx(), titlePaint)
    drawContext.canvas.nativeCanvas.drawText(label, left + tooltipWidth / 2f, 33.dp.toPx(), subPaint)
}

@Composable
private fun StatisticsHeader(title: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Canvas(modifier = Modifier.size(18.dp)) {
            drawCircle(AccentGreen.copy(alpha = 0.16f), radius = size.minDimension / 2f)
            val barWidth = 2.dp.toPx()
            val bottom = size.height * 0.68f
            listOf(0.35f to 0.52f, 0.50f to 0.38f, 0.65f to 0.58f).forEach { (xFactor, topFactor) ->
                drawLine(
                    color = AccentGreen,
                    start = Offset(size.width * xFactor, size.height * topFactor),
                    end = Offset(size.width * xFactor, bottom),
                    strokeWidth = barWidth,
                    cap = StrokeCap.Round,
                )
            }
        }
        Spacer(Modifier.width(6.dp))
        Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun StatisticCard(
    stat: ReportDetailStat,
    iconRes: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .height(132.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(CardBg)
            .padding(start = 12.dp, top = 16.dp, end = 12.dp, bottom = 16.dp),
    ) {
        Image(painter = painterResource(iconRes), contentDescription = null, modifier = Modifier.size(24.dp))
        Spacer(Modifier.height(6.dp))
        Text(stat.title, color = Color(0xFFD9D9D9), fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 1)
        Spacer(Modifier.height(10.dp))
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)) {
                    append(stat.value)
                }
                withStyle(SpanStyle(color = MutedText, fontSize = 12.sp)) {
                    append(" ")
                    append(stat.unit)
                }
            },
            maxLines = 1,
        )
        Spacer(Modifier.height(8.dp))
        Text(stat.subtitle, color = MutedText, fontSize = 12.sp, maxLines = 1)
    }
}

private fun ReportDetailRange.moveDate(date: LocalDate, amount: Int): LocalDate {
    return when (this) {
        ReportDetailRange.Day -> date.plusDays(amount.toLong())
        ReportDetailRange.Week -> date.plusWeeks(amount.toLong())
        ReportDetailRange.Month -> date.plusMonths(amount.toLong())
    }
}

@Composable
private fun reportDetailLabels(): ReportDetailLabels {
    return ReportDetailLabels(
        statisticsFormat = stringResource(R.string.report_detail_statistics_title),
        mostActiveTime = stringResource(R.string.report_detail_most_active_time),
        mostActiveDay = stringResource(R.string.report_detail_most_active_day),
        mostRelaxingTime = stringResource(R.string.report_detail_most_relaxing_time),
        mostRelaxingDay = stringResource(R.string.report_detail_most_relaxing_day),
        stepsUnit = stringResource(R.string.report_detail_unit_steps),
    )
}

private fun buildReportDetail(
    data: ReportData,
    range: ReportDetailRange,
    metric: ReportMetricType,
    selectedDate: LocalDate,
    today: LocalDate,
    locale: Locale,
    labels: ReportDetailLabels,
): ReportDetailData {
    val buckets = when (range) {
        ReportDetailRange.Day -> dayBuckets(data.forReportDate(selectedDate))
        ReportDetailRange.Week -> rangeBuckets(weekDays(selectedDate), mergeToday(data))
        ReportDetailRange.Month -> rangeBuckets(monthDays(selectedDate, today), mergeToday(data))
    }
    val values = buckets.map { metric.valueOf(it.trend) }
    val count = values.size.coerceAtLeast(1)
    val total = values.sum()
    val average = total / count
    val selectedIndex = values.indices.maxByOrNull { values[it] } ?: 0
    val selectedBucket = buckets.getOrNull(selectedIndex)
    val periodTitle = range.periodTitle(selectedDate, today, locale)
    val statisticsRange = range.statisticsTitle(selectedDate, today, locale)
    val axisMax = niceAxisMax(values.maxOrNull() ?: 0.0, metric)
    val active = buckets.maxByOrNull { metric.valueOf(it.trend) } ?: buckets.firstOrNull()
    val relaxing = buckets.minByOrNull { metric.valueOf(it.trend) } ?: buckets.firstOrNull()
    val unit = metric.unitLabel(labels)
    val statisticsTitle = String.format(locale, labels.statisticsFormat, statisticsRange)
    val activeTitle = if (range == ReportDetailRange.Day) labels.mostActiveTime else labels.mostActiveDay
    val relaxingTitle = if (range == ReportDetailRange.Day) labels.mostRelaxingTime else labels.mostRelaxingDay

    return ReportDetailData(
        periodTitle = periodTitle,
        statisticsTitle = statisticsTitle,
        values = values,
        buckets = buckets,
        averageText = metric.formatValue(average, locale, compact = true),
        totalText = metric.formatValue(total, locale, compact = true),
        xAxisLabels = range.axisLabels(selectedDate, today, locale),
        yAxisLabels = listOf(
            metric.formatAxis(axisMax, locale),
            metric.formatAxis(axisMax / 2.0, locale),
            metric.formatAxis(0.0, locale),
        ),
        axisMax = axisMax,
        selectedIndex = selectedIndex,
        tooltipValue = metric.formatValue(values.getOrNull(selectedIndex) ?: 0.0, locale, compact = true),
        tooltipUnit = unit,
        tooltipLabel = selectedBucket?.tooltipLabel(range, locale).orEmpty(),
        activeStat = ReportDetailStat(
            title = activeTitle,
            value = metric.formatValue(active?.let { metric.valueOf(it.trend) } ?: 0.0, locale, compact = true),
            unit = unit,
            subtitle = active?.statSubtitle(range, locale).orEmpty(),
        ),
        relaxingStat = ReportDetailStat(
            title = relaxingTitle,
            value = metric.formatValue(relaxing?.let { metric.valueOf(it.trend) } ?: 0.0, locale, compact = true),
            unit = unit,
            subtitle = relaxing?.statSubtitle(range, locale).orEmpty(),
        ),
    )
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

private fun dayBuckets(data: ReportData): List<DetailBucket> {
    val metrics = StepMetrics(data.todaySteps, data.stepGoal)
    val bucketsByHour = data.trendBuckets
        .filter { it.steps > 0L }
        .associateBy { it.hour.coerceIn(0, 23) }
    val fallbackHour = if (data.today == LocalDate.now().toString()) LocalTime.now().hour.coerceIn(0, 23) else 12
    return (0..23).map { hour ->
        val steps = bucketsByHour[hour]?.steps?.toInt()
            ?: if (bucketsByHour.isEmpty() && hour == fallbackHour) metrics.steps else 0
        DetailBucket(hour, null, hour, trendBucketFromSteps(hour, steps))
    }
}

private fun rangeBuckets(days: List<LocalDate>, history: List<StepDailyRecord>): List<DetailBucket> {
    return days.mapIndexed { index, day ->
        val steps = history.firstOrNull { it.date == day.toString() }?.steps ?: 0
        DetailBucket(index, day, null, trendBucketFromSteps(index, steps))
    }
}

private fun trendBucketFromSteps(index: Int, steps: Int): TrendBucket {
    val safeSteps = steps.coerceAtLeast(0)
    return TrendBucket(
        hour = index,
        steps = safeSteps.toLong(),
        caloriesKcal = safeSteps * 0.071,
        distanceKm = safeSteps * 0.00057,
        exerciseMinutes = safeSteps / 70.0,
    )
}

private fun mergeToday(data: ReportData): List<StepDailyRecord> {
    val todayRecord = StepDailyRecord(data.today, data.todaySteps, data.stepGoal)
    return (data.stepHistory.filterNot { it.date == data.today } + todayRecord).sortedBy { it.date }
}

private fun weekDays(date: LocalDate): List<LocalDate> {
    val start = date.minusDays((date.dayOfWeek.value - 1).toLong())
    return (0..6).map { start.plusDays(it.toLong()) }
}

private fun monthDays(date: LocalDate, today: LocalDate): List<LocalDate> {
    val lastDay = if (date.year == today.year && date.month == today.month) today.dayOfMonth else date.lengthOfMonth()
    return (1..lastDay).map { date.withDayOfMonth(it) }
}

private fun ReportDetailRange.periodTitle(date: LocalDate, today: LocalDate, locale: Locale): String {
    return when (this) {
        ReportDetailRange.Day -> date.format(DateTimeFormatter.ofPattern("MMM d", locale))
        ReportDetailRange.Week -> statisticsTitle(date, today, locale)
        ReportDetailRange.Month -> date.format(DateTimeFormatter.ofPattern("MMM yyyy", locale))
    }
}

private fun ReportDetailRange.statisticsTitle(date: LocalDate, today: LocalDate, locale: Locale): String {
    return when (this) {
        ReportDetailRange.Day -> date.format(DateTimeFormatter.ofPattern("MMM d", locale))
        ReportDetailRange.Week -> {
            val days = weekDays(date)
            "${days.first().format(DateTimeFormatter.ofPattern("MMM d", locale))} - ${days.last().format(DateTimeFormatter.ofPattern("MMM d", locale))}"
        }
        ReportDetailRange.Month -> {
            val days = monthDays(date, today)
            "${days.first().format(DateTimeFormatter.ofPattern("MMM d", locale))} - ${days.last().format(DateTimeFormatter.ofPattern("MMM d", locale))}"
        }
    }
}

private fun ReportDetailRange.axisLabels(date: LocalDate, today: LocalDate, locale: Locale): List<String> {
    return when (this) {
        ReportDetailRange.Day -> listOf("06:00", "12:00", "16:00")
        ReportDetailRange.Week -> {
            val days = weekDays(date)
            listOf(days.first(), days[3], days.last()).map { it.format(DateTimeFormatter.ofPattern("MMM d", locale)) }
        }
        ReportDetailRange.Month -> {
            val days = monthDays(date, today)
            listOf(days.first(), days[days.size / 2], days.last()).map { it.dayOfMonth.toString() }
        }
    }
}

private fun DetailBucket.tooltipLabel(range: ReportDetailRange, locale: Locale): String {
    return when (range) {
        ReportDetailRange.Day -> {
            val h = hour?.coerceIn(0, 23) ?: 0
            String.format(Locale.US, "%02d:00-%02d:59", h, h)
        }
        ReportDetailRange.Week,
        ReportDetailRange.Month -> date?.format(DateTimeFormatter.ofPattern("MMM d", locale)).orEmpty()
    }
}

private fun DetailBucket.statSubtitle(range: ReportDetailRange, locale: Locale): String {
    return tooltipLabel(range, locale)
}

private fun ReportMetricType.valueOf(bucket: TrendBucket): Double {
    return when (this) {
        ReportMetricType.Step -> bucket.steps.toDouble()
        ReportMetricType.Calorie -> bucket.caloriesKcal
        ReportMetricType.Distance -> bucket.distanceKm
        ReportMetricType.Time -> bucket.exerciseMinutes
    }
}

private fun ReportMetricType.unitLabel(labels: ReportDetailLabels): String {
    return when (this) {
        ReportMetricType.Step -> labels.stepsUnit
        ReportMetricType.Calorie -> "Kcal"
        ReportMetricType.Distance -> "Km"
        ReportMetricType.Time -> "Min"
    }
}

private fun ReportMetricType.formatValue(value: Double, locale: Locale, compact: Boolean): String {
    return when (this) {
        ReportMetricType.Step -> NumberFormat.getIntegerInstance(locale).format(value.roundToInt())
        ReportMetricType.Calorie -> NumberFormat.getIntegerInstance(locale).format(value.roundToInt())
        ReportMetricType.Distance -> {
            val pattern = if (value > 0.0 && value < 1.0) "%.2f" else "%.1f"
            String.format(locale, pattern, value)
        }
        ReportMetricType.Time -> if (compact) value.roundToInt().toString() else formatDuration(value)
    }
}

private fun ReportMetricType.formatAxis(value: Double, locale: Locale): String {
    return when (this) {
        ReportMetricType.Step,
        ReportMetricType.Calorie -> formatCompactNumber(value, locale)
        ReportMetricType.Distance -> String.format(locale, "%.1f", value)
        ReportMetricType.Time -> value.roundToInt().toString()
    }
}

private fun formatCompactNumber(value: Double, locale: Locale): String {
    return if (value >= 1000) {
        val thousands = value / 1000.0
        if (thousands % 1.0 == 0.0) "${thousands.roundToInt()}k" else String.format(locale, "%.1fk", thousands)
    } else {
        NumberFormat.getIntegerInstance(locale).format(value.roundToInt())
    }
}

private fun formatDuration(minutes: Double): String {
    val rounded = minutes.roundToInt().coerceAtLeast(0)
    val hours = rounded / 60
    val mins = rounded % 60
    return if (hours > 0) "${hours}h${mins}m" else "${mins}m"
}

private fun niceAxisMax(maxValue: Double, metric: ReportMetricType): Double {
    if (maxValue <= 0.0) {
        return when (metric) {
            ReportMetricType.Distance -> 1.0
            else -> 1000.0
        }
    }
    return when (metric) {
        ReportMetricType.Distance -> (ceil(maxValue * 10.0) / 10.0).coerceAtLeast(0.1)
        ReportMetricType.Time -> ceil(maxValue / 10.0) * 10.0
        else -> {
            val step = when {
                maxValue <= 100 -> 50.0
                maxValue <= 1000 -> 500.0
                maxValue <= 10000 -> 1000.0
                else -> 5000.0
            }
            ceil(maxValue / step) * step
        }
    }.coerceAtLeast(1.0)
}

private val AccentGreen = Color(0xFF10CEAC)
private val CardBg = Color(0xFF1C1C22)
private val ChipBg = Color(0xFF232227)
private val MutedText = Color(0xFF999999)
