package com.android.zenspend.core.ui.util

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class MoneyFormatTest {
    private val original = MoneyFormat.locale

    @After fun restore() { MoneyFormat.locale = original }

    private fun use(l: Locale) { MoneyFormat.locale = { l } }

    @Test fun `us style groups in thousands`() {
        use(Locale.US)
        assertEquals("1,234,567.89", MoneyFormat.format(123_456_789))
        assertEquals("1,000", MoneyFormat.format(100_000))
        assertEquals("0.05", MoneyFormat.format(5))
        assertEquals("-1,250.50", MoneyFormat.format(-125_050))
    }

    @Test fun `indian style groups lakhs and crores`() {
        use(Locale.forLanguageTag("en-IN"))
        assertEquals("12,34,567.89", MoneyFormat.format(123_456_789))
        assertEquals("26,622.60", MoneyFormat.format(2_662_260))
        assertEquals("999", MoneyFormat.format(99_900))
    }

    @Test fun `german style uses dot and comma`() {
        use(Locale.GERMANY)
        assertEquals("1.234.567,89", MoneyFormat.format(123_456_789))
    }

    @Test fun `typed text is grouped while typing`() {
        use(Locale.US)
        assertEquals("1,234,567.", MoneyFormat.groupTyped("1234567."))
        assertEquals("12.5", MoneyFormat.groupTyped("12.5"))
        assertEquals("", MoneyFormat.groupTyped(""))
        use(Locale.forLanguageTag("en-IN"))
        assertEquals("12,34,567.8", MoneyFormat.groupTyped("1234567.8"))
    }

    @Test fun `raw text from minor units is plain`() {
        assertEquals("1234.5", MoneyFormat.rawFromMinor(123_450))
        assertEquals("100", MoneyFormat.rawFromMinor(10_000))
        assertEquals("0", MoneyFormat.rawFromMinor(0))
    }

    @Test fun `cursor offsets skip group separators`() {
        use(Locale.US)
        val (text, offsets) = MoneyFormat.display("1234")
        assertEquals("1,234", text)
        assertEquals(listOf(0, 1, 3, 4, 5), offsets.toList())
    }
}
