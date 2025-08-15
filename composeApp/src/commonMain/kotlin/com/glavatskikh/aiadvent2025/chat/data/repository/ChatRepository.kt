package com.glavatskikh.aiadvent2025.chat.data.repository

import com.glavatskikh.aiadvent2025.chat.data.ApiConfig
import com.glavatskikh.aiadvent2025.chat.data.models.ChatMessage
import com.glavatskikh.aiadvent2025.chat.data.models.GeminiModel
import com.glavatskikh.aiadvent2025.chat.domain.workflow.SimpleChatWorkflow
import com.glavatskikh.aiadvent2025.chat.domain.workflow.Workflow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface ChatRepository {
    val messages: StateFlow<List<ChatMessage>>
    val isLoading: StateFlow<Boolean>
    val currentModel: StateFlow<GeminiModel>

    suspend fun sendMessage(message: String): Result<ChatMessage>
    fun setModel(model: GeminiModel)
    fun clearMessages()
}

class ChatRepositoryImpl(
    private val workflow: Workflow = SimpleChatWorkflow()
) : ChatRepository {

    private val _currentModel = MutableStateFlow(GeminiModel.GEMINI_1_5_FLASH)
    override val currentModel: StateFlow<GeminiModel> = _currentModel.asStateFlow()

    override val messages: StateFlow<List<ChatMessage>> = workflow.messages
    override val isLoading: StateFlow<Boolean> = workflow.isProcessing

    override suspend fun sendMessage(message: String): Result<ChatMessage> {
        if (ApiConfig.GEMINI_API_KEY == "YOUR_API_KEY_HERE") {
            return Result.failure(Exception("Please set your Gemini API key in ApiConfig.kt"))
        }
        return workflow.processInput(message)
    }

    override fun setModel(model: GeminiModel) {
        _currentModel.value = model
        workflow.setModel(model)
    }

    override fun clearMessages() {
        workflow.reset()
    }
}