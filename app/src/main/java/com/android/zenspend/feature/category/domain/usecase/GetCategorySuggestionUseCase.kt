package com.android.zenspend.feature.category.domain.usecase

import com.android.zenspend.core.database.model.TransactionType
import com.android.zenspend.feature.category.data.suggestion.AiCategorySuggestionEngine
import com.android.zenspend.feature.category.data.suggestion.LocalCategorySuggestionEngine
import com.android.zenspend.feature.category.domain.suggestion.CategorySuggestion
import javax.inject.Inject

class GetCategorySuggestionUseCase @Inject constructor(
    private val localEngine: LocalCategorySuggestionEngine,
    private val aiEngine: AiCategorySuggestionEngine
) {
    suspend operator fun invoke(
        merchant: String?,
        title: String?,
        type: TransactionType,
        allowAi: Boolean = false
    ): CategorySuggestion? {
        if (merchant.isNullOrBlank() && title.isNullOrBlank()) return null

        // 1. Try local rules and user history first
        val localSuggestion = localEngine.getSuggestion(merchant, title, type)
        if (localSuggestion != null && localSuggestion.confidence >= 0.7f) {
            return localSuggestion
        }

        // 2. Fallback to AI if allowed and local confidence is low
        if (allowAi) {
            return aiEngine.getSuggestion(merchant, title, type)
        }

        return localSuggestion
    }
}
