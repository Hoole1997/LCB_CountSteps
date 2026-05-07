package com.example.lcb.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val LcbPrimary = Color(0xFF10CEAC)
val LcbPrimaryBar = Color(0xFF43E0C4)
val LcbTextPrimary = Color(0xFF222222)
val LcbTextHeading = Color(0xFF333333)
val LcbTextSecondary = Color(0xFF666666)
val LcbTextTertiary = Color(0xFF999999)
val LcbCardGray = Color(0xFFF5F5F5)
val LcbPageGray = Color(0xFFF6F5F4)
val LcbSettingsBg = Color(0xFFF9F9FA)
val LcbHydrateBg = Color(0xFFF2F7FF)

private val LcbColorScheme = lightColorScheme(
    primary = LcbPrimary,
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onBackground = LcbTextPrimary,
    onSurface = LcbTextPrimary,
)

@Composable
fun LcbTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LcbColorScheme,
        typography = MaterialTheme.typography,
        content = content,
    )
}
