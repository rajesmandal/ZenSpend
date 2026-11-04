package com.android.zenspend.feature.emi.domain.usecase

import com.android.zenspend.core.common.result.Resource
import com.android.zenspend.core.database.model.EMIStatus
import com.android.zenspend.core.database.model.TransactionType
import com.android.zenspend.feature.emi.domain.model.EMIPayment
import com.android.zenspend.feature.emi.domain.model.EmiSchedule
import com.android.zenspend.feature.emi.domain.repository.EMIRepository
import com.android.zenspend.feature.transaction.domain.model.Transaction
import com.android.zenspend.feature.transaction.domain.usecase.AddTransactionUseCase
import javax.inject.Inject

/**
 * Marks the next installment as paid. When [payFromWalletId] is given, an expense is also recorded
 * from that wallet (and linked to the EMI) so balances and reports stay correct.
 */
class RecordEmiInstallmentPaidUseCase @Inject constructor(
    private val emiRepository: EMIRepository,
    private val addTransaction: AddTransactionUseCase
) {
    suspend operator fun invoke(
        emiId: Long,
        payFromWalletId: Long? = null,
        paidDate: Long = System.currentTimeMillis()
    ) {
        val emi = emiRepository.getEMIById(emiId)
            ?: throw IllegalArgumentException("EMI not found")
        if (emi.remainingInstallments <= 0) return

        val number = EmiSchedule.paidInstallments(emi) + 1
        val dueDate = EmiSchedule.installmentDueDate(emi, number)

        var transactionId: Long? = null
        if (payFromWalletId != null) {
            val result = addTransaction(
                Transaction(
                    title = "EMI: ${emi.name}",
                    amount = emi.emiAmount,
                    date = paidDate,
                    note = emi.lenderName.takeIf { it.isNotBlank() },
                    type = TransactionType.EXPENSE,
                    sourceWalletId = payFromWalletId,
                    emiId = emi.id
                )
            )
            transactionId = when (result) {
                is Resource.Success -> result.data
                else -> throw IllegalStateException("Could not record the payment in the wallet")
            }
        }

        emiRepository.insertPayment(
            EMIPayment(
                emiId = emi.id,
                installmentNumber = number,
                amount = emi.emiAmount,
                dueDate = dueDate,
                paidDate = paidDate,
                walletId = payFromWalletId,
                transactionId = transactionId
            )
        )

        val remaining = emi.remainingInstallments - 1
        emiRepository.updateEMI(
            emi.copy(
                remainingInstallments = remaining,
                status = if (remaining == 0) EMIStatus.COMPLETED else EMIStatus.ACTIVE
            )
        )
    }
}
