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
import kotlin.math.max

private val Context.lcbDataStore: DataStore<Preferences> by preferencesDataStore(name = "lcb_steps")
private const val MaxStepHistoryDays = 400
private const val MaxHydrationRecords = 500

class AppPreferences(context: Context) {
    private val dataStore = context.applicationContext.lcbDataStore
    private val database = LcbDatabase.getInstance(context)
    private val dailyStepDao = database.dailyStepDao()
    private val hydrationRecordDao = database.hydrationRecordDao()
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
            waterQuickAmountMl = prefs[Keys.waterQuickAmount] ?: 100,
            language = normalizeLanguageCode(prefs[Keys.language]),
            stepHistory = history.sortedBy { it.date },
            hydrationRecords = water.sortedByDescending { it.timestamp },
        )
    }
        .distinctUntilChanged()

    val language: Flow<String> = preferencesData
        .map { prefs -> normalizeLanguageCode(prefs[Keys.language]) }
        .distinctUntilChanged()

    val homeData: Flow<HomeData> = preferencesData
        .map { prefs ->
            HomeData(
                todaySteps = prefs[Keys.todaySteps] ?: 0,
                stepGoal = prefs[Keys.stepGoal] ?: 8000,
            )
        }
        .distinctUntilChanged()

    val reportData: Flow<ReportData> = combine(preferencesData, stepHistory) { prefs, history ->
        val today = LocalDate.now().toString()
        ReportData(
            today = today,
            todaySteps = prefs[Keys.todaySteps] ?: 0,
            stepGoal = prefs[Keys.stepGoal] ?: 8000,
            stepHistory = history.sortedBy { it.date },
        )
    }
        .distinctUntilChanged()

    val hydrateData: Flow<HydrateData> = combine(preferencesData, hydrationRecords) { prefs, water ->
        HydrateData(
            today = LocalDate.now().toString(),
            waterQuickAmountMl = prefs[Keys.waterQuickAmount] ?: 100,
            hydrationRecords = water.sortedByDescending { it.timestamp },
        )
    }
        .distinctUntilChanged()

    suspend fun ensureToday() {
        migrateHistoryToRoomIfNeeded()
        val recordsToUpsert = mutableListOf<StepDailyRecord>()
        dataStore.edit { prefs ->
            recordsToUpsert += rolloverIfNeeded(prefs, LocalDate.now().toString())
        }
        upsertStepRecords(recordsToUpsert)
    }

    suspend fun recordStepCounter(totalSinceBoot: Int) {
        migrateHistoryToRoomIfNeeded()
        val recordsToUpsert = mutableListOf<StepDailyRecord>()
        dataStore.edit { prefs ->
            val today = LocalDate.now().toString()
            recordsToUpsert += rolloverIfNeeded(prefs, today)
            val baseline = prefs[Keys.stepCounterBaseline]
            if (baseline == null || baseline > totalSinceBoot) {
                prefs[Keys.stepCounterBaseline] = totalSinceBoot
                prefs[Keys.todaySteps] = 0
            } else {
                prefs[Keys.todaySteps] = max(0, totalSinceBoot - baseline)
            }
            recordsToUpsert += currentStepRecord(prefs, today)
        }
        upsertStepRecords(recordsToUpsert)
    }

    suspend fun recordDetectedStep() {
        migrateHistoryToRoomIfNeeded()
        val recordsToUpsert = mutableListOf<StepDailyRecord>()
        dataStore.edit { prefs ->
            val today = LocalDate.now().toString()
            recordsToUpsert += rolloverIfNeeded(prefs, today)
            prefs[Keys.todaySteps] = (prefs[Keys.todaySteps] ?: 0) + 1
            recordsToUpsert += currentStepRecord(prefs, today)
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

    suspend fun setLanguage(language: String) {
        dataStore.edit { prefs -> prefs[Keys.language] = language }
    }

    suspend fun setStepGoal(goal: Int) {
        migrateHistoryToRoomIfNeeded()
        val recordsToUpsert = mutableListOf<StepDailyRecord>()
        dataStore.edit { prefs ->
            prefs[Keys.stepGoal] = goal.coerceAtLeast(1)
            recordsToUpsert += currentStepRecord(prefs, LocalDate.now().toString())
        }
        upsertStepRecords(recordsToUpsert)
    }

    private fun rolloverIfNeeded(prefs: MutablePreferences, today: String): List<StepDailyRecord> {
        val storedDay = prefs[Keys.stepDay]
        if (storedDay == today) return emptyList()
        val previousDay = storedDay
        val records = mutableListOf<StepDailyRecord>()
        if (previousDay != null) {
            records += StepDailyRecord(
                date = previousDay,
                steps = prefs[Keys.todaySteps] ?: 0,
                goal = prefs[Keys.stepGoal] ?: 8000,
            )
        }
        prefs[Keys.stepDay] = today
        prefs[Keys.todaySteps] = 0
        prefs.remove(Keys.stepCounterBaseline)
        return records
    }

    private fun currentStepRecord(prefs: Preferences, today: String): StepDailyRecord {
        return StepDailyRecord(
            date = today,
            steps = prefs[Keys.todaySteps] ?: 0,
            goal = prefs[Keys.stepGoal] ?: 8000,
        )
    }

    private suspend fun upsertStepRecords(records: List<StepDailyRecord>) {
        if (records.isEmpty()) return
        dailyStepDao.upsertAll(records.associateBy { it.date }.values.map { it.toEntity() })
        dailyStepDao.trimToLatest(MaxStepHistoryDays)
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
            null, "", "english", "en" -> "en"
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
        val stepHistory = stringPreferencesKey("step_history")
        val hydrationRecords = stringPreferencesKey("hydration_records")
        val waterQuickAmount = intPreferencesKey("water_quick_amount")
        val language = stringPreferencesKey("language")
        val roomHistoryMigrated = booleanPreferencesKey("room_history_migrated")
        @Suppress("unused")
        val lastSeenTotal = longPreferencesKey("last_seen_total")
    }
}
