package com.example.ads

import android.content.Context
import com.example.R

/**
 * Google AdMob Configuration & ID Placeholders.
 *
 * =========================================================================
 * HOW TO REPLACE TEST IDs WITH YOUR REAL ADMOB IDs:
 * =========================================================================
 * 1. App ID:
 *    In `app/src/main/res/values/strings.xml`, update:
 *    `<string name="admob_app_id">YOUR_REAL_APP_ID_HERE</string>`
 *    or set [REAL_ADMOB_APP_ID] below.
 *
 * 2. Banner Ad Unit ID:
 *    In `app/src/main/res/values/strings.xml`, update:
 *    `<string name="admob_banner_ad_unit_id">YOUR_REAL_BANNER_ID_HERE</string>`
 *    or set [REAL_BANNER_AD_UNIT_ID] below.
 *
 * 3. Interstitial Ad Unit ID:
 *    In `app/src/main/res/values/strings.xml`, update:
 *    `<string name="admob_interstitial_ad_unit_id">YOUR_REAL_INTERSTITIAL_ID_HERE</string>`
 *    or set [REAL_INTERSTITIAL_AD_UNIT_ID] below.
 * =========================================================================
 */
object AdConfig {
    // Official Google AdMob Test IDs (Used during development and testing)
    const val TEST_ADMOB_APP_ID = "ca-app-pub-3940256099942544~3347511713"
    const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
    const val TEST_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"

    // Real AdMob IDs Placeholders (Keep empty until ready for release)
    const val REAL_ADMOB_APP_ID = ""
    const val REAL_BANNER_AD_UNIT_ID = ""
    const val REAL_INTERSTITIAL_AD_UNIT_ID = ""

    fun getAppId(context: Context): String {
        if (REAL_ADMOB_APP_ID.isNotBlank()) return REAL_ADMOB_APP_ID
        return runCatching { context.getString(R.string.admob_app_id) }
            .getOrDefault(TEST_ADMOB_APP_ID)
    }

    fun getBannerAdUnitId(context: Context): String {
        if (REAL_BANNER_AD_UNIT_ID.isNotBlank()) return REAL_BANNER_AD_UNIT_ID
        return runCatching { context.getString(R.string.admob_banner_ad_unit_id) }
            .getOrDefault(TEST_BANNER_AD_UNIT_ID)
    }

    fun getInterstitialAdUnitId(context: Context): String {
        if (REAL_INTERSTITIAL_AD_UNIT_ID.isNotBlank()) return REAL_INTERSTITIAL_AD_UNIT_ID
        return runCatching { context.getString(R.string.admob_interstitial_ad_unit_id) }
            .getOrDefault(TEST_INTERSTITIAL_AD_UNIT_ID)
    }
}
