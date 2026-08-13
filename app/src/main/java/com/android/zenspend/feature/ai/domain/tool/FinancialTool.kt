package com.android.zenspend.feature.ai.domain.tool

import kotlinx.serialization.Serializable

interface FinancialTool {
    val name: String
    val description: String
    suspend fun execute(parameters: Map<String, String>): ToolResult
}

@Serializable
data class ToolResult(
    val toolName: String,
    val data: String, // JSON representation of the tool output
    val status: ToolStatus = ToolStatus.SUCCESS,
    val error: String? = null
)

enum class ToolStatus {
    SUCCESS,
    FAILURE,
    UNSUPPORTED_PARAMETERS
}

// Common parameter keys
object ToolParam {
    const val PERIOD = "period" // e.g., "this_month", "last_month", "today"
    const val CATEGORY = "category"
    const val MERCHANT = "merchant"
    const val WALLET_NAME = "wallet_name"
}
