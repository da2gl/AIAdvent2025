package com.glavatskikh.aiadvent2025.chat.data.models

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class ChatMessage(
    val id: String,
    val content: String,
    val role: MessageRole,
    val timestamp: Instant,
    val tokenUsage: TokenUsage? = null
)

@Serializable
enum class MessageRole {
    USER,
    ASSISTANT,
    SYSTEM
}

@Serializable
data class TokenUsage(
    val promptTokens: Int,
    val responseTokens: Int,
    val totalTokens: Int
)

data class GeminiContentResponse(
    val content: String,
    val tokenUsage: TokenUsage?
)