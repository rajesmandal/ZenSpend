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

@RunWith(AndroidJUnit4::class)
class AccountingRegressionTest {

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
    fun closeDb() {
        db.close()
    }

    @Test
    fun testWalletBalanceAfterMultipleTransfers() = runBlocking {
        val walletA = walletDao.insertWallet(WalletEntity(name = "A", type = WalletType.CASH, openingBalance = 100000, currentBalance = 0))
        val walletB = walletDao.insertWallet(WalletEntity(name = "B", type = WalletType.BANK, openingBalance = 50000, currentBalance = 0))

        // Transfer 200 from A to B
        transactionDao.insertTransaction(TransactionEntity(title = "T1", amount = 20000, date = 1, type = TransactionType.TRANSFER, sourceWalletId = walletA, destinationWalletId = walletB, note = null))
        
        assertEquals(80000L, transactionDao.getCalculatedBalance(walletA).first())
        assertEquals(70000L, transactionDao.getCalculatedBalance(walletB).first())

        // Transfer 100 from B to A
        transactionDao.insertTransaction(TransactionEntity(title = "T2", amount = 10000, date = 2, type = TransactionType.TRANSFER, sourceWalletId = walletB, destinationWalletId = walletA, note = null))

        assertEquals(90000L, transactionDao.getCalculatedBalance(walletA).first())
        assertEquals(60000L, transactionDao.getCalculatedBalance(walletB).first())
    }

    @Test
    fun testWalletBalanceWithRefunds() = runBlocking {
        val walletId = walletDao.insertWallet(WalletEntity(name = "Wallet", type = WalletType.CASH, openingBalance = 100000, currentBalance = 0))

        // Expense of 500
        transactionDao.insertTransaction(TransactionEntity(title = "Buy", amount = 50000, date = 1, type = TransactionType.EXPENSE, sourceWalletId = walletId, note = null))
        assertEquals(50000L, transactionDao.getCalculatedBalance(walletId).first())

        // Refund of 200 (Increases balance)
        transactionDao.insertTransaction(TransactionEntity(title = "Refund", amount = 20000, date = 2, type = TransactionType.REFUND, destinationWalletId = walletId, note = null))
        assertEquals(70000L, transactionDao.getCalculatedBalance(walletId).first())
    }

    @Test
    fun testCreditCardOutstandingCalculation() = runBlocking {
        val ccWalletId = walletDao.insertWallet(WalletEntity(name = "CC", type = WalletType.CREDIT_CARD, openingBalance = 0, currentBalance = 0))

        // Spend 1000 on CC (Balance becomes -1000)
        transactionDao.insertTransaction(TransactionEntity(title = "Spend", amount = 100000, date = 1, type = TransactionType.EXPENSE, sourceWalletId = ccWalletId, note = null))
        assertEquals(-100000L, transactionDao.getCalculatedBalance(ccWalletId).first())

        // Pay 400 to CC (Balance becomes -600)
        val bankId = walletDao.insertWallet(WalletEntity(name = "Bank", type = WalletType.BANK, openingBalance = 500000, currentBalance = 0))
        transactionDao.insertTransaction(TransactionEntity(title = "Pay CC", amount = 40000, date = 2, type = TransactionType.CREDIT_CARD_PAYMENT, sourceWalletId = bankId, destinationWalletId = ccWalletId, note = null))
        
        assertEquals(-60000L, transactionDao.getCalculatedBalance(ccWalletId).first())
        assertEquals(460000L, transactionDao.getCalculatedBalance(bankId).first())
    }
}
