package com.glavatskikh.aiadvent2025.chat.domain.mcp.core

import com.glavatskikh.aiadvent2025.chat.data.models.FunctionCall
import com.glavatskikh.aiadvent2025.chat.data.models.FunctionResponse
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject

// TODO: 15/08/2025 [glavatskikh] gemini rest api error with Serializable. use JsonPrimitive
class MCPBridge {

    suspend fun execute(call: FunctionCall, handlers: List<MCPHandler>): FunctionResponse {
        val handler = handlers.firstOrNull { it.supports(call.name) }
            ?: return errorResponse(call.name, "No handler found")

        val result = handler.execute(call.name, call.args.parse())
        return successResponse(call.name, result)
    }

    private fun errorResponse(name: String, error: String) = FunctionResponse(
        name = name,
        response = buildJsonObject { put("error", JsonPrimitive(error)) }
    )

    private fun successResponse(name: String, result: MCPToolResult) = FunctionResponse(
        name = name,
        response = buildJsonObject {
            put("content", JsonPrimitive(result.content))
            put("isError", JsonPrimitive(result.isError))
        }
    )
}

private fun JsonElement.parse(): Map<String, Any?> {
    if (this !is JsonObject) return emptyMap()

    return jsonObject.mapValues { (_, value) ->
        when {
            value !is JsonPrimitive -> value.toString()
            value.isString -> value.content
            else -> value.content.toIntOrNull()
                ?: value.content.toDoubleOrNull()
                ?: value.content.toBooleanStrictOrNull()
                ?: value.content
        }
    }
}