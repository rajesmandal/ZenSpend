package com.android.zenspend.core.performance

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.zenspend.core.database.AppDatabase
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
import kotlin.system.measureTimeMillis

@RunWith(AndroidJUnit4::class)
class PerformanceStressTest {

    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun testLargeDataTransactionInsertAndBalanceCalculation() = runBlocking {
        val walletId = db.walletDao().insertWallet(
            WalletEntity(name = "Big Wallet", type = WalletType.BANK, openingBalance = 0)
        )

        val transactionCount = 10000
        val batchSize = 1000
        
        println("Inserting $transactionCount transactions...")
        
        val totalTime = measureTimeMillis {
            for (i in 0 until transactionCount / batchSize) {
                val batch = List(batchSize) {
                    TransactionEntity(
                        title = "Transaction $it",
                        amount = 10000, // ₹100
                        date = System.currentTimeMillis(),
                        type = TransactionType.EXPENSE,
                        sourceWalletId = walletId,
                        note = null
                    )
                }
                db.transactionDao().insertTransactions(batch)
            }
        }
        
        println("Insertion took $totalTime ms")

        val calculationTime = measureTimeMillis {
            val balance = db.transactionDao().getCalculatedBalance(walletId).first()
            assertEquals(-(transactionCount * 10000L), balance)
        }
        
        println("Balance calculation for $transactionCount tx took $calculationTime ms")
        
        // Assert reasonable performance (< 1s for calculation)
        assert(calculationTime < 1000)
    }
}
