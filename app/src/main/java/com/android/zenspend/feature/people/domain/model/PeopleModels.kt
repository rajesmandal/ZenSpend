package com.android.zenspend.feature.people.domain.model

import com.android.zenspend.feature.emi.domain.model.EmiSchedule

data class Person(
    val id: Long = 0,
    val name: String,
    val phone: String? = null,
    val note: String? = null
)

/** Money given to a [Person] that comes back in monthly installments. */
data class PersonLoan(
    val id: Long = 0,
    val personId: Long,
    val title: String,
    val principalAmount: Long,
    val installmentAmount: Long,
    val totalTenure: Int,
    val receivedInstallments: Int,
    val startDate: Long,
    val dueDay: Int,
    val isClosed: Boolean,
    val note: String? = null
)

data class LoanReceipt(
    val id: Long = 0,
    val loanId: Long,
    val installmentNumber: Int,
    val amount: Long,
    val dueDate: Long,
    val receivedDate: Long
)

/** Everything derived from a [PersonLoan]. Pure functions, like [EmiSchedule]. */
object LoanSchedule {

    fun remainingInstallments(loan: PersonLoan): Int =
        (loan.totalTenure - loan.receivedInstallments).coerceAtLeast(0)

    /** Still being repaid: not closed by hand and installments left. */
    fun isActive(loan: PersonLoan): Boolean = !loan.isClosed && remainingInstallments(loan) > 0

    fun receivedAmount(loan: PersonLoan): Long =
        loan.receivedInstallments.coerceIn(0, loan.totalTenure) * loan.installmentAmount

    /** What the person still owes you: only counted while the loan is active. */
    fun remainingAmount(loan: PersonLoan): Long =
        if (isActive(loan)) remainingInstallments(loan) * loan.installmentAmount else 0L

    fun totalToReceive(loan: PersonLoan): Long = loan.totalTenure * loan.installmentAmount

    /** What you get back on top of what you gave (0 when the installments add up to less). */
    fun extraOverPrincipal(loan: PersonLoan): Long =
        (totalToReceive(loan) - loan.principalAmount).coerceAtLeast(0L)

    fun progress(loan: PersonLoan): Float =
        if (loan.totalTenure > 0) (loan.receivedInstallments.toFloat() / loan.totalTenure).coerceIn(0f, 1f) else 0f

    fun dueDate(loan: PersonLoan, number: Int): Long =
        EmiSchedule.installmentDueDate(loan.startDate, loan.dueDay, number)

    fun nextDueDate(loan: PersonLoan): Long? =
        if (isActive(loan)) dueDate(loan, loan.receivedInstallments + 1) else null

    fun isOverdue(loan: PersonLoan, now: Long = System.currentTimeMillis()): Boolean =
        nextDueDate(loan)?.let { EmiSchedule.daysUntil(it, now) < 0 } ?: false
}

/** One person with their loans, for the list and the details header. */
data class PersonOverview(
    val person: Person,
    val loans: List<PersonLoan>
) {
    private val active get() = loans.filter(LoanSchedule::isActive)
    val activeLoanCount: Int get() = active.size
    val remaining: Long get() = active.sumOf(LoanSchedule::remainingAmount)
    val monthly: Long get() = active.sumOf { it.installmentAmount }
    val received: Long get() = loans.sumOf(LoanSchedule::receivedAmount)
    val nextDue: Long? get() = active.mapNotNull(LoanSchedule::nextDueDate).minOrNull()
    val hasOverdue: Boolean get() = active.any { LoanSchedule.isOverdue(it) }
}

/** Totals across everyone, shown on top of the People list. */
data class ReceivablesSummary(
    val peopleOwing: Int,
    val remainingTotal: Long,
    val monthlyTotal: Long,
    val receivedTotal: Long
) {
    companion object {
        fun of(overviews: List<PersonOverview>) = ReceivablesSummary(
            peopleOwing = overviews.count { it.activeLoanCount > 0 },
            remainingTotal = overviews.sumOf { it.remaining },
            monthlyTotal = overviews.sumOf { it.monthly },
            receivedTotal = overviews.sumOf { it.received }
        )
    }
}
