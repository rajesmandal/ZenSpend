package com.android.zenspend.feature.sms.domain.usecase

import com.android.zenspend.feature.sms.domain.model.SmsTransactionDraft
import com.android.zenspend.feature.sms.domain.repository.SmsDraftRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSmsDraftsUseCase @Inject constructor(
    private val repository: SmsDraftRepository
) {
    operator fun invoke(): Flow<List<SmsTransactionDraft>> {
        return repository.getPendingDrafts()
    }
}
