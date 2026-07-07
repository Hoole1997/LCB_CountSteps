package com.example.lcb.app.launcher

import android.content.Intent
import android.view.View
import android.os.Handler
import android.os.Looper
import com.blankj.utilcode.util.LogUtils
import com.example.lcb.app.LcbApp
import com.example.lcb.app.MainActivity
import com.example.lcb.app.R
import com.example.lcb.app.data.AppPreferences
import com.example.lcb.app.data.HomeData
import com.example.lcb.app.data.StepDailyRecord
import com.example.lcb.app.data.StepMetrics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import kotlin.math.roundToInt

object StepLauncherWidgetManager {
    private const val Key = "gostep_launcher_step_widget"
    private const val Title = "GoStep"
    private const val PreferredPage = 0
    private const val PreferredLeft = 0
    private const val PreferredTop = 1
    private const val SpanX = 4
    private const val SpanY = 2

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val mainHandler = Handler(Looper.getMainLooper())
    private val views = LinkedHashMap<String, ViewSpec>()
    private var callbacksRegistered = false
    private var refreshRequested = false
    private var lastDate = LocalDate.now()

    fun install(app: LcbApp) {
        val appContext = app.applicationContext
        val preferences = AppPreferences(appContext)
        val view = ensureWidgetView(app, appContext)
        ensureCallbacks(app)
        startMinuteTicker(app, preferences)
        scope.launch(Dispatchers.IO) { preferences.ensureToday() }
        scope.launch {
            preferences.homeData.collectLatest { data ->
                view.update(data.toWidgetState(appContext))
            }
        }
        requestRefresh(app)
    }

    private fun ensureCallbacks(app: LcbApp) {
        if (callbacksRegistered) return
        callbacksRegistered = true
        app.convertsafepower { key: String ->
            if (key == Key) {
                LogUtils.d("Step launcher widget rendered")
            }
        }
        app.maxquicklitememory { emit: (key: String, view: View, page: Int, left: Int, top: Int, spanX: Int, spanY: Int, title: String) -> Unit ->
            views.values.forEach { spec ->
                emit(spec.key, spec.view, spec.page, spec.left, spec.top, spec.spanX, spec.spanY, spec.title)
            }
        }
        val gridReadyListener: () -> Unit = {
            // 固定使用 4x2 首页位置。避免二次 grid ready 时 canPlace 把已渲染的动态项当成冲突后移动组件。
            LogUtils.d("Step launcher widget grid ready, submit ${views.size} views")
            requestRefresh(app)
        }
        app.maxquicklitememory(gridReadyListener)
    }

    private fun ensureWidgetView(app: LcbApp, appContext: android.content.Context): StepLauncherWidgetView {
        val existing = views[Key]?.view as? StepLauncherWidgetView
        if (existing != null) return existing
        val view = StepLauncherWidgetView(appContext).apply {
            setOnClickListener {
                app.startActivity(Intent(appContext, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }
        }
        views[Key] = ViewSpec(Key, view, PreferredPage, PreferredLeft, PreferredTop, SpanX, SpanY, Title)
        return view
    }

    private fun startMinuteTicker(app: LcbApp, preferences: AppPreferences) {
        mainHandler.removeCallbacksAndMessages(TickerToken)
        val ticker = object : Runnable {
            override fun run() {
                (views[Key]?.view as? StepLauncherWidgetView)?.invalidate()
                val today = LocalDate.now()
                if (today != lastDate) {
                    lastDate = today
                    scope.launch(Dispatchers.IO) { preferences.ensureToday() }
                    requestRefresh(app)
                }
                mainHandler.postAtTime(this, TickerToken, System.currentTimeMillis() + 60_000L)
            }
        }
        mainHandler.postAtTime(ticker, TickerToken, System.currentTimeMillis())
    }

    private fun requestRefresh(app: LcbApp) {
        if (refreshRequested) return
        refreshRequested = true
        mainHandler.post {
            refreshRequested = false
            app.localproshield()
        }
    }

    private fun HomeData.toWidgetState(context: android.content.Context): StepLauncherWidgetState {
        val today = runCatching { LocalDate.parse(this.today) }.getOrDefault(LocalDate.now())
        val start = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
        val records = (stepHistory + StepDailyRecord(this.today, todaySteps, stepGoal)).associateBy { it.date }
        val days = (0..6).map { offset ->
            val date = start.plusDays(offset.toLong())
            val record = records[date.toString()]
            LauncherWidgetDayState(
                label = context.weekdayLabel(date.dayOfWeek),
                steps = record?.steps ?: 0,
                goal = (record?.goal ?: stepGoal).coerceAtLeast(1),
            )
        }
        val metrics = StepMetrics(todaySteps, stepGoal)
        return StepLauncherWidgetState(
            days = days,
            minutes = (todaySteps / 70.0).roundToInt(),
            calories = metrics.calories,
            distanceKmText = formatDistance(metrics.distanceKm),
        )
    }

    private fun android.content.Context.weekdayLabel(day: DayOfWeek): String {
        return when (day) {
            DayOfWeek.SUNDAY -> getString(R.string.weekday_sun)
            DayOfWeek.MONDAY -> getString(R.string.weekday_mon)
            DayOfWeek.TUESDAY -> getString(R.string.weekday_tue)
            DayOfWeek.WEDNESDAY -> getString(R.string.weekday_wed)
            DayOfWeek.THURSDAY -> getString(R.string.weekday_thu)
            DayOfWeek.FRIDAY -> getString(R.string.weekday_fri)
            DayOfWeek.SATURDAY -> getString(R.string.weekday_sat)
        }
    }

    private fun formatDistance(distanceKm: Double): String {
        val pattern = if (distanceKm > 0.0 && distanceKm < 1.0) "%.2f" else "%.1f"
        return String.format(Locale.US, pattern, distanceKm)
    }

    private data class ViewSpec(
        val key: String,
        val view: View,
        val page: Int,
        val left: Int,
        val top: Int,
        val spanX: Int,
        val spanY: Int,
        val title: String,
    )

    private object TickerToken
}
