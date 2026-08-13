package com.android.zenspend.feature.recurring

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.zenspend.core.database.AppDatabase
import com.android.zenspend.core.database.dao.*
import com.android.zenspend.core.database.entity.WalletEntity
import com.android.zenspend.core.database.model.RecurrenceType
import com.android.zenspend.core.database.model.TransactionType
import com.android.zenspend.core.database.model.WalletType
import com.android.zenspend.feature.recurring.data.repository.RecurringTransactionRepositoryImpl
import com.android.zenspend.feature.recurring.domain.model.RecurringTransaction
import com.android.zenspend.feature.recurring.domain.usecase.ConfirmRecurringOccurrenceUseCase
import com.android.zenspend.feature.transaction.data.repository.TransactionRepositoryImpl
import com.android.zenspend.feature.transaction.domain.usecase.AddTransactionUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RecurringAccountingTest {

    private lateinit var db: AppDatabase
    private lateinit var walletDao: WalletDao
    private lateinit var transactionDao: TransactionDao
    private lateinit var recurringDao: RecurringTransactionDao
    
    private lateinit var confirmUseCase: ConfirmRecurringOccurrenceUseCase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        walletDao = db.walletDao()
        transactionDao = db.transactionDao()
        recurringDao = db.recurringTransactionDao()

        val txRepo = TransactionRepositoryImpl(transactionDao)
        val addTxUseCase = AddTransactionUseCase(txRepo)
        val recurringRepo = RecurringTransactionRepositoryImpl(recurringDao)
        val preferenceDao = db.merchantPreferenceDao()
        val updatePreferenceUseCase = com.android.zenspend.feature.category.domain.usecase.UpdateMerchantPreferenceUseCase(preferenceDao)
        
        confirmUseCase = ConfirmRecurringOccurrenceUseCase(recurringRepo, txRepo, addTxUseCase, updatePreferenceUseCase)
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun testRecurringLifecycleAndDuplicateProtection() = runBlocking {
        // Initial: SBI Bank = ₹50,000
        val sbiId = walletDao.insertWallet(WalletEntity(name = "SBI", type = WalletType.BANK, openingBalance = 5000000, currentBalance = 5000000))
        
        // Create recurring expense: Rent ₹15,000 Monthly
        val rule = RecurringTransaction(
            name = "Rent",
            amount = 1500000,
            type = TransactionType.EXPENSE,
            recurrenceType = RecurrenceType.MONTHLY,
            sourceWalletId = sbiId,
            destinationWalletId = null,
            categoryId = null,
            startDate = System.currentTimeMillis(),
            lastExecutedDate = null,
            nextExecutionDate = System.currentTimeMillis(),
            note = "Home rent"
        )
        val ruleId = recurringDao.insertRecurringTransaction(rule.toEntity())
        
        // Verify before confirm
        assertEquals(5000000L, transactionDao.getCalculatedBalance(sbiId).first())
        
        // Confirm Now
        val occurrenceDate = System.currentTimeMillis()
        val result = confirmUseCase(ruleId, occurrenceDate)
        assertTrue(result is com.android.zenspend.core.common.result.Resource.Success)
        
        // Verify SBI = ₹35,000 (Room calculates balance based on transactions)
        // Wait for Room updates
        kotlinx.coroutines.delay(200)
        assertEquals(3500000L, transactionDao.getCalculatedBalance(sbiId).first())
        
        // Verify recurring rule advanced
        val updatedRule = recurringDao.getRecurringTransactionById(ruleId)!!
        assertEquals(occurrenceDate, updatedRule.lastExecutedDate)
        assertTrue(updatedRule.nextExecutionDate > occurrenceDate)
        
        // Try creating the same occurrence again
        val duplicateResult = confirmUseCase(ruleId, occurrenceDate)
        assertTrue(duplicateResult is com.android.zenspend.core.common.result.Resource.Error)
        
        // Verify still ₹35,000 (No duplicate transaction)
        assertEquals(3500000L, transactionDao.getCalculatedBalance(sbiId).first())
        
        // Check transaction count
        val allTx = transactionDao.getAllTransactions().first()
        assertEquals(1, allTx.size)
    }
}

// Helper mapper for test
fun RecurringTransaction.toEntity() = com.android.zenspend.core.database.entity.RecurringTransactionEntity(
    id = id,
    name = name,
    amount = amount,
    type = type,
    recurrenceType = recurrenceType,
    sourceWalletId = sourceWalletId,
    destinationWalletId = destinationWalletId,
    categoryId = categoryId,
    startDate = startDate,
    lastExecutedDate = lastExecutedDate,
    nextExecutionDate = nextExecutionDate,
    isActive = isActive,
    note = note,
    createdAt = createdAt,
    updatedAt = updatedAt
)
