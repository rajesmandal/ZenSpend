package com.android.zenspend.feature.emi.domain.usecase

import com.android.zenspend.core.database.model.EMIStatus
import com.android.zenspend.feature.emi.domain.model.EMI
import com.android.zenspend.feature.emi.domain.repository.EMIRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.slot
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class RecordEmiInstallmentPaidUseCaseTest {

    private val emiRepository: EMIRepository = mockk(relaxed = true)
    private lateinit var useCase: RecordEmiInstallmentPaidUseCase

    private val activeEmi = EMI(
        id = 1L,
        name = "Laptop",
        walletId = 5L,
        principalAmount = 60_000L,
        emiAmount = 5_000L,
        interestRate = null,
        totalTenure = 12,
        remainingInstallments = 3,
        startDate = 0L,
        endDate = 0L,
        dueDay = 10,
        status = EMIStatus.ACTIVE
    )

    @Before
    fun setup() {
        useCase = RecordEmiInstallmentPaidUseCase(emiRepository)
    }

    @Test
    fun `invoke decrements remaining installments`() = runTest {
        coEvery { emiRepository.getEMIById(1L) } returns activeEmi
        val slot = slot<EMI>()
        coEvery { emiRepository.updateEMI(capture(slot)) } returns Unit

        useCase(1L)

        assertEquals(2, slot.captured.remainingInstallments)
        assertEquals(EMIStatus.ACTIVE, slot.captured.status)
        coVerify { emiRepository.updateEMI(any()) }
    }

    @Test
    fun `invoke marks completed when last installment paid`() = runTest {
        coEvery { emiRepository.getEMIById(1L) } returns activeEmi.copy(remainingInstallments = 1)
        val slot = slot<EMI>()
        coEvery { emiRepository.updateEMI(capture(slot)) } returns Unit

        useCase(1L)

        assertEquals(0, slot.captured.remainingInstallments)
        assertEquals(EMIStatus.COMPLETED, slot.captured.status)
    }
}
