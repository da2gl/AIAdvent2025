package com.glavatskikh.aiadvent2025.chat.domain.agent

class ChatAgent : BaseAgent(
    agentId = "chat-agent",
    agentName = "Assistant",
    systemPrompt = "" // Empty system instruction for general chat
)