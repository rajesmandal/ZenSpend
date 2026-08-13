package com.android.zenspend.feature.ai.domain.usecase

import com.android.zenspend.feature.ai.domain.model.AiResponse
import com.android.zenspend.feature.ai.domain.repository.AiAssistantRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ChatWithAiUseCase @Inject constructor(
    private val repository: AiAssistantRepository
) {
    operator fun invoke(query: String): Flow<AiResponse> {
        return repository.askQuestion(query)
    }
}
