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
class CreditCardAccountingTest {

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
    fun testCompleteCreditCardLifecycle() = runBlocking {
        // Initial: SBI Bank = ₹50,000 (5000000 paise)
        val sbiId = walletDao.insertWallet(
            WalletEntity(name = "SBI Bank", type = WalletType.BANK, openingBalance = 5000000, currentBalance = 5000000)
        )
        // HDFC CC: Limit = ₹1,00,000, Outstanding = ₹0
        val hdfcId = walletDao.insertWallet(
            WalletEntity(name = "HDFC CC", type = WalletType.CREDIT_CARD, openingBalance = 0, currentBalance = 0)
        )

        // Step 1: Purchase ₹20,000 using HDFC Card
        val purchase = TransactionEntity(
            title = "iPhone",
            amount = 2000000,
            date = System.currentTimeMillis(),
            type = TransactionType.EXPENSE,
            sourceWalletId = hdfcId,
            note = null
        )
        transactionDao.insertTransaction(purchase)
        
        assertEquals(5000000L, transactionDao.getCalculatedBalance(sbiId).first())
        assertEquals(-2000000L, transactionDao.getCalculatedBalance(hdfcId).first()) // Outstanding = 20k

        // Step 2: Pay ₹8,000 from SBI to HDFC
        val payment1 = TransactionEntity(
            title = "Partial Payment",
            amount = 800000,
            date = System.currentTimeMillis(),
            type = TransactionType.CREDIT_CARD_PAYMENT,
            sourceWalletId = sbiId,
            destinationWalletId = hdfcId,
            note = null
        )
        transactionDao.insertTransaction(payment1)
        
        assertEquals(4200000L, transactionDao.getCalculatedBalance(sbiId).first()) // 50k - 8k = 42k
        assertEquals(-1200000L, transactionDao.getCalculatedBalance(hdfcId).first()) // -20k + 8k = -12k

        // Step 3: Refund ₹2,000 to HDFC
        val refund = TransactionEntity(
            title = "Partial Refund",
            amount = 200000,
            date = System.currentTimeMillis(),
            type = TransactionType.REFUND,
            sourceWalletId = hdfcId, // Original source was CC, so refund goes back to CC
            note = null
        )
        transactionDao.insertTransaction(refund)
        
        assertEquals(-1000000L, transactionDao.getCalculatedBalance(hdfcId).first()) // -12k + 2k = -10k
        
        // Step 4: Pay ₹10,000 from SBI
        val payment2 = TransactionEntity(
            title = "Final Payment",
            amount = 1000000,
            date = System.currentTimeMillis(),
            type = TransactionType.CREDIT_CARD_PAYMENT,
            sourceWalletId = sbiId,
            destinationWalletId = hdfcId,
            note = null
        )
        transactionDao.insertTransaction(payment2)
        
        assertEquals(3200000L, transactionDao.getCalculatedBalance(sbiId).first()) // 42k - 10k = 32k
        assertEquals(0L, transactionDao.getCalculatedBalance(hdfcId).first()) // -10k + 10k = 0
    }
}
