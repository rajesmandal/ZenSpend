package com.android.zenspend.core.database.dao

import androidx.room.*
import com.android.zenspend.core.database.entity.EMIEntity
import com.android.zenspend.core.database.model.EMIStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface EMIDao {
    @Query("SELECT * FROM emis WHERE status = :status")
    fun getEMIsByStatus(status: EMIStatus = EMIStatus.ACTIVE): Flow<List<EMIEntity>>

    @Query("SELECT * FROM emis WHERE id = :id")
    suspend fun getEMIById(id: Long): EMIEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEMI(emi: EMIEntity): Long

    @Update
    suspend fun updateEMI(emi: EMIEntity)
}
