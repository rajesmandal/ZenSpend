package com.android.zenspend.feature.report

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.zenspend.core.database.AppDatabase
import com.android.zenspend.core.database.dao.TransactionDao
import com.android.zenspend.core.database.dao.WalletDao
import com.android.zenspend.core.database.entity.TransactionEntity
import com.android.zenspend.core.database.entity.WalletEntity
import com.android.zenspend.core.database.model.TransactionType
import com.android.zenspend.core.database.model.WalletType
import com.android.zenspend.feature.report.data.repository.ReportRepositoryImpl
import com.android.zenspend.feature.report.domain.usecase.GetFinancialOverviewUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReportAccountingTest {

    private lateinit var db: AppDatabase
    private lateinit var walletDao: WalletDao
    private lateinit var transactionDao: TransactionDao
    private lateinit var useCase: GetFinancialOverviewUseCase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        walletDao = db.walletDao()
        transactionDao = db.transactionDao()
        
        val creditCardDao = db.creditCardDao()
        val emiDao = db.emiDao()
        
        val repository = ReportRepositoryImpl(transactionDao, creditCardDao, emiDao)
        useCase = GetFinancialOverviewUseCase(repository)
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun testReportScenario_Task32() = runBlocking {
        // Initial: SBI Bank = ₹50,000 (5,000,000 paise)
        val sbiId = walletDao.insertWallet(WalletEntity(name = "SBI", type = WalletType.BANK, openingBalance = 5000000, currentBalance = 5000000))
        val cashId = walletDao.insertWallet(WalletEntity(name = "Cash", type = WalletType.CASH, openingBalance = 0, currentBalance = 0))
        val hdfcId = walletDao.insertWallet(WalletEntity(name = "HDFC", type = WalletType.CREDIT_CARD, openingBalance = 0, currentBalance = 0))

        val now = System.currentTimeMillis()

        // 1. Purchase: ₹20,000 using HDFC Card
        transactionDao.insertTransaction(TransactionEntity(title = "Purchase", amount = 2000000, date = now, type = com.android.zenspend.core.database.model.TransactionType.EXPENSE, sourceWalletId = hdfcId, note = null))

        // 2. Transfer: ₹5,000 SBI -> Cash
        transactionDao.insertTransaction(TransactionEntity(title = "Transfer", amount = 500000, date = now + 1000, type = com.android.zenspend.core.database.model.TransactionType.TRANSFER, sourceWalletId = sbiId, destinationWalletId = cashId, note = null))

        // 3. CC Payment: ₹8,000 SBI -> HDFC
        transactionDao.insertTransaction(TransactionEntity(title = "CC Payment", amount = 800000, date = now + 2000, type = com.android.zenspend.core.database.model.TransactionType.CREDIT_CARD_PAYMENT, sourceWalletId = sbiId, destinationWalletId = hdfcId, note = null))

        // 4. Refund: ₹2,000 to HDFC
        transactionDao.insertTransaction(TransactionEntity(title = "Refund", amount = 200000, date = now + 3000, type = com.android.zenspend.core.database.model.TransactionType.REFUND, sourceWalletId = hdfcId, note = null))

        // 5. Salary: ₹50,000 SBI
        transactionDao.insertTransaction(TransactionEntity(title = "Salary", amount = 5000000, date = now + 4000, type = com.android.zenspend.core.database.model.TransactionType.INCOME, sourceWalletId = sbiId, destinationWalletId = sbiId, note = null))

        // Execute (Aug 2026 or any range covering 'now')
        val overview = useCase(0, Long.MAX_VALUE).first()

        // Assertions
        assertEquals(5000000L, overview.totalIncome)
        assertEquals(1800000L, overview.totalExpense) // 20k - 2k = 18k
        assertEquals(3200000L, overview.savings) // 50k - 18k = 32k
        assertEquals(64.0f, overview.savingsRate) // (32/50) * 100 = 64%
    }
}
