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
import com.example.lcb.app.ui.createLocalizedConfiguration
import com.example.lcb.app.ui.localizedContext
import com.example.lcb.app.ui.report.ReportDetailScreen
import com.example.lcb.app.ui.report.ReportMetricType
import com.example.lcb.app.ui.theme.LcbTheme

class ReportDetailActivity : FragmentActivity() {
    private val viewModel: LcbAppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        val initialMetric = intent.getStringExtra(ExtraMetric)
            ?.let { runCatching { ReportMetricType.valueOf(it) }.getOrNull() }
            ?: ReportMetricType.Step

        setContent {
            val language by viewModel.language.collectAsStateWithLifecycle()
            val reportData by viewModel.reportData.collectAsStateWithLifecycle()
            val context = LocalContext.current
            val baseConfiguration = LocalConfiguration.current
            val localizedContext = context.localizedContext(language)
            val localizedConfiguration = createLocalizedConfiguration(baseConfiguration, language)

            CompositionLocalProvider(
                LocalContext provides localizedContext,
                LocalConfiguration provides localizedConfiguration,
            ) {
                LcbTheme {
                    ReportDetailScreen(
                        data = reportData,
                        initialMetric = initialMetric,
                        onBack = ::finish,
                    )
                }
            }
        }
    }

    companion object {
        private const val ExtraMetric = "report_metric"

        fun createIntent(context: Context, metric: ReportMetricType): Intent {
            return Intent(context, ReportDetailActivity::class.java)
                .putExtra(ExtraMetric, metric.name)
        }
    }
}
