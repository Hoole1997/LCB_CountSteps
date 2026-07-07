package com.example.lcb.app.ui

import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import java.util.Locale

const val SystemLanguageCode = "system"

fun localeForLanguageCode(languageCode: String): Locale {
    val tag = when (languageCode) {
        "zh-Hans" -> "zh-CN"
        else -> languageCode
    }
    return Locale.forLanguageTag(tag)
}

fun resolveAppLanguageCode(languageCode: String, configuration: Configuration): String {
    if (languageCode != SystemLanguageCode) return languageCode
    val locales = configuration.locales
    for (index in 0 until locales.size()) {
        val supported = supportedLanguageCode(locales[index])
        if (supported != null) return supported
    }
    return supportedLanguageCode(Locale.getDefault()) ?: "en"
}

fun Context.localizedContext(languageCode: String): Context {
    val locale = localeForLanguageCode(resolveAppLanguageCode(languageCode, resources.configuration))
    val configuration = Configuration(resources.configuration).apply {
        setLocales(LocaleList(locale))
    }
    return createConfigurationContext(configuration)
}

fun createLocalizedConfiguration(base: Configuration, languageCode: String): Configuration {
    val locale = localeForLanguageCode(resolveAppLanguageCode(languageCode, base))
    return Configuration(base).apply {
        setLocales(LocaleList(locale))
    }
}

@Composable
fun currentAppLocale(): Locale {
    val locales = LocalConfiguration.current.locales
    return locales[0] ?: Locale.getDefault()
}

private fun supportedLanguageCode(locale: Locale): String? {
    return when (locale.language.lowercase(Locale.US)) {
        "en" -> "en"
        "de" -> "de"
        "es" -> "es"
        "fr" -> "fr"
        "pt" -> "pt"
        "ja" -> "ja"
        "ko" -> "ko"
        "zh" -> "zh-Hans"
        else -> null
    }
}
