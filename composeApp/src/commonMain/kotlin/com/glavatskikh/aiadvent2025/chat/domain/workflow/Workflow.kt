package com.glavatskikh.aiadvent2025.chat.domain.workflow

import com.glavatskikh.aiadvent2025.chat.data.models.ChatMessage
import com.glavatskikh.aiadvent2025.chat.data.models.GeminiModel
import kotlinx.coroutines.flow.StateFlow

interface Workflow {
    val messages: StateFlow<List<ChatMessage>>
    val isProcessing: StateFlow<Boolean>
    val workflowType: WorkflowType

    fun setModel(model: GeminiModel)
    suspend fun processInput(input: String): Result<ChatMessage>
    fun reset()
    fun getHistory(): List<ChatMessage>
}

enum class WorkflowType {
    SIMPLE_CHAT,
    RECIPE_CREATION,
}