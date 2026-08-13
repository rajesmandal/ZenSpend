package com.android.zenspend.feature.voice.domain.model

import com.android.zenspend.core.database.model.TransactionType

data class VoiceTransactionDraft(
    val transcript: String,
    val amount: Long?, // in paise
    val type: TransactionType?,
    val merchant: String?,
    val categoryId: Long?,
    val sourceWalletId: Long?,
    val destinationWalletId: Long?,
    val date: Long = System.currentTimeMillis(),
    val confidenceScore: Float = 0f,
    val parsingStatus: ParsingStatus = ParsingStatus.INCOMPLETE,
    val missingFields: List<VoiceField> = emptyList()
)

enum class ParsingStatus {
    SUCCESS,
    AMBIGUOUS,
    INCOMPLETE,
    ERROR
}

enum class VoiceField {
    AMOUNT,
    TYPE,
    SOURCE_WALLET,
    DESTINATION_WALLET,
    CATEGORY
}
