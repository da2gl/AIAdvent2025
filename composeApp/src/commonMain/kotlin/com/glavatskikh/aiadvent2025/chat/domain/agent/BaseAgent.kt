package com.glavatskikh.aiadvent2025.chat.domain.agent

import com.glavatskikh.aiadvent2025.chat.data.models.ChatMessage
import com.glavatskikh.aiadvent2025.chat.data.models.Content
import com.glavatskikh.aiadvent2025.chat.data.models.FunctionCall
import com.glavatskikh.aiadvent2025.chat.data.models.FunctionCallingConfig
import com.glavatskikh.aiadvent2025.chat.data.models.GeminiContentResponse
import com.glavatskikh.aiadvent2025.chat.data.models.GeminiModel
import com.glavatskikh.aiadvent2025.chat.data.models.MessageRole
import com.glavatskikh.aiadvent2025.chat.data.models.Part
import com.glavatskikh.aiadvent2025.chat.data.models.Tool
import com.glavatskikh.aiadvent2025.chat.data.models.ToolConfig
import com.glavatskikh.aiadvent2025.chat.data.services.GeminiService
import com.glavatskikh.aiadvent2025.chat.domain.mcp.core.MCPBridge
import com.glavatskikh.aiadvent2025.chat.domain.mcp.core.MCPHandler
import com.glavatskikh.aiadvent2025.chat.domain.mcp.core.toDeclaration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock

abstract class BaseAgent(
    override val agentId: String,
    override val agentName: String,
    override val systemPrompt: String? = null
) : Agent {

    private val _model = MutableStateFlow(GeminiModel.GEMINI_1_5_FLASH)

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    override val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    override val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    protected val geminiService = GeminiService()
    protected val mcpBridge = MCPBridge()

    protected open val mcpHandlers: List<MCPHandler> = emptyList()

    override fun setModel(model: GeminiModel) {
        _model.value = model
    }

    override suspend fun process(input: String): Result<ChatMessage> {
        _isProcessing.value = true

        return try {
            val inputMessage = createUserMessage(input)
            addMessage(inputMessage)

            generateResponse(input)
                .map { response -> createAgentMessage(response) }
                .onSuccess { addMessage(it) }
                .also { _isProcessing.value = false }
        } catch (e: Exception) {
            _isProcessing.value = false
            Result.failure(e)
        }
    }

    override fun clearHistory() {
        _messages.value = emptyList()
    }

    override fun getHistory(): List<ChatMessage> = _messages.value

    private fun buildConversationHistory(): List<Content> {
        return _messages.value.map { message ->
            Content(
                parts = listOf(Part(text = message.content)),
                role = when (message.role) {
                    MessageRole.USER -> "user"
                    MessageRole.ASSISTANT -> "model"
                    MessageRole.SYSTEM -> "user"
                }
            )
        }
    }

    private fun generateMessageId(): String {
        return "${agentId}-${Clock.System.now().toEpochMilliseconds()}"
    }

    private suspend fun generateResponse(input: String): Result<GeminiContentResponse> {
        val conversationHistory = buildConversationHistory()
        val (tools, toolConfig) = prepareToolsAndConfig()

        return geminiService.generateContent(
            prompt = input,
            model = _model.value,
            conversationHistory = conversationHistory,
            systemInstruction = systemPrompt,
            tools = tools,
            toolConfig = toolConfig
        ).let { result ->
            handleFunctionCalls(result, conversationHistory)
        }
    }

    private suspend fun handleFunctionCalls(
        result: Result<GeminiContentResponse>,
        conversationHistory: List<Content>
    ): Result<GeminiContentResponse> {
        return result.fold(
            onSuccess = { response ->
                response.functionCall?.let { functionCall ->
                    executeFunctionAndContinue(functionCall, conversationHistory)
                } ?: Result.success(response)
            },
            onFailure = { Result.failure(it) }
        )
    }

    private suspend fun executeFunctionAndContinue(
        functionCall: FunctionCall,
        conversationHistory: List<Content>
    ): Result<GeminiContentResponse> {
        val functionResponse = mcpBridge.execute(functionCall, mcpHandlers)

        val updatedHistory = conversationHistory + listOf(
            Content(parts = listOf(Part(functionCall = functionCall)), role = "model"),
            Content(parts = listOf(Part(functionResponse = functionResponse)), role = "function")
        )

        return geminiService.generateContent(
            prompt = """
                Analyze the function result and present it in a clear, user-friendly format.
                
                Guidelines:
                - If it's JSON, parse and structure it with headings and bullet points
                - If it's plain text, format it nicely with proper spacing
                - If it's tabular data, organize it in a readable table format
                - Always use markdown formatting for better readability
                - Focus on the most important information first
                - Make it conversational and easy to understand
                
                Do not make any additional function calls - just format the existing result.
            """.trimIndent(),
            model = _model.value,
            conversationHistory = updatedHistory,
            systemInstruction = systemPrompt,
            tools = null,
            toolConfig = null
        )
    }

    private fun prepareToolsAndConfig(): Pair<List<Tool>?, ToolConfig?> {
        val mcpTools = mcpHandlers.flatMap { handler -> handler.tools }
        return if (mcpTools.isNotEmpty()) {
            val functionDeclarations = mcpTools.map { it.toDeclaration() }
            val tools = listOf(Tool(functionDeclarations = functionDeclarations))
            val toolConfig = ToolConfig(functionCallingConfig = FunctionCallingConfig())
            Pair(tools, toolConfig)
        } else {
            Pair(null, null)
        }
    }

    private fun createUserMessage(input: String): ChatMessage = ChatMessage(
        id = generateMessageId(),
        content = input,
        role = MessageRole.USER,
        timestamp = Clock.System.now()
    )

    private fun createAgentMessage(response: GeminiContentResponse): ChatMessage = ChatMessage(
        id = generateMessageId(),
        content = response.content,
        role = MessageRole.ASSISTANT,
        timestamp = Clock.System.now(),
        tokenUsage = response.tokenUsage,
        agentName = agentName
    )

    private fun addMessage(message: ChatMessage) {
        _messages.value = _messages.value + message
    }
}