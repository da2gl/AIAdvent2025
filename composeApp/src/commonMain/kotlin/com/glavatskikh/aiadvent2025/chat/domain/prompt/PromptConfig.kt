package com.glavatskikh.aiadvent2025.chat.domain.prompt

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

data class PromptConfig(
    val systemPrompt: String,
    val isDefault: Boolean = false,
    val lastModified: Instant = Clock.System.now()
) {
    companion object {
        val DEFAULT_JSON_PROMPT = """
            You must ALWAYS respond in strict JSON format.
            Response format:
            {
              "response": "your main response content",
              "type": "response_type", 
              "confidence": 0.95,
              "metadata": {
                "timestamp": "ISO_timestamp",
                "source": "gemini"
              }
            }
            
            Do NOT add any text before or after the JSON. Only return valid JSON.
        """.trimIndent()
        
        fun default() = PromptConfig(
            systemPrompt = DEFAULT_JSON_PROMPT,
            isDefault = true
        )
    }
}