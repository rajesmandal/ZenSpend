package com.android.zenspend.feature.report

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.zenspend.core.database.AppDatabase
import com.android.zenspend.core.database.dao.CategoryDao
import com.android.zenspend.core.database.dao.TransactionDao
import com.android.zenspend.core.database.dao.WalletDao
import com.android.zenspend.core.database.entity.CategoryEntity
import com.android.zenspend.core.database.entity.TransactionEntity
import com.android.zenspend.core.database.entity.WalletEntity
import com.android.zenspend.core.database.model.CategoryType
import com.android.zenspend.core.database.model.TransactionStatus
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
class ReportAggregationTest {

    private lateinit var db: AppDatabase
    private lateinit var transactionDao: TransactionDao
    private lateinit var categoryDao: CategoryDao
    private lateinit var walletDao: WalletDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        transactionDao = db.transactionDao()
        categoryDao = db.categoryDao()
        walletDao = db.walletDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun testCategoryAggregation_Detailed() = runBlocking {
        val walletId = walletDao.insertWallet(WalletEntity(name = "Test Wallet", type = WalletType.CASH, openingBalance = 1000000, currentBalance = 1000000))
        val catId = categoryDao.insertCategory(CategoryEntity(name = "Food", icon = null, color = null, type = CategoryType.EXPENSE))
        
        val now = System.currentTimeMillis()
        
        // 1. Expense 1000
        transactionDao.insertTransaction(TransactionEntity(
            title = "Lunch", 
            amount = 100000, 
            date = now, 
            type = TransactionType.EXPENSE, 
            sourceWalletId = walletId, 
            categoryId = catId, 
            status = TransactionStatus.COMPLETED,
            note = null
        ))
        // 2. Expense 2000
        transactionDao.insertTransaction(TransactionEntity(
            title = "Dinner", 
            amount = 200000, 
            date = now + 1000, 
            type = TransactionType.EXPENSE, 
            sourceWalletId = walletId, 
            categoryId = catId, 
            status = TransactionStatus.COMPLETED,
            note = null
        ))
        // 3. Refund 500
        transactionDao.insertTransaction(TransactionEntity(
            title = "Refund", 
            amount = 50000, 
            date = now + 2000, 
            type = TransactionType.REFUND, 
            sourceWalletId = null, 
            destinationWalletId = walletId, 
            categoryId = catId, 
            status = TransactionStatus.COMPLETED,
            note = null
        ))

        val results = transactionDao.getCategorySpending(0, Long.MAX_VALUE).first()
        
        assertEquals(1, results.size)
        val food = results[0]
        assertEquals("Food", food.categoryName)
        assertEquals(250000L, food.totalAmount) // 1000 + 2000 - 500 = 2500
        assertEquals(200000L, food.maxAmount) // Max of expenses is 2000
        assertEquals(3, food.transactionCount)
    }
}
