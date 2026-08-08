package com.android.zenspend.data.ai

import android.graphics.Bitmap
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import org.json.JSONObject

class GeminiManager {

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = "AIzaSyBcOOUe08btiDtCuKfdv2BkATKG97Lxufo",
        generationConfig = generationConfig {
            responseMimeType = "application/json"
        }
    )

    suspend fun extractExpenseFromText(input: String): JSONObject? {
        val prompt = "From the following text, extract the expense title, amount, and category. Return it as a JSON object with the keys 'title', 'amount', and 'category'.\n\nText: $input"
        val response = generativeModel.generateContent(prompt)
        return response.text?.let { JSONObject(it) }
    }

    suspend fun extractExpenseFromImage(bitmap: Bitmap): JSONObject? {
        val inputContent = content {
            image(bitmap)
            text("Extract the total amount, title, and category from this receipt. Return it as a JSON object with the keys 'title', 'amount', and 'category'.")
        }

        val response = generativeModel.generateContent(inputContent)
        return response.text?.let { JSONObject(it) }
    }
}
