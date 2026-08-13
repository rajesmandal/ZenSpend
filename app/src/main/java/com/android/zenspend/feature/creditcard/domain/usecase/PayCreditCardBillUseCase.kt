package com.android.zenspend.feature.creditcard.domain.usecase

import com.android.zenspend.core.common.result.Resource
import com.android.zenspend.core.database.model.TransactionSource
import com.android.zenspend.core.database.model.TransactionType
import com.android.zenspend.feature.transaction.domain.model.Transaction
import com.android.zenspend.feature.transaction.domain.usecase.AddTransactionUseCase
import javax.inject.Inject

class PayCreditCardBillUseCase @Inject constructor(
    private val addTransactionUseCase: AddTransactionUseCase
) {
    suspend operator fun invoke(
        amount: Long,
        sourceWalletId: Long,
        creditCardWalletId: Long,
        date: Long = System.currentTimeMillis(),
        note: String? = null
    ): Resource<Long> {
        val transaction = Transaction(
            title = "Credit Card Bill Payment",
            amount = amount,
            date = date,
            type = TransactionType.CREDIT_CARD_PAYMENT,
            sourceWalletId = sourceWalletId,
            destinationWalletId = creditCardWalletId,
            note = note,
            source = TransactionSource.MANUAL
        )
        return addTransactionUseCase(transaction)
    }
}
