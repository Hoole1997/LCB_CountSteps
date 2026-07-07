package com.example.lcb.app.ui.hydrate

import androidx.annotation.StringRes
import com.example.lcb.app.R
import java.time.LocalDate

internal enum class HydrateReportRange(@param:StringRes val labelRes: Int) {
    Day(R.string.report_tab_day),
    Week(R.string.report_tab_week),
    Month(R.string.report_tab_month),
}

internal data class HydrateReportDetail(
    val periodTitle: String,
    val statisticsTitle: String,
    val values: List<Double>,
    val averageText: String,
    val totalText: String,
    val xAxisLabels: List<String>,
    val yAxisLabels: List<String>,
    val axisMax: Double,
    val selectedIndex: Int,
    val tooltipValue: String,
    val tooltipLabel: String,
    val activeStat: HydrateReportStat,
    val relaxingStat: HydrateReportStat,
)

internal data class HydrateBucket(
    val index: Int,
    val date: LocalDate?,
    val hour: Int?,
    val amountMl: Int,
)

internal data class HydrateReportStat(
    val title: String,
    val value: String,
    val subtitle: String,
)
