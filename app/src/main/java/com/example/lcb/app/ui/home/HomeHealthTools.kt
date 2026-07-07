package com.example.lcb.app.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lcb.app.R
import com.example.lcb.app.data.HomeData
import com.example.lcb.app.data.HydrateData
import com.example.lcb.app.data.localDate
import com.example.lcb.app.data.StepMetrics
import com.example.lcb.app.data.WeightData
import com.example.lcb.app.data.WeightRecord
import com.example.lcb.app.ui.components.AnimatedValueText
import com.example.lcb.app.ui.achievement.AchievementTotalCount
import com.example.lcb.app.ui.achievement.countUnlockedAchievements
import java.time.LocalDate
import java.util.Locale
import kotlin.math.roundToInt

@Composable
internal fun ExerciseCard(metrics: StepMetrics, locale: Locale) {
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .height(122.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(CardBg)
            .padding(horizontal = 16.dp, vertical = 20.dp),
    ) {
        Row(
            modifier = Modifier.height(19.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(painter = painterResource(R.drawable.home_dark_exercise), contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.home_today_exercise),
                color = Color.White,
                fontSize = 16.sp,
                lineHeight = 19.sp,
                fontWeight = FontWeight.Medium,
            )
        }
        Spacer(Modifier.height(18.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(45.dp),
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            ExerciseValue(value = estimatedExerciseMinutes(metrics.steps).toString(), label = stringResource(R.string.home_unit_min))
            ExerciseValue(value = metrics.calories.toString(), label = stringResource(R.string.unit_kcal))
            ExerciseValue(value = formatDistanceKm(metrics.distanceKm, locale), label = stringResource(R.string.unit_km_title))
        }
    }
}

@Composable
private fun ExerciseValue(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        value.AnimatedValueText(
            color = Color.White,
            fontSize = 18.sp,
            lineHeight = 21.sp,
            fontWeight = FontWeight.SemiBold,
            label = "HomeExerciseValue:$label",
        )
        Spacer(Modifier.height(10.dp))
        Text(label, color = TextMuted, fontSize = 12.sp, lineHeight = 14.sp)
    }
}

@Composable
internal fun HealthTools(
    homeData: HomeData,
    hydrateData: HydrateData,
    weightData: WeightData,
    onWaterClick: () -> Unit,
    onAddWater: (Int) -> Unit,
    onWeightClick: () -> Unit,
    onAchievements: () -> Unit,
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
    ) {
        Text(
            text = stringResource(R.string.home_health_tools),
            color = Color.White,
            fontSize = 16.sp,
            lineHeight = 19.sp,
            fontWeight = FontWeight.Medium,
        )
        Spacer(Modifier.height(18.dp))
        WaterDataCard(
            data = hydrateData,
            onClick = onWaterClick,
            onAddWater = onAddWater,
        )
        Spacer(Modifier.height(16.dp))
        if (weightData.weightRecords.isEmpty()) {
            LockedToolCard(
                title = stringResource(R.string.home_log_weight),
                description = stringResource(R.string.home_log_weight_desc),
                button = stringResource(R.string.home_set_goal),
                accent = WeightGreen,
                iconRes = R.drawable.home_dark_weight,
                showLock = false,
                onClick = onWeightClick,
            )
        } else {
            WeightDataCard(
                data = weightData,
                locale = Locale.getDefault(),
                onClick = onWeightClick,
            )
        }
        Spacer(Modifier.height(16.dp))
        AchievementCard(
            unlockedCount = countUnlockedAchievements(
                homeData = homeData,
                hydrateData = hydrateData,
                weightData = weightData,
            ),
            onClick = onAchievements,
        )
    }
}

