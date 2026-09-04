package com.example.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

/**
 * Manages Google Mobile Ads SDK lifecycle, initialization,
 * and interstitial ad caching/presentation with safety checks.
 */
object AdManager {
    private const val TAG = "AdManager"

    @Volatile
    private var isInitialized = false

    private var interstitialAd: InterstitialAd? = null
    private var isLoadingInterstitial = false
    private var lastInterstitialShownTime: Long = 0

    // Minimum cooldown period between interstitial ads (2 minutes = 120,000 ms)
    // Ensures a non-intrusive, policy-compliant user experience
    private const val INTERSTITIAL_COOLDOWN_MS = 120_000L

    /**
     * Initializes Google Mobile Ads SDK safely in the background.
     * Guaranteed never to crash the app even if Play Services is absent.
     */
    fun initialize(context: Context) {
        if (isInitialized) return
        runCatching {
            // AdMob Family/General Audience Compliance
            val requestConfig = RequestConfiguration.Builder()
                .setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_G)
                .build()
            MobileAds.setRequestConfiguration(requestConfig)

            MobileAds.initialize(context) { status ->
                Log.d(TAG, "Mobile Ads SDK initialized: $status")
                isInitialized = true
                loadInterstitial(context.applicationContext)
            }
        }.onFailure { e ->
            Log.w(TAG, "AdMob initialization skipped/deferred: ${e.message}")
        }
    }

    /**
     * Preloads an interstitial ad for smooth transitions.
     */
    fun loadInterstitial(context: Context) {
        if (isLoadingInterstitial || interstitialAd != null) return
        isLoadingInterstitial = true

        runCatching {
            val adUnitId = AdConfig.getInterstitialAdUnitId(context)
            val adRequest = AdRequest.Builder().build()

            InterstitialAd.load(
                context,
                adUnitId,
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        interstitialAd = ad
                        isLoadingInterstitial = false
                        Log.d(TAG, "Interstitial ad loaded successfully")
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        interstitialAd = null
                        isLoadingInterstitial = false
                        Log.w(TAG, "Interstitial ad failed to load: ${loadAdError.message}")
                    }
                }
            )
        }.onFailure { e ->
            isLoadingInterstitial = false
            interstitialAd = null
            Log.w(TAG, "Exception loading interstitial ad: ${e.message}")
        }
    }

    /**
     * Shows an interstitial ad if available and after cooldown.
     * Invokes [onDismissed] immediately if not ready or upon dismissal.
     */
    fun showInterstitial(activity: Activity?, onDismissed: () -> Unit = {}) {
        if (activity == null) {
            onDismissed()
            return
        }

        val currentTime = System.currentTimeMillis()
        val ad = interstitialAd

        if (ad != null && (currentTime - lastInterstitialShownTime >= INTERSTITIAL_COOLDOWN_MS)) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    lastInterstitialShownTime = System.currentTimeMillis()
                    loadInterstitial(activity.applicationContext)
                    onDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    interstitialAd = null
                    Log.w(TAG, "Failed to show interstitial: ${adError.message}")
                    loadInterstitial(activity.applicationContext)
                    onDismissed()
                }

                override fun onAdShowedFullScreenContent() {
                    lastInterstitialShownTime = System.currentTimeMillis()
                }
            }

            runCatching {
                ad.show(activity)
            }.onFailure { e ->
                Log.w(TAG, "Error showing interstitial ad: ${e.message}")
                interstitialAd = null
                onDismissed()
            }
        } else {
            // Not ready or on cooldown: proceed without delay
            if (interstitialAd == null) {
                loadInterstitial(activity.applicationContext)
            }
            onDismissed()
        }
    }
}
