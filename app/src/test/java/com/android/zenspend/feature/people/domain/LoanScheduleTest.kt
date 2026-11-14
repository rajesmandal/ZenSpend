package com.android.zenspend.feature.people.domain

import com.android.zenspend.feature.people.domain.model.LoanSchedule
import com.android.zenspend.feature.people.domain.model.Person
import com.android.zenspend.feature.people.domain.model.PersonLoan
import com.android.zenspend.feature.people.domain.model.PersonOverview
import com.android.zenspend.feature.people.domain.model.ReceivablesSummary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class LoanScheduleTest {

    private fun millis(y: Int, m: Int, d: Int) =
        Calendar.getInstance().apply { clear(); set(y, m, d, 12, 0) }.timeInMillis

    private fun loan(received: Int = 0, closed: Boolean = false, tenure: Int = 10) = PersonLoan(
        id = 1, personId = 1, title = "Bike", principalAmount = 100_000_00,
        installmentAmount = 11_000_00, totalTenure = tenure, receivedInstallments = received,
        startDate = millis(2026, Calendar.JANUARY, 20), dueDay = 5, isClosed = closed
    )

    @Test fun remainingAmountCountsOnlyUnreceivedInstallments() {
        assertEquals(7 * 11_000_00L, LoanSchedule.remainingAmount(loan(received = 3)))
        assertEquals(3 * 11_000_00L, LoanSchedule.receivedAmount(loan(received = 3)))
    }

    @Test fun closedOrFinishedLoanOwesNothing() {
        assertEquals(0L, LoanSchedule.remainingAmount(loan(received = 3, closed = true)))
        assertEquals(0L, LoanSchedule.remainingAmount(loan(received = 10)))
        assertNull(LoanSchedule.nextDueDate(loan(received = 10)))
        assertFalse(LoanSchedule.isActive(loan(closed = true)))
    }

    @Test fun firstInstallmentFallsOnNextDueDayAfterStart() {
        // Given on 20 Jan with due day 5 -> first installment 5 Feb.
        assertEquals(millis(2026, Calendar.FEBRUARY, 5).dayOf(), LoanSchedule.dueDate(loan(), 1).dayOf())
        assertEquals(Calendar.FEBRUARY, Calendar.getInstance().apply { timeInMillis = LoanSchedule.dueDate(loan(), 1) }.get(Calendar.MONTH))
    }

    @Test fun extraOverPrincipalIsTheInterestYouGetBack() {
        assertEquals(10_000_00L, LoanSchedule.extraOverPrincipal(loan()))
    }

    @Test fun summaryTotalsOnlyActiveLoans() {
        val a = PersonOverview(Person(1, "A"), listOf(loan(received = 3), loan(received = 10)))
        val b = PersonOverview(Person(2, "B"), listOf(loan(closed = true)))
        val s = ReceivablesSummary.of(listOf(a, b))
        assertEquals(1, s.peopleOwing)
        assertEquals(7 * 11_000_00L, s.remainingTotal)
        assertEquals(11_000_00L, s.monthlyTotal)
        assertTrue(s.receivedTotal > 0)
    }

    private fun Long.dayOf() = Calendar.getInstance().apply { timeInMillis = this@dayOf }.get(Calendar.DAY_OF_MONTH)
}
