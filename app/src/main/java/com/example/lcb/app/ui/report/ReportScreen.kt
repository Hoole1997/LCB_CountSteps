package com.example.lcb.app.ui.report

import androidx.annotation.StringRes
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lcb.app.R
import com.example.lcb.app.data.ReportData
import com.example.lcb.app.data.StepDailyRecord
import com.example.lcb.app.data.StepMetrics
import com.example.lcb.app.ui.currentAppLocale
import com.example.lcb.app.ui.components.AppBottomBar
import com.example.lcb.app.ui.components.BarChart
import com.example.lcb.app.ui.components.NativeAdSlot
import com.example.lcb.app.ui.components.ScreenFrame
import com.example.lcb.app.ui.components.TabDestination
import com.example.lcb.app.ui.theme.LcbCardGray
import com.example.lcb.app.ui.theme.LcbPageGray
import com.example.lcb.app.ui.theme.LcbPrimary
import com.example.lcb.app.ui.theme.LcbTextHeading
import com.example.lcb.app.ui.theme.LcbTextPrimary
import com.example.lcb.app.ui.theme.LcbTextSecondary
import com.example.lcb.app.ui.theme.LcbTextTertiary
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private enum class ReportTab(@param:StringRes val labelRes: Int) {
    Day(R.string.report_tab_day),
    Week(R.string.report_tab_week),
    Month(R.string.report_tab_month),
}

@Composable
fun ReportScreen(
    data: ReportData,
    active: Boolean = true,
    onHome: () -> Unit,
    onData: () -> Unit,
) {
    var selectedTab by remember { mutableStateOf(ReportTab.Day) }
    val today = LocalDate.parse(data.today)
    val locale = currentAppLocale()
    val history = remember(data.stepHistory, data.todaySteps, data.stepGoal, data.today) {
        mergeToday(data)
    }

    ScreenFrame(
        background = LcbPageGray,
        bottomBar = {
            AppBottomBar(
                selected = TabDestination.Data,
                onHome = onHome,
                onData = onData,
            )
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
                text = stringResource(R.string.report_title),
                modifier = Modifier.padding(start = 16.dp, top = 24.dp),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = LcbTextPrimary,
            )
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .padding(horizontal = 14.dp, vertical = 14.dp),
            ) {
                SegmentControl(selected = selectedTab, onSelected = { selectedTab = it })
                Spacer(Modifier.height(20.dp))
                DateCaption(tab = selectedTab, today = today, locale = locale)
                Spacer(Modifier.height(18.dp))
                when (selectedTab) {
                    ReportTab.Day -> DayReport(data = data, locale = locale)
                    ReportTab.Week -> WeekReport(today = today, history = history)
                    ReportTab.Month -> MonthReport(today = today, history = history, locale = locale)
                }
            }
            NativeAdSlot(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                active = active,
            )
        }
    }
}

