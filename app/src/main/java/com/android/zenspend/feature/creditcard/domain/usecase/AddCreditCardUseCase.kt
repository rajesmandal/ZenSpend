package com.android.zenspend.feature.creditcard.domain.usecase

import com.android.zenspend.core.database.model.WalletType
import com.android.zenspend.feature.creditcard.domain.model.CreditCard
import com.android.zenspend.feature.creditcard.domain.repository.CreditCardRepository
import com.android.zenspend.feature.wallet.domain.model.Wallet
import com.android.zenspend.feature.wallet.domain.repository.WalletRepository
import javax.inject.Inject

class AddCreditCardUseCase @Inject constructor(
    private val walletRepository: WalletRepository,
    private val creditCardRepository: CreditCardRepository
) {
    suspend operator fun invoke(
        name: String,
        bankName: String,
        lastFourDigits: String?,
        creditLimit: Long,
        billingCycleDate: Int,
        paymentDueDate: Int
    ): Long {
        // 1. Create a Wallet for the credit card
        val walletId = walletRepository.insertWallet(
            Wallet(
                name = "$bankName - $name",
                type = WalletType.CREDIT_CARD,
                openingBalance = 0, // Initial outstanding is usually 0
                currentBalance = 0
            )
        )

        // 2. Create the CreditCard metadata
        val creditCard = CreditCard(
            walletId = walletId,
            name = name,
            bankName = bankName,
            lastFourDigits = lastFourDigits,
            creditLimit = creditLimit,
            billingCycleDate = billingCycleDate,
            paymentDueDate = paymentDueDate
        )
        
        return creditCardRepository.insertCreditCard(creditCard)
    }
}
