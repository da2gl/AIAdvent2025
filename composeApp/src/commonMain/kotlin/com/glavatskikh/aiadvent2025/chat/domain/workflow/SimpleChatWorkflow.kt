package com.glavatskikh.aiadvent2025.chat.domain.workflow

import com.glavatskikh.aiadvent2025.chat.data.models.ChatMessage
import com.glavatskikh.aiadvent2025.chat.data.models.GeminiModel
import com.glavatskikh.aiadvent2025.chat.data.models.MessageRole
import com.glavatskikh.aiadvent2025.chat.domain.agent.ChatAgent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock

class SimpleChatWorkflow() : Workflow {

    private val chatAgent = ChatAgent()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    override val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    override val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    override val workflowType = WorkflowType.SIMPLE_CHAT

    override suspend fun processInput(input: String): Result<ChatMessage> {
        _isProcessing.value = true

        // Add user message
        val userMessage = ChatMessage(
            id = generateMessageId(),
            content = input,
            role = MessageRole.USER,
            timestamp = Clock.System.now()
        )
        _messages.value = _messages.value + userMessage

        // Use ChatAgent to process the input
        val result = chatAgent.process(input)

        return result.fold(
            onSuccess = { agentMessage ->
                _messages.value = _messages.value + agentMessage
                _isProcessing.value = false
                Result.success(agentMessage)
            },
            onFailure = { error ->
                _isProcessing.value = false
                Result.failure(error)
            }
        )
    }

    override fun reset() {
        _messages.value = emptyList()
        chatAgent.clearHistory()
    }

    override fun getHistory(): List<ChatMessage> = _messages.value

    override fun setModel(model: GeminiModel) {
        chatAgent.setModel(model)
    }

    private fun generateMessageId(): String {
        return "msg-${Clock.System.now().toEpochMilliseconds()}"
    }
}