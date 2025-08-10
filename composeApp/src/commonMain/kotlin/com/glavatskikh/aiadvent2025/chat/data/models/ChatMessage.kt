package com.glavatskikh.aiadvent2025.chat.data.models

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class ChatMessage(
    val id: String,
    val content: String,
    val role: MessageRole,
    val timestamp: Instant
)

@Serializable
enum class MessageRole {
    USER,
    ASSISTANT,
    SYSTEM
}