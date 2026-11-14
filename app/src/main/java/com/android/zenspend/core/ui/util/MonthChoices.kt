package com.android.zenspend.core.ui.util

import java.util.Calendar

/** Months a picker offers: this month back to the month of the oldest entry (never into the future). */
object MonthChoices {
    private const val MAX_MONTHS = 120

    fun build(now: Calendar, earliestEntryMillis: Long?): List<Calendar> {
        val current = (now.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }
        val first = earliestEntryMillis?.let {
            Calendar.getInstance().apply { timeInMillis = it; set(Calendar.DAY_OF_MONTH, 1) }
        }
        val count = if (first == null) 1 else {
            val diff = (current.get(Calendar.YEAR) - first.get(Calendar.YEAR)) * 12 +
                (current.get(Calendar.MONTH) - first.get(Calendar.MONTH))
            (diff + 1).coerceIn(1, MAX_MONTHS)
        }
        return List(count) { back -> (current.clone() as Calendar).apply { add(Calendar.MONTH, -back) } }
    }
}
