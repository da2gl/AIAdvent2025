package com.glavatskikh.aiadvent2025.chat.domain.workflow

import com.glavatskikh.aiadvent2025.chat.data.models.ChatMessage
import com.glavatskikh.aiadvent2025.chat.data.models.GeminiModel
import com.glavatskikh.aiadvent2025.chat.data.models.MessageRole
import com.glavatskikh.aiadvent2025.chat.domain.agent.ChefAgent
import com.glavatskikh.aiadvent2025.chat.domain.agent.NutritionistAgent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock

class RecipeCreationWorkflow : Workflow {

    private val chefAgent = ChefAgent()
    private val nutritionistAgent = NutritionistAgent()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    override val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    override val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    override val workflowType = WorkflowType.RECIPE_CREATION

    private var currentStep = WorkflowStep.WAITING_FOR_INPUT
    private var recipeContent: String? = null

    private enum class WorkflowStep {
        WAITING_FOR_INPUT,
        CREATING_RECIPE,
        ANALYZING_NUTRITION,
        COMPLETED
    }

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

        // Process with Chef agent
        currentStep = WorkflowStep.CREATING_RECIPE
        val recipeResult = chefAgent.process(input)

        val recipeMessage = recipeResult.getOrElse { error ->
            _isProcessing.value = false
            currentStep = WorkflowStep.WAITING_FOR_INPUT
            return Result.failure(error)
        }

        _messages.value = _messages.value + recipeMessage
        recipeContent = recipeMessage.content

        // TODO: 13/08/2025 [glavatskikh] agent format hack
        // Check if the response contains a complete recipe in special format
        if (recipeMessage.content.contains("=== RECIPE START ===") &&
            recipeMessage.content.contains("=== RECIPE END ===")
        ) {

            // Recipe is complete, proceed to nutritional analysis
            currentStep = WorkflowStep.ANALYZING_NUTRITION
            val nutritionInput = buildNutritionistInput(input, recipeMessage.content)
            val analysisResult = nutritionistAgent.process(nutritionInput)

            val analysisMessage = analysisResult.getOrElse { error ->
                _isProcessing.value = false
                currentStep = WorkflowStep.COMPLETED
                return Result.failure(error)
            }

            _messages.value = _messages.value + analysisMessage
            _isProcessing.value = false
            currentStep = WorkflowStep.COMPLETED

            return Result.success(analysisMessage)
        } else {
            // Chef is asking questions, wait for user response
            _isProcessing.value = false
            currentStep = WorkflowStep.WAITING_FOR_INPUT
            return Result.success(recipeMessage)
        }
    }

    override fun reset() {
        _messages.value = emptyList()
        chefAgent.clearHistory()
        nutritionistAgent.clearHistory()
        currentStep = WorkflowStep.WAITING_FOR_INPUT
        recipeContent = null
    }

    override fun getHistory(): List<ChatMessage> = _messages.value

    override fun setModel(model: GeminiModel) {
        chefAgent.setModel(model)
        nutritionistAgent.setModel(model)
    }

    private fun buildNutritionistInput(originalRequest: String, recipe: String): String {
        return """
            Please analyze the following recipe that was created based on this user request:
            
            User Request: "$originalRequest"
            
            Recipe:
            $recipe
            
            Provide a comprehensive nutritional analysis including calories, macronutrients, 
            allergens, health benefits, and dietary compatibility.
        """.trimIndent()
    }

    private fun generateMessageId(): String {
        return "recipe-${Clock.System.now().toEpochMilliseconds()}"
    }
}