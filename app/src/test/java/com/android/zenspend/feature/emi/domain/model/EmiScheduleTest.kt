package com.android.zenspend.feature.emi.domain.model

import com.android.zenspend.core.database.model.EMIStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class EmiScheduleTest {

    private fun millis(year: Int, month: Int, day: Int): Long =
        Calendar.getInstance().apply {
            clear()
            set(year, month, day, 0, 0, 0)
        }.timeInMillis

    private fun emi(
        remaining: Int = 9,
        status: EMIStatus = EMIStatus.ACTIVE,
        start: Long = millis(2026, Calendar.JANUARY, 15),
        dueDay: Int = 5
    ) = EMI(
        id = 1,
        name = "Phone",
        walletId = null,
        principalAmount = 100_000L,
        emiAmount = 10_000L,
        interestRate = null,
        totalTenure = 12,
        remainingInstallments = remaining,
        startDate = start,
        endDate = 0L,
        dueDay = dueDay,
        status = status
    )

    @Test
    fun `paid and remaining amounts follow the installment count`() {
        val emi = emi(remaining = 9)
        assertEquals(3, EmiSchedule.paidInstallments(emi))
        assertEquals(30_000L, EmiSchedule.paidAmount(emi))
        assertEquals(90_000L, EmiSchedule.remainingAmount(emi))
        assertEquals(120_000L, EmiSchedule.totalPayable(emi))
        assertEquals(20_000L, EmiSchedule.totalInterest(emi))
        assertEquals(0.25f, EmiSchedule.progress(emi), 0.001f)
    }

    @Test
    fun `first installment is the first due day after the start date`() {
        // Started 15 Jan with due day 5 -> first due 5 Feb.
        assertEquals(millis(2026, Calendar.FEBRUARY, 5), EmiSchedule.installmentDueDate(emi(), 1))
        assertEquals(millis(2026, Calendar.MARCH, 5), EmiSchedule.installmentDueDate(emi(), 2))
    }

    @Test
    fun `first installment is in the start month when the due day is still ahead`() {
        val emi = emi(start = millis(2026, Calendar.JANUARY, 1), dueDay = 5)
        assertEquals(millis(2026, Calendar.JANUARY, 5), EmiSchedule.installmentDueDate(emi, 1))
    }

    @Test
    fun `due day beyond month length falls on the last day`() {
        val emi = emi(start = millis(2026, Calendar.JANUARY, 1), dueDay = 31)
        assertEquals(millis(2026, Calendar.FEBRUARY, 28), EmiSchedule.installmentDueDate(emi, 2))
    }

    @Test
    fun `finished or closed EMIs have no next due date`() {
        assertNull(EmiSchedule.nextDueDate(emi(remaining = 0, status = EMIStatus.COMPLETED)))
        assertNull(EmiSchedule.nextDueDate(emi(status = EMIStatus.CANCELLED)))
    }

    @Test
    fun `overdue only when the next due date is in the past`() {
        val emi = emi(remaining = 11) // installment 2 is due 5 Mar 2026
        assertTrue(EmiSchedule.isOverdue(emi, now = millis(2026, Calendar.MARCH, 20)))
        assertFalse(EmiSchedule.isOverdue(emi, now = millis(2026, Calendar.MARCH, 1)))
    }

    @Test
    fun `portfolio sums only active EMIs`() {
        val portfolio = EmiPortfolio.of(
            listOf(
                emi(remaining = 9),
                emi(remaining = 0, status = EMIStatus.COMPLETED),
                emi(status = EMIStatus.CANCELLED)
            )
        )
        assertEquals(1, portfolio.activeCount)
        assertEquals(10_000L, portfolio.monthlyTotal)
        assertEquals(90_000L, portfolio.remainingTotal)
    }
}
