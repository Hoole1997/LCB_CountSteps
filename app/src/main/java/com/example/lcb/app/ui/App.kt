package com.example.lcb.app.ui

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.zIndex
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.lcb.app.HydrateActivity
import com.example.lcb.app.LcbAppViewModel
import com.example.lcb.app.AchievementActivity
import com.example.lcb.app.ui.home.HomeScreen
import com.example.lcb.app.ui.report.ReportScreen
import com.example.lcb.app.ui.theme.LcbTheme
import com.example.lcb.app.utils.loadInterstitial
import net.corekit.core.report.ReportDataManager

private object Routes {
    const val Home = "home"
}

private enum class MainTab { Home, Data }

private const val InterstitialCooldownMs = 90_000L

@Composable
fun LcbStepsApp(viewModel: LcbAppViewModel = viewModel()) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val interstitialGate = remember { InterstitialGate() }
    val baseConfiguration = LocalConfiguration.current
    val language by viewModel.language.collectAsStateWithLifecycle()
    val localizedContext = remember(context, language, baseConfiguration) {
        context.localizedContext(language)
    }
    val localizedConfiguration = remember(baseConfiguration, language) {
        createLocalizedConfiguration(baseConfiguration, language)
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) {
        viewModel.onPermissionResult()
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !viewModel.hasActivityRecognitionPermission()) {
            permissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
        } else {
            viewModel.activateStepSensor()
        }
    }

    CompositionLocalProvider(
        LocalContext provides localizedContext,
        LocalConfiguration provides localizedConfiguration,
    ) {
        LcbTheme {
            NavHost(
                navController = navController,
                startDestination = Routes.Home,
                enterTransition = { EnterTransition.None },
                exitTransition = { ExitTransition.None },
                popEnterTransition = { EnterTransition.None },
                popExitTransition = { ExitTransition.None },
            ) {
                composable(Routes.Home) {
                    var selectedMainTab by rememberSaveable { mutableStateOf(MainTab.Home) }
                    var dataTabVisited by rememberSaveable { mutableStateOf(false) }
                    val homeData by viewModel.homeData.collectAsStateWithLifecycle()
                    val sensorStatus by viewModel.sensorStatus.collectAsStateWithLifecycle()
                    val reportData by viewModel.reportData.collectAsStateWithLifecycle()
                    val hydrateData by viewModel.hydrateData.collectAsStateWithLifecycle()
                    val weightData by viewModel.weightData.collectAsStateWithLifecycle()

                    Box(modifier = Modifier.fillMaxSize()) {
                        KeepAliveMainTab(visible = selectedMainTab == MainTab.Home) {
                            HomeScreen(
                                data = homeData,
                                hydrateData = hydrateData,
                                weightData = weightData,
                                sensorStatus = sensorStatus,
                                active = selectedMainTab == MainTab.Home,
                                onRequestPermission = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                        permissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
                                    }
                                },
                                onSettings = {},
                                onHome = {
                                    reportClick("click_tab_home", "page" to "home")
                                },
                                onData = {
                                    reportClick("click_tab_data", "from" to "home")
                                    dataTabVisited = true
                                    selectedMainTab = MainTab.Data
                                },
                                onHydrate = {
                                    reportClick("click_enter_hydrate", "from" to "home")
                                    activity.showInterstitialThen(interstitialGate) {
                                        context.startActivity(Intent(context, HydrateActivity::class.java))
                                    }
                                },
                                onAddWater = { amount ->
                                    reportClick("click_drink_water", "from" to "home", "amount_ml" to amount)
                                    viewModel.addWater(amount)
                                },
                                onSetStepGoal = viewModel::setStepGoal,
                                onSetStepCountingPaused = viewModel::setStepCountingPaused,
                                onSetStepsForDate = viewModel::setStepsForDate,
                                onSetWeightForDate = viewModel::setWeightForDate,
                                onAchievements = {
                                    reportClick("click_enter_achievements", "from" to "home")
                                    context.startActivity(AchievementActivity.createIntent(context))
                                },
                            )
                        }
                        if (dataTabVisited) {
                            KeepAliveMainTab(visible = selectedMainTab == MainTab.Data) {
                                ReportScreen(
                                    data = reportData,
                                    active = selectedMainTab == MainTab.Data,
                                    onHome = {
                                        reportClick("click_tab_home", "from" to "data")
                                        selectedMainTab = MainTab.Home
                                    },
                                    onData = {
                                        reportClick("click_tab_data", "page" to "data")
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KeepAliveMainTab(
    visible: Boolean,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { alpha = if (visible) 1f else 0f }
            .zIndex(if (visible) 1f else 0f),
    ) {
        content()
    }
}

private class InterstitialGate {
    private var lastShownAt = 0L
    private var navigationCountSinceShown = 1

    fun shouldAttempt(): Boolean {
        navigationCountSinceShown += 1
        val now = SystemClock.elapsedRealtime()
        return navigationCountSinceShown >= 2 && now - lastShownAt >= InterstitialCooldownMs
    }

    fun markShown() {
        lastShownAt = SystemClock.elapsedRealtime()
        navigationCountSinceShown = 0
    }
}

private fun FragmentActivity?.showInterstitialThen(
    gate: InterstitialGate,
    afterAd: () -> Unit,
) {
    if (this == null) {
        afterAd()
        return
    }
    loadInterstitial(condition = gate::shouldAttempt) { shown ->
        if (shown) gate.markShown()
        afterAd()
    }
}

private fun reportClick(eventName: String, vararg params: Pair<String, Any>) {
    ReportDataManager.reportData(
        eventName = eventName,
        data = params.toMap(),
    )
}
