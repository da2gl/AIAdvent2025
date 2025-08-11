package com.glavatskikh.aiadvent2025.chat.data.services

import com.glavatskikh.aiadvent2025.chat.data.models.*
import com.glavatskikh.aiadvent2025.core.network.HttpApiClient
import io.ktor.client.call.*
import io.ktor.client.statement.*
import io.ktor.http.*

class GeminiService(
    private val apiKey: String,
    private val httpClient: HttpApiClient = HttpApiClient()
) {
    
    suspend fun generateContent(
        prompt: String,
        model: GeminiModel = GeminiModel.GEMINI_1_5_FLASH,
        conversationHistory: List<Content> = emptyList(),
        systemInstruction: String? = null
    ): Result<GeminiContentResponse> {
        return try {
            val url = buildUrl(model)
            val request = buildRequest(prompt, conversationHistory, systemInstruction)
            val headers = buildHeaders()
            
            val response = httpClient.post(url, request, headers)
            
            when (response.status) {
                HttpStatusCode.OK -> parseSuccessResponse(response)
                else -> parseErrorResponse(response)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun buildUrl(model: GeminiModel): String {
        return "https://generativelanguage.googleapis.com/v1beta/models/${model.modelName}:generateContent"
    }
    
    private fun buildHeaders(): Map<String, String> {
        return mapOf(
            "x-goog-api-key" to apiKey,
            "Content-Type" to "application/json"
        )
    }
    
    private fun buildRequest(prompt: String, conversationHistory: List<Content>, systemInstruction: String?): GeminiRequest {
        val contents = buildContents(prompt, conversationHistory)
        
        return GeminiRequest(
            contents = contents,
            generationConfig = GenerationConfig(
                temperature = 0.7f,
                topK = 40,
                topP = 0.95f,
                maxOutputTokens = 2048
            ),
            systemInstruction = systemInstruction?.let {
                SystemInstruction(parts = listOf(Part(text = it)))
            }
        )
    }
    
    private fun buildContents(prompt: String, conversationHistory: List<Content>): List<Content> {
        val contents = mutableListOf<Content>()
        contents.addAll(conversationHistory)
        contents.add(
            Content(
                parts = listOf(Part(text = prompt)),
                role = "user"
            )
        )
        return contents
    }
    
    private suspend fun parseSuccessResponse(response: HttpResponse): Result<GeminiContentResponse> {
        return try {
            val geminiResponse: GeminiResponse = response.body()
            val content = extractContent(geminiResponse)
            
            if (content != null) {
                val tokenUsage = extractTokenUsage(geminiResponse)
                val contentResponse = GeminiContentResponse(
                    content = content,
                    tokenUsage = tokenUsage
                )
                Result.success(contentResponse)
            } else {
                Result.failure(Exception("No content in response"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Failed to parse response: ${e.message}", e))
        }
    }
    
    private suspend fun parseErrorResponse(response: HttpResponse): Result<GeminiContentResponse> {
        return try {
            val errorResponse: GeminiError = response.body()
            Result.failure(Exception(errorResponse.error.message))
        } catch (e: Exception) {
            Result.failure(Exception("HTTP ${response.status.value}: ${response.status.description}"))
        }
    }
    
    private fun extractContent(geminiResponse: GeminiResponse): String? {
        return geminiResponse.candidates
            .firstOrNull()
            ?.content
            ?.parts
            ?.firstOrNull()
            ?.text
    }
    
    private fun extractTokenUsage(geminiResponse: GeminiResponse): TokenUsage? {
        return geminiResponse.usageMetadata?.let { usage ->
            TokenUsage(
                promptTokens = usage.promptTokenCount,
                responseTokens = usage.candidatesTokenCount,
                totalTokens = usage.totalTokenCount
            )
        }
    }
    
    fun close() {
        httpClient.close()
    }
}