package com.android.zenspend.core.database.dao

import androidx.room.*
import com.android.zenspend.core.database.entity.*
import com.android.zenspend.core.database.model.SyncState
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncQueueDao {

    @Query("SELECT * FROM wallets WHERE householdId IS NOT NULL AND syncState != 'SYNCED' ORDER BY updatedAt ASC")
    suspend fun getPendingWallets(): List<WalletEntity>

    @Query("SELECT * FROM categories WHERE householdId IS NOT NULL AND syncState != 'SYNCED' ORDER BY updatedAt ASC")
    suspend fun getPendingCategories(): List<CategoryEntity>

    @Query("SELECT * FROM credit_cards WHERE householdId IS NOT NULL AND syncState != 'SYNCED' ORDER BY updatedAt ASC")
    suspend fun getPendingCreditCards(): List<CreditCardEntity>

    @Query("SELECT * FROM emis WHERE householdId IS NOT NULL AND syncState != 'SYNCED' ORDER BY updatedAt ASC")
    suspend fun getPendingEmis(): List<EMIEntity>

    @Query("SELECT * FROM recurring_transactions WHERE householdId IS NOT NULL AND syncState != 'SYNCED' ORDER BY updatedAt ASC")
    suspend fun getPendingRecurring(): List<RecurringTransactionEntity>

    @Query("SELECT * FROM budgets WHERE householdId IS NOT NULL AND syncState != 'SYNCED' ORDER BY updatedAt ASC")
    suspend fun getPendingBudgets(): List<BudgetEntity>

    @Query("SELECT * FROM transactions WHERE householdId IS NOT NULL AND syncState != 'SYNCED' ORDER BY updatedAt ASC")
    suspend fun getPendingTransactions(): List<TransactionEntity>

    @Query("SELECT * FROM wallets WHERE remoteId = :remoteId LIMIT 1")
    suspend fun getWalletByRemoteId(remoteId: String): WalletEntity?

    @Query("SELECT * FROM categories WHERE remoteId = :remoteId LIMIT 1")
    suspend fun getCategoryByRemoteId(remoteId: String): CategoryEntity?

    @Query("SELECT * FROM credit_cards WHERE remoteId = :remoteId LIMIT 1")
    suspend fun getCreditCardByRemoteId(remoteId: String): CreditCardEntity?

    @Query("SELECT * FROM emis WHERE remoteId = :remoteId LIMIT 1")
    suspend fun getEmiByRemoteId(remoteId: String): EMIEntity?

    @Query("SELECT * FROM recurring_transactions WHERE remoteId = :remoteId LIMIT 1")
    suspend fun getRecurringByRemoteId(remoteId: String): RecurringTransactionEntity?

    @Query("SELECT * FROM budgets WHERE remoteId = :remoteId LIMIT 1")
    suspend fun getBudgetByRemoteId(remoteId: String): BudgetEntity?

    @Query("SELECT * FROM transactions WHERE remoteId = :remoteId LIMIT 1")
    suspend fun getTransactionByRemoteId(remoteId: String): TransactionEntity?

    @Update
    suspend fun updateWallet(entity: WalletEntity)

    @Update
    suspend fun updateCategory(entity: CategoryEntity)

    @Update
    suspend fun updateCreditCard(entity: CreditCardEntity)

    @Update
    suspend fun updateEmi(entity: EMIEntity)

    @Update
    suspend fun updateRecurring(entity: RecurringTransactionEntity)

    @Update
    suspend fun updateBudget(entity: BudgetEntity)

    @Update
    suspend fun updateTransaction(entity: TransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallet(entity: WalletEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(entity: CategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCreditCard(entity: CreditCardEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmi(entity: EMIEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurring(entity: RecurringTransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(entity: BudgetEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(entity: TransactionEntity): Long

    @Query("UPDATE wallets SET syncState = :state WHERE id = :id")
    suspend fun updateWalletSyncState(id: Long, state: SyncState)

    @Query("UPDATE transactions SET syncState = :state WHERE id = :id")
    suspend fun updateTransactionSyncState(id: Long, state: SyncState)
}
