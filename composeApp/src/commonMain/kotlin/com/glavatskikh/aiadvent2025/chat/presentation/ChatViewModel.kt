package com.glavatskikh.aiadvent2025.chat.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.glavatskikh.aiadvent2025.chat.data.models.ChatMessage
import com.glavatskikh.aiadvent2025.chat.data.models.GeminiModel
import com.glavatskikh.aiadvent2025.chat.data.repository.ChatRepository
import com.glavatskikh.aiadvent2025.chat.data.repository.ChatRepositoryImpl
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val inputText: String = "",
    val selectedModel: GeminiModel = GeminiModel.GEMINI_1_5_FLASH,
    val availableModels: List<GeminiModel> = GeminiModel.entries,
    val errorMessage: String? = null
)

class ChatViewModel(
    private val repository: ChatRepository = ChatRepositoryImpl()
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()
    
    init {
        viewModelScope.launch {
            combine(
                repository.messages,
                repository.isLoading,
                repository.currentModel
            ) { messages, isLoading, model ->
                _uiState.value.copy(
                    messages = messages,
                    isLoading = isLoading,
                    selectedModel = model
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
    
    fun updateInputText(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }
    
    fun sendMessage() {
        val message = _uiState.value.inputText.trim()
        if (message.isEmpty()) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(inputText = "", errorMessage = null) }
            
            repository.sendMessage(message).fold(
                onSuccess = { },
                onFailure = { error ->
                    _uiState.update { 
                        it.copy(errorMessage = error.message)
                    }
                }
            )
        }
    }
    
    fun selectModel(model: GeminiModel) {
        repository.setModel(model)
        _uiState.update { it.copy(selectedModel = model) }
    }
    
    fun clearChat() {
        repository.clearMessages()
        _uiState.update { it.copy(errorMessage = null) }
    }
    
    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}