package com.android.zenspend.feature.emi.domain.usecase

import com.android.zenspend.core.database.model.EMIStatus
import com.android.zenspend.feature.emi.domain.repository.EMIRepository
import javax.inject.Inject

class RecordEmiInstallmentPaidUseCase @Inject constructor(
    private val emiRepository: EMIRepository
) {
    suspend operator fun invoke(emiId: Long) {
        val emi = emiRepository.getEMIById(emiId)
            ?: throw IllegalArgumentException("EMI not found")
        if (emi.remainingInstallments <= 0) return

        val newRemaining = emi.remainingInstallments - 1
        emiRepository.updateEMI(
            emi.copy(
                remainingInstallments = newRemaining,
                status = if (newRemaining == 0) EMIStatus.COMPLETED else EMIStatus.ACTIVE
            )
        )
    }
}
