package com.example.lcb.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.lcb.app.ui.createLocalizedConfiguration
import com.example.lcb.app.ui.localizedContext
import com.example.lcb.app.ui.report.ReportScreen
import com.example.lcb.app.ui.theme.LcbTheme

class TrendsFragment : Fragment(), MainTabVisibilityAware {
    private val viewModel: LcbAppViewModel by activityViewModels()
    private var active by mutableStateOf(false)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val language by viewModel.language.collectAsStateWithLifecycle()
                val context = LocalContext.current
                val baseConfiguration = LocalConfiguration.current
                val reportData by viewModel.reportData.collectAsStateWithLifecycle()
                val localizedContext = context.localizedContext(language)
                val localizedConfiguration = createLocalizedConfiguration(baseConfiguration, language)

                CompositionLocalProvider(
                    LocalContext provides localizedContext,
                    LocalConfiguration provides localizedConfiguration,
                ) {
                    LcbTheme {
                        ReportScreen(
                            data = reportData,
                            active = active,
                            showBottomBar = false,
                            onHome = { mainActivity()?.showHome() },
                            onData = { mainActivity()?.showTrends() },
                            onMetricClick = { metric ->
                                mainActivity()?.openReportDetail(metric)
                            },
                        )
                    }
                }
            }
        }
    }

    override fun setMainTabVisible(visible: Boolean) {
        active = visible
    }

    private fun mainActivity(): MainActivity? = activity as? MainActivity
}
