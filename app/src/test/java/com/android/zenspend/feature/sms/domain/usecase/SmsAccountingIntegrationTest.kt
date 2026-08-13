package com.android.zenspend.feature.sms.domain.usecase

import com.android.zenspend.core.common.result.Resource
import com.android.zenspend.core.database.model.TransactionSource
import com.android.zenspend.core.database.model.TransactionStatus
import com.android.zenspend.core.database.model.TransactionType
import com.android.zenspend.feature.sms.data.entity.SmsDraftStatus
import com.android.zenspend.feature.sms.domain.model.SmsTransactionDraft
import com.android.zenspend.feature.sms.domain.repository.SmsDraftRepository
import com.android.zenspend.feature.transaction.domain.model.Transaction
import com.android.zenspend.feature.transaction.domain.usecase.AddTransactionUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SmsAccountingIntegrationTest {

    private val smsDraftRepository = mockk<SmsDraftRepository>(relaxed = true)
    private val addTransactionUseCase = mockk<AddTransactionUseCase>()
    private val confirmSmsDraftUseCase = ConfirmSmsDraftUseCase(smsDraftRepository, addTransactionUseCase)

    @Test
    fun `test confirm sms draft updates ledger and status`() = runTest {
        val draftId = 1L
        val transaction = Transaction(
            title = "Amazon",
            amount = 125000L,
            date = 1000L,
            note = "SMS",
            type = TransactionType.EXPENSE,
            sourceWalletId = 10L,
            source = TransactionSource.SMS
        )

        coEvery { addTransactionUseCase(any()) } returns Resource.Success(100L)

        val result = confirmSmsDraftUseCase(draftId, transaction)

        assertEquals(Resource.Success(100L), result)
        
        coVerify { 
            addTransactionUseCase(match { it.externalReference == draftId.toString() }) 
            smsDraftRepository.updateStatus(draftId, SmsDraftStatus.COMMITTED)
        }
    }
}
