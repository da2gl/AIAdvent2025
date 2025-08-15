package com.glavatskikh.aiadvent2025.chat.data.models

import kotlinx.serialization.Serializable

@Serializable
data class GeminiRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig,
    val safetySettings: List<SafetySetting>? = null,
    val systemInstruction: SystemInstruction? = null,
    val tools: List<Tool>? = null,
    val toolConfig: ToolConfig? = null
)

@Serializable
data class Content(
    val parts: List<Part>,
    val role: String
)

@Serializable
data class Part(
    val text: String? = null,
    val functionCall: FunctionCall? = null,
    val functionResponse: FunctionResponse? = null
)

@Serializable
data class GenerationConfig(
    val temperature: Float? = 0.7f,
    val topK: Int? = 40,
    val topP: Float? = 0.95f,
    val maxOutputTokens: Int? = 1024,
    val stopSequences: List<String>? = null
)

@Serializable
data class SafetySetting(
    val category: String,
    val threshold: String
)

@Serializable
data class SystemInstruction(
    val parts: List<Part>
)