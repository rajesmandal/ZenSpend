package com.android.zenspend.core.quickadd

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class QuickAddParserTest {
    @Test fun amountOnly() = assertEquals(QuickEntry(250_00, ""), QuickAddParser.parse("250"))
    @Test fun amountWithTitle() = assertEquals(QuickEntry(250_00, "chai"), QuickAddParser.parse("  250 chai  "))
    @Test fun decimalAndCommaDecimal() {
        assertEquals(QuickEntry(99_50, "milk"), QuickAddParser.parse("99.5 milk"))
        assertEquals(QuickEntry(99_50, ""), QuickAddParser.parse("99,50"))
    }
    @Test fun rejectsNonsenseAndZero() {
        assertNull(QuickAddParser.parse("chai 250"))
        assertNull(QuickAddParser.parse("0"))
        assertNull(QuickAddParser.parse(""))
        assertNull(QuickAddParser.parse(null))
    }
}
