package com.android.zenspend.feature.category.domain.suggestion

import com.android.zenspend.core.database.model.TransactionType

data class CategorySuggestion(
    val categoryId: Long,
    val categoryName: String,
    val confidence: Float,
    val source: SuggestionSource,
    val reason: String? = null
)

enum class SuggestionSource {
    RULE,
    USER_HISTORY,
    AI
}

interface CategorySuggestionEngine {
    suspend fun getSuggestion(
        merchant: String?,
        title: String?,
        type: TransactionType
    ): CategorySuggestion?
}
