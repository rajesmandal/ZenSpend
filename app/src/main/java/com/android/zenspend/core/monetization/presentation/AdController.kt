package com.android.zenspend.core.monetization.presentation

import android.content.Context
import com.android.zenspend.core.monetization.domain.EntitlementManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdController @Inject constructor(
    @ApplicationContext private val context: Context,
    private val entitlementManager: EntitlementManager
) {
    init {
        MobileAds.initialize(context)
    }

    suspend fun shouldShowAds(): Boolean {
        return !entitlementManager.isPremium.first()
    }

    fun isScreenSensitive(route: String?): Boolean {
        if (route == null) return false
        val sensitiveRoutes = listOf(
            "add_transaction",
            "edit_transaction",
            "sms_review",
            "voice_review",
            "backup",
            "credit_card_details",
            "wallet_details"
        )
        return sensitiveRoutes.any { route.contains(it) }
    }

    fun getAdRequest(): AdRequest {
        // Data privacy: do not add custom targeting data
        return AdRequest.Builder().build()
    }
}
