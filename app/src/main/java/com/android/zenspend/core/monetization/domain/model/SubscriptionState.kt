package com.android.zenspend.core.monetization.domain.model

sealed class SubscriptionState {
    object Free : SubscriptionState()
    object Premium : SubscriptionState()
    object Unknown : SubscriptionState()
}
