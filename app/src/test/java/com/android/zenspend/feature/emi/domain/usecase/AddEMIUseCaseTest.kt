package com.android.zenspend.feature.emi.domain.usecase

import com.android.zenspend.core.database.model.EMIStatus
import com.android.zenspend.feature.creditcard.domain.model.CreditCard
import com.android.zenspend.feature.creditcard.domain.repository.CreditCardRepository
import com.android.zenspend.feature.emi.domain.model.EMI
import com.android.zenspend.feature.emi.domain.repository.EMIRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.slot
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Calendar

class AddEMIUseCaseTest {

    private val emiRepository: EMIRepository = mockk(relaxed = true)
    private val creditCardRepository: CreditCardRepository = mockk()
    private lateinit var useCase: AddEMIUseCase

    private val card = CreditCard(
        id = 1L,
        walletId = 10L,
        name = "Millennia",
        bankName = "HDFC",
        lastFourDigits = "1234",
        creditLimit = 100_000L,
        billingCycleDate = 1,
        paymentDueDate = 15
    )

    @Before
    fun setup() {
        useCase = AddEMIUseCase(emiRepository, creditCardRepository)
    }

    @Test
    fun `invoke sets remaining equal to tenure and computes end date`() = runTest {
        coEvery { creditCardRepository.getCreditCardById(1L) } returns card
        coEvery { emiRepository.insertEMI(any()) } returns 99L

        val startDate = Calendar.getInstance().apply {
            set(2026, Calendar.JANUARY, 15, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val emiSlot = slot<EMI>()
        coEvery { emiRepository.insertEMI(capture(emiSlot)) } returns 99L

        val id = useCase(
            creditCardId = 1L,
            name = "iPhone",
            principalAmount = 120_000L,
            emiAmount = 10_000L,
            totalTenure = 12,
            startDate = startDate,
            dueDay = 5
        )

        assertEquals(99L, id)
        val saved = emiSlot.captured
        assertEquals(10L, saved.walletId)
        assertEquals(12, saved.remainingInstallments)
        assertEquals(12, saved.totalTenure)
        assertEquals(EMIStatus.ACTIVE, saved.status)

        val expectedEnd = Calendar.getInstance().apply {
            timeInMillis = startDate
            add(Calendar.MONTH, 12)
        }.timeInMillis
        assertEquals(expectedEnd, saved.endDate)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `invoke fails when card not found`() = runTest {
        coEvery { creditCardRepository.getCreditCardById(1L) } returns null
        useCase(
            creditCardId = 1L,
            name = "Test",
            principalAmount = 100L,
            emiAmount = 10L,
            totalTenure = 6,
            startDate = System.currentTimeMillis(),
            dueDay = 1
        )
    }
}
