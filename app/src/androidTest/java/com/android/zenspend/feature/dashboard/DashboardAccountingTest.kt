package com.android.zenspend.feature.dashboard

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.zenspend.core.database.AppDatabase
import com.android.zenspend.core.database.dao.*
import com.android.zenspend.core.database.entity.WalletEntity
import com.android.zenspend.core.database.entity.TransactionEntity
import com.android.zenspend.core.database.model.TransactionType
import com.android.zenspend.core.database.model.WalletType
import com.android.zenspend.feature.creditcard.data.repository.CreditCardRepositoryImpl
import com.android.zenspend.feature.emi.data.repository.EMIRepositoryImpl
import com.android.zenspend.feature.transaction.data.repository.TransactionRepositoryImpl
import com.android.zenspend.feature.wallet.data.repository.WalletRepositoryImpl
import com.android.zenspend.feature.budget.data.repository.BudgetRepositoryImpl
import com.android.zenspend.feature.dashboard.domain.usecase.ObserveDashboardSummaryUseCase
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DashboardAccountingTest {

    private lateinit var db: AppDatabase
    private lateinit var walletDao: WalletDao
    private lateinit var transactionDao: TransactionDao
    private lateinit var creditCardDao: CreditCardDao
    private lateinit var emiDao: EMIDao
    private lateinit var categoryDao: CategoryDao
    private lateinit var budgetDao: BudgetDao
    private lateinit var recurringTransactionDao: RecurringTransactionDao

    private lateinit var useCase: ObserveDashboardSummaryUseCase
    private lateinit var txRepo: TransactionRepositoryImpl

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        walletDao = db.walletDao()
        transactionDao = db.transactionDao()
        creditCardDao = db.creditCardDao()
        emiDao = db.emiDao()
        categoryDao = db.categoryDao()
        budgetDao = db.budgetDao()
        recurringTransactionDao = db.recurringTransactionDao()

        val walletRepo = WalletRepositoryImpl(walletDao, transactionDao)
        txRepo = TransactionRepositoryImpl(transactionDao)
        val ccRepo = CreditCardRepositoryImpl(creditCardDao, transactionDao)
        val emiRepo = EMIRepositoryImpl(emiDao)
        val budgetRepo = BudgetRepositoryImpl(budgetDao)
        val recurringRepo = com.android.zenspend.feature.recurring.data.repository.RecurringTransactionRepositoryImpl(recurringTransactionDao)

        useCase = ObserveDashboardSummaryUseCase(walletRepo, txRepo, ccRepo, emiRepo, budgetRepo, recurringRepo)
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun testDashboardAccountingLifecycle() = runBlocking {
        val now = System.currentTimeMillis()
        val start = now - 100000
        val end = now + 100000

        // Initial State
        val sbiId = walletDao.insertWallet(WalletEntity(name = "SBI", type = WalletType.BANK, openingBalance = 5000000, currentBalance = 5000000))
        val hdfcId = walletDao.insertWallet(WalletEntity(name = "HDFC", type = WalletType.CREDIT_CARD, openingBalance = 0, currentBalance = 0))
        creditCardDao.insertCreditCard(com.android.zenspend.core.database.entity.CreditCardEntity(walletId = hdfcId, bankName = "HDFC", lastFourDigits = "1234", creditLimit = 10000000, billingCycleDate = 1, paymentDueDate = 15))
        
        // 1. Purchase 20,000 on CC
        transactionDao.insertTransaction(
            TransactionEntity(
                title = "iPhone", 
                amount = 2000000, 
                date = now, 
                type = TransactionType.EXPENSE, 
                sourceWalletId = hdfcId, 
                note = null,
                status = com.android.zenspend.core.database.model.TransactionStatus.COMPLETED,
                isExcludedFromReports = false
            )
        )

        // Verify individual Flow
        val expenseTotal = txRepo.getTotalExpense(start, end).first()
        assertEquals(2000000L, expenseTotal)
        
        // Wait for Flow emission to reflect both expense and updated balance
        var summary = useCase(start, end).first { it.monthlySummary.expense > 0 && it.netWorth.liabilities > 0 }
        assertEquals(5000000L, summary.netWorth.assets)
        assertEquals(2000000L, summary.netWorth.liabilities)
        assertEquals(3000000L, summary.netWorth.total)
        assertEquals(2000000L, summary.monthlySummary.expense)

        // 2. Pay 8,000 from Bank to CC
        transactionDao.insertTransaction(
            TransactionEntity(
                title = "Payment", 
                amount = 800000, 
                date = System.currentTimeMillis(), 
                type = TransactionType.CREDIT_CARD_PAYMENT, 
                sourceWalletId = sbiId, 
                destinationWalletId = hdfcId, 
                note = null,
                status = com.android.zenspend.core.database.model.TransactionStatus.COMPLETED,
                isExcludedFromReports = false
            )
        )
        
        summary = useCase(start, end).filter { it.netWorth.assets < 5000000 }.first()
        assertEquals(4200000L, summary.netWorth.assets) // 50k - 8k
        assertEquals(1200000L, summary.netWorth.liabilities) // 20k - 8k
        assertEquals(3000000L, summary.netWorth.total) // Still 30k
        assertEquals(2000000L, summary.monthlySummary.expense) // Payment is NOT an expense

        // 3. Refund 2,000 to CC
        transactionDao.insertTransaction(
            TransactionEntity(
                title = "Refund", 
                amount = 200000, 
                date = System.currentTimeMillis(), 
                type = TransactionType.REFUND, 
                sourceWalletId = null, 
                destinationWalletId = hdfcId, 
                note = null,
                status = com.android.zenspend.core.database.model.TransactionStatus.COMPLETED,
                isExcludedFromReports = false
            )
        )
        
        summary = useCase(start, end).filter { it.netWorth.liabilities < 1200000 }.first()
        assertEquals(1000000L, summary.netWorth.liabilities) // 12k - 2k
        assertEquals(1800000L, summary.monthlySummary.expense) // Net expense: 20k - 2k = 18k
    }
}
