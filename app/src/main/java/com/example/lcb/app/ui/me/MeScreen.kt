package com.example.lcb.app.ui.me

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lcb.app.BuildConfig
import com.example.lcb.app.R
import com.example.lcb.app.data.HomeData
import com.example.lcb.app.data.HydrateData
import com.example.lcb.app.data.WeightData
import com.example.lcb.app.ui.achievement.AchievementTotalCount
import com.example.lcb.app.ui.achievement.AchievementUiModel
import com.example.lcb.app.ui.achievement.buildAchievementModels
import com.example.lcb.app.ui.components.ChevronGlyph
import com.example.lcb.app.ui.components.NativeAdSlot
import com.example.lcb.app.ui.components.ScreenFrame
import com.example.lcb.app.ui.settings.languageLabel
import java.util.Locale

@Composable
fun MeScreen(
    language: String,
    homeData: HomeData,
    hydrateData: HydrateData,
    weightData: WeightData,
    onLanguageClick: () -> Unit,
    onDailyStepGoalClick: () -> Unit,
    onDailyWaterGoalClick: () -> Unit,
    onWeightGoalClick: () -> Unit,
    onFeedbackClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    onAchievementsClick: () -> Unit,
) {
    val unlockedAchievements = remember(homeData, hydrateData, weightData) {
        buildAchievementModels(homeData, hydrateData, weightData).filter { it.unlocked }
    }

    ScreenFrame(background = MePageBg) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 375.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(start = 16.dp, end = 16.dp, bottom = 104.dp)
                    .statusBarsPadding(),
            ) {
                Spacer(Modifier.height(24.dp))
                Text(
                    text = stringResource(R.string.nav_me),
                    color = Color.White,
                    fontSize = 22.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(26.dp))
                ProCard()
                Spacer(Modifier.height(16.dp))
                NativeAdSlot()
                Spacer(Modifier.height(16.dp))
                AchievementsCard(
                    unlockedAchievements = unlockedAchievements,
                    onClick = onAchievementsClick,
                )
                Spacer(Modifier.height(16.dp))
                GoalSettingsCard(
                    language = language,
                    homeData = homeData,
                    hydrateData = hydrateData,
                    weightData = weightData,
                    onLanguageClick = onLanguageClick,
                    onDailyStepGoalClick = onDailyStepGoalClick,
                    onDailyWaterGoalClick = onDailyWaterGoalClick,
                    onWeightGoalClick = onWeightGoalClick,
                    onFeedbackClick = onFeedbackClick,
                    onPrivacyClick = onPrivacyClick,
                )
            }
        }
    }
}

