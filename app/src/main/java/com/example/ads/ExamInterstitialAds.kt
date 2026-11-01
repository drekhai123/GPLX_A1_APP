package com.example.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Interstitials are only used from explicit actions on the exam result screen. */
object ExamInterstitialAds {
    private const val PREFS_NAME = "exam_interstitial_frequency"
    private const val KEY_LAST_SHOWN = "last_shown_at"
    private const val KEY_DAILY_COUNT = "daily_count"
    private const val KEY_DAILY_DATE = "daily_date"
    private const val MAX_PER_SESSION = 3
    private const val MAX_PER_DAY = 8
    private const val MIN_INTERVAL_MS = 200_000L
    private const val TEST_AD_UNIT_ID = "ca-app-pub-1427617620441230~9002953047"

    private var interstitialAd: InterstitialAd? = null
    private var isLoading = false
    private var shownThisSession = 0

    fun preload(context: Context) {
        if (isLoading || interstitialAd != null || !isEligible(context)) return
        isLoading = true
        InterstitialAd.load(
            context.applicationContext,
            TEST_AD_UNIT_ID,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    isLoading = false
                    interstitialAd = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    isLoading = false
                    interstitialAd = null
                }
            }
        )
    }

    fun showIfEligible(activity: Activity, onFinished: () -> Unit) {
        val ad = interstitialAd
        if (ad == null || !isEligible(activity)) {
            onFinished()
            preload(activity)
            return
        }

        interstitialAd = null
        var finished = false
        fun finishOnce() {
            if (finished) return
            finished = true
            onFinished()
            preload(activity)
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                recordShown(activity)
            }

            override fun onAdDismissedFullScreenContent() = finishOnce()

            override fun onAdFailedToShowFullScreenContent(error: AdError) = finishOnce()
        }
        ad.show(activity)
    }

    private fun isEligible(context: Context): Boolean {
        val now = System.currentTimeMillis()
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val today = todayKey(now)
        val dailyCount = if (prefs.getString(KEY_DAILY_DATE, null) == today) {
            prefs.getInt(KEY_DAILY_COUNT, 0)
        } else 0
        val lastShown = prefs.getLong(KEY_LAST_SHOWN, 0L)
        return shownThisSession < MAX_PER_SESSION &&
            dailyCount < MAX_PER_DAY &&
            (lastShown == 0L || now - lastShown >= MIN_INTERVAL_MS)
    }

    private fun recordShown(context: Context) {
        val now = System.currentTimeMillis()
        val today = todayKey(now)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val count = if (prefs.getString(KEY_DAILY_DATE, null) == today) {
            prefs.getInt(KEY_DAILY_COUNT, 0)
        } else 0
        shownThisSession++
        prefs.edit()
            .putLong(KEY_LAST_SHOWN, now)
            .putString(KEY_DAILY_DATE, today)
            .putInt(KEY_DAILY_COUNT, count + 1)
            .apply()
    }

    private fun todayKey(timeMillis: Long): String =
        SimpleDateFormat("yyyyMMdd", Locale.US).format(Date(timeMillis))
}
