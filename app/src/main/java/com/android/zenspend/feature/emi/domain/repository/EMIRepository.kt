package com.android.zenspend.feature.emi.domain.repository

import com.android.zenspend.feature.emi.domain.model.EMI
import kotlinx.coroutines.flow.Flow

interface EMIRepository {
    fun getAllActiveEMIs(): Flow<List<EMI>>
    suspend fun getEMIById(id: Long): EMI?
    suspend fun insertEMI(emi: EMI): Long
    suspend fun updateEMI(emi: EMI)
}
