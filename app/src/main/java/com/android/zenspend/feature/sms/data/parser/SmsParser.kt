package com.android.zenspend.feature.sms.data.parser

import com.android.zenspend.core.database.model.TransactionType
import com.android.zenspend.feature.sms.domain.model.SmsTransactionDraft
import java.util.regex.Pattern

object SmsParser {

    // Rs. 1,250.00 / Rs1250 / INR 1250 / ₹1,250 / "1250 rs"
    private val AMOUNT_PATTERN = Pattern.compile("(?i)(?:rs\\.?|inr|₹)\\s*([\\d,]+(?:\\.\\d{1,2})?)")
    private val AMOUNT_SUFFIX_PATTERN = Pattern.compile("(?i)([\\d,]+(?:\\.\\d{1,2})?)\\s*(?:rs\\b\\.?|inr\\b)")

    // A/c XXXX1234, a/c no. XX1234, card ending 1234, Card XX1234, acct *1234, account ending in 1234
    private val ACCOUNT_PATTERN = Pattern.compile(
        "(?i)(?:a/c|acct|account|card)\\s*(?:no\\.?|number|ending|ends)?\\s*(?:with|in|is)?\\s*[:\\-]?\\s*[x*•.\\s]*(\\d{4})\\b"
    )

    private val DEBIT_WORDS = listOf(
        "debited", "debit", "spent", "paid", "withdrawn", "purchase", "txn of", "sent", "deducted", "charged"
    )
    private val CREDIT_WORDS = listOf(
        "credited", "credit of", "received", "deposited", "refunded", "refund", "reversed", "reversal", "added to"
    )
    private val DEBIT_ABBREV = Regex("(?i)\\bdr\\b\\.?")
    private val CREDIT_ABBREV = Regex("(?i)\\bcr\\b\\.?")

    private val IGNORE_MESSAGE_KEYWORDS = listOf("otp", "one time password", "will be debited", "to be debited", "request for")
    private val CARD_SPEND_KEYWORDS = listOf("spent", "purchase", "used at", "txn of", "transaction of")

    /** Words that end a merchant name. */
    private val MERCHANT_STOP = Regex(
        "(?i)\\s+(?:on|via|using|ref|refno|upi|info|avl|available|bal|balance|from|to|for|credited|debited|dated|date|at)\\b.*$"
    )

    fun parse(message: String, sender: String, timestamp: Long): SmsTransactionDraft? {
        val lower = message.lowercase()
        if (IGNORE_MESSAGE_KEYWORDS.any { lower.contains(it) }) return null

        val amount = extractAmount(message) ?: return null
        val type = detectType(message) ?: return null
        val accountLast4 = extractAccountLast4(message)
        val merchant = extractMerchant(message)
        val fingerprint = generateFingerprint(message, sender, timestamp)

        return SmsTransactionDraft(
            amount = amount,
            type = type,
            date = timestamp,
            merchant = merchant,
            accountLast4 = accountLast4,
            sender = sender,
            fingerprint = fingerprint,
            confidence = calculateConfidence(accountLast4, merchant)
        )
    }

    private fun extractAmount(message: String): Long? {
        val raw = AMOUNT_PATTERN.matcher(message).let { if (it.find()) it.group(1) else null }
            ?: AMOUNT_SUFFIX_PATTERN.matcher(message).let { if (it.find()) it.group(1) else null }
            ?: return null
        val cleaned = raw.replace(",", "").trimEnd('.')
        return try {
            java.math.BigDecimal(cleaned).movePointRight(2).toBigInteger().toLong().takeIf { it > 0 }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Returns null when the message does not look like a money movement at all (so promos,
     * balance alerts etc. are not turned into fake transactions).
     */
    internal fun detectType(message: String): TransactionType? {
        val lower = message.lowercase()
        // "credit card" must not be read as a credit keyword
        val text = lower.replace("credit card", "creditcard").replace("debit card", "debitcard")

        val hasCard = text.contains("card")
        val isSpend = CARD_SPEND_KEYWORDS.any { text.contains(it) } || text.contains("debited")

        // Bill payment made towards a credit card -> card balance goes down
        val isCardBillPayment = hasCard && !isSpend && (
            Regex("payment.*(received|credited|posted|successful)").containsMatchIn(text) ||
                text.contains("received towards") ||
                text.contains("towards your") && text.contains("card") ||
                text.contains("towards creditcard") ||
                text.contains("card bill") ||
                text.contains("bill payment") ||
                text.contains("card payment")
            )
        if (isCardBillPayment) return TransactionType.CREDIT_CARD_PAYMENT

        if (text.contains("refund") || text.contains("reversed") || text.contains("reversal")) {
            return TransactionType.REFUND
        }

        // Whichever direction word comes first in the message wins ("debited ... credited to X" is a debit)
        val debitPos = firstPosition(text, DEBIT_WORDS, DEBIT_ABBREV)
        val creditPos = firstPosition(text, CREDIT_WORDS, CREDIT_ABBREV)

        return when {
            debitPos == -1 && creditPos == -1 -> null
            creditPos == -1 -> TransactionType.EXPENSE
            debitPos == -1 -> TransactionType.INCOME
            debitPos < creditPos -> TransactionType.EXPENSE
            else -> TransactionType.INCOME
        }
    }

    private fun firstPosition(text: String, words: List<String>, abbrev: Regex): Int {
        val positions = mutableListOf<Int>()
        words.forEach { w ->
            val idx = text.indexOf(w)
            if (idx >= 0) positions.add(idx)
        }
        abbrev.find(text)?.let { positions.add(it.range.first) }
        return positions.minOrNull() ?: -1
    }

    private fun extractAccountLast4(message: String): String? {
        val matcher = ACCOUNT_PATTERN.matcher(message)
        if (matcher.find()) {
            return matcher.group(1)
        }
        return null
    }

    private fun extractMerchant(message: String): String? {
        val patterns = listOf(
            "paid\\s+to\\s+(.+)",
            "\\bat\\s+(.+)",
            "\\bto\\s+(.+)",
            "\\bfrom\\s+(.+)",
            "merchant\\s+(.+)",
            "info[:\\-]\\s*(.+)"
        )

        val ignore = setOf("YOUR", "ACCOUNT", "A/C", "MY", "OUR", "YOU", "THE", "CARD", "BANK", "XX", "XXXX")

        for (p in patterns) {
            val matcher = Pattern.compile("(?i)$p").matcher(message)
            while (matcher.find()) {
                var candidate = matcher.group(1) ?: continue
                candidate = candidate.replace(MERCHANT_STOP, "")
                candidate = candidate.split(Regex("[.,;\\n]")).first().trim()
                val merchant = candidate.uppercase()
                val firstWord = merchant.substringBefore(' ')
                if (merchant.length > 2 &&
                    firstWord !in ignore &&
                    !merchant.matches(Regex("^[X*\\d]+$")) &&
                    !merchant.startsWith("A/C")
                ) {
                    return merchant.take(40)
                }
            }
        }
        return null
    }

    private fun generateFingerprint(message: String, sender: String, timestamp: Long): String {
        val content = "$sender|$timestamp|$message"
        return com.android.zenspend.core.util.ChecksumUtils.calculateSha256(content)
    }

    private fun calculateConfidence(accountLast4: String?, merchant: String?): Float {
        var score = 0.6f
        if (accountLast4 != null) score += 0.2f
        if (merchant != null) score += 0.2f
        return score.coerceIn(0f, 1f)
    }
}
