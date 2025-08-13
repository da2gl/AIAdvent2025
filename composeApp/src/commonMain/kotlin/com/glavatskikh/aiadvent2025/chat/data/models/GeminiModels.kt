package com.glavatskikh.aiadvent2025.chat.data.models

enum class GeminiModel(
    val modelName: String,
    val displayName: String,
    val maxTokens: Int
) {
    GEMINI_PRO("gemini-pro", "Gemini Pro", 30720),
    GEMINI_PRO_VISION("gemini-pro-vision", "Gemini Pro Vision", 12288),
    GEMINI_1_5_PRO("gemini-1.5-pro", "Gemini 1.5 Pro", 1048576),
    GEMINI_1_5_FLASH("gemini-1.5-flash", "Gemini 1.5 Flash", 1048576),
    GEMINI_1_5_FLASH_8B("gemini-1.5-flash-8b", "Gemini 1.5 Flash 8B", 1048576),
    GEMINI_2_0_FLASH_EXP("gemini-2.0-flash-exp", "Gemini 2.0 Flash (Experimental)", 1048576);
}