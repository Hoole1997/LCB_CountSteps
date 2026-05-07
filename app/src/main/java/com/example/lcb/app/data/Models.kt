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

data class AppData(
    val today: String = LocalDate.now().toString(),
    val todaySteps: Int = 0,
    val stepGoal: Int = 8000,
    val waterQuickAmountMl: Int = 100,
    val language: String = "en",
    val stepHistory: List<StepDailyRecord> = emptyList(),
    val hydrationRecords: List<HydrationRecord> = emptyList(),
)

data class HomeData(
    val todaySteps: Int = 0,
    val stepGoal: Int = 8000,
)

data class ReportData(
    val today: String = LocalDate.now().toString(),
    val todaySteps: Int = 0,
    val stepGoal: Int = 8000,
    val stepHistory: List<StepDailyRecord> = emptyList(),
)

data class HydrateData(
    val today: String = LocalDate.now().toString(),
    val waterQuickAmountMl: Int = 100,
    val hydrationRecords: List<HydrationRecord> = emptyList(),
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
