package com.android.zenspend.core.database.dao

import androidx.room.*
import com.android.zenspend.core.database.entity.WalletEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WalletDao {
    @Query("""
        SELECT 
            *,
            openingBalance + 
            COALESCE((SELECT SUM(amount) FROM transactions WHERE destinationWalletId = wallets.id AND status = 'COMPLETED'), 0) -
            COALESCE((SELECT SUM(amount) FROM transactions WHERE sourceWalletId = wallets.id AND status = 'COMPLETED'), 0) as currentBalance
        FROM wallets
        WHERE isActive = 1
    """)
    fun getAllActiveWallets(): Flow<List<com.android.zenspend.core.database.model.WalletWithBalance>>

    @Query("""
        SELECT 
            *,
            openingBalance + 
            COALESCE((SELECT SUM(amount) FROM transactions WHERE destinationWalletId = wallets.id AND status = 'COMPLETED'), 0) -
            COALESCE((SELECT SUM(amount) FROM transactions WHERE sourceWalletId = wallets.id AND status = 'COMPLETED'), 0) as currentBalance
        FROM wallets
        WHERE id = :id
    """)
    suspend fun getWalletById(id: Long): com.android.zenspend.core.database.model.WalletWithBalance?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallet(wallet: WalletEntity): Long

    @Update
    suspend fun updateWallet(wallet: WalletEntity)

    @Query("UPDATE wallets SET isActive = 0 WHERE id = :id")
    suspend fun softDeleteWallet(id: Long)

    @Delete
    suspend fun deleteWallet(wallet: WalletEntity)
}
