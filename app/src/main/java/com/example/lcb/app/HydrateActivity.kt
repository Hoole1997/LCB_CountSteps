package com.example.lcb.app

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.view.Window
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.lcb.app.data.HydrateData
import com.example.lcb.app.data.localDate
import com.example.lcb.app.databinding.ActivityHydrateBinding
import com.example.lcb.app.ui.components.setAnimatedValueText
import com.example.lcb.app.ui.hydrate.HydrateVisualTokens
import com.example.lcb.app.ui.localizedContext
import com.example.lcb.app.ui.sheets.WaterGoalBottomSheetFragment
import com.example.lcb.app.utils.BusinessAdGate
import com.example.lcb.app.utils.loadInterstitial
import kotlinx.coroutines.launch
import net.corekit.core.report.ReportDataManager
import java.time.LocalDate

class HydrateActivity : AppCompatActivity() {
    private lateinit var viewModel: LcbAppViewModel
    private lateinit var binding: ActivityHydrateBinding
    private var currentLanguage = "en"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureSystemBars()
        viewModel = ViewModelProvider(this)[LcbAppViewModel::class.java]
        binding = ActivityHydrateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        configureToolbar()
        bindActions()
        observeData()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_hydrate_toolbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.action_hydrate_report -> {
                loadInterstitial(condition = BusinessAdGate::shouldAttemptInterstitial) { shown ->
                    if (shown) BusinessAdGate.markInterstitialShown()
                    startActivity(HydrateReportActivity.createIntent(this))
                }
                true
            }
            R.id.action_hydrate_settings -> {
                showWaterGoalSheet()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun configureSystemBars() {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
    }

    private fun configureToolbar() {
        setSupportActionBar(binding.hydrateToolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.hydrateToolbar.navigationIcon?.setTint(Color.WHITE)
        ViewCompat.setOnApplyWindowInsetsListener(binding.hydrateToolbar) { view, insets ->
                val top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
                view.updatePadding(top = top)
                view.layoutParams = view.layoutParams.apply {
                    width = ViewGroup.LayoutParams.MATCH_PARENT
                    height = top + dp(44)
                }
                insets
        }
    }

    private fun bindActions() {
        binding.drinkButton.setOnClickListener {
            ReportDataManager.reportData(
                eventName = "click_drink_water",
                data = mapOf("amount_ml" to DrinkAmountMl),
            )
            viewModel.addWater(DrinkAmountMl)
        }
        binding.doneButton.setOnClickListener { finish() }
    }

    private fun observeData() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.language.collect { language ->
                        currentLanguage = language
                        updateStaticText()
                    }
                }
                launch {
                    viewModel.hydrateData.collect { updateHydrateData(it) }
                }
            }
        }
    }

    private fun updateHydrateData(data: HydrateData) {
        val today = runCatching { LocalDate.parse(data.today) }.getOrDefault(LocalDate.now())
        val total = data.hydrationRecords
            .filter { it.localDate() == today }
            .sumOf { it.amountMl }
        val waterGoalMl = data.waterGoalMl.coerceAtLeast(1)
        binding.amountText.setAnimatedValueText(buildAmountText(total, waterGoalMl))
        binding.waterDropView.setProgress(total.toFloat() / waterGoalMl)
        updateStaticText()
    }

    private fun updateStaticText() {
        val localized = localizedContext(currentLanguage)
        binding.subtitleText.text = localized.getString(R.string.hydrate_subtitle)
        binding.drinkButtonText.text = "${DrinkAmountMl}ML"
        binding.doneButton.text = localized.getString(R.string.hydrate_done)
    }

    private fun buildAmountText(total: Int, waterGoalMl: Int): SpannableString {
        val value = "$total/${waterGoalMl}ML"
        return SpannableString(value).apply {
            setSpan(ForegroundColorSpan(HydrateVisualTokens.AccentBlue), 0, total.toString().length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(ForegroundColorSpan(Color.WHITE), total.toString().length, value.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(RelativeSizeSpan(2f), 0, total.toString().length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(RelativeSizeSpan(1f), total.toString().length, value.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(StyleSpan(Typeface.BOLD), 0, value.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    private fun showWaterGoalSheet() {
        if (supportFragmentManager.findFragmentByTag(WaterGoalSheetTag) == null) {
            WaterGoalBottomSheetFragment().show(supportFragmentManager, WaterGoalSheetTag)
        }
    }

    private fun Context.dp(value: Int): Int {
        return (value * resources.displayMetrics.density + 0.5f).toInt()
    }

    private companion object {
        private const val DrinkAmountMl = 200
        private const val WaterGoalSheetTag = "hydrate_water_goal_bottom_sheet"
    }
}
