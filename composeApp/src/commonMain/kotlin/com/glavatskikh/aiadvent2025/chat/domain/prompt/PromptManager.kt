package com.glavatskikh.aiadvent2025.chat.domain.prompt

import kotlinx.coroutines.flow.StateFlow

interface PromptManager {
    val currentPromptConfig: StateFlow<PromptConfig>
    
    suspend fun savePrompt(prompt: String)
    suspend fun getActivePrompt(): String
    suspend fun resetToDefault()
    suspend fun isUsingCustomPrompt(): Boolean
}