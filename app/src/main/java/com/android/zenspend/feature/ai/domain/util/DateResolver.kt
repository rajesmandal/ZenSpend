package com.android.zenspend.feature.ai.domain.util

import java.util.*

object DateResolver {
    fun resolve(period: String?): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        return when (period?.lowercase()) {
            "today" -> {
                val start = calendar.timeInMillis
                calendar.add(Calendar.DAY_OF_YEAR, 1)
                start to calendar.timeInMillis
            }
            "yesterday" -> {
                calendar.add(Calendar.DAY_OF_YEAR, -1)
                val start = calendar.timeInMillis
                calendar.add(Calendar.DAY_OF_YEAR, 1)
                start to calendar.timeInMillis
            }
            "this_week" -> {
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                val start = calendar.timeInMillis
                calendar.add(Calendar.WEEK_OF_YEAR, 1)
                start to calendar.timeInMillis
            }
            "last_month" -> {
                calendar.add(Calendar.MONTH, -1)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                val start = calendar.timeInMillis
                calendar.add(Calendar.MONTH, 1)
                start to calendar.timeInMillis
            }
            "this_year" -> {
                calendar.set(Calendar.DAY_OF_YEAR, 1)
                val start = calendar.timeInMillis
                calendar.add(Calendar.YEAR, 1)
                start to calendar.timeInMillis
            }
            else -> { // Default to this_month
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                val start = calendar.timeInMillis
                calendar.add(Calendar.MONTH, 1)
                start to calendar.timeInMillis
            }
        }
    }
}
