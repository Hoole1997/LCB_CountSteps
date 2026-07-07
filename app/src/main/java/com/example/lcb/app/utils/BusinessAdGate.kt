package com.example.lcb.app.utils

import android.os.SystemClock

/**
 * App 级广告节流器。多个业务入口共享同一套节奏，避免每个页面各自计数导致广告过频。
 */
object BusinessAdGate {
    private const val MinActionsBeforeAttempt = 2
    private const val MinAttemptIntervalMs = 60_000L
    private const val MinShownIntervalMs = 120_000L

    private var actionCountSinceShown = 0
    private var lastAttemptAt = 0L
    private var lastShownAt = 0L

    fun shouldAttemptInterstitial(): Boolean {
        actionCountSinceShown += 1
        val now = SystemClock.elapsedRealtime()
        if (actionCountSinceShown < MinActionsBeforeAttempt) return false
        if (now - lastAttemptAt < MinAttemptIntervalMs) return false
        if (now - lastShownAt < MinShownIntervalMs) return false
        lastAttemptAt = now
        return true
    }

    fun markInterstitialShown() {
        lastShownAt = SystemClock.elapsedRealtime()
        actionCountSinceShown = 0
    }
}
