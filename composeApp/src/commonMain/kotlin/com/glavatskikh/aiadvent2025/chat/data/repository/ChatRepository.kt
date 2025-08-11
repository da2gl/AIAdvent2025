package com.glavatskikh.aiadvent2025.chat.data.repository

import com.glavatskikh.aiadvent2025.chat.data.ApiConfig
import com.glavatskikh.aiadvent2025.chat.data.models.*
import com.glavatskikh.aiadvent2025.chat.data.services.GeminiService
import com.glavatskikh.aiadvent2025.chat.domain.prompt.PromptManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock

interface ChatRepository {
    val messages: StateFlow<List<ChatMessage>>
    val isLoading: StateFlow<Boolean>
    val currentModel: StateFlow<GeminiModel>
    
    suspend fun sendMessage(message: String): Result<ChatMessage>
    fun setModel(model: GeminiModel)
    fun clearMessages()
}

class ChatRepositoryImpl(
    private val promptManager: PromptManager
) : ChatRepository {
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    override val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    override val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _currentModel = MutableStateFlow(GeminiModel.GEMINI_1_5_FLASH)
    override val currentModel: StateFlow<GeminiModel> = _currentModel.asStateFlow()
    
    private val geminiService = GeminiService(ApiConfig.GEMINI_API_KEY)
    
    override suspend fun sendMessage(message: String): Result<ChatMessage> {
        if (ApiConfig.GEMINI_API_KEY == "YOUR_API_KEY_HERE") {
            return Result.failure(Exception("Please set your Gemini API key in ApiConfig.kt"))
        }
        
        _isLoading.value = true
        
        val userMessage = ChatMessage(
            id = generateMessageId(),
            content = message,
            role = MessageRole.USER,
            timestamp = Clock.System.now()
        )
        
        _messages.value = _messages.value + userMessage
        
        val conversationHistory = buildConversationHistory()
        val systemInstruction = promptManager.getActivePrompt()
        
        return try {
            val result = geminiService.generateContent(
                prompt = message,
                model = _currentModel.value,
                conversationHistory = conversationHistory,
                systemInstruction = systemInstruction
            )
            
            result.fold(
                onSuccess = { response ->
                    val assistantMessage = ChatMessage(
                        id = generateMessageId(),
                        content = response.content,
                        role = MessageRole.ASSISTANT,
                        timestamp = Clock.System.now(),
                        tokenUsage = response.tokenUsage
                    )
                    _messages.value = _messages.value + assistantMessage
                    _isLoading.value = false
                    Result.success(assistantMessage)
                },
                onFailure = { error ->
                    _isLoading.value = false
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            _isLoading.value = false
            Result.failure(e)
        }
    }
    
    override fun setModel(model: GeminiModel) {
        _currentModel.value = model
    }
    
    override fun clearMessages() {
        _messages.value = emptyList()
    }
    
    private fun buildConversationHistory(): List<Content> {
        return _messages.value.dropLast(1).map { message ->
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
        return Clock.System.now().toEpochMilliseconds().toString()
    }
}