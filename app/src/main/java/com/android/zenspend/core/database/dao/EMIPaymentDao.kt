package com.android.zenspend.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.android.zenspend.core.database.entity.EMIPaymentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EMIPaymentDao {
    @Query("SELECT * FROM emi_payments WHERE emiId = :emiId ORDER BY installmentNumber ASC")
    fun observePayments(emiId: Long): Flow<List<EMIPaymentEntity>>

    @Query("SELECT * FROM emi_payments WHERE emiId = :emiId ORDER BY installmentNumber DESC LIMIT 1")
    suspend fun getLastPayment(emiId: Long): EMIPaymentEntity?

    @Insert
    suspend fun insertPayment(payment: EMIPaymentEntity): Long

    @Query("DELETE FROM emi_payments WHERE id = :id")
    suspend fun deletePayment(id: Long)
}
