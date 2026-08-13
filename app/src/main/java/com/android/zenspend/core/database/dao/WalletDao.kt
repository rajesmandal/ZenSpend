package com.android.zenspend.core.database.dao

import androidx.room.*
import com.android.zenspend.core.database.entity.WalletEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WalletDao {
    @Query("SELECT * FROM wallets WHERE isActive = 1")
    fun getAllActiveWallets(): Flow<List<WalletEntity>>

    @Query("SELECT * FROM wallets WHERE id = :id")
    suspend fun getWalletById(id: Long): WalletEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallet(wallet: WalletEntity): Long

    @Update
    suspend fun updateWallet(wallet: WalletEntity)

    @Query("UPDATE wallets SET currentBalance = :newBalance, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateBalance(id: Long, newBalance: Long, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE wallets SET isActive = 0 WHERE id = :id")
    suspend fun softDeleteWallet(id: Long)

    @Delete
    suspend fun deleteWallet(wallet: WalletEntity)
}
