package com.android.zenspend.feature.emi.domain.usecase

import com.android.zenspend.feature.emi.domain.model.EMI
import com.android.zenspend.feature.emi.domain.repository.EMIRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveEMIsUseCase @Inject constructor(
    private val repository: EMIRepository
) {
    operator fun invoke(): Flow<List<EMI>> = repository.getAllActiveEMIs()
}
