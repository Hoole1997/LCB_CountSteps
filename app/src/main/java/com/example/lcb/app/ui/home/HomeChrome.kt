package com.example.lcb.app.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lcb.app.R
import com.example.lcb.app.data.StepSensorStatus

@Composable
internal fun HomeNavBar(onSettings: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.home_title),
            color = Color.White,
            fontSize = 22.sp,
            lineHeight = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
        )
        Box(
            modifier = Modifier
                .size(44.dp)
                .clickable(onClick = onSettings),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.home_dark_settings),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
internal fun SensorStateBanner(status: StepSensorStatus, onRequestPermission: () -> Unit) {
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
            .background(Color(0xFF292323))
            .clickable(enabled = status == StepSensorStatus.PermissionRequired, onClick = onRequestPermission)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(message, fontSize = 12.sp, color = Color(0xFFFFC56F), modifier = Modifier.weight(1f))
        if (status == StepSensorStatus.PermissionRequired) {
            Text(stringResource(R.string.home_allow), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        }
    }
}

@Composable
internal fun DarkBottomBar(
    onHome: () -> Unit,
    onData: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(TabBg)
            .navigationBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(49.dp),
            verticalAlignment = Alignment.Top,
        ) {
            DarkTabItem(
                label = stringResource(R.string.nav_home),
                iconRes = R.drawable.home_dark_tab_home,
                color = Color.White,
                modifier = Modifier.weight(1f),
                onClick = onHome,
            )
            DarkTabItem(
                label = stringResource(R.string.nav_trends),
                iconRes = R.drawable.home_dark_tab_data,
                color = TextDim,
                modifier = Modifier.weight(1f),
                onClick = onData,
            )
        }
    }
}

@Composable
private fun DarkTabItem(
    label: String,
    iconRes: Int,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .height(49.dp)
            .clickable(onClick = onClick)
            .padding(top = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(painter = painterResource(iconRes), contentDescription = null, modifier = Modifier.size(24.dp))
        Spacer(Modifier.height(2.dp))
        Text(label, color = color, fontSize = 11.sp, lineHeight = 13.sp, fontWeight = if (color == Color.White) FontWeight.Medium else FontWeight.Normal)
    }
}
