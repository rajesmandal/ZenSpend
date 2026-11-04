package com.android.zenspend.core.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.android.zenspend.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** Formats minor units (paise) as "1,234" or "1,234.50" (locale-aware) — no currency symbol. See [MoneyFormat]. */
fun formatMinorUnits(amount: Long): String = MoneyFormat.format(amount)

/** Greeting resource for the current hour. */
@Composable
fun greetingText(hourOfDay: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)): String {
    val res = when (hourOfDay) {
        in 0..11 -> R.string.greeting_morning
        in 12..16 -> R.string.greeting_afternoon
        else -> R.string.greeting_evening
    }
    return stringResource(res)
}

/** "Today" / "Yesterday" / "04 Nov". */
@Composable
fun relativeDateText(timestamp: Long): String {
    val now = Calendar.getInstance()
    val date = Calendar.getInstance().apply { timeInMillis = timestamp }
    val sameYear = now.get(Calendar.YEAR) == date.get(Calendar.YEAR)
    val dayDiff = now.get(Calendar.DAY_OF_YEAR) - date.get(Calendar.DAY_OF_YEAR)
    return when {
        sameYear && dayDiff == 0 -> stringResource(R.string.date_today)
        sameYear && dayDiff == 1 -> stringResource(R.string.date_yesterday)
        else -> SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(timestamp))
    }
}
