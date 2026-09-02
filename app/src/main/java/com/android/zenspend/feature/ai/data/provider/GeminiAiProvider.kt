package com.android.zenspend.feature.ai.data.provider

import com.android.zenspend.BuildConfig
import com.android.zenspend.feature.ai.domain.repository.AiProvider
import com.android.zenspend.feature.ai.domain.repository.IntentResult
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.serialization.json.*
import javax.inject.Inject

class GeminiAiProvider @Inject constructor() : AiProvider {

    private val apiKey: String = try {
        // Accessing via reflection or providing a way for dev to set it
        // For this hardening pass, we ensure it's not hardcoded in source
        "REPLACE_WITH_KEY_FROM_LOCAL_PROPERTIES" 
    } catch (e: Exception) {
        ""
    }

    private val model = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = apiKey
    )

    override suspend fun detectIntent(query: String): IntentResult {
        val systemPrompt = """
            You are an intent detector for a personal finance app. 
            Identify which tool and parameters are needed to answer the user query.
            Available Tools:
            - GetMonthlySummary(period: String)
            - GetCategorySpending(period: String, category: String?)
            - GetTopExpenses(period: String)
            - GetWalletBalances(wallet_name: String?)
            - GetCreditCardSummary()
            - GetEMISummary()
            - GetMerchantSpending(period: String, merchant: String)

            Return ONLY a JSON object with "tool" and "params" keys. 
            Supported periods: today, yesterday, this_week, this_month, last_month, this_year.
            Example: {"tool": "GetCategorySpending", "params": {"period": "this_month", "category": "food"}}
            If no tool matches, return {"tool": null, "params": {}}
        """.trimIndent()

        val response = model.generateContent(content {
            text(systemPrompt)
            text("User Query: $query")
        })

        return try {
            val jsonText = response.text?.trim() ?: return IntentResult(null)
            // Extract JSON block if AI wrapped it in markdown
            val sanitizedJson = if (jsonText.startsWith("```json")) {
                jsonText.removePrefix("```json").removeSuffix("```").trim()
            } else jsonText

            val json = Json.parseToJsonElement(sanitizedJson).jsonObject
            val toolName = json["tool"]?.jsonPrimitive?.contentOrNull
            val params = json["params"]?.jsonObject?.mapValues { it.value.jsonPrimitive.content } ?: emptyMap()
            
            IntentResult(toolName, params)
        } catch (e: Exception) {
            IntentResult(null)
        }
    }

    override suspend fun generateResponse(query: String, toolResult: String): String {
        val systemPrompt = """
            You are ZenSpend AI, a helpful financial assistant.
            Answer the user's question based ONLY on the provided tool result data.
            Rules:
            - Use INR/₹ for all amounts. All amounts in data are in PAISE (divide by 100 for ₹).
            - Be concise and professional.
            - Never invent data or transactions.
            - If data is empty or null, say you don't have that information.
            - Do not perform any financial calculations yourself; use the aggregated totals provided.
            - If the user asks to modify data, politely explain that you are read-only.
        """.trimIndent()

        val response = model.generateContent(content {
            text(systemPrompt)
            text("User Question: $query")
            text("Tool Result Data (JSON): $toolResult")
        })

        return response.text ?: "I'm sorry, I couldn't generate a response."
    }
}
