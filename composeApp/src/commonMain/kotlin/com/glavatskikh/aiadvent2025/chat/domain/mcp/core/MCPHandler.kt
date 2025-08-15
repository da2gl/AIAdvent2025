package com.glavatskikh.aiadvent2025.chat.domain.mcp.core

interface MCPHandler {
    val name: String
    val description: String
    val tools: List<MCPTool>

    suspend fun execute(toolName: String, arguments: Map<String, Any?>): MCPToolResult
    fun supports(toolName: String): Boolean
}