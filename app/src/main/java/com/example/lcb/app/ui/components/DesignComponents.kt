package com.example.lcb.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lcb.app.R
import com.example.lcb.app.ui.theme.LcbCardGray
import com.example.lcb.app.ui.theme.LcbHydrateBg
import com.example.lcb.app.ui.theme.LcbPrimary
import com.example.lcb.app.ui.theme.LcbTextHeading
import com.example.lcb.app.ui.theme.LcbTextPrimary
import com.example.lcb.app.ui.theme.LcbTextSecondary
import com.example.lcb.app.ui.theme.LcbTextTertiary
import kotlin.math.max

enum class TabDestination { Home, Data }

@Composable
fun ScreenFrame(
    background: Color,
    bottomBar: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.TopCenter),
        ) {
            content()
        }
        bottomBar?.let {
            Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                it()
            }
        }
    }
}

@Composable
fun AppBottomBar(
    selected: TabDestination,
    onHome: () -> Unit,
    onData: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, ambientColor = Color(0x14000000), spotColor = Color(0x14000000))
            .background(Color.White)
            .navigationBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(49.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BottomTabItem(
                label = stringResource(R.string.nav_home),
                selected = selected == TabDestination.Home,
                onClick = onHome,
            ) {
                HomeGlyph(it)
            }
            BottomTabItem(
                label = stringResource(R.string.nav_data),
                selected = selected == TabDestination.Data,
                onClick = onData,
            ) {
                DataGlyph(it)
            }
        }
    }
}

@Composable
private fun BottomTabItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable (Color) -> Unit,
) {
    val color = if (selected) LcbTextPrimary else LcbTextTertiary
    Column(
        modifier = Modifier
            .width(84.dp)
            .height(44.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        icon(color)
        Spacer(Modifier.height(2.dp))
        Text(label, fontSize = 11.sp, fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal, color = color)
    }
}

@Composable
fun RingProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 16.dp,
) {
    Canvas(modifier = modifier) {
        val strokePx = strokeWidth.toPx()
        val size = Size(size.width - strokePx, size.height - strokePx)
        val topLeft = Offset(strokePx / 2f, strokePx / 2f)
        drawArc(
            color = Color(0x80EBEBEB),
            startAngle = 140f,
            sweepAngle = 260f,
            useCenter = false,
            topLeft = topLeft,
            size = size,
            style = Stroke(strokePx, cap = StrokeCap.Round),
        )
        drawArc(
            color = LcbPrimary,
            startAngle = 140f,
            sweepAngle = 260f * progress.coerceIn(0f, 1f),
            useCenter = false,
            topLeft = topLeft,
            size = size,
            style = Stroke(strokePx, cap = StrokeCap.Round),
        )
    }
}

@Composable
fun DataCard(
    icon: MetricIcon,
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    background: Color = LcbCardGray,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(66.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(background)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White),
            contentAlignment = Alignment.Center,
        ) {
            FigmaMetricGlyph(icon)
        }
        Spacer(Modifier.width(8.dp))
        Column(verticalArrangement = Arrangement.Center) {
            Text(title, fontSize = 12.sp, color = LcbTextHeading)
            Spacer(Modifier.height(4.dp))
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = LcbTextPrimary)
        }
        Spacer(Modifier.weight(1f))
        if (onClick != null) ChevronGlyph(color = Color(0xFFCCCCCC))
    }
}

enum class MetricIcon { Distance, Steps, Calories, Hydrate }

@Composable
private fun FigmaMetricGlyph(icon: MetricIcon) {
    val iconRes = when (icon) {
        MetricIcon.Distance -> R.drawable.ic_figma_distance
        MetricIcon.Steps -> R.drawable.ic_figma_steps
        MetricIcon.Calories -> R.drawable.ic_figma_kcal
        MetricIcon.Hydrate -> null
    }
    if (iconRes == null) {
        MetricGlyph(icon)
    } else {
        Image(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
        )
    }
}

