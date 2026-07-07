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
import com.example.lcb.app.ui.home.HomeScreen
import com.example.lcb.app.ui.localizedContext
import com.example.lcb.app.ui.sheets.StepCorrectionDialogFragment
import com.example.lcb.app.ui.sheets.StepGoalBottomSheetFragment
import com.example.lcb.app.ui.sheets.WeightEntryBottomSheetFragment
import com.example.lcb.app.ui.theme.LcbTheme

class HomeFragment : Fragment(), MainTabVisibilityAware {
    private val viewModel: LcbAppViewModel by activityViewModels()
    private var active by mutableStateOf(false)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val language by viewModel.language.collectAsStateWithLifecycle()
                val context = LocalContext.current
                val baseConfiguration = LocalConfiguration.current
                val homeData by viewModel.homeData.collectAsStateWithLifecycle()
                val sensorStatus by viewModel.sensorStatus.collectAsStateWithLifecycle()
                val hydrateData by viewModel.hydrateData.collectAsStateWithLifecycle()
                val weightData by viewModel.weightData.collectAsStateWithLifecycle()
                val localizedContext = context.localizedContext(language)
                val localizedConfiguration = createLocalizedConfiguration(baseConfiguration, language)

                CompositionLocalProvider(
                    LocalContext provides localizedContext,
                    LocalConfiguration provides localizedConfiguration,
                ) {
                    LcbTheme {
                        HomeScreen(
                            data = homeData,
                            hydrateData = hydrateData,
                            weightData = weightData,
                            sensorStatus = sensorStatus,
                            active = active,
                            showBottomBar = false,
                            onRequestPermission = { mainActivity()?.requestActivityPermission() },
                            onSettings = { mainActivity()?.openMeSettings() },
                            onHome = { mainActivity()?.showHome() },
                            onData = { mainActivity()?.showTrends() },
                            onHydrate = { mainActivity()?.openHydrate("home") },
                            onAddWater = { amount ->
                                reportClick("click_drink_water", "from" to "home", "amount_ml" to amount)
                                viewModel.addWater(amount)
                            },
                            onSetStepGoal = viewModel::setStepGoal,
                            onSetStepCountingPaused = viewModel::setStepCountingPaused,
                            onSetStepsForDate = viewModel::setStepsForDate,
                            onSetWeightForDate = viewModel::setWeightForDate,
                            onAchievements = { mainActivity()?.openAchievements("home") },
                            onEditStepGoal = ::showStepGoalSheet,
                            onEditWeight = ::showWeightSheet,
                            onEditSteps = ::showStepCorrectionDialog,
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

    private fun showStepGoalSheet() {
        if (childFragmentManager.findFragmentByTag(StepGoalSheetTag) == null) {
            StepGoalBottomSheetFragment().show(childFragmentManager, StepGoalSheetTag)
        }
    }

    private fun showWeightSheet() {
        if (childFragmentManager.findFragmentByTag(WeightSheetTag) == null) {
            WeightEntryBottomSheetFragment().show(childFragmentManager, WeightSheetTag)
        }
    }

    private fun showStepCorrectionDialog() {
        if (childFragmentManager.findFragmentByTag(StepCorrectionDialogTag) == null) {
            StepCorrectionDialogFragment().show(childFragmentManager, StepCorrectionDialogTag)
        }
    }

    private companion object {
        const val StepGoalSheetTag = "home_step_goal_bottom_sheet"
        const val WeightSheetTag = "home_weight_bottom_sheet"
        const val StepCorrectionDialogTag = "home_step_correction_dialog"
    }
}
