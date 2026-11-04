package com.android.zenspend.core.ui.designsystem.component

import org.junit.Assert.assertEquals
import org.junit.Test

class KeypadTest {

    private fun type(vararg keys: KeypadKey): String =
        keys.fold("") { text, key -> applyKeypadKey(text, key) }

    private fun d(c: Char) = KeypadKey.Digit(c)

    @Test
    fun digitsAppend() {
        assertEquals("125", type(d('1'), d('2'), d('5')))
    }

    @Test
    fun leadingZeroIsReplaced() {
        assertEquals("5", type(d('0'), d('5')))
    }

    @Test
    fun dotOnEmptyStartsWithZero() {
        assertEquals("0.", type(KeypadKey.Dot))
    }

    @Test
    fun secondDotIsIgnored() {
        assertEquals("1.5", type(d('1'), KeypadKey.Dot, d('5'), KeypadKey.Dot))
    }

    @Test
    fun fractionLimitedToTwoDigits() {
        assertEquals("1.25", type(d('1'), KeypadKey.Dot, d('2'), d('5'), d('9')))
    }

    @Test
    fun integerPartLimited() {
        assertEquals("123456789", type(*"1234567890".map { d(it) }.toTypedArray()))
    }

    @Test
    fun backspaceRemovesLastAndIsSafeWhenEmpty() {
        assertEquals("1", type(d('1'), d('2'), KeypadKey.Backspace))
        assertEquals("", type(KeypadKey.Backspace))
    }
}
