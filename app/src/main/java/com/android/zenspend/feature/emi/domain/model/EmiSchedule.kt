package com.android.zenspend.feature.emi.domain.model

import com.android.zenspend.core.database.model.EMIStatus
import java.util.Calendar

/**
 * Everything derived from an [EMI] (what is paid, what is left, when the next installment is due).
 * Pure functions on purpose: no Android, no database, easy to test.
 */
object EmiSchedule {

    fun paidInstallments(emi: EMI): Int =
        (emi.totalTenure - emi.remainingInstallments).coerceIn(0, emi.totalTenure)

    fun paidAmount(emi: EMI): Long = paidInstallments(emi) * emi.emiAmount

    fun remainingAmount(emi: EMI): Long = emi.remainingInstallments.coerceAtLeast(0) * emi.emiAmount

    fun totalPayable(emi: EMI): Long = emi.totalTenure * emi.emiAmount

    /** Total interest / charges over the whole loan (0 when the EMIs add up to less than the principal). */
    fun totalInterest(emi: EMI): Long = (totalPayable(emi) - emi.principalAmount).coerceAtLeast(0L)

    /** 0f..1f share of installments already paid. */
    fun progress(emi: EMI): Float =
        if (emi.totalTenure > 0) paidInstallments(emi).toFloat() / emi.totalTenure else 0f

    /**
     * Due date of installment [number] (1-based). The first installment falls on the first
     * [EMI.dueDay] on or after the start date; each next one is a month later. Months shorter than
     * the due day use their last day.
     */
    fun installmentDueDate(emi: EMI, number: Int): Long {
        val start = Calendar.getInstance().apply { timeInMillis = emi.startDate }
        val firstMonthOffset = if (start.get(Calendar.DAY_OF_MONTH) <= emi.dueDay) 0 else 1
        return Calendar.getInstance().apply {
            timeInMillis = emi.startDate
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.MONTH, firstMonthOffset + (number - 1))
            set(Calendar.DAY_OF_MONTH, emi.dueDay.coerceAtMost(getActualMaximum(Calendar.DAY_OF_MONTH)))
        }.timeInMillis
    }

    /** Due date of the next unpaid installment, or null once everything is paid / the EMI is closed. */
    fun nextDueDate(emi: EMI): Long? =
        if (emi.status == EMIStatus.ACTIVE && emi.remainingInstallments > 0) {
            installmentDueDate(emi, paidInstallments(emi) + 1)
        } else {
            null
        }

    /** Due date of the very last installment. */
    fun finalDueDate(emi: EMI): Long = installmentDueDate(emi, emi.totalTenure.coerceAtLeast(1))

    /** True when the next installment is due before today. */
    fun isOverdue(emi: EMI, now: Long = System.currentTimeMillis()): Boolean =
        nextDueDate(emi)?.let { it < startOfDay(now) } ?: false

    /** Whole days from today to [dueDate]; negative when it has passed. */
    fun daysUntil(dueDate: Long, now: Long = System.currentTimeMillis()): Int {
        val day = 24L * 60 * 60 * 1000
        return Math.floorDiv(startOfDay(dueDate) - startOfDay(now), day).toInt()
    }

    private fun startOfDay(millis: Long): Long = Calendar.getInstance().apply {
        timeInMillis = millis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

/** Totals across a list of EMIs, used by the list screen and the dashboard. */
data class EmiPortfolio(
    val activeCount: Int,
    /** What leaves your pocket every month: sum of the active EMI amounts. */
    val monthlyTotal: Long,
    /** Everything still to be paid across active EMIs. */
    val remainingTotal: Long,
    /** Everything paid so far across active EMIs. */
    val paidTotal: Long,
    /** The active EMI whose next installment comes first. */
    val nextDue: EMI?
) {
    companion object {
        fun of(emis: List<EMI>): EmiPortfolio {
            val active = emis.filter { it.status == EMIStatus.ACTIVE && it.remainingInstallments > 0 }
            return EmiPortfolio(
                activeCount = active.size,
                monthlyTotal = active.sumOf { it.emiAmount },
                remainingTotal = active.sumOf { EmiSchedule.remainingAmount(it) },
                paidTotal = active.sumOf { EmiSchedule.paidAmount(it) },
                nextDue = active.minByOrNull { EmiSchedule.nextDueDate(it) ?: Long.MAX_VALUE }
            )
        }
    }
}
