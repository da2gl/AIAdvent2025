package com.glavatskikh.aiadvent2025.chat.domain.agent

import com.glavatskikh.aiadvent2025.chat.domain.mcp.core.MCPHandler
import com.glavatskikh.aiadvent2025.chat.domain.mcp.handlers.github.GitHubMCPHandler

class ChatAgent(
    override val mcpHandlers: List<MCPHandler> = listOf(GitHubMCPHandler())
) : BaseAgent(
    agentId = "chat-agent",
    agentName = "Assistant",
)