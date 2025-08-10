package com.glavatskikh.aiadvent2025.chat.data.models

import kotlinx.serialization.Serializable

@Serializable
data class GeminiResponse(
    val candidates: List<Candidate>,
    val promptFeedback: PromptFeedback? = null
)

@Serializable
data class Candidate(
    val content: Content,
    val finishReason: String? = null,
    val index: Int,
    val safetyRatings: List<SafetyRating>? = null
)

@Serializable
data class SafetyRating(
    val category: String,
    val probability: String
)

@Serializable
data class PromptFeedback(
    val safetyRatings: List<SafetyRating>? = null
)

@Serializable
data class GeminiError(
    val error: ErrorDetails
)

@Serializable
data class ErrorDetails(
    val code: Int,
    val message: String,
    val status: String
)