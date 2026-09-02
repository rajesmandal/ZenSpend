package com.android.zenspend.feature.emi.data.repository

import com.android.zenspend.core.database.dao.EMIDao
import com.android.zenspend.feature.emi.data.mapper.toDomain
import com.android.zenspend.feature.emi.data.mapper.toEntity
import com.android.zenspend.feature.emi.domain.model.EMI
import com.android.zenspend.feature.emi.domain.repository.EMIRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class EMIRepositoryImpl @Inject constructor(
    private val emiDao: EMIDao
) : EMIRepository {
    override fun getAllActiveEMIs(): Flow<List<EMI>> =
        emiDao.getEMIsByStatus().map { entities ->
            entities.map { it.toDomain() }
        }

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
}