@Composable
private fun ProCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(94.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF176E6D), Color(0xFF157E6F), Color(0xFF1B6A52))))
            .padding(start = 16.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CrownGlyph(modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.me_fitness_lover),
                    color = Color.White,
                    fontSize = 18.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.me_pro_trial),
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 14.sp,
                lineHeight = 18.sp,
                maxLines = 2,
            )
        }
        Box(
            modifier = Modifier
                .width(73.dp)
                .height(38.dp)
                .clip(RoundedCornerShape(100.dp))
                .background(Brush.linearGradient(listOf(Color(0xFFF2D174), Color(0xFFE8BB5E)))),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.me_pro),
                color = Color(0xFF624706),
                fontSize = 16.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun AchievementsCard(
    unlockedAchievements: List<AchievementUiModel>,
    onClick: () -> Unit,
) {
    val unlockedCount = unlockedAchievements.size
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MeCardBg)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            MedalGlyph(modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.home_achievements),
                color = Color.White,
                fontSize = 14.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
            )
            Text(
                "$unlockedCount/$AchievementTotalCount",
                color = MeValueText,
                fontSize = 12.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight.Medium,
            )
            ChevronGlyph(color = MeValueText, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.height(18.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (unlockedAchievements.isEmpty()) Arrangement.Center else Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (unlockedAchievements.isEmpty()) {
                Text(
                    text = stringResource(R.string.achievement_unlocked_count, 0),
                    color = MeValueText,
                    fontSize = 12.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.Medium,
                )
            } else {
                unlockedAchievements.take(MaxMeAchievementPreviewCount).forEach { achievement ->
                    Image(
                        painter = painterResource(achievement.unlockedImageRes),
                        contentDescription = null,
                        modifier = Modifier.size(42.dp),
                    )
                }
                val remaining = unlockedAchievements.size - MaxMeAchievementPreviewCount
                if (remaining > 0) {
                    Text(
                        text = "+$remaining",
                        color = MeValueText,
                        fontSize = 12.sp,
                        lineHeight = 14.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}

@Composable
private fun GoalSettingsCard(
    language: String,
    homeData: HomeData,
    hydrateData: HydrateData,
    weightData: WeightData,
    onLanguageClick: () -> Unit,
    onDailyStepGoalClick: () -> Unit,
    onDailyWaterGoalClick: () -> Unit,
    onWeightGoalClick: () -> Unit,
    onFeedbackClick: () -> Unit,
    onPrivacyClick: () -> Unit,
) {
    val latestWeight = remember(weightData.weightRecords) {
        weightData.weightRecords.maxByOrNull { it.date }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MeCardBg)
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        SettingsRow(
            icon = { LanguageGlyph(modifier = Modifier.size(22.dp)) },
            label = stringResource(R.string.settings_language),
            value = languageLabel(language, stringResource(R.string.language_system)),
            onClick = onLanguageClick,
        )
        SettingsRow(
            icon = { StepGoalGlyph(modifier = Modifier.size(22.dp)) },
            label = stringResource(R.string.me_daily_step_goal),
            value = formatInt(homeData.stepGoal),
            onClick = onDailyStepGoalClick,
        )
        SettingsRow(
            icon = { WaterGoalGlyph(modifier = Modifier.size(22.dp)) },
            label = stringResource(R.string.me_daily_water_goal),
            value = stringResource(R.string.me_water_goal_value, formatInt(hydrateData.waterGoalMl)),
            onClick = onDailyWaterGoalClick,
        )
        SettingsRow(
            icon = { WeightGoalGlyph(modifier = Modifier.size(22.dp)) },
            label = stringResource(R.string.me_weight_goal),
            value = latestWeight?.let { stringResource(R.string.me_weight_goal_value, formatWeightKg(it.weightTenthsKg)) }
                ?: stringResource(R.string.home_weight_no_data),
            onClick = onWeightGoalClick,
        )
        SettingsRow(
            icon = { FeedbackGlyph(modifier = Modifier.size(22.dp)) },
            label = stringResource(R.string.settings_feedback),
            value = null,
            onClick = onFeedbackClick,
        )
        SettingsRow(
            icon = { PrivacyGlyph(modifier = Modifier.size(22.dp)) },
            label = stringResource(R.string.settings_privacy_policy),
            value = null,
            onClick = onPrivacyClick,
        )
        SettingsRow(
            icon = { VersionGlyph(modifier = Modifier.size(22.dp)) },
            label = stringResource(R.string.me_version),
            value = stringResource(R.string.me_version_value, BuildConfig.VERSION_NAME),
            onClick = null,
        )
    }
}

@Composable
private fun SettingsRow(
    icon: @Composable () -> Unit,
    label: String,
    value: String?,
    onClick: (() -> Unit)?,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icon()
        Spacer(Modifier.width(8.dp))
        Text(
            text = label,
            color = Color.White,
            fontSize = 14.sp,
            lineHeight = 16.sp,
            modifier = Modifier.weight(1f),
        )
        value?.let {
            Text(
                text = it,
                color = MeValueText,
                fontSize = 12.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight.Medium,
            )
        }
        if (onClick != null) {
            ChevronGlyph(color = MeValueText, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun CrownGlyph(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val path = Path().apply {
            moveTo(size.width * 0.1f, size.height * 0.34f)
            lineTo(size.width * 0.33f, size.height * 0.52f)
            lineTo(size.width * 0.5f, size.height * 0.22f)
            lineTo(size.width * 0.68f, size.height * 0.52f)
            lineTo(size.width * 0.92f, size.height * 0.34f)
            lineTo(size.width * 0.78f, size.height * 0.84f)
            lineTo(size.width * 0.2f, size.height * 0.84f)
            close()
        }
        drawPath(path, Color(0xFFEFC922))
    }
}

@Composable
private fun MedalGlyph(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        drawCircle(Color(0xFFEFC922), radius = size.minDimension * 0.42f, center = center)
        drawCircle(Color.White, radius = size.minDimension * 0.12f, center = center)
    }
}

@Composable
private fun LanguageGlyph(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        drawCircle(
            color = Color(0xFFB7F0E8),
            radius = size.minDimension * 0.42f,
            center = center,
            style = Stroke(width = size.minDimension * 0.08f),
        )
        drawLine(
            color = Color(0xFF10CEAC),
            start = Offset(size.width * 0.16f, size.height * 0.5f),
            end = Offset(size.width * 0.84f, size.height * 0.5f),
            strokeWidth = size.minDimension * 0.06f,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = Color(0xFF10CEAC),
            start = Offset(size.width * 0.5f, size.height * 0.14f),
            end = Offset(size.width * 0.5f, size.height * 0.86f),
            strokeWidth = size.minDimension * 0.06f,
            cap = StrokeCap.Round,
        )
        drawOval(
            color = Color(0xFF10CEAC),
            topLeft = Offset(size.width * 0.32f, size.height * 0.14f),
            size = Size(size.width * 0.36f, size.height * 0.72f),
            style = Stroke(width = size.minDimension * 0.06f),
        )
    }
}

@Composable
private fun StepGoalGlyph(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        drawCircle(Color(0xFFB7F0E8), radius = size.minDimension * 0.4f, style = Stroke(width = size.minDimension * 0.1f))
        drawLine(Color(0xFF10CEAC), center, Offset(size.width * 0.76f, size.height * 0.22f), strokeWidth = size.minDimension * 0.1f, cap = StrokeCap.Round)
    }
}

@Composable
private fun WaterGoalGlyph(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val drop = Path().apply {
            moveTo(size.width * 0.5f, size.height * 0.08f)
            cubicTo(size.width * 0.82f, size.height * 0.42f, size.width * 0.88f, size.height * 0.74f, size.width * 0.5f, size.height * 0.9f)
            cubicTo(size.width * 0.12f, size.height * 0.74f, size.width * 0.18f, size.height * 0.42f, size.width * 0.5f, size.height * 0.08f)
        }
        drawPath(drop, Color(0xFFBAFFF4), style = Stroke(width = size.minDimension * 0.08f, cap = StrokeCap.Round))
    }
}

@Composable
private fun WeightGoalGlyph(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        drawRoundRect(
            color = Color(0xFF9FF8EF),
            topLeft = Offset(size.width * 0.1f, size.height * 0.16f),
            size = Size(size.width * 0.8f, size.height * 0.68f),
            cornerRadius = CornerRadius(size.width * 0.16f),
            style = Stroke(width = size.minDimension * 0.08f),
        )
        drawCircle(Color(0xFF10CEAC), radius = size.minDimension * 0.08f, center = Offset(size.width * 0.5f, size.height * 0.36f))
    }
}

