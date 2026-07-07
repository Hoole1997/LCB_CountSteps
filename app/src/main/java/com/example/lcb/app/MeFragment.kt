package com.example.lcb.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.lcb.app.ui.createLocalizedConfiguration
import com.example.lcb.app.ui.localizedContext
import com.example.lcb.app.ui.me.MeScreen
import com.example.lcb.app.ui.sheets.LanguageBottomSheetFragment
import com.example.lcb.app.ui.sheets.StepGoalBottomSheetFragment
import com.example.lcb.app.ui.sheets.WaterGoalBottomSheetFragment
import com.example.lcb.app.ui.sheets.WeightEntryBottomSheetFragment
import com.example.lcb.app.ui.theme.LcbTheme

class MeFragment : Fragment(), MainTabVisibilityAware {
    private val viewModel: LcbAppViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val language by viewModel.language.collectAsStateWithLifecycle()
                val context = LocalContext.current
                val baseConfiguration = LocalConfiguration.current
                val homeData by viewModel.homeData.collectAsStateWithLifecycle()
                val hydrateData by viewModel.hydrateData.collectAsStateWithLifecycle()
                val weightData by viewModel.weightData.collectAsStateWithLifecycle()
                val localizedContext = context.localizedContext(language)
                val localizedConfiguration = createLocalizedConfiguration(baseConfiguration, language)

                CompositionLocalProvider(
                    LocalContext provides localizedContext,
                    LocalConfiguration provides localizedConfiguration,
                ) {
                    LcbTheme {
                        MeScreen(
                            language = language,
                            homeData = homeData,
                            hydrateData = hydrateData,
                            weightData = weightData,
                            onLanguageClick = ::showLanguageSheet,
                            onDailyStepGoalClick = ::showStepGoalSheet,
                            onDailyWaterGoalClick = ::showWaterGoalSheet,
                            onWeightGoalClick = ::showWeightSheet,
                            onFeedbackClick = { mainActivity()?.openFeedback() },
                            onPrivacyClick = { mainActivity()?.openPrivacyPolicy() },
                            onAchievementsClick = { mainActivity()?.openAchievements("me") },
                        )
                    }
                }
            }
        }
    }

    override fun setMainTabVisible(visible: Boolean) = Unit

    private fun mainActivity(): MainActivity? = activity as? MainActivity

    private fun showLanguageSheet() {
        if (childFragmentManager.findFragmentByTag(LanguageSheetTag) == null) {
            LanguageBottomSheetFragment().show(childFragmentManager, LanguageSheetTag)
        }
    }

    private fun showStepGoalSheet() {
        if (childFragmentManager.findFragmentByTag(StepGoalSheetTag) == null) {
            StepGoalBottomSheetFragment().show(childFragmentManager, StepGoalSheetTag)
        }
    }

    private fun showWaterGoalSheet() {
        if (childFragmentManager.findFragmentByTag(WaterGoalSheetTag) == null) {
            WaterGoalBottomSheetFragment().show(childFragmentManager, WaterGoalSheetTag)
        }
    }

    private fun showWeightSheet() {
        if (childFragmentManager.findFragmentByTag(WeightSheetTag) == null) {
            WeightEntryBottomSheetFragment().show(childFragmentManager, WeightSheetTag)
        }
    }

    private companion object {
        const val LanguageSheetTag = "language_bottom_sheet"
        const val StepGoalSheetTag = "step_goal_bottom_sheet"
        const val WaterGoalSheetTag = "water_goal_bottom_sheet"
        const val WeightSheetTag = "weight_bottom_sheet"
    }
}
