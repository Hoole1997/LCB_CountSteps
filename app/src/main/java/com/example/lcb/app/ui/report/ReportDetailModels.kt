package com.example.lcb.app.ui.report

import androidx.annotation.StringRes
import com.example.lcb.app.R
import com.example.lcb.app.data.TrendBucket
import java.time.LocalDate

internal enum class ReportDetailRange(@param:StringRes val labelRes: Int) {
    Day(R.string.report_tab_day),
    Week(R.string.report_tab_week),
    Month(R.string.report_tab_month),
}

internal data class ReportDetailLabels(
    val statisticsFormat: String,
    val mostActiveTime: String,
    val mostActiveDay: String,
    val mostRelaxingTime: String,
    val mostRelaxingDay: String,
    val stepsUnit: String,
)

internal data class ReportDetailData(
    val periodTitle: String,
    val statisticsTitle: String,
    val values: List<Double>,
    val buckets: List<DetailBucket>,
    val averageText: String,
    val totalText: String,
    val xAxisLabels: List<String>,
    val yAxisLabels: List<String>,
    val axisMax: Double,
    val selectedIndex: Int,
    val tooltipValue: String,
    val tooltipUnit: String,
    val tooltipLabel: String,
    val activeStat: ReportDetailStat,
    val relaxingStat: ReportDetailStat,
)

internal data class DetailBucket(
    val index: Int,
    val date: LocalDate?,
    val hour: Int?,
    val trend: TrendBucket,
)

internal data class ReportDetailStat(
    val title: String,
    val value: String,
    val unit: String,
    val subtitle: String,
)