@Composable
private fun FeedbackGlyph(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        drawRoundRect(
            color = Color(0xFFB7F0E8),
            topLeft = Offset(size.width * 0.12f, size.height * 0.16f),
            size = Size(size.width * 0.76f, size.height * 0.58f),
            cornerRadius = CornerRadius(size.width * 0.16f),
            style = Stroke(width = size.minDimension * 0.08f),
        )
        val tail = Path().apply {
            moveTo(size.width * 0.42f, size.height * 0.74f)
            lineTo(size.width * 0.34f, size.height * 0.88f)
            lineTo(size.width * 0.56f, size.height * 0.74f)
        }
        drawPath(tail, Color(0xFFB7F0E8), style = Stroke(width = size.minDimension * 0.08f, cap = StrokeCap.Round))
        drawCircle(Color(0xFF10CEAC), radius = size.minDimension * 0.035f, center = Offset(size.width * 0.36f, size.height * 0.44f))
        drawCircle(Color(0xFF10CEAC), radius = size.minDimension * 0.035f, center = Offset(size.width * 0.5f, size.height * 0.44f))
        drawCircle(Color(0xFF10CEAC), radius = size.minDimension * 0.035f, center = Offset(size.width * 0.64f, size.height * 0.44f))
    }
}

@Composable
private fun PrivacyGlyph(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        drawRoundRect(
            color = Color(0xFFB7F0E8),
            topLeft = Offset(size.width * 0.22f, size.height * 0.1f),
            size = Size(size.width * 0.56f, size.height * 0.8f),
            cornerRadius = CornerRadius(size.width * 0.08f),
            style = Stroke(width = size.minDimension * 0.08f),
        )
        drawLine(Color(0xFF10CEAC), Offset(size.width * 0.34f, size.height * 0.3f), Offset(size.width * 0.66f, size.height * 0.3f), strokeWidth = size.minDimension * 0.06f)
        drawLine(Color(0xFF10CEAC), Offset(size.width * 0.34f, size.height * 0.48f), Offset(size.width * 0.66f, size.height * 0.48f), strokeWidth = size.minDimension * 0.06f)
    }
}

@Composable
private fun VersionGlyph(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        drawCircle(Color(0xFFB7F0E8), radius = size.minDimension * 0.4f, style = Stroke(width = size.minDimension * 0.08f))
        drawCircle(Color(0xFF10CEAC), radius = size.minDimension * 0.04f, center = Offset(size.width * 0.5f, size.height * 0.3f))
        drawLine(Color(0xFF10CEAC), Offset(size.width * 0.5f, size.height * 0.44f), Offset(size.width * 0.5f, size.height * 0.7f), strokeWidth = size.minDimension * 0.08f, cap = StrokeCap.Round)
    }
}

private fun formatInt(value: Int): String {
    return String.format(Locale.US, "%,d", value)
}

private fun formatWeightKg(weightTenthsKg: Int): String {
    return if (weightTenthsKg % 10 == 0) {
        (weightTenthsKg / 10).toString()
    } else {
        String.format(Locale.US, "%.1f", weightTenthsKg / 10f)
    }
}

private val MePageBg = Color(0xFF111113)
private val MeCardBg = Color(0xFF1C1C22)
private val MeValueText = Color(0xFF666666)
private const val MaxMeAchievementPreviewCount = 6
