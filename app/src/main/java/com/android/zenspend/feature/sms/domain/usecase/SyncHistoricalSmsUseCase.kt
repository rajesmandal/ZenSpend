package com.android.zenspend.feature.sms.domain.usecase

import com.android.zenspend.feature.sms.domain.repository.SystemSmsRepository
import javax.inject.Inject

class SyncHistoricalSmsUseCase @Inject constructor(
    private val systemSmsRepository: SystemSmsRepository,
    private val smsParserUseCase: SmsParserUseCase
) {
    suspend operator fun invoke() {
        val messages = systemSmsRepository.getHistoricalSms()
        messages.forEach { msg ->
            smsParserUseCase(msg.body, msg.sender, msg.timestamp)
        }
    }
}