@Composable
private fun LockedToolCard(
    title: String,
    description: String,
    button: String,
    accent: Color,
    iconRes: Int,
    showLock: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(158.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(CardBg)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconTile(accent = accent, iconRes = iconRes)
            Spacer(Modifier.width(12.dp))
            Text(title, color = Color.White, fontSize = 14.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium)
        }
        Spacer(Modifier.height(14.dp))
        Text(
            text = description,
            color = TextSubtle,
            fontSize = 12.sp,
            lineHeight = 14.sp,
            maxLines = 1,
        )
        Spacer(Modifier.height(20.dp))
        Row(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .width(216.dp)
                .height(42.dp)
                .clip(RoundedCornerShape(100.dp))
                .background(accent),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (showLock) {
                Image(painter = painterResource(R.drawable.home_dark_lock), contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(2.dp))
            }
            Text(button, color = Color(0xFF222222), fontSize = 16.sp, lineHeight = 19.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun WeightDataCard(
    data: WeightData,
    locale: Locale,
    onClick: () -> Unit,
) {
    val latestRecord = remember(data.weightRecords) {
        data.weightRecords.maxByOrNull { it.date }
    }
    val progress = remember(latestRecord) {
        weightProgress(latestRecord?.weightTenthsKg)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardBg)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconTile(accent = WeightGreen, iconRes = R.drawable.home_dark_weight)
            Spacer(Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.home_weight),
                color = Color.White,
                fontSize = 14.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
            )
            Image(
                painter = painterResource(R.drawable.home_dark_more_blue),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                alpha = 0.72f,
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.home_weight_desc),
            color = TextSubtle,
            fontSize = 12.sp,
            lineHeight = 14.sp,
            maxLines = 2,
        )
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            WeightProgressBadge(progress = progress)
            Spacer(Modifier.width(48.dp))
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                WeightValueText(record = latestRecord, locale = locale)
                Spacer(Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .clip(RoundedCornerShape(35.dp))
                        .background(Color(0xFF302F37))
                        .clickable(onClick = onClick),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.home_weight_update),
                        color = Color.White,
                        fontSize = 14.sp,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}

@Composable
private fun WeightValueText(record: WeightRecord?, locale: Locale) {
    val weightText = record?.let { formatWeightKg(it.weightTenthsKg, locale) } ?: "--"
    val leftText = record?.let {
        stringResource(R.string.home_weight_left, formatWeightKg(weightLeftTenths(it.weightTenthsKg), locale))
    } ?: stringResource(R.string.home_weight_no_data)
    val kgUnit = stringResource(R.string.unit_kg)

    buildAnnotatedString {
            withStyle(SpanStyle(color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)) {
                append(weightText)
            }
            withStyle(SpanStyle(color = Color.White, fontSize = 12.sp)) {
                append(kgUnit)
            }
            withStyle(SpanStyle(color = TextMuted, fontSize = 12.sp)) {
                append(" /")
                append(leftText)
            }
        }.AnimatedValueText(
        lineHeight = 20.sp,
        maxLines = 1,
        label = "HomeWeightValue",
    )
}

@Composable
private fun WeightProgressBadge(progress: Float) {
    val progressValue = animatedProgress(progress, label = "HomeWeightProgress")
    Box(
        modifier = Modifier.size(58.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(58.dp)) {
            val stroke = 5.dp.toPx()
            val radius = size.minDimension / 2f - stroke / 2f
            val center = Offset(size.width / 2f, size.height / 2f)
            drawCircle(
                color = Color(0xFF363844),
                radius = radius,
                center = center,
                style = Stroke(stroke, cap = StrokeCap.Round),
            )
            if (progressValue > 0f) {
                drawArc(
                    brush = Brush.linearGradient(listOf(LinkBlue, WeightGreen)),
                    startAngle = -90f,
                    sweepAngle = 360f * progressValue,
                    useCenter = false,
                    style = Stroke(stroke, cap = StrokeCap.Round),
                )
            }
        }
        Image(
            painter = painterResource(R.drawable.home_dark_weight),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            contentScale = ContentScale.Fit,
        )
    }
}

@Composable
private fun AchievementCard(unlockedCount: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(71.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(CardBg)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconTile(accent = MedalYellow, iconRes = R.drawable.home_dark_medal)
        Spacer(Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(stringResource(R.string.home_achievements), color = TextSubtle, fontSize = 12.sp, lineHeight = 14.sp)
            Spacer(Modifier.height(6.dp))
            Text(
                stringResource(R.string.home_badges_unlocked, unlockedCount, AchievementTotalCount),
                color = Color.White,
                fontSize = 16.sp,
                lineHeight = 19.sp,
                fontWeight = FontWeight.Medium,
            )
        }
        Image(
            painter = painterResource(R.drawable.achievement_badge_01_unlocked),
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            contentScale = ContentScale.Fit,
        )
    }
}

@Composable
private fun IconTile(accent: Color, iconRes: Int) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(accent.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(20.571.dp),
            contentScale = ContentScale.Fit,
        )
    }
}

