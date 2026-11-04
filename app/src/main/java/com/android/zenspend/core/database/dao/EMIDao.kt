package com.android.zenspend.core.database.dao

import androidx.room.*
import com.android.zenspend.core.database.entity.EMIEntity
import com.android.zenspend.core.database.model.EMIStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface EMIDao {
    @Query("SELECT * FROM emis WHERE status = :status AND isDeleted = 0")
    fun getEMIsByStatus(status: EMIStatus = EMIStatus.ACTIVE): Flow<List<EMIEntity>>

    /** Every EMI that has not been removed, active ones first. */
    @Query("SELECT * FROM emis WHERE isDeleted = 0 ORDER BY CASE status WHEN 'ACTIVE' THEN 0 ELSE 1 END, id DESC")
    fun observeAllEMIs(): Flow<List<EMIEntity>>

    @Query("SELECT * FROM emis WHERE id = :id AND isDeleted = 0")
    fun observeEMIById(id: Long): Flow<EMIEntity?>

    @Query("SELECT * FROM emis WHERE walletId = :walletId AND status = :status AND isDeleted = 0")
    fun getEMIsByWalletId(walletId: Long, status: EMIStatus = EMIStatus.ACTIVE): Flow<List<EMIEntity>>

    @Query("SELECT COUNT(*) FROM emis WHERE walletId = :walletId AND status = :status AND isDeleted = 0")
    fun observeActiveEmiCountByWalletId(walletId: Long, status: EMIStatus = EMIStatus.ACTIVE): Flow<Int>

    @Query("SELECT * FROM emis WHERE id = :id")
    suspend fun getEMIById(id: Long): EMIEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEMI(emi: EMIEntity): Long

    @Update
    suspend fun updateEMI(emi: EMIEntity)
}
