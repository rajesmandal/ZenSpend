package com.android.zenspend.feature.sms.domain.usecase

import com.android.zenspend.core.common.result.Resource
import com.android.zenspend.feature.sms.data.entity.SmsDraftStatus
import com.android.zenspend.feature.sms.domain.repository.SmsDraftRepository
import com.android.zenspend.feature.transaction.domain.model.Transaction
import com.android.zenspend.feature.transaction.domain.usecase.AddTransactionUseCase
import javax.inject.Inject

class ConfirmSmsDraftUseCase @Inject constructor(
    private val smsDraftRepository: SmsDraftRepository,
    private val addTransactionUseCase: AddTransactionUseCase
) {
    suspend operator fun invoke(draftId: Long, transaction: Transaction): Resource<Long> {
        val result = addTransactionUseCase(transaction.copy(externalReference = draftId.toString()))
        if (result is Resource.Success) {
            smsDraftRepository.updateStatus(draftId, SmsDraftStatus.COMMITTED)
        }
        return result
    }
}
