package com.example.lcb.app.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.lcb.app.data.HomeData
import com.example.lcb.app.data.HydrateData
import com.example.lcb.app.data.StepMetrics
import com.example.lcb.app.data.StepSensorStatus
import com.example.lcb.app.data.WeightData
import com.example.lcb.app.ui.components.ScreenFrame
import java.util.Locale

@Composable
fun HomeScreen(
    data: HomeData,
    hydrateData: HydrateData,
    weightData: WeightData,
    sensorStatus: StepSensorStatus,
    active: Boolean = true,
    showBottomBar: Boolean = true,
    onRequestPermission: () -> Unit,
    onSettings: () -> Unit,
    onHome: () -> Unit,
    onData: () -> Unit,
    onHydrate: () -> Unit,
    onAddWater: (Int) -> Unit,
    onSetStepGoal: (Int) -> Unit,
    onSetStepCountingPaused: (Boolean) -> Unit,
    onSetStepsForDate: (String, Int) -> Unit,
    onSetWeightForDate: (String, Int) -> Unit,
    onAchievements: () -> Unit,
    onEditStepGoal: () -> Unit = {},
    onEditWeight: () -> Unit = {},
    onEditSteps: () -> Unit = {},
) {
    val metrics = StepMetrics(data.todaySteps, data.stepGoal)
    val locale = Locale.getDefault()

    ScreenFrame(
        background = PageBg,
        bottomBar = if (showBottomBar) {
            {
                DarkBottomBar(
                    onHome = onHome,
                    onData = onData,
                )
            }
        } else {
            null
        },
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 375.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 104.dp)
                    .statusBarsPadding(),
            ) {
                Spacer(Modifier.height(20.dp))
                HomeNavBar(onSettings = onSettings)
                Spacer(Modifier.height(20.dp))
                StepCard(
                    data = data,
                    metrics = metrics,
                    locale = locale,
                    onEditGoal = onEditStepGoal,
                    onTogglePaused = { onSetStepCountingPaused(!data.isStepCountingPaused) },
                    onMore = onEditSteps,
                    onDetail = onData,
                )
                SensorStateBanner(sensorStatus, onRequestPermission)
                Spacer(Modifier.height(if (sensorStatus == StepSensorStatus.Active || sensorStatus == StepSensorStatus.Idle) 20.dp else 8.dp))
                ExerciseCard(metrics = metrics, locale = locale)
                Spacer(Modifier.height(18.dp))
                HealthTools(
                    homeData = data,
                    hydrateData = hydrateData,
                    weightData = weightData,
                    onWaterClick = onHydrate,
                    onAddWater = onAddWater,
                    onWeightClick = onEditWeight,
                    onAchievements = onAchievements,
                )
            }
        }
    }
}
