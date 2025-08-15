package com.glavatskikh.aiadvent2025.chat.domain.mcp.core

abstract class BaseMCPHandler : MCPHandler {
    abstract override val name: String
    abstract override val description: String
    abstract override val tools: List<MCPTool>

    override suspend fun execute(toolName: String, arguments: Map<String, Any?>): MCPToolResult {
        val tool = tools.find { it.name == toolName }
            ?: return MCPToolResult.error("Tool '$toolName' not found")

        return try {
            validate(tool, arguments)
            executeInternal(toolName, arguments)
        } catch (e: Exception) {
            MCPToolResult.error("Failed: ${e.message}")
        }
    }

    override fun supports(toolName: String) = tools.any { it.name == toolName }

    protected abstract suspend fun executeInternal(
        toolName: String,
        arguments: Map<String, Any?>
    ): MCPToolResult

    private fun validate(tool: MCPTool, arguments: Map<String, Any?>) {
        val missing = tool.inputSchema.required.filter { it !in arguments }
        if (missing.isNotEmpty()) {
            throw IllegalArgumentException("Missing: ${missing.joinToString()}")
        }
    }
}