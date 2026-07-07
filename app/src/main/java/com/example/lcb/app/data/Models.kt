package com.example.lcb.app.data

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale
import kotlin.math.roundToInt

data class StepDailyRecord(
    val date: String,
    val steps: Int,
    val goal: Int,
)

data class HydrationRecord(
    val id: Long,
    val timestamp: Long,
    val amountMl: Int,
)

data class WeightRecord(
    val date: String,
    val weightTenthsKg: Int,
    val timestamp: Long,
)

data class AppData(
    val today: String = LocalDate.now().toString(),
    val todaySteps: Int = 0,
    val stepGoal: Int = 8000,
    val isStepCountingPaused: Boolean = false,
    val waterQuickAmountMl: Int = 100,
    val waterGoalMl: Int = 2000,
    val language: String = "system",
    val stepHistory: List<StepDailyRecord> = emptyList(),
    val hydrationRecords: List<HydrationRecord> = emptyList(),
)

data class HomeData(
    val today: String = LocalDate.now().toString(),
    val todaySteps: Int = 0,
    val stepGoal: Int = 8000,
    val isStepCountingPaused: Boolean = false,
    val stepHistory: List<StepDailyRecord> = emptyList(),
)

data class ReportData(
    val today: String = LocalDate.now().toString(),
    val todaySteps: Int = 0,
    val stepGoal: Int = 8000,
    val isStepCountingPaused: Boolean = false,
    val stepHistory: List<StepDailyRecord> = emptyList(),
    val trendBuckets: List<TrendBucket> = emptyList(),
)

data class TrendBucket(
    val hour: Int,
    val steps: Long = 0,
    val caloriesKcal: Double = 0.0,
    val distanceKm: Double = 0.0,
    val exerciseMinutes: Double = 0.0,
)

data class HydrateData(
    val today: String = LocalDate.now().toString(),
    val waterQuickAmountMl: Int = 100,
    val waterGoalMl: Int = 2000,
    val hydrationRecords: List<HydrationRecord> = emptyList(),
)

data class WeightData(
    val today: String = LocalDate.now().toString(),
    val weightRecords: List<WeightRecord> = emptyList(),
)

enum class StepSensorStatus {
    Idle,
    PermissionRequired,
    Unsupported,
    Active,
}

data class StepMetrics(
    val steps: Int,
    val goal: Int,
) {
    val progress: Float = if (goal <= 0) 0f else (steps.toFloat() / goal).coerceIn(0f, 1f)
    private val percentValue: Float = progress * 100f
    val percent: Int = percentValue.toInt()
    val percentText: String = when {
        percentValue <= 0f -> "0"
        percentValue < 0.1f -> "<0.1"
        percentValue < 10f && percentValue % 1f != 0f -> String.format(Locale.US, "%.1f", percentValue)
        else -> percentValue.roundToInt().toString()
    }
    val distanceKm: Double = steps * 0.00057
    val calories: Int = (steps * 0.071).toInt()
}

fun HydrationRecord.localDate(zoneId: ZoneId = ZoneId.systemDefault()): LocalDate {
    return Instant.ofEpochMilli(timestamp).atZone(zoneId).toLocalDate()
}
