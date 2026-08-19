package com.example.lcb.app.utils

import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.android.common.bill.ads.AdResult
import com.android.common.bill.ads.ext.AdShowExt
import com.android.common.bill.ui.NativeAdStyleType
import com.example.lcb.app.LcbApp
import kotlinx.coroutines.launch

fun FragmentActivity.loadNative(
    container: ViewGroup,
    styleType: NativeAdStyleType = NativeAdStyleType.STANDARD,
) {
    lifecycleScope.launch {
        AdShowExt.showNativeAdInContainer(
            context = this@loadNative,
            container = container,
            styleType = styleType,
        )
    }
}

fun FragmentActivity.loadInterstitial(
    condition: () -> Boolean = { true },
    call: (Boolean) -> Unit
) {
    lifecycleScope.launch {
        try {
            if (!condition.invoke()) {
                call.invoke(false)
                return@launch
            }
            LcbApp.fixAdBug(this@loadInterstitial)
            when (AdShowExt.showInterstitialAd(this@loadInterstitial)) {
                is AdResult.Success -> call.invoke(true)
                is AdResult.Failure -> call.invoke(false)
            }
        } catch (_: Exception) {
            call.invoke(false)
        }
    }
}
