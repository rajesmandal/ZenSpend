package com.android.zenspend.core.database.dao

import androidx.room.*
import com.android.zenspend.core.database.entity.CreditCardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CreditCardDao {
    @Query("SELECT * FROM credit_cards WHERE active = 1")
    fun getAllActiveCreditCards(): Flow<List<CreditCardEntity>>

    @Query("SELECT * FROM credit_cards WHERE id = :id")
    suspend fun getCreditCardById(id: Long): CreditCardEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCreditCard(creditCard: CreditCardEntity): Long

    @Update
    suspend fun updateCreditCard(creditCard: CreditCardEntity)
}
