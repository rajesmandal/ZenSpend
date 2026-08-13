package com.android.zenspend.feature.category.data.suggestion

import com.android.zenspend.core.database.dao.CategoryDao
import com.android.zenspend.core.database.model.TransactionType
import com.android.zenspend.feature.ai.domain.repository.AiProvider
import com.android.zenspend.feature.category.domain.suggestion.CategorySuggestion
import com.android.zenspend.feature.category.domain.suggestion.CategorySuggestionEngine
import com.android.zenspend.feature.category.domain.suggestion.SuggestionSource
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject

class AiCategorySuggestionEngine @Inject constructor(
    private val aiProvider: AiProvider,
    private val categoryDao: CategoryDao
) : CategorySuggestionEngine {

    override suspend fun getSuggestion(
        merchant: String?,
        title: String?,
        type: TransactionType
    ): CategorySuggestion? {
        val query = "Categorize this transaction: ${merchant ?: title}. Type: $type."
        
        return try {
            val categories = categoryDao.getAllActiveCategories().first()
            val categoryNames = categories.joinToString(", ") { it.name }
            
            val promptContext = "Context: Suggest one of these categories: $categoryNames. Return ONLY JSON."
            
            val response = aiProvider.generateResponse(query, promptContext)
            
            // Extract JSON from response
            val sanitized = if (response.contains("{")) {
                response.substring(response.indexOf("{"), response.lastIndexOf("}") + 1)
            } else return null
            
            val json = Json.parseToJsonElement(sanitized).jsonObject
            val suggestedName = json["category"]?.jsonPrimitive?.content ?: return null
            val confidence = json["confidence"]?.jsonPrimitive?.content?.toFloatOrNull() ?: 0.5f
            
            val category = categories.find { it.name.equals(suggestedName, ignoreCase = true) }
            if (category != null) {
                CategorySuggestion(
                    categoryId = category.id,
                    categoryName = category.name,
                    confidence = confidence,
                    source = SuggestionSource.AI
                )
            } else null
        } catch (e: Exception) {
            // Log or handle exception if needed
            null
        }
    }
}
