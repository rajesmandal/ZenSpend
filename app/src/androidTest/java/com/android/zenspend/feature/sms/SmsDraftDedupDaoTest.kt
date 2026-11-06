package com.android.zenspend.feature.sms

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.zenspend.core.database.AppDatabase
import com.android.zenspend.core.database.model.TransactionType
import com.android.zenspend.feature.sms.data.repository.SmsDraftRepositoryImpl
import com.android.zenspend.feature.sms.data.entity.SmsDraftStatus
import com.android.zenspend.feature.sms.domain.model.SmsTransactionDraft
import com.android.zenspend.feature.sms.domain.usecase.SmsParserUseCase
import com.android.zenspend.core.util.NotificationHelper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Real Room: SMS duplicate protection (skewed timestamps, unique index, status-agnostic). */
@RunWith(AndroidJUnit4::class)
class SmsDraftDedupDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var repo: SmsDraftRepositoryImpl
    private val t0 = 1_800_000_000_000L
    private val body = "Rs 500.00 debited from A/c XX1234 at AMAZON on 06-11-26. Avl bal Rs 9000"

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        repo = SmsDraftRepositoryImpl(db.smsDraftDao())
    }

    @After
    fun tearDown() = db.close()

    private fun draft(fp: String, date: Long, status: SmsDraftStatus = SmsDraftStatus.PENDING) =
        SmsTransactionDraft(
            amount = 50000, type = TransactionType.EXPENSE, date = date, merchant = "AMAZON",
            accountLast4 = "1234", sender = "VM-HDFCBK", fingerprint = fp, status = status
        )

    @Test
    fun similarDraftWithinWindowIsFoundRegardlessOfStatus() = runBlocking {
        val id = repo.insertDraft(draft("a", t0))
        repo.updateStatus(id, SmsDraftStatus.IGNORED)

        assertTrue(repo.hasSimilarDraft("VM-HDFCBK", 50000, TransactionType.EXPENSE, t0 - 1000, t0 + 5000))
    }

    @Test
    fun draftOutsideWindowOrDifferentSenderAmountTypeIsNotSimilar() = runBlocking {
        repo.insertDraft(draft("a", t0))

        assertFalse(repo.hasSimilarDraft("VM-HDFCBK", 50000, TransactionType.EXPENSE, t0 + 60_000, t0 + 120_000))
        assertFalse(repo.hasSimilarDraft("VM-SBIINB", 50000, TransactionType.EXPENSE, t0 - 1000, t0 + 1000))
        assertFalse(repo.hasSimilarDraft("VM-HDFCBK", 99900, TransactionType.EXPENSE, t0 - 1000, t0 + 1000))
        assertFalse(repo.hasSimilarDraft("VM-HDFCBK", 50000, TransactionType.INCOME, t0 - 1000, t0 + 1000))
    }

    @Test
    fun endToEndLiveAndImportOfSameSmsStoresOneRow() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val useCase = SmsParserUseCase(repo, NotificationHelper(context))

        useCase(body, "VM-HDFCBK", t0)
        useCase(body, "VM-HDFCBK", t0 + 1_734)

        assertEquals(1, repo.getPendingDrafts().first().size)
    }
}