@Composable
fun FigmaSettingsGlyph(modifier: Modifier = Modifier.size(20.dp)) {
    Image(
        painter = painterResource(R.drawable.ic_figma_settings),
        contentDescription = null,
        modifier = modifier,
    )
}

@Composable
fun MetricGlyph(icon: MetricIcon, modifier: Modifier = Modifier.size(24.dp)) {
    Canvas(modifier = modifier) {
        when (icon) {
            MetricIcon.Distance -> {
                drawCircle(Color(0xFF2274E2), radius = size.minDimension * 0.3f, center = Offset(size.width * 0.5f, size.height * 0.42f))
                drawRoundRect(Color(0x332274E2), topLeft = Offset(size.width * 0.08f, size.height * 0.62f), size = Size(size.width * 0.84f, size.height * 0.22f), cornerRadius = CornerRadius(16f))
                drawCircle(Color.White, radius = size.minDimension * 0.08f, center = Offset(size.width * 0.5f, size.height * 0.42f))
            }
            MetricIcon.Steps -> {
                drawOval(LcbPrimary, topLeft = Offset(size.width * 0.12f, size.height * 0.42f), size = Size(size.width * 0.44f, size.height * 0.32f))
                drawOval(LcbPrimary, topLeft = Offset(size.width * 0.44f, size.height * 0.24f), size = Size(size.width * 0.42f, size.height * 0.32f))
                drawCircle(LcbPrimary, radius = size.minDimension * 0.09f, center = Offset(size.width * 0.58f, size.height * 0.14f))
            }
            MetricIcon.Calories -> {
                val path = Path().apply {
                    moveTo(size.width * 0.5f, size.height * 0.05f)
                    cubicTo(size.width * 0.9f, size.height * 0.35f, size.width * 0.75f, size.height * 0.92f, size.width * 0.5f, size.height * 0.94f)
                    cubicTo(size.width * 0.2f, size.height * 0.9f, size.width * 0.12f, size.height * 0.52f, size.width * 0.34f, size.height * 0.28f)
                    cubicTo(size.width * 0.42f, size.height * 0.2f, size.width * 0.46f, size.height * 0.12f, size.width * 0.5f, size.height * 0.05f)
                }
                drawPath(path, Color(0xFFFF2F78))
            }
            MetricIcon.Hydrate -> {
                val path = Path().apply {
                    moveTo(size.width * 0.5f, size.height * 0.08f)
                    cubicTo(size.width * 0.82f, size.height * 0.42f, size.width * 0.88f, size.height * 0.72f, size.width * 0.5f, size.height * 0.92f)
                    cubicTo(size.width * 0.12f, size.height * 0.72f, size.width * 0.18f, size.height * 0.42f, size.width * 0.5f, size.height * 0.08f)
                }
                drawPath(path, Color(0xFF2274E2))
                drawRoundRect(Color(0x5581B0EE), topLeft = Offset(size.width * 0.2f, size.height * 0.66f), size = Size(size.width * 0.6f, size.height * 0.14f), cornerRadius = CornerRadius(12f))
            }
        }
    }
}

@Composable
fun HomeGlyph(color: Color, modifier: Modifier = Modifier.size(24.dp)) {
    Canvas(modifier = modifier) {
        val path = Path().apply {
            moveTo(size.width * 0.1f, size.height * 0.48f)
            lineTo(size.width * 0.5f, size.height * 0.14f)
            lineTo(size.width * 0.9f, size.height * 0.48f)
            lineTo(size.width * 0.82f, size.height * 0.48f)
            lineTo(size.width * 0.82f, size.height * 0.88f)
            lineTo(size.width * 0.6f, size.height * 0.88f)
            lineTo(size.width * 0.6f, size.height * 0.62f)
            lineTo(size.width * 0.4f, size.height * 0.62f)
            lineTo(size.width * 0.4f, size.height * 0.88f)
            lineTo(size.width * 0.18f, size.height * 0.88f)
            lineTo(size.width * 0.18f, size.height * 0.48f)
            close()
        }
        drawPath(path, color)
    }
}

