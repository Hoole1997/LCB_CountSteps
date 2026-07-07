package com.example.lcb.app.ui.hydrate

import android.graphics.Paint
import android.graphics.Typeface
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lcb.app.R
import com.example.lcb.app.data.HydrateData
import com.example.lcb.app.data.HydrationRecord
import com.example.lcb.app.data.localDate
import com.example.lcb.app.ui.components.AnimatedValueText
import com.example.lcb.app.ui.components.NativeAdSlot
import com.example.lcb.app.ui.currentAppLocale
import com.example.lcb.app.ui.theme.LcbDarkPage
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.roundToInt

@Composable
fun HydrateReportScreen(
    data: HydrateData,
    onBack: () -> Unit,
) {
    var selectedRange by remember { mutableStateOf(HydrateReportRange.Day) }
    val today = remember(data.today) { runCatching { LocalDate.parse(data.today) }.getOrDefault(LocalDate.now()) }
    var selectedDate by remember(data.today) { mutableStateOf(today) }
    val locale = currentAppLocale()
    val detail = remember(data, selectedRange, selectedDate, today, locale) {
        buildHydrateReportDetail(data, selectedRange, selectedDate, today, locale)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LcbDarkPage)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 28.dp),
    ) {
        HydrateReportTopBar(onBack = onBack)
        Spacer(Modifier.height(15.dp))
        HydrateRangeTabs(
            selected = selectedRange,
            onSelected = { selectedRange = it },
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(16.dp))
        NativeAdSlot(modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(Modifier.height(20.dp))
        HydrateSummaryCard(
            detail = detail,
            range = selectedRange,
            selectedDate = selectedDate,
            today = today,
            onPrevious = { selectedDate = selectedRange.moveDate(selectedDate, -1) },
            onNext = { selectedDate = selectedRange.moveDate(selectedDate, 1) },
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(22.dp))
        HydrateReportChart(
            detail = detail,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .height(245.dp),
        )
        Spacer(Modifier.height(22.dp))
        HydrateStatisticsHeader(
            title = detail.statisticsTitle,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            HydrateStatisticCard(
                title = detail.activeStat.title,
                value = detail.activeStat.value,
                subtitle = detail.activeStat.subtitle,
                modifier = Modifier.weight(1f),
            )
            HydrateStatisticCard(
                title = detail.relaxingStat.title,
                value = detail.relaxingStat.value,
                subtitle = detail.relaxingStat.subtitle,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun HydrateReportTopBar(onBack: () -> Unit) {
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
private fun HydrateRangeTabs(
    selected: HydrateReportRange,
    onSelected: (HydrateReportRange) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth()) {
        HydrateReportRange.values().forEach { range ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelected(range) },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                val active = range == selected
                Text(
                    text = stringResource(range.labelRes),
                    color = if (active) AccentBlue else MutedText,
                    fontSize = if (active) 16.sp else 14.sp,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(Modifier.height(5.dp))
                Box(
                    modifier = Modifier
                        .width(20.dp)
                        .height(2.dp)
                        .clip(RoundedCornerShape(100.dp))
                        .background(if (active) AccentBlue else Color.Transparent),
                )
            }
        }
    }
}

@Composable
private fun HydrateSummaryCard(
    detail: HydrateReportDetail,
    range: HydrateReportRange,
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
        HydratePeriodSwitcherRow(
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
            HydrateSummaryValue(value = detail.averageText, label = stringResource(R.string.report_detail_avg))
            HydrateSummaryValue(value = detail.totalText, label = stringResource(R.string.report_detail_total))
        }
    }
}

@Composable
private fun HydratePeriodSwitcherRow(
    text: String,
    canGoNext: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HydrateSmallArrow(enabled = true, reverse = false, onClick = onPrevious)
        Text(text, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        HydrateSmallArrow(enabled = canGoNext, reverse = true, onClick = onNext)
    }
}

@Composable
private fun HydrateSmallArrow(enabled: Boolean, reverse: Boolean, onClick: () -> Unit) {
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
private fun HydrateSummaryValue(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        value.AnimatedValueText(
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            label = "HydrateReportSummary:$label",
        )
        Spacer(Modifier.height(4.dp))
        Text(label, color = MutedText, fontSize = 12.sp)
    }
}

@Composable
private fun HydrateReportChart(
    detail: HydrateReportDetail,
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
                    drawCircle(Color(0xFF666666), radius = 3.dp.toPx(), center = Offset(x, baseline))
                    if (rawValue > 0.0) {
                        val barHeight = (chartHeight * (rawValue / maxValue).toFloat()).coerceAtLeast(8.dp.toPx())
                        drawRoundRect(
                            color = AccentBlue,
                            topLeft = Offset(x - barWidth / 2f, baseline - barHeight),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
                        )
                    }
                }

                val selectedIndex = detail.selectedIndex.coerceIn(0, values.lastIndex.coerceAtLeast(0))
                val selectedX = if (count == 1) size.width / 2f else selectedIndex * gap
                drawLine(
                    color = AccentBlue,
                    start = Offset(selectedX, 36.dp.toPx()),
                    end = Offset(selectedX, baseline - 12.dp.toPx()),
                    strokeWidth = 1.dp.toPx(),
                )
                drawHydrateTooltip(
                    centerX = selectedX,
                    value = detail.tooltipValue,
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

private fun DrawScope.drawHydrateTooltip(
    centerX: Float,
    value: String,
    label: String,
) {
    val tooltipWidth = 82.dp.toPx()
    val tooltipHeight = 45.dp.toPx()
    val left = (centerX - tooltipWidth / 2f).coerceIn(0f, size.width - tooltipWidth)
    drawRoundRect(
        color = AccentBlue,
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
    drawPath(trianglePath, AccentBlue)
    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        textSize = 14.sp.toPx()
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }
    val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(220, 244, 255)
        textSize = 11.sp.toPx()
        textAlign = Paint.Align.CENTER
    }
    drawContext.canvas.nativeCanvas.drawText(value, left + tooltipWidth / 2f, 17.dp.toPx(), titlePaint)
    drawContext.canvas.nativeCanvas.drawText(label, left + tooltipWidth / 2f, 33.dp.toPx(), subPaint)
}

@Composable
private fun HydrateStatisticsHeader(title: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Canvas(modifier = Modifier.size(18.dp)) {
            drawCircle(AccentBlue.copy(alpha = 0.16f), radius = size.minDimension / 2f)
            val barWidth = 2.dp.toPx()
            val bottom = size.height * 0.68f
            listOf(0.35f to 0.52f, 0.50f to 0.38f, 0.65f to 0.58f).forEach { (xFactor, topFactor) ->
                drawLine(
                    color = AccentBlue,
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
private fun HydrateStatisticCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .height(132.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(CardBg)
            .padding(start = 12.dp, top = 16.dp, end = 12.dp, bottom = 16.dp),
    ) {
        Canvas(modifier = Modifier.size(24.dp)) {
            drawCircle(AccentBlue.copy(alpha = 0.16f), radius = size.minDimension / 2f)
            drawCircle(AccentBlue, radius = size.minDimension * 0.22f, center = Offset(size.width * 0.5f, size.height * 0.62f))
            val drop = androidx.compose.ui.graphics.Path().apply {
                moveTo(size.width * 0.5f, size.height * 0.2f)
                cubicTo(size.width * 0.78f, size.height * 0.46f, size.width * 0.72f, size.height * 0.78f, size.width * 0.5f, size.height * 0.82f)
                cubicTo(size.width * 0.28f, size.height * 0.78f, size.width * 0.22f, size.height * 0.46f, size.width * 0.5f, size.height * 0.2f)
                close()
            }
            drawPath(drop, AccentBlue)
        }
        Spacer(Modifier.height(6.dp))
        Text(title, color = Color(0xFFD9D9D9), fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 1)
        Spacer(Modifier.height(10.dp))
        value.AnimatedValueText(
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            label = "HydrateReportStat:$title",
        )
        Spacer(Modifier.height(8.dp))
        Text(subtitle, color = MutedText, fontSize = 12.sp, maxLines = 1)
    }
}

private fun HydrateReportRange.moveDate(date: LocalDate, amount: Int): LocalDate {
    return when (this) {
        HydrateReportRange.Day -> date.plusDays(amount.toLong())
        HydrateReportRange.Week -> date.plusWeeks(amount.toLong())
        HydrateReportRange.Month -> date.plusMonths(amount.toLong())
    }
}

private fun buildHydrateReportDetail(
    data: HydrateData,
    range: HydrateReportRange,
    selectedDate: LocalDate,
    today: LocalDate,
    locale: Locale,
): HydrateReportDetail {
    val zoneId = ZoneId.systemDefault()
    val buckets = when (range) {
        HydrateReportRange.Day -> hydrateDayBuckets(data.hydrationRecords, selectedDate, zoneId)
        HydrateReportRange.Week -> hydrateRangeBuckets(weekDays(selectedDate), data.hydrationRecords, zoneId)
        HydrateReportRange.Month -> hydrateRangeBuckets(monthDays(selectedDate, today), data.hydrationRecords, zoneId)
    }
    val values = buckets.map { it.amountMl.toDouble() }
    val total = values.sum()
    val average = total / values.size.coerceAtLeast(1)
    val selectedIndex = values.indices.maxByOrNull { values[it] } ?: 0
    val active = buckets.maxByOrNull { it.amountMl } ?: buckets.firstOrNull()
    val relaxing = buckets.filter { it.amountMl > 0 }.minByOrNull { it.amountMl } ?: buckets.firstOrNull()
    val statisticsRange = range.statisticsTitle(selectedDate, today, locale)
    val axisMax = niceWaterAxisMax(values.maxOrNull() ?: 0.0)

    return HydrateReportDetail(
        periodTitle = range.periodTitle(selectedDate, today, locale),
        statisticsTitle = "Statistics ($statisticsRange)",
        values = values,
        averageText = formatWaterAmount(average, locale),
        totalText = formatWaterAmount(total, locale),
        xAxisLabels = range.axisLabels(selectedDate, today, locale),
        yAxisLabels = listOf(
            formatWaterAxis(axisMax, locale),
            formatWaterAxis(axisMax / 2.0, locale),
            formatWaterAxis(0.0, locale),
        ),
        axisMax = axisMax,
        selectedIndex = selectedIndex,
        tooltipValue = formatWaterAmount(values.getOrNull(selectedIndex) ?: 0.0, locale),
        tooltipLabel = buckets.getOrNull(selectedIndex)?.label(range, locale).orEmpty(),
        activeStat = HydrateReportStat(
            title = if (range == HydrateReportRange.Day) "Most Active Time" else "Most Active Day",
            value = formatWaterAmount(active?.amountMl?.toDouble() ?: 0.0, locale),
            subtitle = active?.label(range, locale).orEmpty(),
        ),
        relaxingStat = HydrateReportStat(
            title = if (range == HydrateReportRange.Day) "Most Relaxing Time" else "Most Relaxing Day",
            value = formatWaterAmount(relaxing?.amountMl?.toDouble() ?: 0.0, locale),
            subtitle = relaxing?.label(range, locale).orEmpty(),
        ),
    )
}

private fun hydrateDayBuckets(records: List<HydrationRecord>, date: LocalDate, zoneId: ZoneId): List<HydrateBucket> {
    val recordsByHour = records
        .filter { it.localDate(zoneId) == date }
        .groupBy { Instant.ofEpochMilli(it.timestamp).atZone(zoneId).hour.coerceIn(0, 23) }
        .mapValues { (_, items) -> items.sumOf { it.amountMl } }
    val fallbackHour = if (date == LocalDate.now()) LocalTime.now().hour.coerceIn(0, 23) else 12
    return (0..23).map { hour ->
        HydrateBucket(
            index = hour,
            date = null,
            hour = hour,
            amountMl = recordsByHour[hour] ?: if (recordsByHour.isEmpty() && hour == fallbackHour) 0 else 0,
        )
    }
}

private fun hydrateRangeBuckets(days: List<LocalDate>, records: List<HydrationRecord>, zoneId: ZoneId): List<HydrateBucket> {
    val totalsByDate = records
        .groupBy { it.localDate(zoneId) }
        .mapValues { (_, items) -> items.sumOf { it.amountMl } }
    return days.mapIndexed { index, day ->
        HydrateBucket(
            index = index,
            date = day,
            hour = null,
            amountMl = totalsByDate[day] ?: 0,
        )
    }
}

private fun HydrateReportRange.periodTitle(date: LocalDate, today: LocalDate, locale: Locale): String {
    return when (this) {
        HydrateReportRange.Day -> date.format(DateTimeFormatter.ofPattern("MMM d", locale))
        HydrateReportRange.Week -> statisticsTitle(date, today, locale)
        HydrateReportRange.Month -> date.format(DateTimeFormatter.ofPattern("MMM yyyy", locale))
    }
}

private fun HydrateReportRange.statisticsTitle(date: LocalDate, today: LocalDate, locale: Locale): String {
    return when (this) {
        HydrateReportRange.Day -> date.format(DateTimeFormatter.ofPattern("MMM d", locale))
        HydrateReportRange.Week -> {
            val days = weekDays(date)
            "${days.first().format(DateTimeFormatter.ofPattern("MMM d", locale))} - ${days.last().format(DateTimeFormatter.ofPattern("MMM d", locale))}"
        }
        HydrateReportRange.Month -> {
            val days = monthDays(date, today)
            "${days.first().format(DateTimeFormatter.ofPattern("MMM d", locale))} - ${days.last().format(DateTimeFormatter.ofPattern("MMM d", locale))}"
        }
    }
}

private fun HydrateReportRange.axisLabels(date: LocalDate, today: LocalDate, locale: Locale): List<String> {
    return when (this) {
        HydrateReportRange.Day -> listOf("06:00", "12:00", "16:00")
        HydrateReportRange.Week -> {
            val days = weekDays(date)
            listOf(days.first(), days[3], days.last()).map { it.format(DateTimeFormatter.ofPattern("MMM d", locale)) }
        }
        HydrateReportRange.Month -> {
            val days = monthDays(date, today)
            listOf(days.first(), days[days.size / 2], days.last()).map { it.dayOfMonth.toString() }
        }
    }
}

private fun HydrateBucket.label(range: HydrateReportRange, locale: Locale): String {
    return when (range) {
        HydrateReportRange.Day -> {
            val h = hour?.coerceIn(0, 23) ?: 0
            String.format(Locale.US, "%02d:00-%02d:59", h, h)
        }
        HydrateReportRange.Week,
        HydrateReportRange.Month -> date?.format(DateTimeFormatter.ofPattern("MMM d", locale)).orEmpty()
    }
}

private fun weekDays(date: LocalDate): List<LocalDate> {
    val start = date.minusDays((date.dayOfWeek.value - 1).toLong())
    return (0..6).map { start.plusDays(it.toLong()) }
}

private fun monthDays(date: LocalDate, today: LocalDate): List<LocalDate> {
    val lastDay = if (date.year == today.year && date.month == today.month) today.dayOfMonth else date.lengthOfMonth()
    return (1..lastDay).map { date.withDayOfMonth(it) }
}

private fun formatWaterAmount(value: Double, locale: Locale): String {
    return "${NumberFormat.getIntegerInstance(locale).format(value.roundToInt().coerceAtLeast(0))} ML"
}

private fun formatWaterAxis(value: Double, locale: Locale): String {
    return if (value >= 1000.0) {
        val thousands = value / 1000.0
        if (thousands % 1.0 == 0.0) "${thousands.roundToInt()}k" else String.format(locale, "%.1fk", thousands)
    } else {
        NumberFormat.getIntegerInstance(locale).format(value.roundToInt())
    }
}

private fun niceWaterAxisMax(maxValue: Double): Double {
    if (maxValue <= 0.0) return 1000.0
    val step = when {
        maxValue <= 500 -> 100.0
        maxValue <= 2000 -> 500.0
        else -> 1000.0
    }
    return (ceil(maxValue / step) * step).coerceAtLeast(100.0)
}

private val AccentBlue = Color(0xFF2CABFF)
private val CardBg = Color(0xFF1C1C22)
private val MutedText = Color(0xFF999999)
