package com.example.devgate.data.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Unified provider router — dispatches chat requests to any configured AI provider.
 * 
 * Supported providers:
 * - GEMINI: Google Gemini API (existing, API key based)
 * - VERTEX_AI: Google Cloud Vertex AI (enterprise, gCloud ADC based)
 * - CLAUDE: Anthropic Claude API
 * - OLLAMA: Local Ollama LLM server (llama3, gemma, etc.)
 * - HERMES: Hermes Agent (routes through the vertex_orchestrator backend)
 */
object ProviderRouter {

    data class ProviderSettings(
        val geminiApiKey: String = "",
        val vertexProjectId: String = "",
        val vertexAccessToken: String = "",
        val vertexLocation: String = "us-central1",
        val claudeApiKey: String = "",
        val ollamaBaseUrl: String = "http://10.0.2.2:11434/",
        val hermesBackendUrl: String = "http://10.0.2.2:8000/",
        val hermesApiKey: String = ""
    )

    suspend fun route(
        provider: AiProvider,
        prompt: String,
        systemInstruction: String? = null,
        model: String? = null,
        settings: ProviderSettings,
        temperature: Float = 0.7f,
        maxTokens: Int = 2048
    ): UnifiedChatResponse = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()

        try {
            val result = when (provider) {
                AiProvider.GEMINI -> {
                    val mdl = model ?: "gemini-2.5-flash"
                    GeminiApiClient.queryGemini(
                        prompt = prompt,
                        systemInstructionText = systemInstruction,
                        modelName = mdl,
                        temperature = temperature
                    )
                }

                AiProvider.VERTEX_AI -> {
                    val mdl = model ?: "gemini-2.5-pro"
                    VertexApiClient.queryVertex(
                        prompt = prompt,
                        systemInstruction = systemInstruction,
                        model = mdl,
                        projectId = settings.vertexProjectId,
                        location = settings.vertexLocation,
                        accessToken = settings.vertexAccessToken,
                        temperature = temperature,
                        maxTokens = maxTokens
                    )
                }

                AiProvider.CLAUDE -> {
                    val mdl = model ?: "claude-sonnet-4-20250514"
                    ClaudeApiClient.queryClaude(
                        prompt = prompt,
                        systemInstruction = systemInstruction,
                        model = mdl,
                        apiKey = settings.claudeApiKey,
                        temperature = temperature,
                        maxTokens = maxTokens
                    )
                }

                AiProvider.OLLAMA -> {
                    val mdl = model ?: "llama3"
                    OllamaApiClient.updateBaseUrl(settings.ollamaBaseUrl)
                    OllamaApiClient.queryOllama(
                        prompt = prompt,
                        systemInstruction = systemInstruction,
                        model = mdl,
                        temperature = temperature,
                        maxTokens = maxTokens
                    )
                }

                AiProvider.HERMES -> {
                    // Hermes routes through the vertex_orchestrator Python backend
                    // which itself dispatches to CrewAI, AutoGen, or Aider
                    val mdl = model ?: "gemini-2.5-pro"
                    HermesBridgeClient.queryHermes(
                        prompt = prompt,
                        systemInstruction = systemInstruction,
                        model = mdl,
                        backendUrl = settings.hermesBackendUrl,
                        apiKey = settings.hermesApiKey,
                        temperature = temperature,
                        maxTokens = maxTokens
                    )
                }
            }

            val elapsed = System.currentTimeMillis() - startTime
            val content = result.getOrDefault("Provider returned no response.")
            val error = result.exceptionOrNull()?.message

            UnifiedChatResponse(
                content = content,
                provider = provider.name,
                model = model ?: getDefaultModel(provider),
                latencyMs = elapsed,
                isSuccess = error == null,
                error = error
            )
        } catch (e: Exception) {
            val elapsed = System.currentTimeMillis() - startTime
            UnifiedChatResponse(
                content = "Error: ${e.message}",
                provider = provider.name,
                model = model ?: getDefaultModel(provider),
                latencyMs = elapsed,
                isSuccess = false,
                error = e.message
            )
        }
    }

    fun getDefaultModel(provider: AiProvider): String = when (provider) {
        AiProvider.GEMINI -> "gemini-2.5-flash"
        AiProvider.VERTEX_AI -> "gemini-2.5-pro"
        AiProvider.CLAUDE -> "claude-sonnet-4-20250514"
        AiProvider.OLLAMA -> "llama3"
        AiProvider.HERMES -> "gemini-2.5-pro"
    }

    fun getAvailableModels(provider: AiProvider): List<String> = when (provider) {
        AiProvider.GEMINI -> listOf("gemini-2.5-flash", "gemini-2.5-pro", "gemini-2.0-flash")
        AiProvider.VERTEX_AI -> listOf("gemini-2.5-pro", "gemini-2.5-flash", "gemini-2.0-flash")
        AiProvider.CLAUDE -> listOf("claude-sonnet-4-20250514", "claude-opus-4-20250514", "claude-haiku-4-20250514")
        AiProvider.OLLAMA -> listOf("llama3", "llama3:8b", "gemma2", "mistral", "codellama", "phi3")
        AiProvider.HERMES -> listOf("gemini-2.5-pro", "gemini-2.5-flash", "vertex_ai/gemini-2.5-pro")
    }
}