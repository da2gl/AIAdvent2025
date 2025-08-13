package com.glavatskikh.aiadvent2025.chat.domain.agent

import com.glavatskikh.aiadvent2025.chat.data.models.ChatMessage
import com.glavatskikh.aiadvent2025.chat.data.models.Content
import com.glavatskikh.aiadvent2025.chat.data.models.GeminiModel
import com.glavatskikh.aiadvent2025.chat.data.models.MessageRole
import com.glavatskikh.aiadvent2025.chat.data.models.Part
import com.glavatskikh.aiadvent2025.chat.data.services.GeminiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock

abstract class BaseAgent(
    override val agentId: String,
    override val agentName: String,
    override val systemPrompt: String
) : Agent {

    private val _model = MutableStateFlow(GeminiModel.GEMINI_1_5_FLASH)

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    override val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    override val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    protected val geminiService = GeminiService()

    override fun setModel(model: GeminiModel) {
        _model.value = model
    }

    override suspend fun process(input: String): Result<ChatMessage> {
        _isProcessing.value = true

        val inputMessage = ChatMessage(
            id = generateMessageId(),
            content = input,
            role = MessageRole.USER,
            timestamp = Clock.System.now()
        )
        _messages.value = _messages.value + inputMessage

        val conversationHistory = buildConversationHistory()

        return try {
            val result = geminiService.generateContent(
                prompt = input,
                model = _model.value,
                conversationHistory = conversationHistory,
                systemInstruction = systemPrompt
            )

            result.fold(
                onSuccess = { response ->
                    val agentMessage = ChatMessage(
                        id = generateMessageId(),
                        content = response.content,
                        role = MessageRole.ASSISTANT,
                        timestamp = Clock.System.now(),
                        tokenUsage = response.tokenUsage,
                        agentName = agentName
                    )
                    _messages.value = _messages.value + agentMessage
                    _isProcessing.value = false
                    Result.success(agentMessage)
                },
                onFailure = { error ->
                    _isProcessing.value = false
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            _isProcessing.value = false
            Result.failure(e)
        }
    }

    override fun clearHistory() {
        _messages.value = emptyList()
    }

    override fun getHistory(): List<ChatMessage> = _messages.value

    private fun buildConversationHistory(): List<Content> {
        return _messages.value.map { message ->
            Content(
                parts = listOf(Part(text = message.content)),
                role = when (message.role) {
                    MessageRole.USER -> "user"
                    MessageRole.ASSISTANT -> "model"
                    MessageRole.SYSTEM -> "user"
                }
            )
        }
    }

    private fun generateMessageId(): String {
        return "${agentId}-${Clock.System.now().toEpochMilliseconds()}"
    }
}