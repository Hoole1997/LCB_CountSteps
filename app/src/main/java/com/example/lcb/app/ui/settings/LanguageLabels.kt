package com.example.lcb.app.ui.settings

import android.content.Context
import com.example.lcb.app.R
import com.example.lcb.app.ui.SystemLanguageCode

fun languageLabel(languageCode: String, systemLabel: String): String {
    if (languageCode == SystemLanguageCode) return systemLabel
    return languageAutonymOptions.firstOrNull { it.code == languageCode }?.label ?: languageAutonymOptions.first().label
}

internal data class LanguageOption(val code: String, val label: String)

internal fun languageOptions(context: Context): List<LanguageOption> {
    return listOf(LanguageOption(SystemLanguageCode, context.getString(R.string.language_system))) + languageAutonymOptions
}

// Language names intentionally use autonyms so users can recognize their language in any locale.
private val languageAutonymOptions = listOf(
    LanguageOption("en", "English"),
    LanguageOption("de", "Deutsch"),
    LanguageOption("es", "Español"),
    LanguageOption("fr", "Français"),
    LanguageOption("pt", "Português"),
    LanguageOption("ja", "日本語"),
    LanguageOption("ko", "한국어"),
    LanguageOption("zh-Hans", "简体中文"),
)
