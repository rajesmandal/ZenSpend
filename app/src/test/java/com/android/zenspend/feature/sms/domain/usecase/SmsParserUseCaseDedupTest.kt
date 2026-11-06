package com.android.zenspend.feature.sms.domain.usecase

import com.android.zenspend.core.database.model.TransactionType
import com.android.zenspend.core.util.NotificationHelper
import com.android.zenspend.feature.sms.data.entity.SmsDraftStatus
import com.android.zenspend.feature.sms.domain.model.SmsTransactionDraft
import com.android.zenspend.feature.sms.domain.repository.SmsDraftRepository
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Bug: the same SMS showed up twice. The live receiver stamps it with the SMSC send time
 * (whole seconds) while the inbox import stamps the device receive time, so the two copies
 * had different timestamps -> different fingerprints -> two drafts.
 */
class SmsParserUseCaseDedupTest {

    /** Behaves like the real table: unique fingerprint, IGNORE on conflict (returns -1). */
    private class FakeRepo : SmsDraftRepository {
        val drafts = mutableListOf<SmsTransactionDraft>()
        override fun getPendingDrafts(): Flow<List<SmsTransactionDraft>> = flowOf(drafts.toList())
        override suspend fun insertDraft(draft: SmsTransactionDraft): Long {
            if (drafts.any { it.fingerprint == draft.fingerprint }) return -1L
            drafts += draft.copy(id = drafts.size + 1L)
            return drafts.size.toLong()
        }
        override suspend fun updateStatus(id: Long, status: SmsDraftStatus) {
            val i = drafts.indexOfFirst { it.id == id }
            if (i >= 0) drafts[i] = drafts[i].copy(status = status)
        }
        override suspend fun getDraftByFingerprint(fingerprint: String) =
            drafts.firstOrNull { it.fingerprint == fingerprint }
        override suspend fun hasSimilarDraft(
            sender: String, amount: Long, type: TransactionType, from: Long, to: Long
        ) = drafts.any {
            it.sender == sender && it.amount == amount && it.type == type && it.date in from..to
        }
    }

    private val notifier: NotificationHelper = mockk(relaxed = true)
    private lateinit var repo: FakeRepo
    private lateinit var useCase: SmsParserUseCase

    private val sender = "VM-HDFCBK"
    private val body = "Rs 500.00 debited from A/c XX1234 at AMAZON on 06-11-26. Avl bal Rs 9000"
    private val t0 = 1_800_000_000_000L

    @Before
    fun setUp() {
        repo = FakeRepo()
        useCase = SmsParserUseCase(repo, notifier)
    }

    @Test
    fun `live receive then inbox import of same sms with skewed timestamp gives one draft`() = runTest {
        useCase(body, sender, t0)            // receiver: SMSC time, whole seconds
        useCase(body, sender, t0 + 1_734)    // import: device receive time

        assertEquals(1, repo.drafts.size)
        verify(exactly = 1) { notifier.showSmsDetectedNotification(any(), any()) }
    }

    @Test
    fun `inbox import first then live receive also gives one draft`() = runTest {
        useCase(body, sender, t0 + 2_000)
        useCase(body, sender, t0)

        assertEquals(1, repo.drafts.size)
    }

    @Test
    fun `exact same sms twice gives one draft`() = runTest {
        useCase(body, sender, t0)
        useCase(body, sender, t0)

        assertEquals(1, repo.drafts.size)
        verify(exactly = 1) { notifier.showSmsDetectedNotification(any(), any()) }
    }

    @Test
    fun `genuinely repeated payment an hour later is kept`() = runTest {
        useCase(body, sender, t0)
        useCase(body, sender, t0 + 60 * 60 * 1000L)

        assertEquals(2, repo.drafts.size)
    }

    @Test
    fun `same amount from a different sender is kept`() = runTest {
        useCase(body, sender, t0)
        useCase(body, "VM-SBIINB", t0 + 1_000)

        assertEquals(2, repo.drafts.size)
    }

    @Test
    fun `debit and credit of same amount close together are both kept`() = runTest {
        useCase(body, sender, t0)
        useCase("Rs 500.00 credited to A/c XX1234 from RAHUL on 06-11-26", sender, t0 + 1_000)

        assertEquals(2, repo.drafts.size)
    }

    @Test
    fun `already confirmed draft still blocks the late duplicate`() = runTest {
        useCase(body, sender, t0)
        repo.updateStatus(repo.drafts.first().id, SmsDraftStatus.COMMITTED)

        useCase(body, sender, t0 + 1_500)

        assertEquals(1, repo.drafts.size)
    }

    @Test
    fun `notification is not shown when insert is ignored by the unique index`() = runTest {
        // Simulates a race: both copies pass the pre-check, second insert hits the unique index.
        val racing = object : SmsDraftRepository by repo {
            override suspend fun getDraftByFingerprint(fingerprint: String) = null
            override suspend fun hasSimilarDraft(
                sender: String, amount: Long, type: TransactionType, from: Long, to: Long
            ) = false
        }
        val racingUseCase = SmsParserUseCase(racing, notifier)

        racingUseCase(body, sender, t0)
        racingUseCase(body, sender, t0)

        assertEquals(1, repo.drafts.size)
        verify(exactly = 1) { notifier.showSmsDetectedNotification(any(), any()) }
    }
}
