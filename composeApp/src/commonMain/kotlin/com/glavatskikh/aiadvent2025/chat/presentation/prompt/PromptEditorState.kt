package com.glavatskikh.aiadvent2025.chat.presentation.prompt

data class PromptEditorState(
    val currentPrompt: String = "",
    val originalPrompt: String = "",
    val isEditing: Boolean = false,
    val hasChanges: Boolean = false,
    val isUsingCustomPrompt: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null
)