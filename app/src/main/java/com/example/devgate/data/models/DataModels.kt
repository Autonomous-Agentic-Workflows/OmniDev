package com.example.devgate.data.models

import androidx.compose.ui.graphics.vector.ImageVector

// Updated ToolModule enum to include new providers
enum class ToolModule(val title: String, val badge: String, val description: String) {
    GIT("Git Engine", "v2.44", "Repo management, commits, AI diff messages, branch tree & push/pull sync"),
    GEMINI_CLI("Gemini CLI", "v3.5", "Interactive terminal CLI for prompt engineering & automated AI scripts"),
    GEMMA("Gemma OS", "2B/7B", "Open-source local model testing, quantization & system prompt playground"),
    SPARK("Spark Lab", "AI Code", "Instant code generation, bug fixing, unit test authoring & snippet bank"),
    JULES("Jules Agent", "Auto-Flow", "Autonomous task orchestrator, multi-step code reviewer & CI agent"),
    VERTEX_AI("Vertex AI", "Enterprise", "Google Cloud enterprise AI with IP-protected inference via gCloud ADC"),
    CLAUDE("Claude", "Anthropic", "Claude reasoning engine for deep analysis, code review, and architecture"),
    OLLAMA("Ollama", "Local LLM", "On-device local LLM inference with llama3, gemma2, mistral, codellama"),
    HERMES("Hermes", "Orchestrator", "Bridge to vertex_orchestrator backend routing to CrewAI/AutoGen/Aider")
}

// Existing data classes (unchanged)
data class GitRepo(
    val id: String,
    val name: String,
    val activeBranch: String,
    val uncommittedChangesCount: Int,
    val totalCommits: Int,
    val remoteUrl: String,
    val isClean: Boolean
)

data class GitCommit(
    val id: String,
    val repoId: String,
    val hash: String,
    val message: String,
    val author: String,
    val timestamp: Long,
    val branch: String,
    val filesChangedCount: Int
)

data class GitDiffFile(
    val fileName: String,
    val status: String,
    val additions: Int,
    val deletions: Int,
    val diffContent: String
)

data class CliEntry(
    val id: String = java.util.UUID.randomUUID().toString(),
    val command: String,
    val response: String,
    val timestamp: Long = System.currentTimeMillis(),
    val modelUsed: String = "gemini-2.5-flash",
    val isSuccess: Boolean = true,
    val executionTimeMs: Long = 0L
)

data class GemmaModelConfig(
    val modelVariant: String = "Gemma 2B IT",
    val quantization: String = "INT4 (Optimal)",
    val maxTokens: Int = 512,
    val temperature: Float = 0.7f,
    val topP: Float = 0.9f,
    val systemPrompt: String = "You are Gemma, a lightweight high-performance open-source AI assistant specialized in fast edge tasks."
)

data class SparkSnippet(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val language: String,
    val code: String,
    val description: String,
    val tags: List<String>,
    val timestamp: Long = System.currentTimeMillis()
)

enum class JulesStepStatus { PENDING, RUNNING, COMPLETED, FAILED, AWAITING_APPROVAL }

data class JulesStep(
    val id: String,
    val taskId: String,
    val stepIndex: Int,
    val name: String,
    val detail: String,
    val status: JulesStepStatus,
    val logOutput: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class JulesTask(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val status: JulesStepStatus,
    val createdTime: Long = System.currentTimeMillis(),
    val currentStepIndex: Int = 0,
    val totalSteps: Int = 4,
    val resultSummary: String = ""
)

// --- New multi-provider models ---

data class ProviderChatEntry(
    val id: String = java.util.UUID.randomUUID().toString(),
    val provider: String,
    val model: String,
    val prompt: String,
    val response: String,
    val timestamp: Long = System.currentTimeMillis(),
    val latencyMs: Long = 0L,
    val isSuccess: Boolean = true,
    val error: String? = null
)

data class ProviderSettingsState(
    val geminiApiKey: String = "",
    val vertexProjectId: String = "",
    val vertexAccessToken: String = "",
    val vertexLocation: String = "us-central1",
    val claudeApiKey: String = "",
    val ollamaBaseUrl: String = "http://10.0.2.2:11434/",
    val ollamaModel: String = "llama3",
    val hermesBackendUrl: String = "http://10.0.2.2:8000/",
    val hermesApiKey: String = ""
) {
    fun toRouterSettings(): com.example.devgate.data.api.ProviderRouter.ProviderSettings {
        return com.example.devgate.data.api.ProviderRouter.ProviderSettings(
            geminiApiKey = geminiApiKey,
            vertexProjectId = vertexProjectId,
            vertexAccessToken = vertexAccessToken,
            vertexLocation = vertexLocation,
            claudeApiKey = claudeApiKey,
            ollamaBaseUrl = ollamaBaseUrl,
            hermesBackendUrl = hermesBackendUrl,
            hermesApiKey = hermesApiKey
        )
    }
}