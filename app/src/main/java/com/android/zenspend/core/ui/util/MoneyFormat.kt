package com.android.zenspend.core.ui.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import java.math.BigDecimal
import java.math.BigInteger
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.Locale

/**
 * Single place that decides how money looks. Grouping and the decimal mark follow the locale, so
 * en-US gives 1,234,567.89 and en-IN gives 12,34,567.89. To force one style for the whole app,
 * change [locale] (for example to Locale("en", "IN")).
 */
object MoneyFormat {
    private const val MINOR_PER_MAJOR = 100
    private const val FRACTION_DIGITS = 2
    private const val INDIAN_SECONDARY_GROUP = 2

    /** Locale used for every amount. Read on each call so a system locale change is picked up. */
    var locale: () -> Locale = { Locale.getDefault() }

    internal class Style(val group: Char, val decimal: Char, val primary: Int, val secondary: Int)

    internal fun style(): Style {
        val loc = locale()
        val symbols = DecimalFormatSymbols.getInstance(loc)
        // Learn the grouping sizes from the locale itself (3,3 for US, 3,2 for India).
        val sample = NumberFormat.getInstance(loc).format(BigInteger("123456789"))
        val cuts = sample.indices.filter { !sample[it].isDigit() }
        val digitsAfterLastCut = if (cuts.isEmpty()) 0 else sample.length - 1 - cuts.last()
        val primary = if (digitsAfterLastCut > 0) digitsAfterLastCut else Int.MAX_VALUE
        val learned = if (cuts.size >= 2) cuts[cuts.size - 1] - cuts[cuts.size - 2] - 1 else primary
        // Plain JVMs (unit tests) lack India's lakh pattern that Android's ICU has, so state it here.
        val secondary = if (loc.country == "IN") INDIAN_SECONDARY_GROUP else learned
        return Style(symbols.groupingSeparator, symbols.decimalSeparator, primary, secondary)
    }

    private fun groupDigits(digits: String, s: Style): String {
        if (digits.length <= s.primary) return digits
        val out = StringBuilder()
        var rest = digits
        out.insert(0, rest.takeLast(s.primary))
        rest = rest.dropLast(s.primary)
        while (rest.isNotEmpty()) {
            out.insert(0, s.group)
            out.insert(0, rest.takeLast(s.secondary))
            rest = rest.dropLast(s.secondary)
        }
        return out.toString()
    }

    /** Minor units (paise) as "1,234" or "1,234.50", no currency symbol. */
    fun format(minor: Long): String {
        val s = style()
        val negative = minor < 0
        val abs = BigInteger.valueOf(minor).abs()
        val (whole, frac) = abs.divideAndRemainder(BigInteger.valueOf(MINOR_PER_MAJOR.toLong()))
        val grouped = groupDigits(whole.toString(), s)
        val tail = if (frac.signum() == 0) "" else s.decimal + frac.toString().padStart(FRACTION_DIGITS, '0')
        return (if (negative) "-" else "") + grouped + tail
    }

    /** Raw, locale-independent text ("1234.5") that goes into an input field's state. */
    fun rawFromMinor(minor: Long): String =
        BigDecimal.valueOf(minor, FRACTION_DIGITS).stripTrailingZeros().toPlainString()

    /** Raw typed text ("1234567.8") shown grouped ("12,34,567.8"). Keeps a trailing dot while typing. */
    fun groupTyped(raw: String): String = display(raw, style()).first

    /** Grouped text plus, for each raw character offset, where the cursor lands in the grouped text. */
    internal fun display(raw: String, s: Style = style()): Pair<String, IntArray> {
        val dot = raw.indexOf('.')
        val whole = if (dot >= 0) raw.substring(0, dot) else raw
        val grouped = groupDigits(whole, s)
        val text = grouped + if (dot >= 0) s.decimal + raw.substring(dot + 1) else ""

        // offsets[i] = position in [text] for the cursor sitting after i raw characters.
        val offsets = IntArray(raw.length + 1)
        var pos = 0
        var rawIndex = 0
        offsets[0] = 0
        while (rawIndex < raw.length) {
            // skip group separators that sit before the next raw character
            while (pos < text.length && text[pos] == s.group && rawIndex < whole.length) pos++
            pos++
            rawIndex++
            offsets[rawIndex] = pos
        }
        return text to offsets
    }

    /** Visual transformation for amount text fields; the field's state stays raw digits and '.'. */
    val visualTransformation: VisualTransformation = VisualTransformation { input ->
        val s = style()
        val raw = input.text
        val (text, offsets) = display(raw, s)
        TransformedText(
            AnnotatedString(text),
            object : OffsetMapping {
                override fun originalToTransformed(offset: Int) = offsets[offset.coerceIn(0, raw.length)]
                override fun transformedToOriginal(offset: Int): Int {
                    val target = offset.coerceIn(0, text.length)
                    var best = 0
                    for (i in offsets.indices) if (offsets[i] <= target) best = i
                    return best
                }
            }
        )
    }
}