@Composable
private fun SegmentControl(selected: ReportTab, onSelected: (ReportTab) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(LcbCardGray)
            .padding(0.dp),
    ) {
        ReportTab.values().forEach { tab ->
            val active = selected == tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (active) LcbTextPrimary else Color.Transparent)
                    .clickable { onSelected(tab) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(tab.labelRes),
                    color = if (active) Color.White else LcbTextSecondary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun DateCaption(tab: ReportTab, today: LocalDate, locale: Locale) {
    val text = when (tab) {
        ReportTab.Day -> today.format(DateTimeFormatter.ofPattern("MMM d,yyyy", locale))
        ReportTab.Week -> {
            val start = today.minusDays((today.dayOfWeek.value - 1).toLong())
            val end = start.plusDays(6)
            "${start.format(DateTimeFormatter.ofPattern("MMM d,yyyy", locale))}-${end.format(DateTimeFormatter.ofPattern("MMM d,yyyy", locale))}"
        }
        ReportTab.Month -> today.format(DateTimeFormatter.ofPattern("MMM yyyy", locale))
    }
    Text(
        text = text,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        color = LcbTextHeading,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
    )
}

@Composable
private fun DayReport(data: ReportData, locale: Locale) {
    val metrics = StepMetrics(data.todaySteps, data.stepGoal)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(158.dp)) {
            val stroke = 16.dp.toPx()
            drawArc(
                Color(0xFFEDEDED),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(stroke, cap = StrokeCap.Round),
            )
            drawArc(
                LcbPrimary,
                startAngle = -90f,
                sweepAngle = 360f * metrics.progress,
                useCenter = false,
                style = Stroke(stroke, cap = StrokeCap.Round),
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(R.string.report_steps), fontSize = 14.sp, color = LcbTextSecondary)
            Text(metrics.steps.toString(), fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = LcbTextHeading)
            Text(stringResource(R.string.report_target_value, metrics.goal), fontSize = 12.sp, color = LcbTextTertiary)
        }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .padding(horizontal = 8.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        StatBlock(stringResource(R.string.report_total_distance_km), String.format(locale, "%.1f", metrics.distanceKm))
        Box(Modifier.width(1.dp).height(40.dp).background(Color(0xFFF2F2F2)))
        StatBlock(stringResource(R.string.report_total_consumption_cal), metrics.calories.toString())
    }
}

@Composable
private fun WeekReport(today: LocalDate, history: List<StepDailyRecord>) {
    val start = today.minusDays((today.dayOfWeek.value - 1).toLong())
    val days = (0..6).map { start.plusDays(it.toLong()) }
    val values = days.map { day -> history.firstOrNull { it.date == day.toString() }?.steps ?: 0 }
    Column {
        ChartShell {
            BarChart(
                values = values,
                labels = weekdayLabels(),
                modifier = Modifier.fillMaxSize(),
                barWidth = 18.dp,
                selectedIndex = today.dayOfWeek.value - 1,
            )
        }
        ReportStats(
            total = values.sum(),
            average = if (values.isEmpty()) 0 else values.sum() / values.size,
            max = values.maxOrNull() ?: 0,
            labels = listOf(stringResource(R.string.report_total_steps), stringResource(R.string.report_steps_avg), stringResource(R.string.report_steps_max)),
        )
    }
}

@Composable
private fun MonthReport(today: LocalDate, history: List<StepDailyRecord>, locale: Locale) {
    val first = today.withDayOfMonth(1)
    val days = (0 until today.lengthOfMonth()).map { first.plusDays(it.toLong()) }
    val values = days.map { day -> history.firstOrNull { it.date == day.toString() }?.steps ?: 0 }
    val labelDays = listOf(1, 8, 15, 22, 29).filter { it <= today.lengthOfMonth() }
    val labels = labelDays.map { "${today.monthValue}/$it" }
    Column {
        ChartShell {
            BarChart(
                values = values,
                labels = labels,
                modifier = Modifier.fillMaxSize(),
                barWidth = 4.dp,
                selectedIndex = today.dayOfMonth - 1,
            )
        }
        val totalDistance = values.sum() * 0.00057
        val totalCalories = (values.sum() * 0.071).toInt()
        ReportStats(
            total = values.sum(),
            average = totalDistance.toInt(),
            max = totalCalories,
            labels = listOf(stringResource(R.string.report_total_steps), stringResource(R.string.report_total_distance), stringResource(R.string.report_total_consumption)),
            middleValue = String.format(locale, "%.1f", totalDistance),
        )
    }
}

@Composable
private fun ChartShell(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(308.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(top = 26.dp, start = 8.dp, end = 8.dp, bottom = 16.dp),
    ) {
        content()
    }
}

@Composable
private fun ReportStats(
    total: Int,
    average: Int,
    max: Int,
    labels: List<String>,
    middleValue: String = average.toString(),
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 22.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        StatBlock(labels[0], total.toString(), modifier = Modifier.weight(1f))
        StatBlock(labels[1], middleValue, modifier = Modifier.weight(1f))
        StatBlock(labels[2], max.toString(), modifier = Modifier.weight(1f))
    }
}

@Composable
private fun StatBlock(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = LcbTextHeading)
        Spacer(Modifier.height(8.dp))
        Text(label, fontSize = 12.sp, color = LcbTextSecondary, textAlign = TextAlign.Center)
    }
}

private fun mergeToday(data: ReportData): List<StepDailyRecord> {
    val todayRecord = StepDailyRecord(data.today, data.todaySteps, data.stepGoal)
    return (data.stepHistory.filterNot { it.date == data.today } + todayRecord).sortedBy { it.date }
}

@Composable
private fun weekdayLabels(): List<String> {
    return listOf(
        stringResource(R.string.weekday_mon),
        stringResource(R.string.weekday_tue),
        stringResource(R.string.weekday_wed),
        stringResource(R.string.weekday_thu),
        stringResource(R.string.weekday_fri),
        stringResource(R.string.weekday_sat),
        stringResource(R.string.weekday_sun),
    )
}
