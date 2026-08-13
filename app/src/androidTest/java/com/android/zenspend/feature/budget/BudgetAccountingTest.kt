package com.android.zenspend.feature.budget

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.zenspend.core.database.AppDatabase
import com.android.zenspend.core.database.dao.BudgetDao
import com.android.zenspend.core.database.dao.TransactionDao
import com.android.zenspend.core.database.entity.BudgetEntity
import com.android.zenspend.core.database.entity.TransactionEntity
import com.android.zenspend.core.database.model.TransactionType
import com.android.zenspend.core.database.model.WalletType
import com.android.zenspend.core.database.entity.WalletEntity
import com.android.zenspend.feature.budget.data.repository.BudgetRepositoryImpl
import com.android.zenspend.feature.transaction.data.repository.TransactionRepositoryImpl
import com.android.zenspend.feature.category.data.repository.CategoryRepositoryImpl
import com.android.zenspend.core.database.dao.CategoryDao
import com.android.zenspend.feature.budget.domain.usecase.ObserveBudgetsWithProgressUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BudgetAccountingTest {

    private lateinit var db: AppDatabase
    private lateinit var budgetDao: BudgetDao
    private lateinit var transactionDao: TransactionDao
    private lateinit var categoryDao: CategoryDao
    private lateinit var useCase: ObserveBudgetsWithProgressUseCase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        budgetDao = db.budgetDao()
        transactionDao = db.transactionDao()
        categoryDao = db.categoryDao()

        val budgetRepo = BudgetRepositoryImpl(budgetDao)
        val txRepo = TransactionRepositoryImpl(transactionDao)
        val catRepo = CategoryRepositoryImpl(categoryDao)

        useCase = ObserveBudgetsWithProgressUseCase(budgetRepo, txRepo, catRepo)
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun testBudgetSpendingAggregation() = runBlocking {
        val walletId = db.walletDao().insertWallet(WalletEntity(name = "Cash", type = WalletType.CASH, openingBalance = 1000000, currentBalance = 1000000))
        
        // Setup Budget: ₹10,000
        budgetDao.insertBudget(BudgetEntity(amount = 1000000, categoryId = null, startDate = 0, endDate = Long.MAX_VALUE))

        // 1. Add Expense ₹6,000
        transactionDao.insertTransaction(TransactionEntity(title = "Spend", amount = 600000, date = 1000, type = TransactionType.EXPENSE, sourceWalletId = walletId, note = null))
        
        var result = useCase(0, Long.MAX_VALUE).first()
        assertEquals(600000L, result[0].spentAmount)
        assertEquals(0.6f, result[0].progress)

        // 2. Add Transfer (Should NOT affect budget)
        transactionDao.insertTransaction(TransactionEntity(title = "Transfer", amount = 200000, date = 2000, type = TransactionType.TRANSFER, sourceWalletId = walletId, destinationWalletId = walletId, note = null))
        
        result = useCase(0, Long.MAX_VALUE).first()
        assertEquals(600000L, result[0].spentAmount) // Unchanged

        // 3. Add Refund ₹1,000
        transactionDao.insertTransaction(TransactionEntity(title = "Refund", amount = 100000, date = 3000, type = TransactionType.REFUND, sourceWalletId = walletId, note = null))
        
        result = useCase(0, Long.MAX_VALUE).first()
        assertEquals(500000L, result[0].spentAmount) // 6000 - 1000 = 5000
    }
}
