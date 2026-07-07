package com.example.lcb.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.lcb.app.databinding.ActivityMainHomeBinding
import com.example.lcb.app.ui.createLocalizedConfiguration
import com.example.lcb.app.ui.localizedContext
import com.example.lcb.app.ui.components.AppBottomBar
import com.example.lcb.app.ui.components.TabDestination
import com.example.lcb.app.ui.theme.LcbTheme
import com.example.lcb.app.ui.report.ReportMetricType
import com.example.lcb.app.utils.BusinessAdGate
import com.example.lcb.app.utils.loadInterstitial
import net.corekit.core.report.ReportDataManager

class MainActivity : FragmentActivity() {
    private lateinit var binding: ActivityMainHomeBinding
    private val viewModel: LcbAppViewModel by viewModels()
    private var selectedTab by mutableStateOf(TabDestination.Home)
    private var stepSensorLifecycleActive = false

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) {
        if (stepSensorLifecycleActive) viewModel.onPermissionResult()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureSystemBars()
        binding = ActivityMainHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupBottomBar()
        setupBackHandling()
        val restoredTab = savedInstanceState
            ?.getString(StateSelectedTab)
            ?.let { runCatching { TabDestination.valueOf(it) }.getOrNull() }
            ?: TabDestination.Home
        showTab(restoredTab, reportClick = false)
    }

    override fun onStart() {
        super.onStart()
        stepSensorLifecycleActive = true
        activateStepSensor()
    }

    override fun onStop() {
        stepSensorLifecycleActive = false
        viewModel.deactivateStepSensor()
        super.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(StateSelectedTab, selectedTab.name)
        super.onSaveInstanceState(outState)
    }

    override fun onBackPressed() {
        LcbApp.backLaunchActivity()
    }

    fun showHome() {
        showTab(TabDestination.Home)
    }

    fun showTrends() {
        showTab(TabDestination.Data)
    }

    fun showMe() {
        showTab(TabDestination.Me)
    }

    fun requestActivityPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
        }
    }

    fun openMeSettings() {
        showMe()
    }

    fun openFeedback() {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:walaa98alhasan@gmail.com")
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedback_email_subject))
        }
        runCatching { startActivity(intent) }
    }

    fun openPrivacyPolicy() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://walaa98alhasan.com/privacy.html"))
        runCatching { startActivity(intent) }
    }

    fun openHydrate(source: String) {
        reportClick("click_enter_hydrate", "from" to source)
        loadInterstitial(condition = BusinessAdGate::shouldAttemptInterstitial) { shown ->
            if (shown) BusinessAdGate.markInterstitialShown()
            startActivity(Intent(this, HydrateActivity::class.java))
        }
    }

    fun openAchievements(source: String) {
        reportClick("click_enter_achievements", "from" to source)
        loadInterstitial(condition = BusinessAdGate::shouldAttemptInterstitial) { shown ->
            if (shown) BusinessAdGate.markInterstitialShown()
            startActivity(AchievementActivity.createIntent(this))
        }
    }

    fun openReportDetail(metric: ReportMetricType) {
        loadInterstitial(condition = BusinessAdGate::shouldAttemptInterstitial) { shown ->
            if (shown) BusinessAdGate.markInterstitialShown()
            startActivity(ReportDetailActivity.createIntent(this, metric))
        }
    }

    private fun configureSystemBars() {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
    }

    private fun setupBackHandling() {
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (selectedTab != TabDestination.Home) {
                        showTab(TabDestination.Home, reportClick = false)
                    } else {
                        LcbApp.backLaunchActivity()
                    }
                }
            },
        )
    }

    private fun setupBottomBar() {
        binding.mainBottomBar.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        binding.mainBottomBar.setContent {
            val context = LocalContext.current
            val baseConfiguration = LocalConfiguration.current
            val language by viewModel.language.collectAsStateWithLifecycle()
            val localizedContext = context.localizedContext(language)
            val localizedConfiguration = createLocalizedConfiguration(baseConfiguration, language)

            CompositionLocalProvider(
                LocalContext provides localizedContext,
                LocalConfiguration provides localizedConfiguration,
            ) {
                LcbTheme {
                    AppBottomBar(
                        selected = selectedTab,
                        onHome = ::showHome,
                        onData = ::showTrends,
                        onMe = ::showMe,
                        dark = true,
                        homeLabel = localizedContext.getString(R.string.nav_home),
                        dataLabel = localizedContext.getString(R.string.nav_trends),
                        meLabel = localizedContext.getString(R.string.nav_me),
                    )
                }
            }
        }
    }

    private fun showTab(destination: TabDestination, reportClick: Boolean = true) {
        if (reportClick) {
            when (destination) {
                TabDestination.Home -> reportClick("click_tab_home", "from" to selectedTab.name.lowercase())
                TabDestination.Data -> reportClick("click_tab_data", "from" to selectedTab.name.lowercase())
                TabDestination.Me -> reportClick("click_tab_me", "from" to selectedTab.name.lowercase())
            }
        }
        selectedTab = destination

        val tag = destination.fragmentTag
        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction().setReorderingAllowed(true)
        fragmentManager.fragments.forEach { transaction.hide(it) }
        val existing = fragmentManager.findFragmentByTag(tag)
        val visibleFragment = existing ?: destination.newFragment()
        if (existing == null) {
            transaction.add(R.id.main_fragment_container, visibleFragment, tag)
        } else {
            transaction.show(existing)
        }
        fragmentManager.fragments.forEach { fragment ->
            (fragment as? MainTabVisibilityAware)?.setMainTabVisible(fragment.tag == tag)
        }
        (visibleFragment as? MainTabVisibilityAware)?.setMainTabVisible(true)
        transaction.commit()
    }

    private fun activateStepSensor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
        } else {
            viewModel.activateStepSensor()
        }
    }

    private val TabDestination.fragmentTag: String
        get() = when (this) {
            TabDestination.Home -> "main_home"
            TabDestination.Data -> "main_trends"
            TabDestination.Me -> "main_me"
        }

    private fun TabDestination.newFragment(): Fragment {
        return when (this) {
            TabDestination.Home -> HomeFragment()
            TabDestination.Data -> TrendsFragment()
            TabDestination.Me -> MeFragment()
        }
    }
}

fun reportClick(eventName: String, vararg params: Pair<String, Any>) {
    ReportDataManager.reportData(
        eventName = eventName,
        data = params.toMap(),
    )
}

interface MainTabVisibilityAware {
    fun setMainTabVisible(visible: Boolean)
}

private const val StateSelectedTab = "selected_tab"
