package com.android.zenspend.feature.emi.domain.usecase

import com.android.zenspend.core.database.model.EMILenderType
import com.android.zenspend.core.database.model.EMIStatus
import com.android.zenspend.feature.creditcard.domain.repository.CreditCardRepository
import com.android.zenspend.feature.emi.domain.model.EMI
import com.android.zenspend.feature.emi.domain.model.EmiSchedule
import com.android.zenspend.feature.emi.domain.repository.EMIRepository
import javax.inject.Inject

/** What the user typed on the EMI form, already converted to numbers (amounts in minor units). */
data class EmiDraft(
    val lenderType: EMILenderType,
    /** Used for bank / person / other. For a credit card the card's bank name is used instead. */
    val lenderName: String,
    val creditCardId: Long?,
    val name: String,
    val principalAmount: Long,
    val emiAmount: Long,
    val totalTenure: Int,
    /** Installments already paid before the EMI was added to the app. Ignored when editing. */
    val paidInstallments: Int,
    val startDate: Long,
    val dueDay: Int,
    val interestRate: Double?,
    val note: String?
)

/** Creates a new EMI, or updates [existingId] while keeping the installments paid so far. */
class SaveEMIUseCase @Inject constructor(
    private val emiRepository: EMIRepository,
    private val creditCardRepository: CreditCardRepository
) {
    suspend operator fun invoke(draft: EmiDraft, existingId: Long? = null): Long {
        require(draft.name.isNotBlank()) { "EMI name is required" }
        require(draft.principalAmount > 0) { "Principal amount must be greater than zero" }
        require(draft.emiAmount > 0) { "EMI amount must be greater than zero" }
        require(draft.totalTenure > 0) { "Tenure must be at least 1 month" }
        require(draft.dueDay in 1..31) { "Due day must be between 1 and 31" }

        var walletId: Long? = null
        var lenderName = draft.lenderName.trim()
        if (draft.lenderType == EMILenderType.CREDIT_CARD) {
            val cardId = draft.creditCardId ?: throw IllegalArgumentException("Choose a credit card")
            val card = creditCardRepository.getCreditCardById(cardId)
                ?: throw IllegalArgumentException("Credit card not found")
            walletId = card.walletId
            val last4 = card.lastFourDigits?.let { " ••••$it" }.orEmpty()
            lenderName = "${card.bankName}$last4"
        } else {
            require(lenderName.isNotBlank()) { "Enter who this EMI is owed to" }
        }

        val existing = existingId?.let { emiRepository.getEMIById(it) }
        val paid = if (existing != null) EmiSchedule.paidInstallments(existing) else draft.paidInstallments
        require(paid in 0..draft.totalTenure) { "Paid installments cannot be more than the tenure" }
        val remaining = draft.totalTenure - paid

        val base = EMI(
            id = existing?.id ?: 0,
            name = draft.name.trim(),
            walletId = walletId,
            principalAmount = draft.principalAmount,
            emiAmount = draft.emiAmount,
            interestRate = draft.interestRate,
            totalTenure = draft.totalTenure,
            remainingInstallments = remaining,
            startDate = draft.startDate,
            endDate = 0L,
            dueDay = draft.dueDay,
            status = when {
                remaining == 0 -> EMIStatus.COMPLETED
                existing != null && existing.status == EMIStatus.CANCELLED -> EMIStatus.CANCELLED
                else -> EMIStatus.ACTIVE
            },
            lenderType = draft.lenderType,
            lenderName = lenderName,
            note = draft.note?.trim()?.takeIf { it.isNotEmpty() }
        )
        val emi = base.copy(endDate = EmiSchedule.finalDueDate(base))

        return if (existing != null) {
            emiRepository.updateEMI(emi)
            existing.id
        } else {
            emiRepository.insertEMI(emi)
        }
    }
}
