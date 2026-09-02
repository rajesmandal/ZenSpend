package com.android.zenspend.core.monetization.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.android.zenspend.core.monetization.presentation.AdController
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

@Composable
fun BannerAd(
    adController: AdController,
    unitId: String,
    currentRoute: String? = null,
    modifier: Modifier = Modifier
) {
    var shouldShow by remember { mutableStateOf(false) }

    LaunchedEffect(currentRoute) {
        shouldShow = adController.shouldShowAds() && !adController.isScreenSensitive(currentRoute)
    }

    if (shouldShow) {
        AndroidView(
            modifier = modifier.fillMaxWidth(),
            factory = { context ->
                AdView(context).apply {
                    setAdSize(AdSize.BANNER)
                    adUnitId = unitId
                    loadAd(adController.getAdRequest())
                }
            }
        )
    }
}
