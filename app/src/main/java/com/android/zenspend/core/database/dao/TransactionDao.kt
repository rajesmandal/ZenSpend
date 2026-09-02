package com.android.zenspend.core.database.dao

import androidx.room.*
import com.android.zenspend.core.database.entity.TransactionEntity
import com.android.zenspend.core.database.model.TransactionType
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE isDeleted = 0 ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE sourceWalletId = :walletId OR destinationWalletId = :walletId ORDER BY date DESC")
    fun getTransactionsByWallet(walletId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE categoryId = :categoryId ORDER BY date DESC")
    fun getTransactionsByCategory(categoryId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE recurringOccurrenceKey = :key")
    suspend fun getTransactionByOccurrenceKey(key: String): TransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)

    @Transaction
    suspend fun insertTransactionAndUpdateBalance(
        transaction: TransactionEntity,
        sourceWalletId: Long,
        sourceNewBalance: Long,
        destWalletId: Long? = null,
        destNewBalance: Long? = null
    ) {
        insertTransaction(transaction)
    }

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    // Calculated balance: Opening Balance + Total Incoming - Total Outgoing
    @Query("""
        SELECT 
            (SELECT openingBalance FROM wallets WHERE id = :walletId) +
            COALESCE((SELECT SUM(amount) FROM transactions WHERE destinationWalletId = :walletId AND status = 'COMPLETED'), 0) -
            COALESCE((SELECT SUM(amount) FROM transactions WHERE sourceWalletId = :walletId AND status = 'COMPLETED'), 0)
    """)
    fun getCalculatedBalance(walletId: Long): Flow<Long?>

    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM transactions 
        WHERE type = :type 
        AND status = 'COMPLETED'
        AND isExcludedFromReports = 0
        AND date BETWEEN :startDate AND :endDate
    """)
    fun getTotalAmountByType(type: TransactionType, startDate: Long, endDate: Long): Flow<Long>

    @Query("""
        SELECT 
            (SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = :expenseType AND status = 'COMPLETED' AND isExcludedFromReports = 0 AND date BETWEEN :startDate AND :endDate) - 
            (SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = :refundType AND status = 'COMPLETED' AND isExcludedFromReports = 0 AND date BETWEEN :startDate AND :endDate)
    """)
    fun getTotalNetExpense(expenseType: TransactionType, refundType: TransactionType, startDate: Long, endDate: Long): Flow<Long>

    @Query("""
        SELECT COALESCE(
            SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END) - 
            SUM(CASE WHEN type = 'REFUND' THEN amount ELSE 0 END), 
            0
        ) FROM transactions 
        WHERE (type = 'EXPENSE' OR type = 'REFUND')
        AND categoryId = :categoryId
        AND status = 'COMPLETED'
        AND date BETWEEN :startDate AND :endDate
    """)
    fun getCategoryTotalExpense(categoryId: Long, startDate: Long, endDate: Long): Flow<Long>

    @Query("""
        SELECT * FROM transactions 
        WHERE (:walletId IS NULL OR sourceWalletId = :walletId OR destinationWalletId = :walletId)
        AND (:categoryId IS NULL OR categoryId = :categoryId)
        AND (:type IS NULL OR type = :type)
        AND (:searchQuery IS NULL OR title LIKE '%' || :searchQuery || '%' OR note LIKE '%' || :searchQuery || '%')
        AND (:startDate IS NULL OR date >= :startDate)
        AND (:endDate IS NULL OR date <= :endDate)
        ORDER BY date DESC
    """)
    fun getFilteredTransactions(
        walletId: Long? = null,
        categoryId: Long? = null,
        type: String? = null,
        searchQuery: String? = null,
        startDate: Long? = null,
        endDate: Long? = null
    ): Flow<List<TransactionEntity>>

    @Query("""
        SELECT 
            c.id as categoryId, 
            c.name as categoryName, 
            COALESCE(
                SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amount WHEN t.type = 'REFUND' THEN -t.amount ELSE 0 END), 
                0
            ) as totalAmount,
            MAX(CASE WHEN t.type = 'EXPENSE' THEN t.amount ELSE 0 END) as maxAmount,
            COUNT(t.id) as transactionCount
        FROM categories c
        INNER JOIN transactions t ON t.categoryId = c.id
        WHERE t.date BETWEEN :startDate AND :endDate
        AND t.status = 'COMPLETED'
        GROUP BY c.id
        HAVING totalAmount > 0
        ORDER BY totalAmount DESC
    """)
    fun getCategorySpending(startDate: Long, endDate: Long): Flow<List<com.android.zenspend.core.database.model.CategorySpendingEntity>>

    @Query("""
        SELECT 
            c.id as categoryId, 
            c.name as categoryName, 
            SUM(t.amount) as totalAmount,
            MAX(t.amount) as maxAmount,
            COUNT(t.id) as transactionCount
        FROM transactions t
        INNER JOIN categories c ON t.categoryId = c.id
        WHERE t.type = 'INCOME' 
        AND t.date BETWEEN :startDate AND :endDate
        AND t.status = 'COMPLETED'
        GROUP BY c.id
        ORDER BY totalAmount DESC
    """)
    fun getCategoryIncome(startDate: Long, endDate: Long): Flow<List<com.android.zenspend.core.database.model.CategorySpendingEntity>>

    @Query("""
        SELECT 
            date(date / 1000, 'unixepoch') as date,
            SUM(CASE WHEN type = 'INCOME' THEN amount ELSE 0 END) as incomeAmount,
            SUM(CASE WHEN type = 'EXPENSE' THEN amount WHEN type = 'REFUND' THEN -amount ELSE 0 END) as expenseAmount
        FROM transactions
        WHERE date BETWEEN :startDate AND :endDate
        AND status = 'COMPLETED'
        GROUP BY date
        ORDER BY date ASC
    """)
    fun getDailyTrend(startDate: Long, endDate: Long): Flow<List<com.android.zenspend.core.database.model.TrendPointEntity>>

    @Query("""
        SELECT 
            w.id as walletId,
            w.name as walletName,
            SUM(CASE WHEN (t.sourceWalletId = w.id AND t.type = 'INCOME') OR (t.destinationWalletId = w.id AND t.type != 'INCOME') THEN t.amount ELSE 0 END) as incomeAmount,
            SUM(CASE WHEN (t.sourceWalletId = w.id AND t.type = 'EXPENSE') OR (t.sourceWalletId = w.id AND t.type = 'TRANSFER') THEN t.amount ELSE 0 END) as expenseAmount,
            0 as netMovement
        FROM wallets w
        LEFT JOIN transactions t ON (t.sourceWalletId = w.id OR t.destinationWalletId = w.id)
        WHERE (t.date IS NULL OR t.date BETWEEN :startDate AND :endDate)
        GROUP BY w.id
    """)
    fun getWalletActivity(startDate: Long, endDate: Long): Flow<List<com.android.zenspend.core.database.model.WalletActivityEntity>>
}
