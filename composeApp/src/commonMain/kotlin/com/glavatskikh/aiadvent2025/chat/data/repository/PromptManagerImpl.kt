package com.glavatskikh.aiadvent2025.chat.data.repository

import com.glavatskikh.aiadvent2025.chat.domain.prompt.PromptConfig
import com.glavatskikh.aiadvent2025.chat.domain.prompt.PromptManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock

class PromptManagerImpl : PromptManager {
    
    private val _currentPromptConfig = MutableStateFlow(PromptConfig.default())
    override val currentPromptConfig: StateFlow<PromptConfig> = _currentPromptConfig.asStateFlow()
    
    override suspend fun savePrompt(prompt: String) {
        require(prompt.isNotBlank()) { "Prompt cannot be empty" }
        
        val config = PromptConfig(
            systemPrompt = prompt,
            isDefault = false,
            lastModified = Clock.System.now()
        )
        
        _currentPromptConfig.value = config
    }
    
    override suspend fun getActivePrompt(): String {
        return _currentPromptConfig.value.systemPrompt
    }
    
    override suspend fun resetToDefault() {
        _currentPromptConfig.value = PromptConfig.default()
    }
    
    override suspend fun isUsingCustomPrompt(): Boolean {
        return !_currentPromptConfig.value.isDefault
    }
}