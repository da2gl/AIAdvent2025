package com.glavatskikh.aiadvent2025.chat.domain.agent

import com.glavatskikh.aiadvent2025.chat.data.models.ChatMessage
import com.glavatskikh.aiadvent2025.chat.data.models.GeminiModel
import kotlinx.coroutines.flow.StateFlow

interface Agent {
    val agentId: String
    val agentName: String
    val systemPrompt: String?
    val messages: StateFlow<List<ChatMessage>>
    val isProcessing: StateFlow<Boolean>

    fun setModel(model: GeminiModel)
    suspend fun process(input: String): Result<ChatMessage>
    fun clearHistory()
    fun getHistory(): List<ChatMessage>
}
