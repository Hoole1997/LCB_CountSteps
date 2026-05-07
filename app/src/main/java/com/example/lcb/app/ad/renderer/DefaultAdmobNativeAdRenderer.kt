package com.example.lcb.app.ad.renderer

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.android.common.bill.ads.renderer.AdmobNativeAdRenderer
import com.android.common.bill.ui.NativeAdStyle
import com.example.lcb.app.R
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdView

class DefaultAdmobNativeAdRenderer(
    private val layoutResId: Int = R.layout.layout_native_ads
) : AdmobNativeAdRenderer {

    override fun createLayout(
        context: Context,
        style: NativeAdStyle
    ): NativeAdView {
        return LayoutInflater.from(context)
            .inflate(layoutResId, null) as NativeAdView
    }

    override fun bindData(
        adView: NativeAdView,
        nativeAd: com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd
    ) {
        val titleView = adView.findViewById<TextView>(R.id.tv_ad_title)
        val ctaButton = adView.findViewById<TextView>(R.id.btn_ad_cta)
        val iconView = adView.findViewById<ImageView>(R.id.iv_ad_icon)
        val descView = adView.findViewById<TextView>(R.id.tv_ad_description)

        titleView?.text = nativeAd.headline.orEmpty()
        ctaButton?.text = nativeAd.callToAction ?: "INSTALL"
        descView?.text = nativeAd.body.orEmpty()
        nativeAd.icon?.drawable?.let {
            iconView?.setImageDrawable(it)
            iconView?.visibility = View.VISIBLE
        }

        adView.headlineView = titleView
        adView.callToActionView = ctaButton
        adView.iconView = iconView
        adView.bodyView = descView
        adView.advertiserView = null
        adView.priceView = null
        adView.storeView = null
        adView.registerNativeAd(nativeAd, null)
    }
}
