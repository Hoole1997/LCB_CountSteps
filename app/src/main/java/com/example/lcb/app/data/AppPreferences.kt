package com.example.lcb.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.time.LocalDate
import java.time.LocalTime

private val Context.lcbDataStore: DataStore<Preferences> by preferencesDataStore(name = "lcb_steps")
private const val MaxStepHistoryDays = 400
private const val MaxHydrationRecords = 500
private const val MaxWeightHistoryDays = 400

class AppPreferences(context: Context) {
    private val dataStore = context.applicationContext.lcbDataStore
    private val database = LcbDatabase.getInstance(context)
    private val dailyStepDao = database.dailyStepDao()
    private val hydrationRecordDao = database.hydrationRecordDao()
    private val weightRecordDao = database.weightRecordDao()
    private val hourlyStepDao = database.hourlyStepDao()
    private val gson = Gson()

    private val preferencesData: Flow<Preferences> = dataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }

    private val stepHistory: Flow<List<StepDailyRecord>> = dailyStepDao.observeAll()
        .map { records -> records.map { it.toModel() } }
        .distinctUntilChanged()

    private val hydrationRecords: Flow<List<HydrationRecord>> = hydrationRecordDao.observeAll()
        .map { records -> records.map { it.toModel() } }
        .distinctUntilChanged()

    private val weightRecords: Flow<List<WeightRecord>> = weightRecordDao.observeAll()
        .map { records -> records.map { it.toModel() } }
        .distinctUntilChanged()

    private val hourlyStepRecords: Flow<List<HourlyStepEntity>> = hourlyStepDao.observeAll()
        .distinctUntilChanged()

    val data: Flow<AppData> = combine(
        preferencesData,
        stepHistory,
        hydrationRecords,
    ) { prefs, history, water ->
        val today = LocalDate.now().toString()
        AppData(
            today = today,
            todaySteps = prefs[Keys.todaySteps] ?: 0,
            stepGoal = prefs[Keys.stepGoal] ?: 8000,
            isStepCountingPaused = prefs[Keys.stepCountingPaused] ?: false,
            waterQuickAmountMl = prefs[Keys.waterQuickAmount] ?: 100,
            waterGoalMl = prefs.waterGoal,
            language = normalizeLanguageCode(prefs[Keys.language]),
            stepHistory = history.sortedBy { it.date },
            hydrationRecords = water.sortedByDescending { it.timestamp },
        )
    }
        .distinctUntilChanged()

    val language: Flow<String> = preferencesData
        .map { prefs -> normalizeLanguageCode(prefs[Keys.language]) }
        .distinctUntilChanged()

    val homeData: Flow<HomeData> = combine(preferencesData, stepHistory) { prefs, history ->
        val today = LocalDate.now().toString()
        HomeData(
            today = today,
            todaySteps = prefs[Keys.todaySteps] ?: 0,
            stepGoal = prefs[Keys.stepGoal] ?: 8000,
            isStepCountingPaused = prefs[Keys.stepCountingPaused] ?: false,
            stepHistory = history.sortedBy { it.date },
        )
    }
        .distinctUntilChanged()

    val reportData: Flow<ReportData> = combine(preferencesData, stepHistory, hourlyStepRecords) { prefs, history, hourly ->
        val today = LocalDate.now().toString()
        ReportData(
            today = today,
            todaySteps = prefs[Keys.todaySteps] ?: 0,
            stepGoal = prefs[Keys.stepGoal] ?: 8000,
            isStepCountingPaused = prefs[Keys.stepCountingPaused] ?: false,
            stepHistory = history.sortedBy { it.date },
            trendBuckets = hourly
                .filter { it.date == today }
                .map { it.toBucket() }
                .sortedBy { it.hour },
        )
    }
        .distinctUntilChanged()

    val hydrateData: Flow<HydrateData> = combine(preferencesData, hydrationRecords) { prefs, water ->
        HydrateData(
            today = LocalDate.now().toString(),
            waterQuickAmountMl = prefs[Keys.waterQuickAmount] ?: 100,
            waterGoalMl = prefs.waterGoal,
            hydrationRecords = water.sortedByDescending { it.timestamp },
        )
    }
        .distinctUntilChanged()

    val weightData: Flow<WeightData> = weightRecords
        .map { records ->
            WeightData(
                today = LocalDate.now().toString(),
                weightRecords = records.sortedByDescending { it.date },
            )
        }
        .distinctUntilChanged()

    suspend fun ensureToday() {
        migrateHistoryToRoomIfNeeded()
        val recordsToUpsert = mutableListOf<StepDailyRecord>()
        dataStore.edit { prefs ->
            val today = LocalDate.now().toString()
            val result = StepCountingAlgorithm.ensureToday(
                state = prefs.readStepState(),
                today = today,
                goal = prefs.stepGoal,
            )
            prefs.writeStepState(result.state)
            result.finalizedRecord?.let { recordsToUpsert += it }
        }
        upsertStepRecords(recordsToUpsert)
    }

    suspend fun recordStepCounter(totalSinceBoot: Int, stepsAlreadyCounted: Int = 0) {
        migrateHistoryToRoomIfNeeded()
        val recordsToUpsert = mutableListOf<StepDailyRecord>()
        val today = LocalDate.now().toString()
        var countedDelta = 0
        dataStore.edit { prefs ->
            val result = StepCountingAlgorithm.recordCounterSample(
                state = prefs.readStepState(),
                totalSinceBoot = totalSinceBoot,
                today = today,
                goal = prefs.stepGoal,
                stepsAlreadyCounted = stepsAlreadyCounted,
            )
            countedDelta = result.countedDelta
            prefs.writeStepState(result.state)
            result.finalizedRecord?.let { recordsToUpsert += it }
            recordsToUpsert += currentStepRecord(result.state, prefs.stepGoal)
        }
        upsertStepRecords(recordsToUpsert)
        addHourlySteps(today, countedDelta)
    }

    suspend fun recordDetectedStep(): Int {
        migrateHistoryToRoomIfNeeded()
        val recordsToUpsert = mutableListOf<StepDailyRecord>()
        val today = LocalDate.now().toString()
        var countedDelta = 0
        dataStore.edit { prefs ->
            val result = StepCountingAlgorithm.recordDetectorStep(
                state = prefs.readStepState(),
                today = today,
                goal = prefs.stepGoal,
            )
            countedDelta = result.countedDelta
            prefs.writeStepState(result.state)
            result.finalizedRecord?.let { recordsToUpsert += it }
            recordsToUpsert += currentStepRecord(result.state, prefs.stepGoal)
        }
        upsertStepRecords(recordsToUpsert)
        addHourlySteps(today, countedDelta)
        return countedDelta
    }

    suspend fun setStepCountingPaused(paused: Boolean) {
        migrateHistoryToRoomIfNeeded()
        val recordsToUpsert = mutableListOf<StepDailyRecord>()
        dataStore.edit { prefs ->
            val today = LocalDate.now().toString()
            val result = StepCountingAlgorithm.setPaused(
                state = prefs.readStepState(),
                paused = paused,
                today = today,
                goal = prefs.stepGoal,
            )
            prefs.writeStepState(result.state)
            result.finalizedRecord?.let { recordsToUpsert += it }
            recordsToUpsert += currentStepRecord(result.state, prefs.stepGoal)
        }
        upsertStepRecords(recordsToUpsert)
    }

    suspend fun addWater(amountMl: Int) {
        migrateHistoryToRoomIfNeeded()
        val now = System.currentTimeMillis()
        val id = now * 1_000 + Math.floorMod(System.nanoTime(), 1_000L)
        hydrationRecordDao.insert(
            HydrationRecordEntity(
                id = id,
                timestamp = now,
                amountMl = amountMl,
            )
        )
        hydrationRecordDao.trimToLatest(MaxHydrationRecords)
    }

    suspend fun setWaterGoalMl(goalMl: Int) {
        // Shared hydration target used by both the Hydrate activity and Home water card.
        dataStore.edit { prefs -> prefs[Keys.waterGoal] = goalMl.coerceIn(500, 5000) }
    }

    suspend fun setWeightForDate(date: String, weightTenthsKg: Int) {
        val targetDate = runCatching { LocalDate.parse(date) }.getOrNull() ?: return
        if (targetDate > LocalDate.now()) return

        weightRecordDao.upsert(
            WeightRecordEntity(
                date = targetDate.toString(),
                weightTenthsKg = weightTenthsKg.coerceIn(300, 2500),
                timestamp = System.currentTimeMillis(),
            )
        )
        weightRecordDao.trimToLatest(MaxWeightHistoryDays)
    }

    suspend fun setLanguage(language: String) {
        dataStore.edit { prefs -> prefs[Keys.language] = language }
    }

    suspend fun setStepGoal(goal: Int) {
        migrateHistoryToRoomIfNeeded()
        val recordsToUpsert = mutableListOf<StepDailyRecord>()
        dataStore.edit { prefs ->
            val today = LocalDate.now().toString()
            val result = StepCountingAlgorithm.ensureToday(
                state = prefs.readStepState(),
                today = today,
                goal = prefs.stepGoal,
            )
            prefs.writeStepState(result.state)
            prefs[Keys.stepGoal] = goal.coerceAtLeast(1)
            result.finalizedRecord?.let { recordsToUpsert += it }
            recordsToUpsert += currentStepRecord(result.state, prefs.stepGoal)
        }
        upsertStepRecords(recordsToUpsert)
    }

    suspend fun setTodaySteps(steps: Int) {
        migrateHistoryToRoomIfNeeded()
        val recordsToUpsert = mutableListOf<StepDailyRecord>()
        val today = LocalDate.now().toString()
        var manualDelta = 0
        dataStore.edit { prefs ->
            val previousSteps = prefs[Keys.todaySteps] ?: 0
            val result = StepCountingAlgorithm.setBusinessSteps(
                state = prefs.readStepState(),
                steps = steps,
                today = today,
                goal = prefs.stepGoal,
            )
            manualDelta = result.state.businessSteps - previousSteps
            prefs.writeStepState(result.state)
            result.finalizedRecord?.let { recordsToUpsert += it }
            recordsToUpsert += currentStepRecord(result.state, prefs.stepGoal)
        }
        upsertStepRecords(recordsToUpsert)
        addHourlySteps(today, manualDelta)
    }

    suspend fun setStepsForDate(date: String, steps: Int) {
        val targetDate = runCatching { LocalDate.parse(date) }.getOrNull() ?: return
        val today = LocalDate.now()
        if (targetDate > today) return
        if (targetDate == today) {
            setTodaySteps(steps)
            return
        }

        migrateHistoryToRoomIfNeeded()
        val targetDateText = targetDate.toString()
        val existing = dailyStepDao.getByDate(targetDateText)
        val currentGoal = preferencesData.first()[Keys.stepGoal] ?: 8000
        dailyStepDao.upsert(
            DailyStepEntity(
                date = targetDateText,
                steps = steps.coerceAtLeast(0),
                goal = existing?.goal ?: currentGoal,
            )
        )
        dailyStepDao.trimToLatest(MaxStepHistoryDays)
    }

    private fun currentStepRecord(state: StepCountingAlgorithm.State, goal: Int): StepDailyRecord {
        return StepDailyRecord(
            date = state.date ?: LocalDate.now().toString(),
            steps = state.businessSteps.coerceAtLeast(0),
            goal = goal.coerceAtLeast(1),
        )
    }

    private val Preferences.stepGoal: Int
        get() = this[Keys.stepGoal] ?: 8000

    private val Preferences.waterGoal: Int
        get() = this[Keys.waterGoal] ?: 2000

    private fun Preferences.readStepState(): StepCountingAlgorithm.State {
        return StepCountingAlgorithm.State(
            date = this[Keys.stepDay],
            businessSteps = this[Keys.todaySteps] ?: 0,
            counterAnchorTotal = this[Keys.stepCounterAnchorTotal],
            isPaused = this[Keys.stepCountingPaused] ?: false,
            resumeAnchorPending = this[Keys.stepCounterResumeAnchorPending] ?: false,
        )
    }

    private fun MutablePreferences.writeStepState(state: StepCountingAlgorithm.State) {
        state.date?.let { this[Keys.stepDay] = it } ?: remove(Keys.stepDay)
        this[Keys.todaySteps] = state.businessSteps.coerceAtLeast(0)
        state.counterAnchorTotal?.let { this[Keys.stepCounterAnchorTotal] = it } ?: remove(Keys.stepCounterAnchorTotal)
        this[Keys.stepCountingPaused] = state.isPaused
        this[Keys.stepCounterResumeAnchorPending] = state.resumeAnchorPending
        remove(Keys.stepCounterBaseline)
    }

    private suspend fun upsertStepRecords(records: List<StepDailyRecord>) {
        if (records.isEmpty()) return
        dailyStepDao.upsertAll(records.associateBy { it.date }.values.map { it.toEntity() })
        dailyStepDao.trimToLatest(MaxStepHistoryDays)
    }

    private suspend fun addHourlySteps(date: String, delta: Int) {
        if (delta <= 0) return
        val hour = LocalTime.now().hour.coerceIn(0, 23)
        val existing = hourlyStepDao.getByDateHour(date, hour)
        hourlyStepDao.upsert(
            HourlyStepEntity(
                date = date,
                hour = hour,
                steps = ((existing?.steps ?: 0) + delta).coerceAtLeast(0),
            )
        )
        hourlyStepDao.trimToLatestDays(MaxStepHistoryDays)
    }

    private suspend fun migrateHistoryToRoomIfNeeded() {
        val prefs = preferencesData.first()
        if (prefs[Keys.roomHistoryMigrated] == true) return

        val legacyStepRecords = prefs[Keys.stepHistory]
            ?.fromJson<List<StepDailyRecord>>()
            .orEmpty()
        val legacyHydrationRecords = prefs[Keys.hydrationRecords]
            ?.fromJson<List<HydrationRecord>>()
            .orEmpty()

        if (legacyStepRecords.isNotEmpty()) {
            dailyStepDao.upsertAll(
                legacyStepRecords
                    .sortedBy { it.date }
                    .takeLast(MaxStepHistoryDays)
                    .map { it.toEntity() }
            )
            dailyStepDao.trimToLatest(MaxStepHistoryDays)
        }

        if (legacyHydrationRecords.isNotEmpty()) {
            hydrationRecordDao.insertAll(
                legacyHydrationRecords
                    .sortedByDescending { it.timestamp }
                    .take(MaxHydrationRecords)
                    .map { it.toEntity() }
            )
            hydrationRecordDao.trimToLatest(MaxHydrationRecords)
        }

        dataStore.edit { editable ->
            editable[Keys.roomHistoryMigrated] = true
            editable.remove(Keys.stepHistory)
            editable.remove(Keys.hydrationRecords)
        }
    }

    private inline fun <reified T> String.fromJson(): T? {
        return runCatching {
            gson.fromJson<T>(this, object : TypeToken<T>() {}.type)
        }.getOrNull()
    }

    private fun normalizeLanguageCode(language: String?): String {
        return when (language?.trim()?.lowercase()) {
            null, "", "system", "follow system", "auto" -> "system"
            "english", "en" -> "en"
            "deutsch", "german", "de" -> "de"
            "español", "espanol", "spanish", "es" -> "es"
            "français", "francais", "french", "fr" -> "fr"
            "português", "portugues", "portuguese", "pt" -> "pt"
            "日本語", "japanese", "ja" -> "ja"
            "한국어", "korean", "ko" -> "ko"
            "简体中文", "simplified chinese", "zh-hans", "zh-cn", "zh" -> "zh-Hans"
            else -> "en"
        }
    }

    private object Keys {
        val stepDay = stringPreferencesKey("step_day")
        val todaySteps = intPreferencesKey("today_steps")
        val stepGoal = intPreferencesKey("step_goal")
        val stepCounterBaseline = intPreferencesKey("step_counter_baseline")
        val stepCounterAnchorTotal = intPreferencesKey("step_counter_anchor_total")
        val stepCountingPaused = booleanPreferencesKey("step_counting_paused")
        val stepCounterResumeAnchorPending = booleanPreferencesKey("step_counter_resume_anchor_pending")
        val stepHistory = stringPreferencesKey("step_history")
        val hydrationRecords = stringPreferencesKey("hydration_records")
        val waterQuickAmount = intPreferencesKey("water_quick_amount")
        val waterGoal = intPreferencesKey("water_goal_ml")
        val language = stringPreferencesKey("language")
        val roomHistoryMigrated = booleanPreferencesKey("room_history_migrated")
        @Suppress("unused")
        val lastSeenTotal = longPreferencesKey("last_seen_total")
    }
}
