package com.android.zenspend.feature.creditcard.data.repository

import com.android.zenspend.core.database.dao.CreditCardDao
import com.android.zenspend.core.database.dao.TransactionDao
import com.android.zenspend.feature.creditcard.data.mapper.toDomain
import com.android.zenspend.feature.creditcard.data.mapper.toEntity
import com.android.zenspend.feature.creditcard.domain.model.CreditCard
import com.android.zenspend.feature.creditcard.domain.repository.CreditCardRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CreditCardRepositoryImpl @Inject constructor(
    private val creditCardDao: CreditCardDao,
    private val transactionDao: TransactionDao
) : CreditCardRepository {
    override fun getAllActiveCreditCards(): Flow<List<CreditCard>> =
        creditCardDao.getAllActiveCreditCards().map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun getCreditCardById(id: Long): CreditCard? =
        creditCardDao.getCreditCardById(id)?.toDomain()

    override suspend fun insertCreditCard(creditCard: CreditCard): Long =
        creditCardDao.insertCreditCard(creditCard.toEntity())

    override suspend fun updateCreditCard(creditCard: CreditCard) =
        creditCardDao.updateCreditCard(creditCard.toEntity())

    override fun observeCreditCardOutstanding(walletId: Long): Flow<Long?> =
        transactionDao.getCalculatedBalance(walletId).map { it?.let { balance -> -balance } }
}
