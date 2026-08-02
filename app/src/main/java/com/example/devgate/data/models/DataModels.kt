package com.example.devgate.data.models

import androidx.compose.ui.graphics.vector.ImageVector

enum class ToolModule(val title: String, val badge: String, val description: String) {
    GIT("Git Engine", "v2.44", "Repo management, commits, AI diff messages, branch tree & push/pull sync"),
    GEMINI_CLI("Gemini CLI", "v3.5", "Interactive terminal CLI for prompt engineering & automated AI scripts"),
    GEMMA("Gemma OS", "2B/7B", "Open-source local model testing, quantization & system prompt playground"),
    SPARK("Spark Lab", "AI Code", "Instant code generation, bug fixing, unit test authoring & snippet bank"),
    JULES("Jules Agent", "Auto-Flow", "Autonomous task orchestrator, multi-step code reviewer & CI agent")
}

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
    val status: String, // "MODIFIED", "ADDED", "DELETED"
    val additions: Int,
    val deletions: Int,
    val diffContent: String
)

data class CliEntry(
    val id: String = java.util.UUID.randomUUID().toString(),
    val command: String,
    val response: String,
    val timestamp: Long = System.currentTimeMillis(),
    val modelUsed: String = "gemini-3.5-flash",
    val isSuccess: Boolean = true,
    val executionTimeMs: Long = 0L
)

data class GemmaModelConfig(
    val modelVariant: String = "Gemma 2B IT", // "Gemma 2B IT", "Gemma 7B IT", "Gemma 2B Base"
    val quantization: String = "INT4 (Optimal)", // "FP16", "INT8", "INT4 (Optimal)"
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
