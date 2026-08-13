package com.android.zenspend.feature.sms.domain.usecase

import com.android.zenspend.feature.sms.data.entity.SmsDraftStatus
import com.android.zenspend.feature.sms.domain.repository.SmsDraftRepository
import javax.inject.Inject

class IgnoreSmsDraftUseCase @Inject constructor(
    private val repository: SmsDraftRepository
) {
    suspend operator fun invoke(id: Long) {
        repository.updateStatus(id, SmsDraftStatus.IGNORED)
    }
}
