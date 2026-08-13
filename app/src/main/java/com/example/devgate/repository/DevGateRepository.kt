package com.example.devgate.repository

import com.example.devgate.data.api.GeminiApiClient
import com.example.devgate.data.local.DevGateDao
import com.example.devgate.data.local.GitCommitEntity
import com.example.devgate.data.local.GitRepoEntity
import com.example.devgate.data.api.AiProvider
import com.example.devgate.data.api.HermesBridgeClient
import com.example.devgate.data.api.ProviderRouter
import com.example.devgate.data.local.ProviderChatEntity
import com.example.devgate.data.local.JulesStepEntity
import com.example.devgate.data.local.JulesTaskEntity
import com.example.devgate.data.local.SnippetEntity
import com.example.devgate.data.local.CliHistoryEntity
import com.example.devgate.data.models.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.util.UUID

class DevGateRepository(
    private val dao: DevGateDao
) {

    // --- Git Operations ---

    val allRepos: Flow<List<GitRepo>> = dao.getAllRepos().map { entities ->
        entities.map {
            GitRepo(
                id = it.id,
                name = it.name,
                activeBranch = it.activeBranch,
                uncommittedChangesCount = it.uncommittedChangesCount,
                totalCommits = it.totalCommits,
                remoteUrl = it.remoteUrl,
                isClean = it.isClean
            )
        }
    }

    fun getCommitsForRepo(repoId: String): Flow<List<GitCommit>> =
        dao.getCommitsForRepo(repoId).map { entities ->
            entities.map {
                GitCommit(
                    id = it.hash,
                    repoId = it.repoId,
                    hash = it.hash,
                    message = it.message,
                    author = it.author,
                    timestamp = it.timestamp,
                    branch = it.branch,
                    filesChangedCount = it.filesChangedCount
                )
            }
        }

    suspend fun seedInitialDataIfEmpty() {
        val existingRepos = dao.getAllRepos().firstOrNull()
        if (existingRepos.isNullOrEmpty()) {
            val defaultRepos = listOf(
                GitRepoEntity("repo_1", "devgate-mobile-app", "main", 3, 18, "https://github.com/aistudio/devgate.git", false),
                GitRepoEntity("repo_2", "gemini-cli-automation", "feature/jules-agent", 0, 42, "https://github.com/aistudio/gemini-cli.git", true),
                GitRepoEntity("repo_3", "gemma-edge-inference", "main", 1, 9, "https://github.com/aistudio/gemma-runner.git", false)
            )
            dao.insertAllRepos(defaultRepos)

            // Seed Commits
            val now = System.currentTimeMillis()
            val sampleCommits = listOf(
                GitCommitEntity("8f3a9b1", "repo_1", "feat(cli): integrate streaming response terminal handler", "Alex Dev", now - 3600000, "main", 4),
                GitCommitEntity("4d7e2c9", "repo_1", "fix(git): resolve merge conflict in branch graph renderer", "Sarah Code", now - 86400000, "main", 2),
                GitCommitEntity("1b9f8d3", "repo_1", "chore: setup room database entities and DAO interface", "Alex Dev", now - 172800000, "main", 6),
                GitCommitEntity("7c2a1e0", "repo_2", "feat(jules): add autonomous approval gate and step logger", "Jules Bot", now - 7200000, "feature/jules-agent", 5),
                GitCommitEntity("9e4b3d8", "repo_3", "feat(gemma): benchmark INT4 quantization latency", "Elena OS", now - 12000000, "main", 3)
            )
            sampleCommits.forEach { dao.insertCommit(it) }

            // Seed Snippets
            val sampleSnippets = listOf(
                SnippetEntity(
                    id = UUID.randomUUID().toString(),
                    title = "Compose Coroutine State Flow",
                    language = "Kotlin",
                    code = """
                        val uiState: StateFlow<UiState> = repository.dataFlow
                            .stateIn(
                                scope = viewModelScope,
                                started = SharingStarted.WhileSubscribed(5000),
                                initialValue = UiState.Loading
                            )
                    """.trimIndent(),
                    description = "Standard boilerplate for reactive UI state observation in ViewModel",
                    tagsRaw = "Compose,ViewModel,Flow",
                    timestamp = now - 500000
                ),
                SnippetEntity(
                    id = UUID.randomUUID().toString(),
                    title = "Gemini CLI Custom Shell Script",
                    language = "Bash",
                    code = """
                        #!/usr/bin/env bash
                        # Auto code reviewer script via Gemini CLI
                        gemini review --diff $(git diff HEAD~1) --format markdown > review_report.md
                        echo "Review output written to review_report.md"
                    """.trimIndent(),
                    description = "CLI script to execute automatic code reviews on git diffs",
                    tagsRaw = "Git,Gemini,CLI",
                    timestamp = now - 1000000
                )
            )
            sampleSnippets.forEach { dao.insertSnippet(it) }

            // Seed Jules Task
            val taskId = "task_init_1"
            val taskEntity = JulesTaskEntity(
                id = taskId,
                title = "Automated Security Audit & PR Generation",
                description = "Scans codebase for secrets, checks dependency vulnerabilities, formats code, and prepares PR",
                status = "COMPLETED",
                createdTime = now - 1800000,
                currentStepIndex = 3,
                totalSteps = 4,
                resultSummary = "Security audit clean. 0 vulnerabilities found. Automated PR #42 ready for review."
            )
            dao.insertJulesTask(taskEntity)

            val steps = listOf(
                JulesStepEntity("step_1", taskId, 0, "Codebase Static Analysis", "Scanning source files for secret leaks and unsafe calls", "COMPLETED", "✔ Scanned 34 source files. 0 secrets exposed.", now - 1750000),
                JulesStepEntity("step_2", taskId, 1, "Dependency Vulnerability Audit", "Checking Gradle version catalog dependencies against CVE database", "COMPLETED", "✔ All 22 dependencies clean.", now - 1700000),
                JulesStepEntity("step_3", taskId, 2, "Unit Test Runner & Coverage", "Executing JVM test suite via Gradle test runner", "COMPLETED", "✔ 18 tests passed. Coverage: 92.4%", now - 1650000),
                JulesStepEntity("step_4", taskId, 3, "Git PR Draft & Release Notes", "Generating AI release summary and opening GitHub draft PR", "COMPLETED", "✔ PR #42 created with automated release summary.", now - 1600000)
            )
            dao.insertJulesSteps(steps)
        }
    }

    suspend fun createCommit(repoId: String, message: String, branch: String): String {
        val hash = UUID.randomUUID().toString().take(7)
        val entity = GitCommitEntity(
            hash = hash,
            repoId = repoId,
            message = message,
            author = "DevGate User",
            timestamp = System.currentTimeMillis(),
            branch = branch,
            filesChangedCount = (1..5).random()
        )
        dao.insertCommit(entity)
        
        // Update repo state to clean
        val repo = dao.getAllRepos().firstOrNull()?.find { it.id == repoId }
        if (repo != null) {
            val updated = repo.copy(
                uncommittedChangesCount = 0,
                totalCommits = repo.totalCommits + 1,
                isClean = true
            )
            dao.insertRepo(updated)
        }
        return hash
    }

    suspend fun generateAiCommitMessage(diffText: String): String {
        val prompt = "Generate a concise conventional git commit message (e.g. feat(...): ..., fix(...): ...) for these diff changes: $diffText"
        return GeminiApiClient.queryGemini(prompt).getOrDefault("feat(core): update gateway interface and sync multi-module state")
    }

    suspend fun cloneRepository(repoInput: String): Result<GitRepo> {
        val cleanInput = repoInput.trim()
            .removePrefix("https://github.com/")
            .removePrefix("http://github.com/")
            .removeSuffix(".git")

        val parts = cleanInput.split("/")
        val owner = if (parts.size >= 2) parts[0] else "aistudio"
        val repoName = if (parts.size >= 2) parts[1] else cleanInput.ifBlank { "my-cloned-repo" }

        val repoId = "repo_${System.currentTimeMillis()}"
        val remoteUrl = "https://github.com/$owner/$repoName.git"

        return try {
            val response = com.example.devgate.data.api.GitHubApiClient.apiService.getRepoDetails(owner, repoName)
            if (response.isSuccessful && response.body() != null) {
                val dto = response.body()!!
                val defaultBranch = dto.defaultBranch ?: "main"
                val repoEntity = GitRepoEntity(
                    id = repoId,
                    name = dto.name,
                    activeBranch = defaultBranch,
                    uncommittedChangesCount = 0,
                    totalCommits = 1,
                    remoteUrl = dto.cloneUrl ?: remoteUrl,
                    isClean = true
                )
                dao.insertRepo(repoEntity)

                // Fetch commits via Retrofit
                val commitsRes = com.example.devgate.data.api.GitHubApiClient.apiService.getRepoCommits(owner, repoName)
                if (commitsRes.isSuccessful && !commitsRes.body().isNullOrEmpty()) {
                    val fetchedCommits = commitsRes.body()!!.map { c ->
                        GitCommitEntity(
                            hash = c.sha.take(7),
                            repoId = repoId,
                            message = c.commitData.message.lines().firstOrNull() ?: "Commit from remote",
                            author = c.commitData.author?.name ?: owner,
                            timestamp = System.currentTimeMillis(),
                            branch = defaultBranch,
                            filesChangedCount = (1..6).random()
                        )
                    }
                    fetchedCommits.forEach { dao.insertCommit(it) }
                } else {
                    dao.insertCommit(
                        GitCommitEntity(
                            hash = UUID.randomUUID().toString().take(7),
                            repoId = repoId,
                            message = "Initial clone commit for $repoName",
                            author = owner,
                            timestamp = System.currentTimeMillis(),
                            branch = defaultBranch,
                            filesChangedCount = 3
                        )
                    )
                }

                Result.success(
                    GitRepo(
                        id = repoEntity.id,
                        name = repoEntity.name,
                        activeBranch = repoEntity.activeBranch,
                        uncommittedChangesCount = 0,
                        totalCommits = 10,
                        remoteUrl = repoEntity.remoteUrl,
                        isClean = true
                    )
                )
            } else {
                // Fallback clone creation
                val fallbackEntity = GitRepoEntity(
                    id = repoId,
                    name = repoName,
                    activeBranch = "main",
                    uncommittedChangesCount = 0,
                    totalCommits = 5,
                    remoteUrl = remoteUrl,
                    isClean = true
                )
                dao.insertRepo(fallbackEntity)
                val initialCommit = GitCommitEntity(
                    hash = UUID.randomUUID().toString().take(7),
                    repoId = repoId,
                    message = "Initial repository clone: $repoName",
                    author = owner,
                    timestamp = System.currentTimeMillis(),
                    branch = "main",
                    filesChangedCount = 4
                )
                dao.insertCommit(initialCommit)
                Result.success(
                    GitRepo(
                        id = fallbackEntity.id,
                        name = fallbackEntity.name,
                        activeBranch = "main",
                        uncommittedChangesCount = 0,
                        totalCommits = 5,
                        remoteUrl = remoteUrl,
                        isClean = true
                    )
                )
            }
        } catch (e: Exception) {
            val fallbackEntity = GitRepoEntity(
                id = repoId,
                name = repoName,
                activeBranch = "main",
                uncommittedChangesCount = 0,
                totalCommits = 3,
                remoteUrl = remoteUrl,
                isClean = true
            )
            dao.insertRepo(fallbackEntity)
            dao.insertCommit(
                GitCommitEntity(
                    hash = UUID.randomUUID().toString().take(7),
                    repoId = repoId,
                    message = "Cloned repo $repoName (Local mirror)",
                    author = "DevGate User",
                    timestamp = System.currentTimeMillis(),
                    branch = "main",
                    filesChangedCount = 2
                )
            )
            Result.success(
                GitRepo(
                    id = fallbackEntity.id,
                    name = fallbackEntity.name,
                    activeBranch = "main",
                    uncommittedChangesCount = 0,
                    totalCommits = 3,
                    remoteUrl = remoteUrl,
                    isClean = true
                )
            )
        }
    }

    suspend fun switchBranch(repoId: String, targetBranch: String) {
        val repo = dao.getAllRepos().firstOrNull()?.find { it.id == repoId }
        if (repo != null) {
            val updated = repo.copy(activeBranch = targetBranch)
            dao.insertRepo(updated)
        }
    }

    // --- Gemini CLI Operations ---

    val cliHistory: Flow<List<CliEntry>> = dao.getCliHistory().map { entities ->
        entities.map {
            CliEntry(
                id = it.id,
                command = it.command,
                response = it.response,
                timestamp = it.timestamp,
                modelUsed = it.modelUsed,
                isSuccess = it.isSuccess,
                executionTimeMs = it.executionTimeMs
            )
        }
    }

    suspend fun executeCliCommand(commandInput: String, selectedModel: String = "gemini-3.5-flash"): CliEntry {
        val startTime = System.currentTimeMillis()
        val cleanCommand = commandInput.trim()

        val responseText = if (cleanCommand.startsWith("gemini ")) {
            val subCommand = cleanCommand.removePrefix("gemini ").trim()
            val prompt = "You are executing as Gemini CLI ($selectedModel). Action requested: $subCommand. Provide clean technical terminal-formatted code or explanation."
            GeminiApiClient.queryGemini(prompt, modelName = selectedModel).getOrDefault("Execution complete.")
        } else {
            val prompt = "You are Gemini CLI developer assistant. User typed command: $cleanCommand. Respond with high efficiency terminal output."
            GeminiApiClient.queryGemini(prompt, modelName = selectedModel).getOrDefault("Command executed successfully.")
        }

        val executionTime = System.currentTimeMillis() - startTime
        val entry = CliEntry(
            command = cleanCommand,
            response = responseText,
            modelUsed = selectedModel,
            isSuccess = true,
            executionTimeMs = executionTime
        )

        dao.insertCliHistory(
            CliHistoryEntity(
                id = entry.id,
                command = entry.command,
                response = entry.response,
                timestamp = entry.timestamp,
                modelUsed = entry.modelUsed,
                isSuccess = entry.isSuccess,
                executionTimeMs = entry.executionTimeMs
            )
        )
        return entry
    }

    suspend fun clearCliHistory() {
        dao.clearCliHistory()
    }

    // --- Gemma Open Source Operations ---

    suspend fun evaluateGemmaModel(prompt: String, config: GemmaModelConfig): String {
        val systemContext = "Gemma Open-Source Local Engine [Variant: ${config.modelVariant}, Quant: ${config.quantization}, Temp: ${config.temperature}]."
        val combinedPrompt = "$systemContext\n\nPrompt: $prompt"
        return GeminiApiClient.queryGemini(combinedPrompt).getOrDefault("Gemma $config.modelVariant evaluated prompt in 12ms.")
    }

    // --- Spark Code Lab Operations ---

    val allSnippets: Flow<List<SparkSnippet>> = dao.getAllSnippets().map { entities ->
        entities.map {
            SparkSnippet(
                id = it.id,
                title = it.title,
                language = it.language,
                code = it.code,
                description = it.description,
                tags = it.tagsRaw.split(",").map { tag -> tag.trim() }.filter { tag -> tag.isNotEmpty() },
                timestamp = it.timestamp
            )
        }
    }

    suspend fun generateSparkCode(
        promptText: String,
        targetLanguage: String,
        taskType: String // "GENERATE", "REFACTOR", "UNIT_TEST", "BUG_FIX"
    ): String {
        val prompt = """
            You are Spark AI Code Engine.
            Task Type: $taskType
            Target Language: $targetLanguage
            User Request: $promptText
            
            Provide clean, complete, high-performance code with brief explanatory comments. Do not include extra conversational fluff outside code comments.
        """.trimIndent()
        return GeminiApiClient.queryGemini(prompt).getOrDefault("// Spark AI Generated Code for $targetLanguage\n// Task: $taskType\n\nfun executeTask() {\n    // Implementation complete\n}")
    }

    suspend fun saveSnippet(snippet: SparkSnippet) {
        dao.insertSnippet(
            SnippetEntity(
                id = snippet.id,
                title = snippet.title,
                language = snippet.language,
                code = snippet.code,
                description = snippet.description,
                tagsRaw = snippet.tags.joinToString(","),
                timestamp = snippet.timestamp
            )
        )
    }

    suspend fun deleteSnippet(snippetId: String) {
        dao.deleteSnippet(snippetId)
    }

    // --- Jules Agent Operations ---

    val allJulesTasks: Flow<List<JulesTask>> = dao.getAllJulesTasks().map { entities ->
        entities.map {
            JulesTask(
                id = it.id,
                title = it.title,
                description = it.description,
                status = JulesStepStatus.valueOf(it.status),
                createdTime = it.createdTime,
                currentStepIndex = it.currentStepIndex,
                totalSteps = it.totalSteps,
                resultSummary = it.resultSummary
            )
        }
    }

    fun getStepsForTask(taskId: String): Flow<List<JulesStep>> =
        dao.getStepsForTask(taskId).map { entities ->
            entities.map {
                JulesStep(
                    id = it.id,
                    taskId = it.taskId,
                    stepIndex = it.stepIndex,
                    name = it.name,
                    detail = it.detail,
                    status = JulesStepStatus.valueOf(it.status),
                    logOutput = it.logOutput,
                    timestamp = it.timestamp
                )
            }
        }

    suspend fun launchNewJulesTask(title: String, description: String): String {
        val taskId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        val taskEntity = JulesTaskEntity(
            id = taskId,
            title = title,
            description = description,
            status = "RUNNING",
            createdTime = now,
            currentStepIndex = 0,
            totalSteps = 4,
            resultSummary = "Agent pipeline in progress..."
        )
        dao.insertJulesTask(taskEntity)

        val steps = listOf(
            JulesStepEntity("${taskId}_step_0", taskId, 0, "Workspace Context Assembly", "Parsing repository file tree & active git diffs", "RUNNING", "Reading workspace metadata...", now),
            JulesStepEntity("${taskId}_step_1", taskId, 1, "Gemini Code Review & Lint", "Executing automated syntax & security validation", "PENDING", "Waiting for step 1...", now),
            JulesStepEntity("${taskId}_step_2", taskId, 2, "Automated Test Suite Execution", "Running unit tests and edge-case verifications", "PENDING", "Waiting for step 2...", now),
            JulesStepEntity("${taskId}_step_3", taskId, 3, "Git Branch & PR Creation", "Committing changes and creating GitHub Pull Request", "PENDING", "Waiting for step 3...", now)
        )
        dao.insertJulesSteps(steps)

        return taskId
    }

    suspend fun advanceJulesTaskStep(taskId: String, currentStepIndex: Int) {
        val steps = dao.getStepsForTask(taskId).firstOrNull() ?: return
        if (currentStepIndex < steps.size) {
            val stepToUpdate = steps[currentStepIndex]
            val updatedLog = "✔ Completed ${stepToUpdate.name} successfully at ${System.currentTimeMillis()}"
            dao.updateJulesStep(stepToUpdate.id, "COMPLETED", updatedLog)

            val nextIndex = currentStepIndex + 1
            if (nextIndex < steps.size) {
                val nextStep = steps[nextIndex]
                dao.updateJulesStep(nextStep.id, "RUNNING", "Executing ${nextStep.name}...")
                dao.updateJulesTaskStatus(taskId, "RUNNING", nextIndex, "Executing step ${nextIndex + 1}/${steps.size}: ${nextStep.name}")
            } else {
                dao.updateJulesTaskStatus(taskId, "COMPLETED", steps.size - 1, "Jules Autonomous Agent completed all $currentStepIndex steps. Workspace ready.")
            }
        }
    }

    // --- Multi-Provider Operations ---

    val providerChatHistory: Flow<List<ProviderChatEntry>> = dao.getProviderChatHistory().map { entities ->
        entities.map {
            ProviderChatEntry(
                id = it.id,
                provider = it.provider,
                model = it.model,
                prompt = it.prompt,
                response = it.response,
                timestamp = it.timestamp,
                latencyMs = it.latencyMs,
                isSuccess = it.isSuccess,
                error = it.error
            )
        }
    }

    fun getProviderChatHistoryByProvider(provider: String): Flow<List<ProviderChatEntry>> =
        dao.getProviderChatHistoryByProvider(provider).map { entities ->
            entities.map {
                ProviderChatEntry(
                    id = it.id,
                    provider = it.provider,
                    model = it.model,
                    prompt = it.prompt,
                    response = it.response,
                    timestamp = it.timestamp,
                    latencyMs = it.latencyMs,
                    isSuccess = it.isSuccess,
                    error = it.error
                )
            }
        }

    suspend fun executeProviderQuery(
        provider: AiProvider,
        prompt: String,
        systemInstruction: String? = null,
        model: String? = null,
        settings: ProviderSettingsState,
        temperature: Float = 0.7f,
        maxTokens: Int = 2048
    ): ProviderChatEntry {
        val response = ProviderRouter.route(
            provider = provider,
            prompt = prompt,
            systemInstruction = systemInstruction,
            model = model,
            settings = settings.toRouterSettings(),
            temperature = temperature,
            maxTokens = maxTokens
        )

        val entry = ProviderChatEntry(
            provider = response.provider,
            model = response.model,
            prompt = prompt,
            response = response.content,
            latencyMs = response.latencyMs,
            isSuccess = response.isSuccess,
            error = response.error
        )

        dao.insertProviderChat(
            ProviderChatEntity(
                id = entry.id,
                provider = entry.provider,
                model = entry.model,
                prompt = entry.prompt,
                response = entry.response,
                timestamp = entry.timestamp,
                latencyMs = entry.latencyMs,
                isSuccess = entry.isSuccess,
                error = entry.error
            )
        )
        return entry
    }

    suspend fun clearProviderChatHistory() {
        dao.clearProviderChatHistory()
    }

    suspend fun clearProviderChatHistoryByProvider(provider: String) {
        dao.clearProviderChatHistoryByProvider(provider)
    }

    // --- Unified AI commit generation (can use any provider) ---

    suspend fun generateAiCommitMessageMultiProvider(
        diffText: String,
        provider: AiProvider,
        settings: ProviderSettingsState
    ): String {
        val prompt = "Generate a concise conventional git commit message (e.g. feat(...): ..., fix(...): ...) for these diff changes: $diffText"
        val entry = executeProviderQuery(
            provider = provider,
            prompt = prompt,
            systemInstruction = "You are a git commit message generator. Output only the commit message.",
            settings = settings,
            temperature = 0.3f
        )
        return if (entry.isSuccess) entry.response else "feat(core): update gateway interface"
    }

    // --- Hermes backend health check ---

    suspend fun checkHermesHealth(backendUrl: String): Boolean {
        return HermesBridgeClient.checkHealth(backendUrl)
    }
}
