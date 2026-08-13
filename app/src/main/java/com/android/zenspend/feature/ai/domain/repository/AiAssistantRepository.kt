package com.android.zenspend.feature.ai.domain.repository

import com.android.zenspend.feature.ai.domain.model.AiResponse
import kotlinx.coroutines.flow.Flow

interface AiAssistantRepository {
    fun askQuestion(query: String): Flow<AiResponse>
}

interface AiProvider {
    suspend fun detectIntent(query: String): IntentResult
    suspend fun generateResponse(query: String, toolResult: String): String
}

data class IntentResult(
    val toolName: String?,
    val parameters: Map<String, String> = emptyMap()
)
