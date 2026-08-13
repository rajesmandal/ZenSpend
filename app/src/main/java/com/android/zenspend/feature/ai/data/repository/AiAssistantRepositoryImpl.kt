package com.android.zenspend.feature.ai.data.repository

import com.android.zenspend.feature.ai.domain.model.AiResponse
import com.android.zenspend.feature.ai.domain.repository.AiAssistantRepository
import com.android.zenspend.feature.ai.domain.repository.AiProvider
import com.android.zenspend.feature.ai.domain.tool.ToolRegistry
import com.android.zenspend.feature.ai.domain.tool.ToolStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class AiAssistantRepositoryImpl @Inject constructor(
    private val aiProvider: AiProvider,
    private val toolRegistry: ToolRegistry
) : AiAssistantRepository {

    override fun askQuestion(query: String): Flow<AiResponse> = flow {
        emit(AiResponse.Loading)
        try {
            // Handshake 1: Detect Intent
            val intent = aiProvider.detectIntent(query)
            
            if (intent.toolName == null) {
                // If no tool matched, let the AI try to answer generally or say it can't help
                val response = aiProvider.generateResponse(query, "{}")
                emit(AiResponse.Success(response))
                return@flow
            }

            val tool = toolRegistry.getTool(intent.toolName)
            if (tool == null) {
                emit(AiResponse.Error("I'm sorry, I don't know how to handle that request yet."))
                return@flow
            }

            // Local Execution: Context Minimal
            val toolResult = tool.execute(intent.parameters)
            
            if (toolResult.status != ToolStatus.SUCCESS) {
                emit(AiResponse.Error("I encountered an error while fetching your data: ${toolResult.error}"))
                return@flow
            }

            // Handshake 2: Generate Response with Local Context
            val response = aiProvider.generateResponse(query, toolResult.data)
            emit(AiResponse.Success(response))

        } catch (e: Exception) {
            emit(AiResponse.Error("Assistant error: ${e.message ?: "Unknown error"}"))
        }
    }
}
