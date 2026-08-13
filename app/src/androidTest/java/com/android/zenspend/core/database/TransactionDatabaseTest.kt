package com.android.zenspend.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.zenspend.core.database.dao.TransactionDao
import com.android.zenspend.core.database.dao.WalletDao
import com.android.zenspend.core.database.entity.TransactionEntity
import com.android.zenspend.core.database.entity.WalletEntity
import com.android.zenspend.core.database.model.TransactionType
import com.android.zenspend.core.database.model.WalletType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class TransactionDatabaseTest {

    private lateinit var db: AppDatabase
    private lateinit var walletDao: WalletDao
    private lateinit var transactionDao: TransactionDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        walletDao = db.walletDao()
        transactionDao = db.transactionDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun testCreditCardOverpayment() = runBlocking {
        // 1. Setup: HDFC CC with 1k outstanding (Negative opening balance)
        val hdfcId = walletDao.insertWallet(
            WalletEntity(name = "HDFC CC", type = WalletType.CREDIT_CARD, openingBalance = -100000, currentBalance = -100000)
        )
        val axisId = walletDao.insertWallet(
            WalletEntity(name = "Axis Bank", type = WalletType.BANK, openingBalance = 500000, currentBalance = 500000)
        )

        // 2. Action: Pay ₹1,500
        val payment = TransactionEntity(
            title = "Overpayment",
            amount = 150000,
            date = System.currentTimeMillis(),
            type = TransactionType.CREDIT_CARD_PAYMENT,
            sourceWalletId = axisId,
            destinationWalletId = hdfcId,
            note = null
        )
        transactionDao.insertTransaction(payment)
        
        // 3. Verify Calculated Balance
        val calculatedHdfc = transactionDao.getCalculatedBalance(hdfcId).first()
        val calculatedAxis = transactionDao.getCalculatedBalance(axisId).first()
        
        assertEquals(50000L, calculatedHdfc) // -100k + 150k = +50k (Positive credit)
        assertEquals(350000L, calculatedAxis) // 500k - 150k = 350k
    }

    @Test
    fun testRefundAffectsBalance() = runBlocking {
        val cashId = walletDao.insertWallet(
            WalletEntity(name = "Cash", type = WalletType.CASH, openingBalance = 100000, currentBalance = 100000)
        )

        // 1. Expense ₹200
        val expense = TransactionEntity(
            title = "Lunch", amount = 20000, date = System.currentTimeMillis(),
            type = TransactionType.EXPENSE, sourceWalletId = cashId, note = null
        )
        val expenseId = transactionDao.insertTransaction(expense)

        // 2. Refund ₹200
        val refund = TransactionEntity(
            title = "Lunch Refund", amount = 20000, date = System.currentTimeMillis(),
            type = TransactionType.REFUND, sourceWalletId = null, 
            destinationWalletId = cashId,
            originalTransactionId = expenseId, note = null
        )
        transactionDao.insertTransaction(refund)

        // 3. Verify
        val balance = transactionDao.getCalculatedBalance(cashId).first()
        assertEquals(100000L, balance) // 100k - 20k + 20k = 100k
    }

    @Test
    fun testTransactionEditRestoresBalance() = runBlocking {
        val cashId = walletDao.insertWallet(
            WalletEntity(name = "Cash", type = WalletType.CASH, openingBalance = 100000, currentBalance = 100000)
        )

        // 1. Expense ₹200
        val expense = TransactionEntity(
            title = "Lunch", amount = 20000, date = System.currentTimeMillis(),
            type = TransactionType.EXPENSE, sourceWalletId = cashId, note = null
        )
        val id = transactionDao.insertTransaction(expense)

        // 2. Edit to ₹300
        val editedExpense = expense.copy(id = id, amount = 30000)
        transactionDao.updateTransaction(editedExpense)

        // 3. Verify
        val balance = transactionDao.getCalculatedBalance(cashId).first()
        assertEquals(70000L, balance) // 100k - 30k = 70k
    }

    @Test
    fun testTransactionDeleteRestoresBalance() = runBlocking {
        val cashId = walletDao.insertWallet(
            WalletEntity(name = "Cash", type = WalletType.CASH, openingBalance = 100000, currentBalance = 100000)
        )

        val expense = TransactionEntity(
            title = "Lunch", amount = 20000, date = System.currentTimeMillis(),
            type = TransactionType.EXPENSE, sourceWalletId = cashId, note = null
        )
        val id = transactionDao.insertTransaction(expense)
        
        transactionDao.deleteTransaction(expense.copy(id = id))

        val balance = transactionDao.getCalculatedBalance(cashId).first()
        assertEquals(100000L, balance) // Restored to opening
    }

    @Test
    fun testTransferExclusionFromReports() = runBlocking {
        val bankId = walletDao.insertWallet(WalletEntity(name = "Bank", type = WalletType.BANK, openingBalance = 100000, currentBalance = 100000))
        val cashId = walletDao.insertWallet(WalletEntity(name = "Cash", type = WalletType.CASH, openingBalance = 0, currentBalance = 0))

        val transfer = TransactionEntity(
            title = "Withdraw", amount = 50000, date = System.currentTimeMillis(),
            type = TransactionType.TRANSFER, sourceWalletId = bankId, destinationWalletId = cashId, note = null
        )
        transactionDao.insertTransaction(transfer)

        val totalIncome = transactionDao.getTotalAmountByType(TransactionType.INCOME, 0, Long.MAX_VALUE).first()
        val totalExpense = transactionDao.getTotalNetExpense(TransactionType.EXPENSE, TransactionType.REFUND, 0, Long.MAX_VALUE).first()

        assertEquals(0L, totalIncome)
        assertEquals(0L, totalExpense)
    }
}
