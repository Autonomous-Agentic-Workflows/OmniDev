package com.example.devgate.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.devgate.data.local.AppDatabase
import com.example.devgate.data.api.ProviderRouter
import com.example.devgate.data.models.*
import com.example.devgate.repository.DevGateRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class DevGateScreen {
    DASHBOARD,
    GIT,
    GEMINI_CLI,
    GEMMA,
    SPARK,
    JULES,
    VERTEX_AI,
    CLAUDE,
    OLLAMA,
    HERMES,
    SETTINGS
}

data class DevGateUiState(
    val currentScreen: DevGateScreen = DevGateScreen.DASHBOARD,
    val selectedRepoId: String = "repo_1",
    val globalGatePrompt: String = "",
    // Git State
    val commitMessageInput: String = "",
    val isGeneratingCommitMsg: Boolean = false,
    val selectedBranch: String = "main",
    val showCloneDialog: Boolean = false,
    val cloneUrlInput: String = "",
    val isCloningRepo: Boolean = false,
    val cloneErrorMessage: String? = null,
    val showBranchDialog: Boolean = false,
    val newBranchInput: String = "",
    // CLI State
    val cliInput: String = "",
    val selectedCliModel: String = "gemini-3.5-flash",
    val isCliExecuting: Boolean = false,
    // Gemma State
    val gemmaConfig: GemmaModelConfig = GemmaModelConfig(),
    val gemmaPromptInput: String = "Synthesize an edge-computing JSON parser function for Android.",
    val gemmaOutput: String = "",
    val isGemmaRunning: Boolean = false,
    val gemmaLatencyMs: Long = 0L,
    // Spark State
    val sparkPrompt: String = "Create a Compose Custom Canvas wave animation slider component.",
    val sparkLanguage: String = "Kotlin",
    val sparkTaskType: String = "GENERATE", // "GENERATE", "REFACTOR", "UNIT_TEST", "BUG_FIX"
    val sparkOutputCode: String = "",
    val isSparkGenerating: Boolean = false,
    val sparkSearchQuery: String = "",
    // Jules State
    val selectedTaskId: String = "task_init_1",
    val isLaunchingTask: Boolean = false,
    val newTaskTitle: String = "",
    val newTaskDesc: String = "",
    val showNewTaskDialog: Boolean = false,
    // System Status
    val apiKeyConfigured: Boolean = true,
    val activeBannerIndex: Int = 0,
    // Multi-Provider State
    val providerSettings: ProviderSettingsState = ProviderSettingsState(),
    val selectedProvider: com.example.devgate.data.api.AiProvider = com.example.devgate.data.api.AiProvider.GEMINI,
    val providerPromptInput: String = "",
    val providerSelectedModel: String = "gemini-2.5-flash",
    val providerOutput: String = "",
    val isProviderExecuting: Boolean = false,
    val providerLatencyMs: Long = 0L,
    val hermesBackendStatus: Boolean = false,
    val isCheckingHermesHealth: Boolean = false
)

class DevGateViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: DevGateRepository

    private val _uiState = MutableStateFlow(DevGateUiState())
    val uiState: StateFlow<DevGateUiState> = _uiState.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = DevGateRepository(database.devGateDao())

        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
    }

    val repos: StateFlow<List<GitRepo>> = repository.allRepos.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val currentCommits: StateFlow<List<GitCommit>> = _uiState
        .flatMapLatest { state -> repository.getCommitsForRepo(state.selectedRepoId) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val cliHistory: StateFlow<List<CliEntry>> = repository.cliHistory.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val snippets: StateFlow<List<SparkSnippet>> = repository.allSnippets.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val julesTasks: StateFlow<List<JulesTask>> = repository.allJulesTasks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val currentJulesSteps: StateFlow<List<JulesStep>> = _uiState
        .flatMapLatest { state -> repository.getStepsForTask(state.selectedTaskId) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Navigation
    fun navigateTo(screen: DevGateScreen) {
        _uiState.update { it.copy(currentScreen = screen) }
    }

    fun setSelectedRepo(repoId: String) {
        _uiState.update { it.copy(selectedRepoId = repoId) }
    }

    fun setGlobalGatePrompt(prompt: String) {
        _uiState.update { it.copy(globalGatePrompt = prompt) }
    }

    // Git Actions
    fun setCommitMessageInput(msg: String) {
        _uiState.update { it.copy(commitMessageInput = msg) }
    }

    fun generateAiCommitMessage() {
        viewModelScope.launch {
            _uiState.update { it.copy(isGeneratingCommitMsg = true) }
            val sampleDiff = "diff --git a/src/Gate.kt b/src/Gate.kt\n+ fun orchestrateWorkflow() {\n+   julesAgent.runAudit()\n+ }"
            val aiMsg = repository.generateAiCommitMessage(sampleDiff)
            _uiState.update { it.copy(commitMessageInput = aiMsg, isGeneratingCommitMsg = false) }
        }
    }

    fun commitChanges() {
        val msg = _uiState.value.commitMessageInput.ifEmpty { "chore: auto sync repository changes" }
        viewModelScope.launch {
            repository.createCommit(_uiState.value.selectedRepoId, msg, _uiState.value.selectedBranch)
            _uiState.update { it.copy(commitMessageInput = "") }
        }
    }

    fun openCloneDialog(show: Boolean) {
        _uiState.update { it.copy(showCloneDialog = show, cloneErrorMessage = null) }
    }

    fun setCloneUrlInput(url: String) {
        _uiState.update { it.copy(cloneUrlInput = url) }
    }

    fun cloneRepository() {
        val input = _uiState.value.cloneUrlInput.ifEmpty { "aistudio/devgate-template" }
        viewModelScope.launch {
            _uiState.update { it.copy(isCloningRepo = true, cloneErrorMessage = null) }
            val result = repository.cloneRepository(input)
            result.onSuccess { newRepo ->
                _uiState.update {
                    it.copy(
                        selectedRepoId = newRepo.id,
                        selectedBranch = newRepo.activeBranch,
                        showCloneDialog = false,
                        cloneUrlInput = "",
                        isCloningRepo = false
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        cloneErrorMessage = err.message ?: "Clone operation failed",
                        isCloningRepo = false
                    )
                }
            }
        }
    }

    fun openBranchDialog(show: Boolean) {
        _uiState.update { it.copy(showBranchDialog = show) }
    }

    fun setNewBranchInput(branch: String) {
        _uiState.update { it.copy(newBranchInput = branch) }
    }

    fun switchBranch(targetBranch: String) {
        viewModelScope.launch {
            repository.switchBranch(_uiState.value.selectedRepoId, targetBranch)
            _uiState.update { it.copy(selectedBranch = targetBranch) }
        }
    }

    fun createAndSwitchBranch() {
        val newBranch = _uiState.value.newBranchInput.trim().ifEmpty { "feature/new-gateway" }
        viewModelScope.launch {
            repository.switchBranch(_uiState.value.selectedRepoId, newBranch)
            _uiState.update {
                it.copy(
                    selectedBranch = newBranch,
                    showBranchDialog = false,
                    newBranchInput = ""
                )
            }
        }
    }

    // CLI Actions
    fun setCliInput(text: String) {
        _uiState.update { it.copy(cliInput = text) }
    }

    fun setSelectedCliModel(model: String) {
        _uiState.update { it.copy(selectedCliModel = model) }
    }

    fun executeCliCommand() {
        val cmd = _uiState.value.cliInput.ifEmpty { "gemini review" }
        viewModelScope.launch {
            _uiState.update { it.copy(isCliExecuting = true) }
            repository.executeCliCommand(cmd, _uiState.value.selectedCliModel)
            _uiState.update { it.copy(cliInput = "", isCliExecuting = false) }
        }
    }

    fun clearCliHistory() {
        viewModelScope.launch {
            repository.clearCliHistory()
        }
    }

    // Gemma Actions
    fun updateGemmaConfig(config: GemmaModelConfig) {
        _uiState.update { it.copy(gemmaConfig = config) }
    }

    fun setGemmaPromptInput(prompt: String) {
        _uiState.update { it.copy(gemmaPromptInput = prompt) }
    }

    fun runGemmaEvaluation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isGemmaRunning = true) }
            val start = System.currentTimeMillis()
            val result = repository.evaluateGemmaModel(_uiState.value.gemmaPromptInput, _uiState.value.gemmaConfig)
            val elapsed = System.currentTimeMillis() - start
            _uiState.update {
                it.copy(
                    gemmaOutput = result,
                    isGemmaRunning = false,
                    gemmaLatencyMs = elapsed.coerceAtLeast(14L)
                )
            }
        }
    }

    // Spark Actions
    fun setSparkPrompt(prompt: String) {
        _uiState.update { it.copy(sparkPrompt = prompt) }
    }

    fun setSparkLanguage(lang: String) {
        _uiState.update { it.copy(sparkLanguage = lang) }
    }

    fun setSparkTaskType(type: String) {
        _uiState.update { it.copy(sparkTaskType = type) }
    }

    fun generateSparkCode() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSparkGenerating = true) }
            val code = repository.generateSparkCode(
                promptText = _uiState.value.sparkPrompt,
                targetLanguage = _uiState.value.sparkLanguage,
                taskType = _uiState.value.sparkTaskType
            )
            _uiState.update { it.copy(sparkOutputCode = code, isSparkGenerating = false) }
        }
    }

    fun saveCurrentSparkSnippet(title: String) {
        val code = _uiState.value.sparkOutputCode
        if (code.isBlank()) return
        viewModelScope.launch {
            val snippet = SparkSnippet(
                title = title.ifEmpty { "${_uiState.value.sparkTaskType} (${_uiState.value.sparkLanguage})" },
                language = _uiState.value.sparkLanguage,
                code = code,
                description = _uiState.value.sparkPrompt,
                tags = listOf(_uiState.value.sparkLanguage, _uiState.value.sparkTaskType, "Spark")
            )
            repository.saveSnippet(snippet)
        }
    }

    fun deleteSnippet(id: String) {
        viewModelScope.launch {
            repository.deleteSnippet(id)
        }
    }

    // Jules Actions
    fun setSelectedTaskId(id: String) {
        _uiState.update { it.copy(selectedTaskId = id) }
    }

    fun openNewTaskDialog(show: Boolean) {
        _uiState.update { it.copy(showNewTaskDialog = show) }
    }

    fun setNewTaskTitle(title: String) {
        _uiState.update { it.copy(newTaskTitle = title) }
    }

    fun setNewTaskDesc(desc: String) {
        _uiState.update { it.copy(newTaskDesc = desc) }
    }

    fun launchJulesTask() {
        val title = _uiState.value.newTaskTitle.ifEmpty { "Autonomous Repository Security Audit" }
        val desc = _uiState.value.newTaskDesc.ifEmpty { "Verify code safety, run test suite, and open PR" }

        viewModelScope.launch {
            _uiState.update { it.copy(isLaunchingTask = true, showNewTaskDialog = false) }
            val taskId = repository.launchNewJulesTask(title, desc)
            _uiState.update { it.copy(selectedTaskId = taskId, isLaunchingTask = false, newTaskTitle = "", newTaskDesc = "") }
        }
    }

    fun advanceSelectedJulesStep() {
        val taskId = _uiState.value.selectedTaskId
        val steps = currentJulesSteps.value
        val runningIndex = steps.indexOfFirst { it.status == JulesStepStatus.RUNNING }
        val stepIndexToAdvance = if (runningIndex >= 0) runningIndex else 0

        viewModelScope.launch {
            repository.advanceJulesTaskStep(taskId, stepIndexToAdvance)
        }
    }

    // --- Multi-Provider Actions ---

    val providerChatHistory: StateFlow<List<ProviderChatEntry>> = repository.providerChatHistory.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setSelectedProvider(provider: com.example.devgate.data.api.AiProvider) {
        val defaultModel = ProviderRouter.getDefaultModel(provider)
        _uiState.update {
            it.copy(
                selectedProvider = provider,
                providerSelectedModel = defaultModel
            )
        }
    }

    fun setProviderPromptInput(text: String) {
        _uiState.update { it.copy(providerPromptInput = text) }
    }

    fun setProviderModel(model: String) {
        _uiState.update { it.copy(providerSelectedModel = model) }
    }

    fun updateProviderSettings(settings: ProviderSettingsState) {
        _uiState.update { it.copy(providerSettings = settings) }
    }

    fun executeProviderQuery() {
        val prompt = _uiState.value.providerPromptInput.ifBlank { return }
        val provider = _uiState.value.selectedProvider
        val model = _uiState.value.providerSelectedModel
        val settings = _uiState.value.providerSettings

        viewModelScope.launch {
            _uiState.update { it.copy(isProviderExecuting = true) }
            val entry = repository.executeProviderQuery(
                provider = provider,
                prompt = prompt,
                model = model,
                settings = settings
            )
            _uiState.update {
                it.copy(
                    providerOutput = entry.response,
                    isProviderExecuting = false,
                    providerLatencyMs = entry.latencyMs,
                    providerPromptInput = ""
                )
            }
        }
    }

    fun clearProviderChatHistory() {
        viewModelScope.launch {
            repository.clearProviderChatHistory()
            _uiState.update { it.copy(providerOutput = "") }
        }
    }

    fun checkHermesHealth() {
        val url = _uiState.value.providerSettings.hermesBackendUrl
        viewModelScope.launch {
            _uiState.update { it.copy(isCheckingHermesHealth = true) }
            val healthy = repository.checkHermesHealth(url)
            _uiState.update {
                it.copy(
                    hermesBackendStatus = healthy,
                    isCheckingHermesHealth = false
                )
            }
        }
    }

    // Update global gate dispatch to route to new providers
    fun dispatchGlobalGatePrompt() {
        val prompt = _uiState.value.globalGatePrompt.trim()
        if (prompt.isEmpty()) return

        val lower = prompt.lowercase()
        when {
            lower.contains("vertex") || lower.contains("gcloud") -> {
                _uiState.update { it.copy(currentScreen = DevGateScreen.VERTEX_AI, providerPromptInput = prompt) }
                executeProviderQuery()
            }
            lower.contains("claude") || lower.contains("anthropic") -> {
                _uiState.update { it.copy(currentScreen = DevGateScreen.CLAUDE, providerPromptInput = prompt) }
                executeProviderQuery()
            }
            lower.contains("ollama") || lower.contains("local llm") -> {
                _uiState.update { it.copy(currentScreen = DevGateScreen.OLLAMA, providerPromptInput = prompt) }
                executeProviderQuery()
            }
            lower.contains("hermes") || lower.contains("orchestrator") -> {
                _uiState.update { it.copy(currentScreen = DevGateScreen.HERMES, providerPromptInput = prompt) }
                executeProviderQuery()
            }
            lower.contains("git") || lower.contains("commit") || lower.contains("branch") -> {
                _uiState.update { it.copy(currentScreen = DevGateScreen.GIT, commitMessageInput = prompt) }
            }
            lower.contains("spark") || lower.contains("code") || lower.contains("snippet") -> {
                _uiState.update { it.copy(currentScreen = DevGateScreen.SPARK, sparkPrompt = prompt) }
                generateSparkCode()
            }
            lower.contains("gemma") || lower.contains("local") || lower.contains("quant") -> {
                _uiState.update { it.copy(currentScreen = DevGateScreen.GEMMA, gemmaPromptInput = prompt) }
                runGemmaEvaluation()
            }
            lower.contains("jules") || lower.contains("agent") || lower.contains("auto") -> {
                _uiState.update { it.copy(currentScreen = DevGateScreen.JULES, newTaskTitle = prompt, showNewTaskDialog = true) }
            }
            else -> {
                _uiState.update { it.copy(currentScreen = DevGateScreen.GEMINI_CLI, cliInput = prompt) }
                executeCliCommand()
            }
        }
        _uiState.update { it.copy(globalGatePrompt = "") }
    }
}
