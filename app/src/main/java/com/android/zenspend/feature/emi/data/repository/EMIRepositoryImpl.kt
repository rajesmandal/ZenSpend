package com.android.zenspend.feature.emi.data.repository

import com.android.zenspend.core.database.dao.EMIDao
import com.android.zenspend.core.database.dao.EMIPaymentDao
import com.android.zenspend.feature.emi.data.mapper.toDomain
import com.android.zenspend.feature.emi.data.mapper.toEntity
import com.android.zenspend.feature.emi.domain.model.EMI
import com.android.zenspend.feature.emi.domain.model.EMIPayment
import com.android.zenspend.feature.emi.domain.repository.EMIRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class EMIRepositoryImpl @Inject constructor(
    private val emiDao: EMIDao,
    private val paymentDao: EMIPaymentDao
) : EMIRepository {
    override fun getAllActiveEMIs(): Flow<List<EMI>> =
        emiDao.getEMIsByStatus().map { entities ->
            entities.map { it.toDomain() }
        }

    override fun observeAllEMIs(): Flow<List<EMI>> =
        emiDao.observeAllEMIs().map { entities -> entities.map { it.toDomain() } }

    override fun observeEMI(id: Long): Flow<EMI?> =
        emiDao.observeEMIById(id).map { it?.toDomain() }

    override fun getActiveEMIsByWalletId(walletId: Long): Flow<List<EMI>> =
        emiDao.getEMIsByWalletId(walletId).map { entities ->
            entities.map { it.toDomain() }
        }

    override fun observeActiveEmiCountByWalletId(walletId: Long): Flow<Int> =
        emiDao.observeActiveEmiCountByWalletId(walletId)

    override suspend fun getEMIById(id: Long): EMI? =
        emiDao.getEMIById(id)?.toDomain()

    override suspend fun insertEMI(emi: EMI): Long =
        emiDao.insertEMI(emi.toEntity())

    override suspend fun updateEMI(emi: EMI) =
        emiDao.updateEMI(emi.toEntity())

    override suspend fun archiveEMI(id: Long) {
        val entity = emiDao.getEMIById(id) ?: return
        emiDao.updateEMI(entity.copy(isDeleted = true, updatedAt = System.currentTimeMillis()))
    }

    override fun observePayments(emiId: Long): Flow<List<EMIPayment>> =
        paymentDao.observePayments(emiId).map { list -> list.map { it.toDomain() } }

    override suspend fun getLastPayment(emiId: Long): EMIPayment? =
        paymentDao.getLastPayment(emiId)?.toDomain()

    override suspend fun insertPayment(payment: EMIPayment): Long =
        paymentDao.insertPayment(payment.toEntity())

    override suspend fun deletePayment(id: Long) = paymentDao.deletePayment(id)
}
