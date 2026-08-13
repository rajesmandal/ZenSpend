package com.android.zenspend.feature.sms.data.repository

import com.android.zenspend.feature.sms.data.dao.SmsDraftDao
import com.android.zenspend.feature.sms.data.entity.SmsDraftStatus
import com.android.zenspend.feature.sms.data.mapper.toDomain
import com.android.zenspend.feature.sms.data.mapper.toEntity
import com.android.zenspend.feature.sms.domain.model.SmsTransactionDraft
import com.android.zenspend.feature.sms.domain.repository.SmsDraftRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SmsDraftRepositoryImpl @Inject constructor(
    private val smsDraftDao: SmsDraftDao
) : SmsDraftRepository {

    override fun getPendingDrafts(): Flow<List<SmsTransactionDraft>> {
        return smsDraftDao.getDraftsByStatus(SmsDraftStatus.PENDING).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertDraft(draft: SmsTransactionDraft): Long {
        return smsDraftDao.insertDraft(draft.toEntity())
    }

    override suspend fun updateStatus(id: Long, status: SmsDraftStatus) {
        smsDraftDao.updateStatus(id, status)
    }

    override suspend fun getDraftByFingerprint(fingerprint: String): SmsTransactionDraft? {
        return smsDraftDao.getDraftByFingerprint(fingerprint)?.toDomain()
    }
}
