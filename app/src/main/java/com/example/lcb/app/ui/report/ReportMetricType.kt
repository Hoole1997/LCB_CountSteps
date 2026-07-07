package com.example.lcb.app.ui.report

import androidx.annotation.StringRes
import com.example.lcb.app.R

enum class ReportMetricType(@param:StringRes val labelRes: Int) {
    Step(R.string.report_metric_step),
    Calorie(R.string.report_metric_calorie),
    Distance(R.string.report_metric_distance),
    Time(R.string.report_metric_time),
}
