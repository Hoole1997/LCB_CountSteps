package com.example.lcb.app.ui.achievement

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.example.lcb.app.R
import com.example.lcb.app.data.HomeData
import com.example.lcb.app.data.HydrateData
import com.example.lcb.app.data.StepDailyRecord
import com.example.lcb.app.data.WeightData
import com.example.lcb.app.data.localDate
import java.time.LocalDate

@Immutable
data class AchievementUiModel(
    @param:StringRes val nameRes: Int,
    val unlocked: Boolean,
    @param:DrawableRes val unlockedImageRes: Int,
    @param:DrawableRes val lockedImageRes: Int,
)

fun buildAchievementModels(
    homeData: HomeData,
    hydrateData: HydrateData,
    weightData: WeightData,
): List<AchievementUiModel> {
    val stepRecords = normalizedStepRecords(homeData)
    val totalSteps = stepRecords.sumOf { it.steps }
    val totalDistanceKm = totalSteps * StepDistanceKm
    val maxDailySteps = stepRecords.maxOfOrNull { it.steps } ?: 0
    val stepGoalDays = stepRecords.filter { it.steps >= it.goal.coerceAtLeast(1) }.mapNotNull { parseDate(it.date) }.toSet()
    val waterGoal = hydrateData.waterGoalMl.coerceAtLeast(1)
    val waterGoalDays = hydrateData.hydrationRecords
        .groupBy { it.localDate() }
        .filterValues { records -> records.sumOf { it.amountMl } >= waterGoal }
        .keys
    val weightRecordDates = weightData.weightRecords.mapNotNull { parseDate(it.date) }.toSet()
    val reachedGoalWeight = weightData.weightRecords.any { it.weightTenthsKg <= GoalWeightTenthsKg }

    val unlockedStates = listOf(
        totalSteps >= 1_000,
        stepGoalDays.isNotEmpty(),
        maxDailySteps >= 10_000,
        totalSteps >= 100_000,
        longestConsecutiveDays(stepGoalDays) >= 3,
        longestConsecutiveDays(stepGoalDays) >= 7,
        longestConsecutiveDays(stepGoalDays) >= 30,
        waterGoalDays.isNotEmpty(),
        longestConsecutiveDays(waterGoalDays) >= 7,
        reachedGoalWeight,
        longestConsecutiveDays(weightRecordDates) >= 30,
        totalDistanceKm >= 10.0,
    )

    return achievementDefinitions.mapIndexed { index, definition ->
        AchievementUiModel(
            nameRes = definition.nameRes,
            unlocked = unlockedStates.getOrElse(index) { false },
            unlockedImageRes = definition.unlockedImageRes,
            lockedImageRes = definition.lockedImageRes,
        )
    }
}

fun countUnlockedAchievements(
    homeData: HomeData,
    hydrateData: HydrateData,
    weightData: WeightData,
): Int {
    return buildAchievementModels(homeData, hydrateData, weightData).count { it.unlocked }
}

private fun normalizedStepRecords(homeData: HomeData): List<StepDailyRecord> {
    val todayRecord = StepDailyRecord(homeData.today, homeData.todaySteps, homeData.stepGoal)
    return (homeData.stepHistory.filterNot { it.date == homeData.today } + todayRecord)
        .filter { parseDate(it.date) != null }
}

private fun longestConsecutiveDays(days: Set<LocalDate>): Int {
    if (days.isEmpty()) return 0
    val sorted = days.sorted()
    var longest = 1
    var current = 1
    for (index in 1 until sorted.size) {
        current = if (sorted[index - 1].plusDays(1) == sorted[index]) current + 1 else 1
        if (current > longest) longest = current
    }
    return longest
}

private fun parseDate(value: String): LocalDate? {
    return runCatching { LocalDate.parse(value) }.getOrNull()
}

private data class AchievementDefinition(
    @param:StringRes val nameRes: Int,
    @param:DrawableRes val unlockedImageRes: Int,
    @param:DrawableRes val lockedImageRes: Int,
)

private val achievementDefinitions = listOf(
    AchievementDefinition(R.string.achievement_first_steps, R.drawable.achievement_badge_01_unlocked, R.drawable.achievement_badge_01_locked),
    AchievementDefinition(R.string.achievement_goal_getter, R.drawable.achievement_badge_02_unlocked, R.drawable.achievement_badge_02_locked),
    AchievementDefinition(R.string.achievement_10k_day, R.drawable.achievement_badge_03_unlocked, R.drawable.achievement_badge_03_locked),
    AchievementDefinition(R.string.achievement_100k_club, R.drawable.achievement_badge_04_unlocked, R.drawable.achievement_badge_04_locked),
    AchievementDefinition(R.string.achievement_on_fire, R.drawable.achievement_badge_05_unlocked, R.drawable.achievement_badge_05_locked),
    AchievementDefinition(R.string.achievement_consistent, R.drawable.achievement_badge_06_unlocked, R.drawable.achievement_badge_06_locked),
    AchievementDefinition(R.string.achievement_unstoppable, R.drawable.achievement_badge_07_unlocked, R.drawable.achievement_badge_07_locked),
    AchievementDefinition(R.string.achievement_hydrated, R.drawable.achievement_badge_08_unlocked, R.drawable.achievement_badge_08_locked),
    AchievementDefinition(R.string.achievement_aqua_streak, R.drawable.achievement_badge_09_unlocked, R.drawable.achievement_badge_09_locked),
    AchievementDefinition(R.string.achievement_goal_weight, R.drawable.achievement_badge_10_unlocked, R.drawable.achievement_badge_10_locked),
    AchievementDefinition(R.string.achievement_steady, R.drawable.achievement_badge_11_unlocked, R.drawable.achievement_badge_11_locked),
    AchievementDefinition(R.string.achievement_explorer, R.drawable.achievement_badge_12_unlocked, R.drawable.achievement_badge_12_locked),
)

const val AchievementTotalCount = 12

private const val StepDistanceKm = 0.00057
private const val GoalWeightTenthsKg = 740
