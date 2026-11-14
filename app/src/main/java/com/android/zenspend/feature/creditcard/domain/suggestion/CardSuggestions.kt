package com.android.zenspend.feature.creditcard.domain.suggestion

import com.android.zenspend.feature.creditcard.domain.model.CreditCard
import com.android.zenspend.feature.sms.domain.model.SmsTransactionDraft

/** A card seen in SMS that is not in the app yet. */
data class CardSuggestion(
    val last4: String,
    val bankName: String?,
    val transactionCount: Int,
    val totalAmount: Long
)

object CardSuggestions {

    /**
     * Credit-card SMS drafts whose last 4 digits match no saved card, one suggestion per card
     * (most active first). [dismissed] holds last-4 values the user chose "Not now" for.
     */
    fun from(
        drafts: List<SmsTransactionDraft>,
        cards: List<CreditCard>,
        dismissed: Set<String> = emptySet()
    ): List<CardSuggestion> {
        val known = cards.mapNotNull { it.lastFourDigits }.toSet()
        return drafts
            .filter { it.isCard && !it.accountLast4.isNullOrBlank() }
            .groupBy { it.accountLast4!! }
            .filterKeys { it !in known && it !in dismissed }
            .map { (last4, group) ->
                CardSuggestion(
                    last4 = last4,
                    bankName = group.firstNotNullOfOrNull { BankSenders.bankName(it.sender) },
                    transactionCount = group.size,
                    totalAmount = group.sumOf { it.amount }
                )
            }
            .sortedByDescending { it.transactionCount }
    }
}
