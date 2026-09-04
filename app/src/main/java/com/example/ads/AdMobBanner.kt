package com.example.ads

import android.content.Context
import android.util.Log
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

/**
 * Non-intrusive Google AdMob Banner Composable.
 *
 * AdMob Policy & Design Considerations:
 * 1. Clearly separated with a subtle "Sponsored" indicator.
 * 2. Automatically collapses (zero height) if ad fails to load.
 * 3. Safely lifecycle-aware (pauses/resumes/destroys with Composable).
 * 4. Safe against crashes on devices lacking Google Play Services or during unit testing.
 */
@Composable
fun AdMobBanner(
    modifier: Modifier = Modifier
) {
    val isPreview = LocalInspectionMode.current
    val isRobolectricTest = remember {
        runCatching { Class.forName("org.robolectric.Robolectric") }.isSuccess
    }

    if (isPreview || isRobolectricTest) {
        // Silent placeholder during preview or local unit testing
        return
    }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var isAdLoaded by remember { mutableStateOf(false) }

    val adView = remember {
        AdView(context).apply {
            adUnitId = AdConfig.getBannerAdUnitId(context)
            setAdSize(AdSize.BANNER)
            adListener = object : AdListener() {
                override fun onAdLoaded() {
                    isAdLoaded = true
                    visibility = View.VISIBLE
                    Log.d("AdMobBanner", "Banner ad loaded successfully")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    isAdLoaded = false
                    visibility = View.GONE
                    Log.w("AdMobBanner", "Banner ad failed to load: ${loadAdError.message}")
                }
            }
            runCatching {
                loadAd(AdRequest.Builder().build())
            }.onFailure { e ->
                Log.w("AdMobBanner", "Error loading banner ad: ${e.message}")
            }
        }
    }

    DisposableEffect(lifecycleOwner, adView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> runCatching { adView.pause() }
                Lifecycle.Event.ON_RESUME -> runCatching { adView.resume() }
                Lifecycle.Event.ON_DESTROY -> runCatching { adView.destroy() }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            runCatching { adView.destroy() }
        }
    }

    if (isAdLoaded) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .clip(RoundedCornerShape(12.dp)),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            tonalElevation = 1.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "SPONSORED",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                AndroidView(
                    factory = { adView },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
