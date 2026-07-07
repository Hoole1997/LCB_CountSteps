package com.example.lcb.app.ui.report

import androidx.annotation.StringRes
import com.example.lcb.app.R
import com.example.lcb.app.data.TrendBucket

internal enum class ReportTab(@param:StringRes val labelRes: Int) {
    Day(R.string.report_tab_day),
    Week(R.string.report_tab_week),
    Month(R.string.report_tab_month),
}

internal enum class TrendIcon {
    Step,
    Calorie,
    Distance,
    Time,
}

internal data class LocalPeriodTrend(
    val steps: Long,
    val caloriesKcal: Double,
    val distanceKm: Double,
    val exerciseMinutes: Double,
    val buckets: List<TrendBucket>,
    val chartStartLabel: String,
    val chartEndLabel: String,
)
