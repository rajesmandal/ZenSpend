package com.android.zenspend.feature.creditcard.domain.repository

import com.android.zenspend.feature.creditcard.domain.model.CreditCard
import kotlinx.coroutines.flow.Flow

interface CreditCardRepository {
    fun getAllActiveCreditCards(): Flow<List<CreditCard>>
    suspend fun getCreditCardById(id: Long): CreditCard?
    suspend fun insertCreditCard(creditCard: CreditCard): Long
    suspend fun updateCreditCard(creditCard: CreditCard)
    fun observeCreditCardOutstanding(walletId: Long): Flow<Long?>
}
