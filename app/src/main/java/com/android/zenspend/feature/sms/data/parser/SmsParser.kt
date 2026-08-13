package com.android.zenspend.feature.sms.data.parser

import com.android.zenspend.core.database.model.TransactionType
import com.android.zenspend.feature.sms.domain.model.SmsTransactionDraft
import java.util.regex.Pattern

object SmsParser {

    private val AMOUNT_PATTERN = Pattern.compile("(?i)(?:rs\\.?|inr|₹)\\s*([\\d,]+(?:\\.\\d{1,2})?)")
    private val ACCOUNT_PATTERN = Pattern.compile("(?i)(?:a/c|card|acct|account)\\s*(?:ending\\s*)?[x*]*(\\d{4})")
    
    private val DEBIT_KEYWORDS = listOf("debited", "spent", "paid", "withdrawn", "purchase", "txn of")
    private val CREDIT_KEYWORDS = listOf("credited", "received", "deposited", "refunded", "reversed")
    private val CC_PAYMENT_KEYWORDS = listOf("payment received", "bill payment", "card payment", "towards card", "towards credit card")

    fun parse(message: String, sender: String, timestamp: Long): SmsTransactionDraft? {
        val amount = extractAmount(message) ?: return null
        val type = detectType(message)
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
            confidence = calculateConfidence(message, amount, type)
        )
    }

    private fun extractAmount(message: String): Long? {
        val matcher = AMOUNT_PATTERN.matcher(message)
        if (matcher.find()) {
            val amountStr = matcher.group(1)?.replace(",", "") ?: return null
            return try {
                if (amountStr.contains(".")) {
                    (amountStr.toDouble() * 100).toLong()
                } else {
                    amountStr.toLong() * 100
                }
            } catch (e: Exception) {
                null
            }
        }
        return null
    }

    private fun detectType(message: String): TransactionType {
        val lowerMessage = message.lowercase()
        
        // Special case for card payment which often contains "received" and "card"
        if ((lowerMessage.contains("payment") || lowerMessage.contains("received")) && 
            (lowerMessage.contains("card") || lowerMessage.contains("towards"))) {
            return TransactionType.CREDIT_CARD_PAYMENT
        }
        
        if (lowerMessage.contains("refund") || lowerMessage.contains("reversed")) {
            return TransactionType.REFUND
        }

        if (CREDIT_KEYWORDS.any { lowerMessage.contains(it) }) {
            return TransactionType.INCOME
        }
        
        if (DEBIT_KEYWORDS.any { lowerMessage.contains(it) }) {
            return TransactionType.EXPENSE
        }

        return TransactionType.EXPENSE
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
            "at\\s+([^\\s.,]+)",
            "paid\\s+to\\s+([^\\s.,]+)",
            "to\\s+([^\\s.,]+)",
            "from\\s+([^\\s.,]+)",
            "merchant\\s+([^\\s.,]+)"
        )

        val ignoreList = listOf("YOUR", "ACCOUNT", "A/C", "MY", "OUR")

        for (p in patterns) {
            val matcher = Pattern.compile("(?i)$p").matcher(message)
            if (matcher.find()) {
                val merchant = matcher.group(1)?.uppercase()
                if (merchant != null && merchant.length > 2 && !ignoreList.contains(merchant)) {
                    return merchant
                }
            }
        }
        return null
    }

    private fun generateFingerprint(message: String, sender: String, timestamp: Long): String {
        val content = "$sender|$timestamp|$message"
        return com.android.zenspend.core.util.ChecksumUtils.calculateSha256(content)
    }

    private fun calculateConfidence(message: String, amount: Long, type: TransactionType): Float {
        var score = 0.5f
        if (amount > 0) score += 0.2f
        if (detectType(message) != TransactionType.EXPENSE || message.lowercase().contains("debited")) {
            score += 0.2f
        }
        return score.coerceIn(0f, 1f)
    }
}
