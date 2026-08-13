package com.android.zenspend.feature.sms.domain.repository

import com.android.zenspend.feature.sms.data.entity.SmsDraftStatus
import com.android.zenspend.feature.sms.domain.model.SmsTransactionDraft
import kotlinx.coroutines.flow.Flow

interface SmsDraftRepository {
    fun getPendingDrafts(): Flow<List<SmsTransactionDraft>>
    suspend fun insertDraft(draft: SmsTransactionDraft): Long
    suspend fun updateStatus(id: Long, status: SmsDraftStatus)
    suspend fun getDraftByFingerprint(fingerprint: String): SmsTransactionDraft?
}
