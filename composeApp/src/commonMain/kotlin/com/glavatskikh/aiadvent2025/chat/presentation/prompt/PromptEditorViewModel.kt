package com.glavatskikh.aiadvent2025.chat.presentation.prompt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.glavatskikh.aiadvent2025.chat.domain.prompt.PromptConfig
import com.glavatskikh.aiadvent2025.chat.domain.prompt.PromptManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PromptEditorViewModel(
    private val promptManager: PromptManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(PromptEditorState())
    val uiState: StateFlow<PromptEditorState> = _uiState.asStateFlow()
    
    init {
        viewModelScope.launch {
            promptManager.currentPromptConfig.collect { config ->
                _uiState.update { state ->
                    state.copy(
                        currentPrompt = config.systemPrompt,
                        originalPrompt = config.systemPrompt,
                        isUsingCustomPrompt = !config.isDefault,
                        hasChanges = false
                    )
                }
            }
        }
    }
    
    fun openEditor() {
        viewModelScope.launch {
            val currentPrompt = promptManager.getActivePrompt()
            _uiState.update { state ->
                state.copy(
                    isEditing = true,
                    currentPrompt = currentPrompt,
                    originalPrompt = currentPrompt,
                    hasChanges = false,
                    error = null
                )
            }
        }
    }
    
    fun closeEditor() {
        _uiState.update { state ->
            state.copy(
                isEditing = false,
                currentPrompt = state.originalPrompt,
                hasChanges = false,
                error = null
            )
        }
    }
    
    fun updatePrompt(newPrompt: String) {
        _uiState.update { state ->
            state.copy(
                currentPrompt = newPrompt,
                hasChanges = newPrompt != state.originalPrompt,
                error = if (newPrompt.isBlank()) "Prompt cannot be empty" else null
            )
        }
    }
    
    fun savePrompt() {
        val currentPrompt = _uiState.value.currentPrompt
        
        if (currentPrompt.isBlank()) {
            _uiState.update { it.copy(error = "Prompt cannot be empty") }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            
            try {
                promptManager.savePrompt(currentPrompt)
                _uiState.update { state ->
                    state.copy(
                        isSaving = false,
                        isEditing = false,
                        originalPrompt = currentPrompt,
                        hasChanges = false,
                        isUsingCustomPrompt = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        isSaving = false,
                        error = e.message ?: "Failed to save prompt"
                    )
                }
            }
        }
    }
    
    fun resetToDefault() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            
            try {
                promptManager.resetToDefault()
                val defaultPrompt = PromptConfig.DEFAULT_JSON_PROMPT
                _uiState.update { state ->
                    state.copy(
                        isSaving = false,
                        currentPrompt = defaultPrompt,
                        originalPrompt = defaultPrompt,
                        hasChanges = false,
                        isUsingCustomPrompt = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        isSaving = false,
                        error = e.message ?: "Failed to reset prompt"
                    )
                }
            }
        }
    }
}