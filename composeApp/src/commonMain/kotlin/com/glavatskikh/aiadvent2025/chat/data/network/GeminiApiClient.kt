package com.glavatskikh.aiadvent2025.chat.data.network

import com.glavatskikh.aiadvent2025.chat.data.models.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class GeminiApiClient(
    private val apiKey: String
) {
    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
        
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.INFO
        }
        
        install(HttpTimeout) {
            requestTimeoutMillis = 60000
            connectTimeoutMillis = 30000
            socketTimeoutMillis = 60000
        }
        
        defaultRequest {
            contentType(ContentType.Application.Json)
        }
    }
    
    suspend fun generateContent(
        prompt: String,
        model: GeminiModel = GeminiModel.GEMINI_1_5_FLASH,
        conversationHistory: List<Content> = emptyList()
    ): Result<String> {
        return try {
            val url = buildUrl(model)
            
            val contents = buildContents(prompt, conversationHistory)
            
            val request = GeminiRequest(
                contents = contents,
                generationConfig = GenerationConfig(
                    temperature = 0.7f,
                    topK = 40,
                    topP = 0.95f,
                    maxOutputTokens = 2048
                )
            )
            
            val response: HttpResponse = httpClient.post(url) {
                setBody(request)
            }
            
            when (response.status) {
                HttpStatusCode.OK -> {
                    val geminiResponse: GeminiResponse = response.body()
                    val content = geminiResponse.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    if (content != null) {
                        Result.success(content)
                    } else {
                        Result.failure(Exception("No content in response"))
                    }
                }
                else -> {
                    val errorResponse: GeminiError = response.body()
                    Result.failure(Exception(errorResponse.error.message))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun buildUrl(model: GeminiModel): String {
        return "https://generativelanguage.googleapis.com/v1beta/models/${model.modelName}:generateContent?key=$apiKey"
    }
    
    private fun buildContents(
        prompt: String,
        conversationHistory: List<Content>
    ): List<Content> {
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
    
    fun close() {
        httpClient.close()
    }
}