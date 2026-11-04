package com.android.zenspend.feature.emi.domain.repository

import com.android.zenspend.feature.emi.domain.model.EMI
import com.android.zenspend.feature.emi.domain.model.EMIPayment
import kotlinx.coroutines.flow.Flow

interface EMIRepository {
    fun getAllActiveEMIs(): Flow<List<EMI>>

    /** Active and finished EMIs, active first. Removed EMIs are never returned. */
    fun observeAllEMIs(): Flow<List<EMI>>
    fun observeEMI(id: Long): Flow<EMI?>
    fun getActiveEMIsByWalletId(walletId: Long): Flow<List<EMI>>
    fun observeActiveEmiCountByWalletId(walletId: Long): Flow<Int>
    suspend fun getEMIById(id: Long): EMI?
    suspend fun insertEMI(emi: EMI): Long
    suspend fun updateEMI(emi: EMI)

    /** Hides the EMI everywhere (kept in the database so sync can propagate the removal). */
    suspend fun archiveEMI(id: Long)

    fun observePayments(emiId: Long): Flow<List<EMIPayment>>
    suspend fun getLastPayment(emiId: Long): EMIPayment?
    suspend fun insertPayment(payment: EMIPayment): Long
    suspend fun deletePayment(id: Long)
}
