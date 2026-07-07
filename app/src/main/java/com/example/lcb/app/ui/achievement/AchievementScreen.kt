package com.example.lcb.app.ui.achievement

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lcb.app.R
import com.example.lcb.app.data.HomeData
import com.example.lcb.app.data.HydrateData
import com.example.lcb.app.data.WeightData
import com.example.lcb.app.ui.components.NativeAdSlot
import com.example.lcb.app.ui.theme.LcbDarkPage

@Composable
fun AchievementScreen(
    homeData: HomeData,
    hydrateData: HydrateData,
    weightData: WeightData,
    onBack: () -> Unit,
) {
    val achievements = remember(homeData, hydrateData, weightData) {
        buildAchievementModels(homeData, hydrateData, weightData)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LcbDarkPage),
        contentAlignment = Alignment.TopCenter,
    ) {
        AchievementHeaderBackground()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 375.dp)
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 48.dp),
        ) {
            AchievementTopBar(onBack = onBack)
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.achievement_badge_title),
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                fontSize = 24.sp,
                lineHeight = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(10.dp))
            Text(
            text = stringResource(R.string.achievement_badges_total, AchievementTotalCount),
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                fontSize = 14.sp,
                lineHeight = 22.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(18.dp))
            NativeAdSlot(modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(Modifier.height(24.dp))
            AchievementGrid(
                achievements = achievements,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
    }
}

@Composable
private fun AchievementHeaderBackground() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(578.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF1F3A35).copy(alpha = 0.74f), Color.Transparent),
                    center = Offset(size.width * 0.22f, size.height * 0.02f),
                    radius = size.width * 0.9f,
                ),
            )
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF233D44).copy(alpha = 0.6f), Color.Transparent),
                    center = Offset(size.width * 0.88f, size.height * 0.02f),
                    radius = size.width * 0.78f,
                ),
            )
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0x3310CEAC), Color.Transparent),
                    startY = 0f,
                    endY = size.height * 0.7f,
                ),
            )
        }
        Image(
            painter = painterResource(R.drawable.achievement_bg_light),
            contentDescription = null,
            modifier = Modifier
                .width(374.dp)
                .height(452.dp)
                .offset(y = (-146).dp),
            alpha = 0.23f,
            contentScale = ContentScale.FillBounds,
        )
        Image(
            painter = painterResource(R.drawable.achievement_bg_badge),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 34.dp)
                .offset(x = 10.dp)
                .size(168.dp),
            alpha = 0.12f,
            contentScale = ContentScale.Fit,
        )
    }
}

@Composable
private fun AchievementTopBar(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Canvas(
            modifier = Modifier
                .padding(start = 16.dp)
                .size(24.dp)
                .clickable(onClick = onBack),
        ) {
            drawLine(Color.White, Offset(size.width * 0.62f, size.height * 0.24f), Offset(size.width * 0.36f, size.height * 0.5f), 2.1.dp.toPx(), StrokeCap.Round)
            drawLine(Color.White, Offset(size.width * 0.36f, size.height * 0.5f), Offset(size.width * 0.62f, size.height * 0.76f), 2.1.dp.toPx(), StrokeCap.Round)
        }
    }
}

@Composable
private fun AchievementGrid(
    achievements: List<AchievementUiModel>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        achievements.chunked(3).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                rowItems.forEach { achievement ->
                    AchievementItem(model = achievement)
                }
            }
        }
    }
}

@Composable
private fun AchievementItem(model: AchievementUiModel) {
    Column(
        modifier = Modifier.width(80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(if (model.unlocked) model.unlockedImageRes else model.lockedImageRes),
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            contentScale = ContentScale.Fit,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = stringResource(model.nameRes),
            color = Color.White,
            fontSize = 12.sp,
            lineHeight = 14.sp,
            textAlign = TextAlign.Center,
            maxLines = 2,
            modifier = Modifier.width(71.dp),
        )
    }
}
