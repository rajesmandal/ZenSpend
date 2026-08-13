package com.android.zenspend.feature.sms.domain.model

data class SmsMessage(
    val body: String,
    val sender: String,
    val timestamp: Long
)
