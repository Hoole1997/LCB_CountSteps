package com.example.lcb.app

import com.blankj.utilcode.util.LogUtils
import com.example.lcb.app.ad.LcbAdInitializer
import net.corekit.metrics.adjust.AdjustTracker

class LcbApp : com.stepwise.pedometer.gostep.Rc4w4wk8() {

    companion object {

        var lcbApp: LcbApp? = null

        fun backLaunchActivity() {
            lcbApp?.autocenter()
        }
    }

    override fun onCreate() {
        super.onCreate()
        lcbApp = this
        LcbAdInitializer.initialize(this)
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

    override fun hyperscanprohub(): Class<Any>? {
        @Suppress("UNCHECKED_CAST")
        return MainActivity::class.java as Class<Any>
    }

    override fun restoremap(): List<Class<Any>> {
        @Suppress("UNCHECKED_CAST")
        return listOf(
            MainActivity::class.java
        ) as List<Class<Any>>
    }

}
