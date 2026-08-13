package com.example.devgate.data.api

import com.squareup.moshi.JsonClass

// --- Unified Provider Types ---

enum class AiProvider {
    GEMINI, VERTEX_AI, CLAUDE, OLLAMA, HERMES
}

data class ProviderConfig(
    val provider: AiProvider,
    val displayName: String,
    val apiKey: String = "",
    val baseUrl: String = "",
    val model: String = "",
    val projectId: String = "",
    val location: String = "us-central1",
    val isEnabled: Boolean = false
)

@JsonClass(generateAdapter = false)
data class UnifiedChatRequest(
    val prompt: String,
    val systemInstruction: String? = null,
    val model: String,
    val temperature: Float = 0.7f,
    val maxTokens: Int = 2048
)

@JsonClass(generateAdapter = false)
data class UnifiedChatResponse(
    val content: String,
    val provider: String,
    val model: String,
    val latencyMs: Long,
    val isSuccess: Boolean,
    val error: String? = null
)

// --- Claude (Anthropic) API Models ---

@JsonClass(generateAdapter = false)
data class ClaudeRequest(
    val model: String,
    val max_tokens: Int,
    val system: String? = null,
    val messages: List<ClaudeMessage>,
    val temperature: Float = 0.7f
)

@JsonClass(generateAdapter = false)
data class ClaudeMessage(
    val role: String,
    val content: String
)

@JsonClass(generateAdapter = false)
data class ClaudeResponse(
    val id: String? = null,
    val content: List<ClaudeContentBlock>? = null,
    val model: String? = null,
    val error: ApiError? = null
)

@JsonClass(generateAdapter = false)
data class ClaudeContentBlock(
    val type: String? = null,
    val text: String? = null
)

// --- Ollama API Models ---

@JsonClass(generateAdapter = false)
data class OllamaRequest(
    val model: String,
    val prompt: String,
    val system: String? = null,
    val stream: Boolean = false,
    val options: OllamaOptions? = null
)

@JsonClass(generateAdapter = false)
data class OllamaOptions(
    val temperature: Float = 0.7f,
    val num_predict: Int = 2048
)

@JsonClass(generateAdapter = false)
data class OllamaResponse(
    val model: String? = null,
    val response: String? = null,
    val done: Boolean? = null,
    val error: String? = null
)

// --- Vertex AI API Models ---

@JsonClass(generateAdapter = false)
data class VertexRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = false)
data class VertexResponse(
    val candidates: List<Candidate>? = null,
    val error: ApiError? = null
)