@Composable
fun DataGlyph(color: Color, modifier: Modifier = Modifier.size(24.dp)) {
    Canvas(modifier = modifier) {
        val width = size.width * 0.16f
        listOf(0.32f, 0.58f, 0.82f).forEachIndexed { index, x ->
            val h = listOf(0.45f, 0.68f, 0.34f)[index] * size.height
            drawRoundRect(
                color = color,
                topLeft = Offset(size.width * x - width / 2f, size.height * 0.86f - h),
                size = Size(width, h),
                cornerRadius = CornerRadius(width, width),
            )
        }
    }
}

@Composable
fun SettingsGlyph(color: Color, modifier: Modifier = Modifier.size(22.dp)) {
    Canvas(modifier = modifier) {
        drawCircle(color, radius = size.minDimension * 0.36f, style = Stroke(width = size.minDimension * 0.13f))
        drawCircle(color, radius = size.minDimension * 0.12f)
        for (i in 0 until 8) {
            val angle = Math.toRadians((i * 45).toDouble())
            val start = Offset(
                x = center.x + kotlin.math.cos(angle).toFloat() * size.minDimension * 0.42f,
                y = center.y + kotlin.math.sin(angle).toFloat() * size.minDimension * 0.42f,
            )
            val end = Offset(
                x = center.x + kotlin.math.cos(angle).toFloat() * size.minDimension * 0.5f,
                y = center.y + kotlin.math.sin(angle).toFloat() * size.minDimension * 0.5f,
            )
            drawLine(color, start, end, strokeWidth = size.minDimension * 0.08f, cap = StrokeCap.Round)
        }
    }
}

@Composable
fun BackGlyph(color: Color = LcbTextHeading, modifier: Modifier = Modifier.size(24.dp)) {
    Canvas(modifier = modifier) {
        drawLine(color, Offset(size.width * 0.68f, size.height * 0.18f), Offset(size.width * 0.28f, size.height * 0.5f), strokeWidth = 3f, cap = StrokeCap.Round)
        drawLine(color, Offset(size.width * 0.28f, size.height * 0.5f), Offset(size.width * 0.68f, size.height * 0.82f), strokeWidth = 3f, cap = StrokeCap.Round)
    }
}

@Composable
fun ChevronGlyph(color: Color = LcbTextTertiary, modifier: Modifier = Modifier.size(18.dp)) {
    Canvas(modifier = modifier) {
        drawLine(color, Offset(size.width * 0.35f, size.height * 0.22f), Offset(size.width * 0.65f, size.height * 0.5f), strokeWidth = 3f, cap = StrokeCap.Round)
        drawLine(color, Offset(size.width * 0.65f, size.height * 0.5f), Offset(size.width * 0.35f, size.height * 0.78f), strokeWidth = 3f, cap = StrokeCap.Round)
    }
}

@Composable
fun BackButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        BackGlyph()
    }
}

@Composable
fun BarChart(
    values: List<Int>,
    labels: List<String>,
    modifier: Modifier,
    barWidth: Dp,
    selectedIndex: Int? = null,
) {
    val maxValue = max(values.maxOrNull() ?: 0, 1)
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom,
        ) {
            values.forEachIndexed { index, value ->
                val fraction = (value.toFloat() / maxValue).coerceIn(0.04f, 1f)
                Box(
                    modifier = Modifier
                        .width(barWidth)
                        .fillMaxHeight(fraction)
                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                        .background(if (selectedIndex == index) LcbPrimary else Color(0xFF43E0C4)),
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            labels.forEach {
                Text(it, modifier = Modifier.width(36.dp), fontSize = 12.sp, color = Color(0xFF8F8F8F), textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
fun EmptyChartGrid(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val lines = 7
        repeat(lines) { index ->
            val y = size.height * index / (lines - 1)
            drawLine(Color(0xFFF4F4F4), Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
        }
    }
}

@Composable
fun HydrateCard(
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DataCard(
        icon = MetricIcon.Hydrate,
        title = stringResource(R.string.hydrate_title),
        value = value,
        modifier = modifier,
        background = LcbHydrateBg,
        onClick = onClick,
    )
}
