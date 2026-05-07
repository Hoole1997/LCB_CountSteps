package com.example.lcb.app.ui.hydrate

import android.widget.ImageView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.bumptech.glide.Glide
import com.example.lcb.app.R
import com.example.lcb.app.data.HydrateData
import com.example.lcb.app.data.HydrationRecord
import com.example.lcb.app.data.localDate
import com.example.lcb.app.ui.currentAppLocale
import com.example.lcb.app.ui.components.BackButton
import com.example.lcb.app.ui.components.MetricGlyph
import com.example.lcb.app.ui.components.MetricIcon
import com.example.lcb.app.ui.components.ScreenFrame
import com.example.lcb.app.ui.theme.LcbCardGray
import com.example.lcb.app.ui.theme.LcbPrimary
import com.example.lcb.app.ui.theme.LcbTextHeading
import com.example.lcb.app.ui.theme.LcbTextPrimary
import com.example.lcb.app.ui.theme.LcbTextSecondary
import com.example.lcb.app.ui.theme.LcbTextTertiary
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.min

private const val WaterRecordPageSize = 20

@Composable
fun HydrateScreen(
    data: HydrateData,
    onBack: () -> Unit,
    onDrink: (Int) -> Unit,
) {
    val today = LocalDate.parse(data.today)
    val todayRecords = remember(data.hydrationRecords, today) {
        data.hydrationRecords.filter { it.localDate() == today }
    }
    val total = remember(todayRecords) { todayRecords.sumOf { it.amountMl } }
    val locale = currentAppLocale()
    val listState = rememberLazyListState()
    var visibleRecordCount by remember(today) { mutableIntStateOf(WaterRecordPageSize) }
    val visibleRecords = remember(todayRecords, visibleRecordCount) {
        todayRecords.take(visibleRecordCount)
    }
    val canLoadMoreRecords = visibleRecordCount < todayRecords.size

    LaunchedEffect(listState, canLoadMoreRecords, visibleRecordCount, todayRecords.size) {
        if (!canLoadMoreRecords) return@LaunchedEffect
        snapshotFlow {
            val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = listState.layoutInfo.totalItemsCount
            totalItems > 0 && lastVisibleIndex >= totalItems - 4
        }
            .distinctUntilChanged()
            .filter { it }
            .collect {
                visibleRecordCount = min(visibleRecordCount + WaterRecordPageSize, todayRecords.size)
            }
    }

    ScreenFrame(background = Color.White) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(bottom = 28.dp),
        ) {
            item { HydrateTopBar(onBack) }
            item { WeekSelector(today, locale) }
            item { Spacer(Modifier.height(30.dp)) }
            item { HydrateProgress(total) }
            item { Spacer(Modifier.height(18.dp)) }
            item {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 30.dp)
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(LcbTextPrimary)
                        .clickable { onDrink(data.waterQuickAmountMl) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(stringResource(R.string.hydrate_drink_amount, data.waterQuickAmountMl), fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.White)
                }
            }
            item { Spacer(Modifier.height(32.dp)) }
            item {
                Text(
                    stringResource(R.string.hydrate_record),
                    modifier = Modifier.padding(horizontal = 16.dp),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = LcbTextHeading,
                )
            }
            item { Spacer(Modifier.height(14.dp)) }
            if (todayRecords.isEmpty()) {
                item { EmptyWaterState() }
            } else {
                items(visibleRecords, key = { it.id }) { record ->
                    WaterRecordRow(
                        record = record,
                        locale = locale,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    )
                }
                if (canLoadMoreRecords) {
                    item(key = "water-record-pagination-sentinel") {
                        Spacer(Modifier.height(1.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun HydrateTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BackButton(onBack)
        Text(
            stringResource(R.string.hydrate_title),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = LcbTextHeading,
            modifier = Modifier.padding(start = 6.dp),
        )
    }
}

@Composable
private fun WeekSelector(today: LocalDate, locale: Locale) {
    val start = today.minusDays((today.dayOfWeek.value - 1).toLong())
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        (0..6).forEach { index ->
            val day = start.plusDays(index.toLong())
            val selected = day == today
            Column(
                modifier = Modifier
                    .width(44.dp)
                    .height(58.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (selected) LcbTextPrimary else LcbCardGray),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    day.dayOfMonth.toString().padStart(2, '0'),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (selected) Color.White else LcbTextHeading,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    day.format(DateTimeFormatter.ofPattern("EEE", locale)),
                    fontSize = 12.sp,
                    color = LcbTextTertiary,
                )
            }
        }
    }
}

@Composable
private fun HydrateProgress(total: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(total.toString(), fontSize = 32.sp, fontWeight = FontWeight.Bold, color = LcbTextHeading)
            Spacer(Modifier.width(4.dp))
            Text(stringResource(R.string.hydrate_unit_ml), fontSize = 14.sp, color = LcbTextSecondary, modifier = Modifier.padding(bottom = 5.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(
            if (total == 0) stringResource(R.string.hydrate_empty_message) else stringResource(R.string.hydrate_keep_message),
            fontSize = 14.sp,
            color = LcbTextTertiary,
        )
        Spacer(Modifier.height(14.dp))
        WaterCupGif()
    }
}

@Composable
private fun WaterCupGif() {
    AndroidView(
        modifier = Modifier.size(width = 240.dp, height = 270.dp),
        factory = { context ->
            ImageView(context).apply {
                adjustViewBounds = true
                scaleType = ImageView.ScaleType.FIT_CENTER
                Glide.with(this)
                    .asGif()
                    .load(R.drawable.water_cup)
                    .into(this)
            }
        },
        update = {},
        onRelease = { imageView ->
            Glide.with(imageView).clear(imageView)
            imageView.setImageDrawable(null)
        },
    )
}

@Composable
private fun EmptyWaterState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Canvas(modifier = Modifier.size(width = 126.dp, height = 55.dp)) {
            drawOval(
                Brush.horizontalGradient(listOf(Color(0xFFEDEDED), Color(0xFFF9F9F9))),
                topLeft = Offset(0f, size.height * 0.45f),
                size = Size(size.width, size.height * 0.55f),
            )
            val path = Path().apply {
                moveTo(size.width * 0.5f, 0f)
                cubicTo(size.width * 0.85f, size.height * 0.42f, size.width * 0.74f, size.height * 0.78f, size.width * 0.5f, size.height * 0.82f)
                cubicTo(size.width * 0.26f, size.height * 0.78f, size.width * 0.15f, size.height * 0.42f, size.width * 0.5f, 0f)
            }
            drawPath(path, Color(0xFF333333))
        }
        Spacer(Modifier.height(14.dp))
        Text(stringResource(R.string.hydrate_no_record), fontSize = 12.sp, color = LcbTextTertiary, textAlign = TextAlign.Center)
    }
}

@Composable
private fun WaterRecordRow(
    record: HydrationRecord,
    locale: Locale,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(73.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF9F9F9))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(32.dp),
            contentAlignment = Alignment.Center,
        ) {
            MetricGlyph(MetricIcon.Hydrate, Modifier.size(28.dp))
        }
        Spacer(Modifier.width(8.dp))
        Column {
            Text(stringResource(R.string.hydrate_record_amount, record.amountMl), fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = LcbTextPrimary)
            Spacer(Modifier.height(4.dp))
            Text(
                formatTime(record.timestamp, locale),
                fontSize = 11.sp,
                color = LcbTextTertiary,
            )
        }
    }
}

private fun formatTime(timestamp: Long, locale: Locale): String {
    return Instant.ofEpochMilli(timestamp)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", locale))
}
