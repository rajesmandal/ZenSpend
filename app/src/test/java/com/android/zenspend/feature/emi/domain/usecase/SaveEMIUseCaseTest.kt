package com.android.zenspend.feature.emi.domain.usecase

import com.android.zenspend.core.database.model.EMILenderType
import com.android.zenspend.core.database.model.EMIStatus
import com.android.zenspend.feature.creditcard.domain.model.CreditCard
import com.android.zenspend.feature.creditcard.domain.repository.CreditCardRepository
import com.android.zenspend.feature.emi.domain.model.EMI
import com.android.zenspend.feature.emi.domain.repository.EMIRepository
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class SaveEMIUseCaseTest {

    private val emiRepository: EMIRepository = mockk(relaxed = true)
    private val creditCardRepository: CreditCardRepository = mockk()
    private val useCase = SaveEMIUseCase(emiRepository, creditCardRepository)

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

    private val startDate = Calendar.getInstance().apply {
        set(2026, Calendar.JANUARY, 15, 0, 0, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private fun draft(
        type: EMILenderType = EMILenderType.BANK,
        lender: String = "SBI",
        paid: Int = 0
    ) = EmiDraft(
        lenderType = type,
        lenderName = lender,
        creditCardId = 1L,
        name = "Bike loan",
        principalAmount = 120_000L,
        emiAmount = 10_000L,
        totalTenure = 12,
        paidInstallments = paid,
        startDate = startDate,
        dueDay = 5,
        interestRate = null,
        note = "  "
    )

    @Test
    fun `new bank EMI starts with full tenure remaining`() = runTest {
        val saved = slot<EMI>()
        coEvery { emiRepository.insertEMI(capture(saved)) } returns 7L

        assertEquals(7L, useCase(draft()))

        assertEquals(12, saved.captured.remainingInstallments)
        assertEquals(EMIStatus.ACTIVE, saved.captured.status)
        assertEquals("SBI", saved.captured.lenderName)
        assertEquals(null, saved.captured.walletId)
        assertEquals(null, saved.captured.note)
    }

    @Test
    fun `already paid installments reduce the remaining count`() = runTest {
        val saved = slot<EMI>()
        coEvery { emiRepository.insertEMI(capture(saved)) } returns 7L

        useCase(draft(paid = 5))

        assertEquals(7, saved.captured.remainingInstallments)
    }

    @Test
    fun `credit card EMI links the card wallet and uses the bank name`() = runTest {
        coEvery { creditCardRepository.getCreditCardById(1L) } returns card
        val saved = slot<EMI>()
        coEvery { emiRepository.insertEMI(capture(saved)) } returns 7L

        useCase(draft(type = EMILenderType.CREDIT_CARD, lender = ""))

        assertEquals(10L, saved.captured.walletId)
        assertTrue(saved.captured.lenderName.startsWith("HDFC"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `person EMI needs a lender name`() = runTest {
        useCase(draft(type = EMILenderType.PERSON, lender = " "))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `paid installments cannot exceed the tenure`() = runTest {
        useCase(draft(paid = 13))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `missing credit card is rejected`() = runTest {
        coEvery { creditCardRepository.getCreditCardById(1L) } returns null
        useCase(draft(type = EMILenderType.CREDIT_CARD))
    }
}
