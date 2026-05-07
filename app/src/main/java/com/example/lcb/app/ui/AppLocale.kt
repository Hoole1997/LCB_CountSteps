package com.example.lcb.app.ui

import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import java.util.Locale

fun localeForLanguageCode(languageCode: String): Locale {
    val tag = when (languageCode) {
        "zh-Hans" -> "zh-CN"
        else -> languageCode
    }
    return Locale.forLanguageTag(tag)
}

fun Context.localizedContext(languageCode: String): Context {
    val locale = localeForLanguageCode(languageCode)
    val configuration = Configuration(resources.configuration).apply {
        setLocales(LocaleList(locale))
    }
    return createConfigurationContext(configuration)
}

fun createLocalizedConfiguration(base: Configuration, languageCode: String): Configuration {
    val locale = localeForLanguageCode(languageCode)
    return Configuration(base).apply {
        setLocales(LocaleList(locale))
    }
}

@Composable
fun currentAppLocale(): Locale {
    val locales = LocalConfiguration.current.locales
    return locales[0] ?: Locale.getDefault()
}
