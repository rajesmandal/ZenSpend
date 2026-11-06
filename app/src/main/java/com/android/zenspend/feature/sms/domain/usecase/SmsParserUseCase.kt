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

        // Same SMS can arrive twice with different timestamps (live receiver uses the SMSC send
        // time, inbox import uses the device receive time), so the exact fingerprint is not enough.
        if (repository.getDraftByFingerprint(draft.fingerprint) != null) return
        if (repository.hasSimilarDraft(
                sender = draft.sender,
                amount = draft.amount,
                type = draft.type,
                from = timestamp - DUPLICATE_WINDOW_MS,
                to = timestamp + DUPLICATE_WINDOW_MS
            )
        ) return

        // The unique index ignores a racing second insert (-1): do not notify for it.
        if (repository.insertDraft(draft) > 0) {
            notificationHelper.showSmsDetectedNotification(draft.merchant, draft.amount)
        }
    }

    private companion object {
        /** Delivery lag between SMSC time and device time stays well inside this. */
        const val DUPLICATE_WINDOW_MS = 10 * 60 * 1000L
    }
}
