package com.android.zenspend.feature.sms.domain.model

import com.android.zenspend.core.database.model.TransactionType
import com.android.zenspend.feature.sms.data.entity.SmsDraftStatus

data class SmsTransactionDraft(
    val id: Long = 0,
    val amount: Long,
    val type: TransactionType,
    val date: Long,
    val merchant: String?,
    val accountLast4: String?,
    val sender: String,
    val fingerprint: String,
    val status: SmsDraftStatus = SmsDraftStatus.PENDING,
    val confidence: Float = 1.0f
)
