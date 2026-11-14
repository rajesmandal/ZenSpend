package com.android.zenspend.core.quickadd

/** What was typed into the notification: "250" or "250 chai" (amount first, optional title after). */
data class QuickEntry(val amountMinor: Long, val title: String)

object QuickAddParser {
    private val pattern = Regex("""^\s*(\d+(?:[.,]\d{1,2})?)\s*(.*)$""")

    fun parse(input: CharSequence?): QuickEntry? {
        val match = pattern.matchEntire(input?.toString().orEmpty()) ?: return null
        val amount = match.groupValues[1].replace(',', '.').toDoubleOrNull() ?: return null
        val minor = Math.round(amount * MINOR_PER_MAJOR)
        if (minor <= 0) return null
        return QuickEntry(minor, match.groupValues[2].trim())
    }

    private const val MINOR_PER_MAJOR = 100
}
