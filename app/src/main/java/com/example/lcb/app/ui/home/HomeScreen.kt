package com.example.lcb.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lcb.app.R
import com.example.lcb.app.data.HomeData
import com.example.lcb.app.data.StepMetrics
import com.example.lcb.app.data.StepSensorStatus
import com.example.lcb.app.ui.currentAppLocale
import com.example.lcb.app.ui.components.AppBottomBar
import com.example.lcb.app.ui.components.DataCard
import com.example.lcb.app.ui.components.FigmaSettingsGlyph
import com.example.lcb.app.ui.components.MetricGlyph
import com.example.lcb.app.ui.components.MetricIcon
import com.example.lcb.app.ui.components.NativeAdSlot
import com.example.lcb.app.ui.components.RingProgress
import com.example.lcb.app.ui.components.ScreenFrame
import com.example.lcb.app.ui.components.TabDestination
import com.example.lcb.app.ui.theme.LcbCardGray
import com.example.lcb.app.ui.theme.LcbTextHeading
import com.example.lcb.app.ui.theme.LcbTextPrimary
import com.example.lcb.app.ui.theme.LcbTextSecondary
import com.example.lcb.app.ui.theme.LcbTextTertiary
import java.util.Locale

@Composable
fun HomeScreen(
    data: HomeData,
    sensorStatus: StepSensorStatus,
    active: Boolean = true,
    onRequestPermission: () -> Unit,
    onSettings: () -> Unit,
    onHome: () -> Unit,
    onData: () -> Unit,
    onHydrate: () -> Unit,
    onSetGoal: (Int) -> Unit,
) {
    val metrics = StepMetrics(data.todaySteps, data.stepGoal)
    val locale = currentAppLocale()

    ScreenFrame(
        background = Color.White,
        bottomBar = {
            AppBottomBar(
                selected = TabDestination.Home,
                onHome = onHome,
                onData = onData,
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 96.dp)
                .statusBarsPadding(),
        ) {
            HomeTopBar(
                onHydrate = onHydrate,
                onSettings = onSettings,
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(166.dp),
                contentAlignment = Alignment.Center,
            ) {
                RingProgress(
                    progress = metrics.progress,
                    modifier = Modifier.size(width = 142.dp, height = 132.dp),
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val percentText = metrics.percentText
                    val percentFontSize = when {
                        percentText.length <= 2 -> 38.sp
                        percentText.length <= 3 -> 35.sp
                        percentText.length <= 4 -> 31.sp
                        else -> 28.sp
                    }
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = percentText,
                            fontSize = percentFontSize,
                            fontWeight = FontWeight.Bold,
                            color = LcbTextPrimary,
                            lineHeight = 40.sp,
                        )
                        Text(
                            text = "%",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = LcbTextPrimary,
                            modifier = Modifier.padding(bottom = 5.dp),
                        )
                    }
                    Text(stringResource(R.string.home_completed), fontSize = 13.sp, color = LcbTextTertiary)
                }
            }
            GoalSummary(metrics = metrics, onSetGoal = onSetGoal)
            SensorStateBanner(sensorStatus, onRequestPermission)
            NativeAdSlot(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                active = active,
            )
            FitnessData(metrics = metrics, locale = locale)
        }
    }
}

@Composable
private fun HomeTopBar(
    onHydrate: () -> Unit,
    onSettings: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.home_title),
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold,
            color = LcbTextHeading,
            modifier = Modifier.weight(1f),
        )
        Box(
            modifier = Modifier
                .size(40.dp)
                .clickable(onClick = onHydrate),
            contentAlignment = Alignment.Center,
        ) {
            MetricGlyph(MetricIcon.Hydrate, Modifier.size(23.dp))
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 8.dp, end = 8.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF3B30)),
            )
        }
        Box(
            modifier = Modifier
                .size(40.dp)
                .clickable(onClick = onSettings),
            contentAlignment = Alignment.Center,
        ) {
            FigmaSettingsGlyph()
        }
    }
}

@Composable
private fun GoalSummary(metrics: StepMetrics, onSetGoal: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .height(64.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(LcbCardGray)
            .padding(horizontal = 28.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        GoalColumn(
            value = metrics.goal.toString(),
            unit = stringResource(R.string.home_step_unit),
            label = stringResource(R.string.home_today_goal),
            modifier = Modifier.clickable { onSetGoal(if (metrics.goal == 8000) 10000 else 8000) },
        )
        Box(
            modifier = Modifier
                .height(38.dp)
                .width(1.dp)
                .background(Color(0xFFEAEAEA)),
        )
        GoalColumn(
            value = metrics.steps.toString(),
            unit = stringResource(R.string.home_step_unit),
            label = stringResource(R.string.home_today_steps),
        )
    }
}

@Composable
private fun GoalColumn(
    value: String,
    unit: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.width(92.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(value, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = LcbTextPrimary)
            Spacer(Modifier.width(2.dp))
            Text(unit, fontSize = 12.sp, color = LcbTextPrimary, modifier = Modifier.padding(bottom = 1.dp))
        }
        Spacer(Modifier.height(5.dp))
        Text(label, fontSize = 11.sp, color = LcbTextTertiary, textAlign = TextAlign.Center)
    }
}

@Composable
private fun SensorStateBanner(status: StepSensorStatus, onRequestPermission: () -> Unit) {
    val message = when (status) {
        StepSensorStatus.PermissionRequired -> stringResource(R.string.home_permission_required)
        StepSensorStatus.Unsupported -> stringResource(R.string.home_sensor_unsupported)
        StepSensorStatus.Idle -> null
        StepSensorStatus.Active -> null
    } ?: return

    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFFFF5E5))
            .clickable(enabled = status == StepSensorStatus.PermissionRequired, onClick = onRequestPermission)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(message, fontSize = 12.sp, color = LcbTextSecondary, modifier = Modifier.weight(1f))
        if (status == StepSensorStatus.PermissionRequired) {
            Text(stringResource(R.string.home_allow), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = LcbTextPrimary)
        }
    }
}

@Composable
private fun FitnessData(metrics: StepMetrics, locale: Locale) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(stringResource(R.string.home_fitness_data), fontSize = 16.sp, fontWeight = FontWeight.Medium, color = LcbTextHeading)
        Spacer(Modifier.height(12.dp))
        DataCard(
            icon = MetricIcon.Distance,
            title = stringResource(R.string.home_today_distance),
            value = stringResource(
                R.string.metric_value_with_unit,
                formatDistanceKm(metrics.distanceKm, locale),
                stringResource(R.string.unit_km),
            ),
        )
        Spacer(Modifier.height(12.dp))
        DataCard(
            icon = MetricIcon.Steps,
            title = stringResource(R.string.home_today_steps),
            value = stringResource(R.string.home_steps_value, metrics.steps),
        )
        Spacer(Modifier.height(12.dp))
        DataCard(
            icon = MetricIcon.Calories,
            title = stringResource(R.string.home_calories_burned),
            value = stringResource(R.string.home_calories_value, metrics.calories),
        )
    }
}

private fun formatDistanceKm(distanceKm: Double, locale: Locale): String {
    val pattern = if (distanceKm > 0.0 && distanceKm < 1.0) "%.2f" else "%.1f"
    return String.format(locale, pattern, distanceKm)
}
