package com.android.zenspend.feature.emi.domain.usecase

import com.android.zenspend.core.database.model.EMIStatus
import com.android.zenspend.feature.emi.domain.model.EMI
import com.android.zenspend.feature.emi.domain.repository.EMIRepository
import com.android.zenspend.feature.transaction.domain.repository.TransactionRepository
import javax.inject.Inject

/** Takes back the most recent payment (and the wallet expense it created, if any). */
class UndoLastEmiPaymentUseCase @Inject constructor(
    private val emiRepository: EMIRepository,
    private val transactionRepository: TransactionRepository
) {
    /** @return false when there was nothing to undo. */
    suspend operator fun invoke(emiId: Long): Boolean {
        val emi = emiRepository.getEMIById(emiId) ?: return false
        val last = emiRepository.getLastPayment(emiId) ?: return false

        last.transactionId?.let { id ->
            transactionRepository.getTransactionById(id)?.let { transactionRepository.deleteTransaction(it) }
        }
        emiRepository.deletePayment(last.id)
        emiRepository.updateEMI(
            emi.copy(
                remainingInstallments = (emi.remainingInstallments + 1).coerceAtMost(emi.totalTenure),
                status = EMIStatus.ACTIVE
            )
        )
        return true
    }
}

/** Stops tracking an EMI that was foreclosed or cancelled; its history stays visible. */
class CloseEmiUseCase @Inject constructor(
    private val emiRepository: EMIRepository
) {
    suspend operator fun invoke(emiId: Long) {
        val emi: EMI = emiRepository.getEMIById(emiId) ?: return
        emiRepository.updateEMI(emi.copy(status = EMIStatus.CANCELLED))
    }
}

/** Removes an EMI from the app (its payment history goes with it; wallet expenses stay). */
class DeleteEmiUseCase @Inject constructor(
    private val emiRepository: EMIRepository
) {
    suspend operator fun invoke(emiId: Long) = emiRepository.archiveEMI(emiId)
}
