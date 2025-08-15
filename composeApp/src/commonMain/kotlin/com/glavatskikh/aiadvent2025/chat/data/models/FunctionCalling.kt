package com.glavatskikh.aiadvent2025.chat.data.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class Tool(
    val functionDeclarations: List<FunctionDeclaration>
)

@Serializable
data class FunctionDeclaration(
    val name: String,
    val description: String,
    val parameters: JsonElement
)

@Serializable
data class FunctionCall(
    val name: String,
    val args: JsonElement
)

@Serializable
data class FunctionResponse(
    val name: String,
    val response: JsonElement
)

@Serializable
data class ToolConfig(
    val functionCallingConfig: FunctionCallingConfig
)

@Serializable
data class FunctionCallingConfig(
    val mode: String = "AUTO"
)