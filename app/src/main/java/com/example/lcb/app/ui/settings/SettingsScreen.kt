package com.example.lcb.app.ui.settings

import androidx.annotation.StringRes
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lcb.app.BuildConfig
import com.example.lcb.app.R
import com.example.lcb.app.ui.components.BackButton
import com.example.lcb.app.ui.components.ChevronGlyph
import com.example.lcb.app.ui.components.ScreenFrame
import com.example.lcb.app.ui.theme.LcbSettingsBg
import com.example.lcb.app.ui.theme.LcbTextHeading
import com.example.lcb.app.ui.theme.LcbTextPrimary
import com.example.lcb.app.ui.theme.LcbTextSecondary
import com.example.lcb.app.ui.theme.LcbTextTertiary

@Composable
fun SettingsScreen(
    language: String,
    onBack: () -> Unit,
    onLanguageSelected: (String) -> Unit,
    onFeedback: () -> Unit,
    onPrivacyPolicy: () -> Unit,
) {
    var languageSheetVisible by remember { mutableStateOf(false) }
    val currentLanguage = remember(language) { languageOptions.firstOrNull { it.code == language } ?: languageOptions.first() }
    ScreenFrame(background = LcbSettingsBg) {
        Column(modifier = Modifier.fillMaxSize()) {
            SettingsTopBar(onBack)
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White),
            ) {
                SettingRow(
                    title = stringResource(R.string.settings_language),
                    value = stringResource(currentLanguage.labelRes),
                    onClick = { languageSheetVisible = true },
                )
                DividerLine()
                SettingRow(stringResource(R.string.settings_feedback), null, onClick = onFeedback)
                DividerLine()
                SettingRow(stringResource(R.string.settings_privacy_policy), null, onClick = onPrivacyPolicy)
            }
            Spacer(Modifier.weight(1f))
            Text(
                "V${BuildConfig.VERSION_NAME.substringBefore("-")}",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 112.dp),
                fontSize = 14.sp,
                color = LcbTextHeading,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
        if (languageSheetVisible) {
            LanguageSheet(
                current = currentLanguage.code,
                onCancel = { languageSheetVisible = false },
                onConfirm = { languageCode ->
                    onLanguageSelected(languageCode)
                    languageSheetVisible = false
                },
            )
        }
    }
}

@Composable
private fun SettingsTopBar(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(88.dp)
            .background(Color.White)
            .statusBarsPadding(),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .height(44.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            BackButton(onBack)
        }
        Text(
            stringResource(R.string.settings_title),
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 13.dp),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = LcbTextHeading,
        )
    }
}

@Composable
private fun SettingRow(title: String, value: String?, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(69.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, fontSize = 16.sp, color = LcbTextHeading, modifier = Modifier.weight(1f))
        if (value != null) {
            Text(value, fontSize = 14.sp, color = LcbTextTertiary)
            Spacer(Modifier.size(8.dp))
        }
        ChevronGlyph(color = Color(0xFFCCCCCC))
    }
}

@Composable
private fun DividerLine() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .padding(start = 16.dp)
            .background(Color(0xFFF2F2F2)),
    )
}

@Composable
private fun LanguageSheet(
    current: String,
    onCancel: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var selected by remember(current) { mutableStateOf(current) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x994D4D4D))
            .clickable(onClick = onCancel),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(596.dp)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(Color.White)
                .clickable(onClick = {}),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .size(width = 66.dp, height = 8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFEAEAEA)),
            )
            Spacer(Modifier.height(24.dp))
            Text(
                stringResource(R.string.settings_select_language),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = LcbTextHeading,
            )
            Spacer(Modifier.height(22.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                languageOptions.forEach { language ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clickable { selected = language.code }
                            .padding(horizontal = 30.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            stringResource(language.labelRes),
                            fontSize = 16.sp,
                            fontWeight = if (selected == language.code) FontWeight.Medium else FontWeight.Normal,
                            color = if (selected == language.code) LcbTextPrimary else LcbTextSecondary,
                            modifier = Modifier.weight(1f),
                        )
                        if (selected == language.code) {
                            Text("✓", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = LcbTextPrimary)
                        }
                    }
                }
            }
            Spacer(Modifier.weight(1f))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(102.dp)
                    .background(Color.White)
                    .padding(horizontal = 30.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF5F5F5))
                        .clickable(onClick = onCancel),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(stringResource(R.string.settings_cancel), fontSize = 16.sp, fontWeight = FontWeight.Medium, color = LcbTextSecondary)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(LcbTextPrimary)
                        .clickable { onConfirm(selected) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(stringResource(R.string.settings_confirm), fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.White)
                }
            }
        }
    }
}

private data class LanguageOption(
    val code: String,
    @param:StringRes val labelRes: Int,
)

private val languageOptions = listOf(
    LanguageOption("en", R.string.language_english),
    LanguageOption("de", R.string.language_german),
    LanguageOption("es", R.string.language_spanish),
    LanguageOption("fr", R.string.language_french),
    LanguageOption("pt", R.string.language_portuguese),
    LanguageOption("ja", R.string.language_japanese),
    LanguageOption("ko", R.string.language_korean),
    LanguageOption("zh-Hans", R.string.language_simplified_chinese),
)
