package com.glavatskikh.aiadvent2025.chat.domain.mcp.core

import com.glavatskikh.aiadvent2025.chat.data.models.FunctionDeclaration
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject

data class MCPTool(
    val name: String,
    val description: String,
    val inputSchema: MCPSchema
)

// TODO: 15/08/2025 [glavatskikh] gemini rest api error with Serializable. use JsonPrimitive
fun MCPTool.toDeclaration() = FunctionDeclaration(name, description, inputSchema.toJson())

data class MCPSchema(
    val type: String = "object",
    val properties: Map<String, SchemaProperty>,
    val required: List<String>
)

private fun MCPSchema.toJson() = buildJsonObject {
    put("type", JsonPrimitive("object"))
    put("properties", buildJsonObject {
        properties.forEach { (name, prop) ->
            put(name, buildJsonObject {
                put("type", JsonPrimitive(prop.type))
                put("description", JsonPrimitive(prop.description))
            })
        }
    })
    put("required", buildJsonArray {
        required.forEach { add(JsonPrimitive(it)) }
    })
}

data class SchemaProperty(
    val type: String,
    val description: String,
)

data class MCPToolResult(
    val content: String,
    val isError: Boolean = false
) {
    companion object {
        fun success(text: String) = MCPToolResult(
            content = text,
            isError = false
        )

        fun error(message: String) = MCPToolResult(
            content = "Error: $message",
            isError = true
        )
    }
}
