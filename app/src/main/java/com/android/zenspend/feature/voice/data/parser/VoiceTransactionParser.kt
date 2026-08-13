package com.android.zenspend.feature.voice.data.parser

import com.android.zenspend.core.database.model.TransactionType
import com.android.zenspend.feature.voice.domain.model.*
import java.util.regex.Pattern

object VoiceTransactionParser {

    private val AMOUNT_PATTERN = Pattern.compile("(?i)(\\d+(?:\\.\\d{1,2})?)\\s*(?:rs\\.?|inr|₹|rupaye|rupees|bucks|k)?")
    
    private val DEBIT_KEYWORDS = listOf("expense", "spent", "paid", "kharcha", "spend", "kaat liya", "nikala")
    private val CREDIT_KEYWORDS = listOf("income", "salary", "credited", "aaya", "mile", "mila", "deposit")
    private val TRANSFER_KEYWORDS = listOf("transfer", "bheja", "bejo")
    private val CC_PAYMENT_KEYWORDS = listOf("bill pay", "card pay", "towards card")
    private val REFUND_KEYWORDS = listOf("refund", "vaapas", "back")

    fun parse(
        transcript: String,
        wallets: List<com.android.zenspend.feature.wallet.domain.model.Wallet>,
        categories: List<com.android.zenspend.feature.category.domain.model.Category>
    ): VoiceTransactionDraft {
        val lowerTranscript = transcript.lowercase()
        val amount = extractAmount(lowerTranscript)
        val type = detectType(lowerTranscript)
        
        val matchedWallets = matchWallets(lowerTranscript, wallets)
        val matchedCategories = matchCategories(lowerTranscript, categories)

        val missingFields = mutableListOf<VoiceField>()
        if (amount == null) missingFields.add(VoiceField.AMOUNT)
        if (type == null) missingFields.add(VoiceField.TYPE)
        
        var sourceWalletId: Long? = null
        var destWalletId: Long? = null
        var parsingStatus = ParsingStatus.SUCCESS

        when (type) {
            TransactionType.INCOME -> {
                if (matchedWallets.size == 1) destWalletId = matchedWallets.first().id
                else if (matchedWallets.size > 1) parsingStatus = ParsingStatus.AMBIGUOUS
                else missingFields.add(VoiceField.DESTINATION_WALLET)
            }
            TransactionType.TRANSFER, TransactionType.CREDIT_CARD_PAYMENT -> {
                if (matchedWallets.size == 2) {
                    val seIndex = lowerTranscript.indexOf(" se ")
                    val meIndex = lowerTranscript.indexOf(" me ")
                    
                    if (seIndex != -1 && meIndex != -1) {
                        val firstWallet = matchedWallets[0]
                        val secondWallet = matchedWallets[1]
                        
                        val firstIndex = lowerTranscript.indexOf(firstWallet.name.lowercase())
                        val secondIndex = lowerTranscript.indexOf(secondWallet.name.lowercase())
                        
                        // Check which wallet is before "se" (from) and which is before "me" (to)
                        if (firstIndex < seIndex && secondIndex > seIndex && secondIndex < meIndex) {
                            sourceWalletId = firstWallet.id
                            destWalletId = secondWallet.id
                        } else if (secondIndex < seIndex && firstIndex > seIndex && firstIndex < meIndex) {
                            sourceWalletId = secondWallet.id
                            destWalletId = firstWallet.id
                        } else {
                            // Fallback simple assignment if "se/me" logic is unclear
                            sourceWalletId = matchedWallets.find { lowerTranscript.indexOf(it.name.lowercase()) < seIndex + 5 }?.id
                            destWalletId = matchedWallets.find { it.id != sourceWalletId }?.id
                        }
                    } else {
                        // Default if se/me keywords not found but 2 wallets matched
                        sourceWalletId = matchedWallets[0].id
                        destWalletId = matchedWallets[1].id
                    }
                    
                    if (sourceWalletId == destWalletId) {
                        parsingStatus = ParsingStatus.ERROR
                    }
                } else if (matchedWallets.size < 2) {
                    missingFields.add(VoiceField.SOURCE_WALLET)
                    missingFields.add(VoiceField.DESTINATION_WALLET)
                    parsingStatus = ParsingStatus.INCOMPLETE
                }
            }
            else -> {
                if (matchedWallets.size == 1) sourceWalletId = matchedWallets.first().id
                else if (matchedWallets.size > 1) parsingStatus = ParsingStatus.AMBIGUOUS
                else missingFields.add(VoiceField.SOURCE_WALLET)
            }
        }

        if (missingFields.isNotEmpty() && parsingStatus == ParsingStatus.SUCCESS) {
            parsingStatus = ParsingStatus.INCOMPLETE
        }

        return VoiceTransactionDraft(
            transcript = transcript,
            amount = amount,
            type = type,
            merchant = extractMerchant(lowerTranscript),
            categoryId = matchedCategories.firstOrNull()?.id,
            sourceWalletId = sourceWalletId,
            destinationWalletId = destWalletId,
            confidenceScore = calculateConfidence(amount, type, matchedWallets.size),
            parsingStatus = parsingStatus,
            missingFields = missingFields
        )
    }

    private fun extractAmount(text: String): Long? {
        val matcher = AMOUNT_PATTERN.matcher(text)
        if (matcher.find()) {
            val amountStr = matcher.group(1) ?: return null
            val isK = text.contains("${amountStr}k", ignoreCase = true)
            return try {
                val value = amountStr.toDouble()
                val multiplier = if (isK) 1000 else 1
                (value * multiplier * 100).toLong()
            } catch (e: Exception) {
                null
            }
        }
        return null
    }

    private fun detectType(text: String): TransactionType? {
        if (CC_PAYMENT_KEYWORDS.any { text.contains(it) }) return TransactionType.CREDIT_CARD_PAYMENT
        if (REFUND_KEYWORDS.any { text.contains(it) }) return TransactionType.REFUND
        if (TRANSFER_KEYWORDS.any { text.contains(it) }) return TransactionType.TRANSFER
        if (CREDIT_KEYWORDS.any { text.contains(it) }) return TransactionType.INCOME
        if (DEBIT_KEYWORDS.any { text.contains(it) }) return TransactionType.EXPENSE
        return null
    }

    private fun matchWallets(text: String, wallets: List<com.android.zenspend.feature.wallet.domain.model.Wallet>) =
        wallets.filter { text.contains(it.name.lowercase()) }

    private fun matchCategories(text: String, categories: List<com.android.zenspend.feature.category.domain.model.Category>) =
        categories.filter { text.contains(it.name.lowercase()) }

    private fun extractMerchant(text: String): String? {
        val patterns = listOf("at\\s+([^\\s]+)", "from\\s+([^\\s]+)", "to\\s+([^\\s]+)", "on\\s+([^\\s]+)")
        for (p in patterns) {
            val matcher = Pattern.compile(p).matcher(text)
            if (matcher.find()) return matcher.group(1)?.uppercase()
        }
        return null
    }

    private fun calculateConfidence(amount: Long?, type: TransactionType?, walletCount: Int): Float {
        var score = 0f
        if (amount != null) score += 0.4f
        if (type != null) score += 0.3f
        if (walletCount == 1) score += 0.3f
        return score
    }
}
