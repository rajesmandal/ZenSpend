package com.android.zenspend.feature.sms.domain.usecase

import com.android.zenspend.feature.sms.data.parser.SmsParser
import com.android.zenspend.feature.sms.domain.repository.SmsDraftRepository
import javax.inject.Inject

class SmsParserUseCase @Inject constructor(
    private val repository: SmsDraftRepository,
    private val notificationHelper: com.android.zenspend.core.util.NotificationHelper
) {
    suspend operator fun invoke(message: String, sender: String, timestamp: Long) {
        val draft = SmsParser.parse(message, sender, timestamp) ?: return
        
        // Deduplication check
        if (repository.getDraftByFingerprint(draft.fingerprint) == null) {
            repository.insertDraft(draft)
            notificationHelper.showSmsDetectedNotification(draft.merchant, draft.amount)
        }
    }
}
