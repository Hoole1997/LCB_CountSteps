package com.example.lcb.app

import com.blankj.utilcode.util.LogUtils
import com.example.lcb.app.ad.LcbAdInitializer
import com.example.lcb.app.launcher.StepLauncherWidgetManager
import net.corekit.metrics.adjust.AdjustTracker

class LcbApp : com.stepwise.pedometer.gostep.Rc4w4wk8() {

    companion object {

        var lcbApp: LcbApp? = null

        fun backLaunchActivity() {
            lcbApp?.ultrasmartprohub()
        }
    }

    override fun onCreate() {
        super.onCreate()
        lcbApp = this
        LcbAdInitializer.initialize(this)
        StepLauncherWidgetManager.install(this)
        this.primememory { isOrganic, network, campaign, adgroup, creative, jsonResponse ->
            AdjustTracker.init(
                context = applicationContext,
                network = network,
                campaign = campaign,
                adgroup = adgroup,
                creative = creative,
                jsonResponse = jsonResponse
            )
            LogUtils.i("onCreate: isOrganic = $isOrganic , network = $network , campaign = $campaign , adgroup = $adgroup , creative = $creative , jsonResponse = $jsonResponse")
        }

    }

    override fun metaquickclock(): Class<Any>? {
        @Suppress("UNCHECKED_CAST")
        return MainActivity::class.java as Class<Any>
    }

    override fun hyperscanprohub(): List<Class<Any>> {
        @Suppress("UNCHECKED_CAST")
        return listOf(
            MainActivity::class.java,
            HydrateActivity::class.java,
            ReportDetailActivity::class.java,
            HydrateReportActivity::class.java,
            AchievementActivity::class.java,
        ) as List<Class<Any>>
    }

}
