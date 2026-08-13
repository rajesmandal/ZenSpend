package com.android.zenspend.feature.ai.domain.model

data class AiMessage(
    val id: String,
    val content: String,
    val role: AiRole,
    val timestamp: Long = System.currentTimeMillis()
)

enum class AiRole {
    USER,
    ASSISTANT,
    SYSTEM
}

data class AiSession(
    val id: String,
    val messages: List<AiMessage>
)

sealed class AiResponse {
    data class Success(val content: String) : AiResponse()
    data class Error(val message: String) : AiResponse()
    object Loading : AiResponse()
}