@Composable
private fun WaterDataCard(
    data: HydrateData,
    onClick: () -> Unit,
    onAddWater: (Int) -> Unit,
) {
    val today = runCatching { LocalDate.parse(data.today) }.getOrDefault(LocalDate.now())
    val waterByDate = remember(data.hydrationRecords) {
        data.hydrationRecords
            .groupBy { it.localDate() }
            .mapValues { entry -> entry.value.sumOf { it.amountMl } }
    }
    val todayTotal = waterByDate[today] ?: 0
    val waterGoalMl = data.waterGoalMl.coerceAtLeast(1)
    val progress = (todayTotal.toFloat() / waterGoalMl).coerceIn(0f, 1f)
    val percent = (progress * 100f).roundToInt()
    val days = (6 downTo 0).map { today.minusDays(it.toLong()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardBg)
            .padding(horizontal = 12.dp, vertical = 16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .clickable(onClick = onClick),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconTile(accent = WaterBlue, iconRes = R.drawable.home_dark_water)
            Spacer(Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.home_log_water),
                color = Color.White,
                fontSize = 14.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
            )
            Image(
                painter = painterResource(R.drawable.home_dark_more_blue),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                alpha = 0.72f,
            )
        }
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            WaterProgressBadge(progress = progress, percent = percent)
            Spacer(Modifier.width(48.dp))
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                buildAnnotatedString {
                        withStyle(SpanStyle(color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)) {
                            append(todayTotal.toString())
                        }
                        withStyle(SpanStyle(color = Color.White, fontSize = 12.sp)) {
                            append("ML")
                        }
                        withStyle(SpanStyle(color = TextMuted, fontSize = 12.sp)) {
                            append(" /${waterGoalMl}ML")
                        }
                    }.AnimatedValueText(
                    lineHeight = 20.sp,
                    maxLines = 1,
                    label = "HomeWaterValue",
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .clip(RoundedCornerShape(35.dp))
                        .background(Color(0xFF302F37))
                        .clickable { onAddWater(HomeWaterAmountMl) },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "+${HomeWaterAmountMl}ML",
                        color = Color.White,
                        fontSize = 14.sp,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            days.forEach { day ->
                val amount = waterByDate[day] ?: 0
                val isToday = day == today
                Column(
                    modifier = Modifier.width(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    DailyStatusIcon(value = amount, goal = waterGoalMl)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = weekdayLabel(day.dayOfWeek),
                        color = if (isToday) LinkBlue else TextMuted,
                        fontSize = 12.sp,
                        lineHeight = 14.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun WaterProgressBadge(progress: Float, percent: Int) {
    val progressValue = animatedProgress(progress, label = "HomeWaterProgress")
    Box(
        modifier = Modifier.size(58.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(58.dp)) {
            val stroke = 5.dp.toPx()
            val radius = size.minDimension / 2f - stroke / 2f
            val center = Offset(size.width / 2f, size.height / 2f)
            drawCircle(
                color = Color(0xFF363844),
                radius = radius,
                center = center,
                style = Stroke(stroke, cap = StrokeCap.Round),
            )
            if (progressValue > 0f) {
                drawArc(
                    color = WaterBlue,
                    startAngle = -90f,
                    sweepAngle = 360f * progressValue,
                    useCenter = false,
                    style = Stroke(stroke, cap = StrokeCap.Round),
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(R.drawable.home_dark_water),
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                contentScale = ContentScale.Fit,
            )
            "$percent%".AnimatedValueText(
                color = Color.White,
                fontSize = 12.sp,
                lineHeight = 14.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Normal,
                label = "HomeWaterPercent",
            )
        }
    }
}

private fun estimatedExerciseMinutes(steps: Int): Int {
    return (steps / 70.0).roundToInt()
}

private fun formatDistanceKm(distanceKm: Double, locale: Locale): String {
    val pattern = if (distanceKm > 0.0 && distanceKm < 1.0) "%.2f" else "%.1f"
    return String.format(locale, pattern, distanceKm)
}

private fun formatWeightKg(weightTenthsKg: Int, locale: Locale): String {
    return if (weightTenthsKg % 10 == 0) {
        (weightTenthsKg / 10).toString()
    } else {
        String.format(locale, "%.1f", weightTenthsKg / 10f)
    }
}

private fun weightLeftTenths(weightTenthsKg: Int): Int {
    return (weightTenthsKg - WeightGoalTenthsKg).coerceAtLeast(0)
}

private fun weightProgress(weightTenthsKg: Int?): Float {
    if (weightTenthsKg == null || weightTenthsKg <= 0) return 0f
    return (WeightGoalTenthsKg.toFloat() / weightTenthsKg).coerceIn(0f, 1f)
}

private const val HomeWaterAmountMl = 200
private const val WeightGoalTenthsKg = 740
