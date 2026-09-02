package com.android.zenspend.feature.ai.domain

import com.android.zenspend.core.monetization.domain.EntitlementManager
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiQuotaManager @Inject constructor(
    private val entitlementManager: com.android.zenspend.core.monetization.domain.EntitlementManager,
    private val aiPreferences: com.android.zenspend.core.datastore.AiPreferences
) {
    private val FREE_DAILY_LIMIT = 3

    suspend fun canExecuteQuery(): Boolean {
        if (entitlementManager.isPremium.first()) return true
        val currentUsage = aiPreferences.dailyUsage.first()
        return currentUsage < FREE_DAILY_LIMIT
    }

    suspend fun recordQuery() {
        if (!entitlementManager.isPremium.first()) {
            aiPreferences.incrementUsage()
        }
    }

    suspend fun getRemainingQuota(): Int {
        if (entitlementManager.isPremium.first()) return Int.MAX_VALUE
        val currentUsage = aiPreferences.dailyUsage.first()
        return (FREE_DAILY_LIMIT - currentUsage).coerceAtLeast(0)
    }
}
