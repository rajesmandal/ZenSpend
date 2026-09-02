package com.android.zenspend.core.database.dao

import androidx.room.*
import com.android.zenspend.core.database.entity.*

@Dao
interface BackupDao {
    @Query("SELECT * FROM wallets")
    suspend fun getAllWallets(): List<WalletEntity>

    @Query("SELECT * FROM categories")
    suspend fun getAllCategories(): List<CategoryEntity>

    @Query("SELECT * FROM credit_cards")
    suspend fun getAllCreditCards(): List<CreditCardEntity>

    @Query("SELECT * FROM emis")
    suspend fun getAllEmis(): List<EMIEntity>

    @Query("SELECT * FROM recurring_transactions")
    suspend fun getAllRecurringTransactions(): List<RecurringTransactionEntity>

    @Query("SELECT * FROM tags")
    suspend fun getAllTags(): List<TagEntity>

    @Query("SELECT * FROM transactions")
    suspend fun getAllTransactions(): List<TransactionEntity>

    @Query("SELECT * FROM transaction_tag_cross_ref")
    suspend fun getAllTransactionTags(): List<TransactionTagCrossRef>

    @Query("SELECT * FROM attachments")
    suspend fun getAllAttachments(): List<AttachmentEntity>

    @Query("SELECT * FROM budgets")
    suspend fun getAllBudgets(): List<BudgetEntity>

    @Query("SELECT * FROM sms_drafts")
    suspend fun getAllSmsDrafts(): List<com.android.zenspend.feature.sms.data.entity.SmsDraftEntity>

    @Query("SELECT * FROM merchant_category_preferences")
    suspend fun getAllMerchantPreferences(): List<com.android.zenspend.feature.category.data.entity.MerchantCategoryPreferenceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallets(wallets: List<WalletEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCreditCards(cards: List<CreditCardEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmis(emis: List<EMIEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurringTransactions(recurring: List<RecurringTransactionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTags(tags: List<TagEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactionTags(tags: List<TransactionTagCrossRef>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttachments(attachments: List<AttachmentEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudgets(budgets: List<BudgetEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSmsDrafts(drafts: List<com.android.zenspend.feature.sms.data.entity.SmsDraftEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMerchantPreferences(prefs: List<com.android.zenspend.feature.category.data.entity.MerchantCategoryPreferenceEntity>)

    @Query("""
        SELECT 
            t.date, t.title, t.type, t.amount, 
            c.name as categoryName, 
            sw.name as sourceWalletName, 
            dw.name as destinationWalletName, 
            t.note,
            t.id as transactionId
        FROM transactions t
        LEFT JOIN categories c ON t.categoryId = c.id
        LEFT JOIN wallets sw ON t.sourceWalletId = sw.id
        LEFT JOIN wallets dw ON t.destinationWalletId = dw.id
        WHERE (:startDate IS NULL OR t.date >= :startDate)
        AND (:endDate IS NULL OR t.date <= :endDate)
        ORDER BY t.date DESC
    """)
    suspend fun getTransactionsForExport(startDate: Long?, endDate: Long?): List<TransactionExportRow>
}

data class TransactionExportRow(
    val date: Long,
    val title: String,
    val type: String,
    val amount: Long,
    val categoryName: String?,
    val sourceWalletName: String?,
    val destinationWalletName: String?,
    val note: String?,
    val transactionId: Long
)
