package com.android.zenspend.feature.recurring

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.zenspend.core.database.AppDatabase
import com.android.zenspend.core.database.model.RecurrenceType
import com.android.zenspend.core.database.model.TransactionType
import com.android.zenspend.feature.recurring.data.repository.RecurringTransactionRepositoryImpl
import com.android.zenspend.feature.recurring.domain.model.RecurringTransaction
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Real Room (in-memory) integration test for archive -> restore of recurring rules. */
@RunWith(AndroidJUnit4::class)
class RecurringArchiveRestoreTest {

    private lateinit var db: AppDatabase
    private lateinit var repo: RecurringTransactionRepositoryImpl

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        repo = RecurringTransactionRepositoryImpl(db.recurringTransactionDao())
    }

    @After
    fun tearDown() = db.close()

    private suspend fun insertRule(active: Boolean = true): Long = repo.insertRecurringTransaction(
        RecurringTransaction(
            name = "Rent", amount = 1000, type = TransactionType.EXPENSE,
            recurrenceType = RecurrenceType.MONTHLY, sourceWalletId = null, destinationWalletId = null,
            categoryId = null, startDate = 0, lastExecutedDate = null, nextExecutionDate = 0,
            isActive = active, note = null
        )
    )

    @Test
    fun archivedRuleMovesToArchivedList() = runBlocking {
        val id = insertRule()
        repo.archiveRecurringTransaction(id)

        assertTrue(repo.getAllRecurringTransactions().first().isEmpty())
        assertEquals(listOf(id), repo.getArchivedRecurringTransactions().first().map { it.id })
    }

    @Test
    fun restoredRuleComesBackPausedNotActive() = runBlocking {
        val id = insertRule()
        repo.archiveRecurringTransaction(id)

        repo.restoreRecurringTransaction(id)

        val all = repo.getAllRecurringTransactions().first()
        assertEquals(listOf(id), all.map { it.id })
        // Must NOT silently resume: otherwise an old overdue rule would prompt/charge immediately.
        assertFalse(all.first().isActive)
        assertTrue(repo.getAllActiveRecurringTransactions().first().isEmpty())
        assertTrue(repo.getArchivedRecurringTransactions().first().isEmpty())
    }

    @Test
    fun restoreBumpsUpdatedAtSoSyncSeesIt() = runBlocking {
        val id = insertRule()
        repo.archiveRecurringTransaction(id)
        val archivedAt = repo.getRecurringTransactionById(id)!!.updatedAt

        Thread.sleep(5)
        repo.restoreRecurringTransaction(id)

        assertTrue(repo.getRecurringTransactionById(id)!!.updatedAt > archivedAt)
    }

    @Test
    fun restoreOnLiveActiveRuleIsNoOp() = runBlocking {
        val id = insertRule(active = true)

        repo.restoreRecurringTransaction(id)

        // A live rule must stay active; restore only touches archived rows.
        assertTrue(repo.getRecurringTransactionById(id)!!.isActive)
        assertEquals(1, repo.getAllActiveRecurringTransactions().first().size)
    }

    @Test
    fun restoreUnknownIdDoesNotCrashOrCreateRow() = runBlocking {
        repo.restoreRecurringTransaction(9999)

        assertNull(repo.getRecurringTransactionById(9999))
        assertTrue(repo.getAllRecurringTransactions().first().isEmpty())
    }
}
