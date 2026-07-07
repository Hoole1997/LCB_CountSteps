package com.example.lcb.app

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.lcb.app.ui.achievement.AchievementScreen
import com.example.lcb.app.ui.createLocalizedConfiguration
import com.example.lcb.app.ui.localizedContext
import com.example.lcb.app.ui.theme.LcbTheme

class AchievementActivity : FragmentActivity() {
    private val viewModel: LcbAppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )

        setContent {
            val language by viewModel.language.collectAsStateWithLifecycle()
            val homeData by viewModel.homeData.collectAsStateWithLifecycle()
            val hydrateData by viewModel.hydrateData.collectAsStateWithLifecycle()
            val weightData by viewModel.weightData.collectAsStateWithLifecycle()
            val context = LocalContext.current
            val baseConfiguration = LocalConfiguration.current
            val localizedContext = context.localizedContext(language)
            val localizedConfiguration = createLocalizedConfiguration(baseConfiguration, language)

            CompositionLocalProvider(
                LocalContext provides localizedContext,
                LocalConfiguration provides localizedConfiguration,
            ) {
                LcbTheme {
                    AchievementScreen(
                        homeData = homeData,
                        hydrateData = hydrateData,
                        weightData = weightData,
                        onBack = ::finish,
                    )
                }
            }
        }
    }

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, AchievementActivity::class.java)
        }
    }
}
