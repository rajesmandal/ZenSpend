package com.android.zenspend.feature.emi.domain.usecase

import com.android.zenspend.feature.creditcard.domain.repository.CreditCardRepository
import com.android.zenspend.feature.emi.domain.model.EMI
import com.android.zenspend.feature.emi.domain.repository.EMIRepository
import java.util.Calendar
import javax.inject.Inject

class AddEMIUseCase @Inject constructor(
    private val emiRepository: EMIRepository,
    private val creditCardRepository: CreditCardRepository
) {
    suspend operator fun invoke(
        creditCardId: Long,
        name: String,
        principalAmount: Long,
        emiAmount: Long,
        totalTenure: Int,
        startDate: Long,
        dueDay: Int,
        interestRate: Double? = null
    ): Long {
        val card = creditCardRepository.getCreditCardById(creditCardId)
            ?: throw IllegalArgumentException("Credit card not found")
        require(name.isNotBlank()) { "EMI name is required" }
        require(principalAmount > 0) { "Principal amount must be greater than zero" }
        require(emiAmount > 0) { "EMI amount must be greater than zero" }
        require(totalTenure > 0) { "Tenure must be at least 1 month" }
        require(dueDay in 1..31) { "Due day must be between 1 and 31" }

        val endDate = Calendar.getInstance().apply {
            timeInMillis = startDate
            add(Calendar.MONTH, totalTenure)
        }.timeInMillis

        val emi = EMI(
            name = name.trim(),
            walletId = card.walletId,
            principalAmount = principalAmount,
            emiAmount = emiAmount,
            interestRate = interestRate,
            totalTenure = totalTenure,
            remainingInstallments = totalTenure,
            startDate = startDate,
            endDate = endDate,
            dueDay = dueDay
        )
        return emiRepository.insertEMI(emi)
    }
}
