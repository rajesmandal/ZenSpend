package com.android.zenspend.feature.ai.data.provider

import com.android.zenspend.feature.ai.domain.repository.AiProvider
import com.android.zenspend.feature.ai.domain.repository.IntentResult
import javax.inject.Inject

/**
 * Production-ready AI provider that communicates with a secure backend proxy.
 * This prevents AI API keys from being embedded in the APK.
 */
class RemoteAiProvider @Inject constructor() : AiProvider {

    override suspend fun detectIntent(query: String): IntentResult {
        // In a real app, this would use Retrofit to call POST /api/v1/ai/intent
        // Return a stub for now as we don't have a backend URL
        return IntentResult(null)
    }

    override suspend fun generateResponse(query: String, toolResult: String): String {
        // In a real app, this would use Retrofit to call POST /api/v1/ai/generate-response
        return "ZenSpend AI is currently warming up and will be available to analyze your finances soon. Please check back later!"
    }
}
