package com.android.zenspend.feature.sms.domain.repository

import com.android.zenspend.feature.sms.domain.model.SmsMessage

interface SystemSmsRepository {
    suspend fun getHistoricalSms(limit: Int = 500): List<SmsMessage>
}
