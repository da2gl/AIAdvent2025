package com.glavatskikh.aiadvent2025.chat.data.models

import kotlinx.serialization.Serializable

@Serializable
data class GeminiParameter(
    val type: String,
    val description: String
)

@Serializable
data class GeminiSchema(
    val type: String = "object",
    val properties: Map<String, GeminiParameter>,
    val required: List<String>
)

@Serializable
data class FunctionResult(
    val content: String,
    val isError: Boolean = false
)

@Serializable
data class ErrorResult(
    val error: String
)