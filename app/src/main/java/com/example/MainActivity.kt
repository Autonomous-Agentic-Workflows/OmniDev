package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.devgate.ui.components.BottomNavBar
import com.example.devgate.ui.components.GlobalGateHeader
import com.example.devgate.ui.screens.*
import com.example.devgate.ui.viewmodel.DevGateScreen
import com.example.devgate.ui.viewmodel.DevGateViewModel
import com.example.ui.theme.DevGateTheme

class MainActivity : ComponentActivity() {

    private val viewModel: DevGateViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            DevGateTheme {
                DevGateApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun DevGateApp(viewModel: DevGateViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val repos by viewModel.repos.collectAsStateWithLifecycle()
    val currentCommits by viewModel.currentCommits.collectAsStateWithLifecycle()
    val cliHistory by viewModel.cliHistory.collectAsStateWithLifecycle()
    val snippets by viewModel.snippets.collectAsStateWithLifecycle()
    val julesTasks by viewModel.julesTasks.collectAsStateWithLifecycle()
    val currentJulesSteps by viewModel.currentJulesSteps.collectAsStateWithLifecycle()
    val providerChatHistory by viewModel.providerChatHistory.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            GlobalGateHeader(
                currentScreen = uiState.currentScreen,
                promptValue = uiState.globalGatePrompt,
                onPromptChange = viewModel::setGlobalGatePrompt,
                onSubmitPrompt = viewModel::dispatchGlobalGatePrompt
            )
        },
        bottomBar = {
            BottomNavBar(
                currentScreen = uiState.currentScreen,
                onSelectScreen = viewModel::navigateTo
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState.currentScreen) {
                DevGateScreen.DASHBOARD -> {
                    DashboardScreen(
                        repos = repos,
                        activeRepoId = uiState.selectedRepoId,
                        recentCommits = currentCommits,
                        cliHistory = cliHistory,
                        julesTasks = julesTasks,
                        snippets = snippets,
                        onSelectRepo = viewModel::setSelectedRepo,
                        onNavigate = viewModel::navigateTo,
                        onQuickPrompt = { prompt ->
                            viewModel.setGlobalGatePrompt(prompt)
                            viewModel.dispatchGlobalGatePrompt()
                        }
                    )
                }

                DevGateScreen.GIT -> {
                    GitScreen(
                        repos = repos,
                        selectedRepoId = uiState.selectedRepoId,
                        commits = currentCommits,
                        commitMessageInput = uiState.commitMessageInput,
                        isGeneratingMsg = uiState.isGeneratingCommitMsg,
                        selectedBranch = uiState.selectedBranch,
                        showCloneDialog = uiState.showCloneDialog,
                        cloneUrlInput = uiState.cloneUrlInput,
                        isCloningRepo = uiState.isCloningRepo,
                        cloneErrorMessage = uiState.cloneErrorMessage,
                        showBranchDialog = uiState.showBranchDialog,
                        newBranchInput = uiState.newBranchInput,
                        onSelectRepo = viewModel::setSelectedRepo,
                        onCommitMessageChange = viewModel::setCommitMessageInput,
                        onGenerateAiCommitMsg = viewModel::generateAiCommitMessage,
                        onCommitChanges = viewModel::commitChanges,
                        onOpenCloneDialog = viewModel::openCloneDialog,
                        onCloneUrlChange = viewModel::setCloneUrlInput,
                        onCloneRepository = viewModel::cloneRepository,
                        onOpenBranchDialog = viewModel::openBranchDialog,
                        onNewBranchChange = viewModel::setNewBranchInput,
                        onSwitchBranch = viewModel::switchBranch,
                        onCreateBranch = viewModel::createAndSwitchBranch
                    )
                }

                DevGateScreen.GEMINI_CLI -> {
                    GeminiCliScreen(
                        cliHistory = cliHistory,
                        cliInput = uiState.cliInput,
                        selectedModel = uiState.selectedCliModel,
                        isExecuting = uiState.isCliExecuting,
                        onCliInputChange = viewModel::setCliInput,
                        onSelectModel = viewModel::setSelectedCliModel,
                        onExecuteCommand = viewModel::executeCliCommand,
                        onClearHistory = viewModel::clearCliHistory
                    )
                }

                DevGateScreen.GEMMA -> {
                    GemmaScreen(
                        config = uiState.gemmaConfig,
                        promptInput = uiState.gemmaPromptInput,
                        output = uiState.gemmaOutput,
                        isRunning = uiState.isGemmaRunning,
                        latencyMs = uiState.gemmaLatencyMs,
                        onUpdateConfig = viewModel::updateGemmaConfig,
                        onPromptInputChange = viewModel::setGemmaPromptInput,
                        onRunEvaluation = viewModel::runGemmaEvaluation
                    )
                }

                DevGateScreen.SPARK -> {
                    SparkScreen(
                        snippets = snippets,
                        prompt = uiState.sparkPrompt,
                        selectedLanguage = uiState.sparkLanguage,
                        selectedTaskType = uiState.sparkTaskType,
                        outputCode = uiState.sparkOutputCode,
                        isGenerating = uiState.isSparkGenerating,
                        onPromptChange = viewModel::setSparkPrompt,
                        onSelectLanguage = viewModel::setSparkLanguage,
                        onSelectTaskType = viewModel::setSparkTaskType,
                        onGenerateCode = viewModel::generateSparkCode,
                        onSaveSnippet = viewModel::saveCurrentSparkSnippet,
                        onDeleteSnippet = viewModel::deleteSnippet
                    )
                }

                DevGateScreen.JULES -> {
                    JulesScreen(
                        tasks = julesTasks,
                        selectedTaskId = uiState.selectedTaskId,
                        steps = currentJulesSteps,
                        isLaunchingTask = uiState.isLaunchingTask,
                        showNewTaskDialog = uiState.showNewTaskDialog,
                        newTaskTitle = uiState.newTaskTitle,
                        newTaskDesc = uiState.newTaskDesc,
                        onSelectTask = viewModel::setSelectedTaskId,
                        onOpenNewTaskDialog = viewModel::openNewTaskDialog,
                        onNewTaskTitleChange = viewModel::setNewTaskTitle,
                        onNewTaskDescChange = viewModel::setNewTaskDesc,
                        onLaunchTask = viewModel::launchJulesTask,
                        onAdvanceStep = viewModel::advanceSelectedJulesStep
                    )
                }

                DevGateScreen.SETTINGS -> {
                    SettingsScreen(
                        apiKeyConfigured = uiState.apiKeyConfigured,
                        providerSettings = uiState.providerSettings,
                        onUpdateProviderSettings = viewModel::updateProviderSettings,
                        hermesBackendStatus = uiState.hermesBackendStatus,
                        isCheckingHermesHealth = uiState.isCheckingHermesHealth,
                        onCheckHermesHealth = viewModel::checkHermesHealth
                    )
                }

                DevGateScreen.VERTEX_AI -> {
                    ProviderScreen(
                        title = "Vertex AI Enterprise",
                        providerName = "VERTEX_AI",
                        provider = com.example.devgate.data.api.AiProvider.VERTEX_AI,
                        chatHistory = providerChatHistory.filter { it.provider == "VERTEX_AI" },
                        promptInput = uiState.providerPromptInput,
                        selectedModel = uiState.providerSelectedModel,
                        isExecuting = uiState.isProviderExecuting,
                        output = uiState.providerOutput,
                        latencyMs = uiState.providerLatencyMs,
                        availableModels = com.example.devgate.data.api.ProviderRouter.getAvailableModels(com.example.devgate.data.api.AiProvider.VERTEX_AI),
                        onPromptChange = viewModel::setProviderPromptInput,
                        onSelectModel = viewModel::setProviderModel,
                        onExecute = { viewModel.setSelectedProvider(com.example.devgate.data.api.AiProvider.VERTEX_AI); viewModel.executeProviderQuery() },
                        onClearHistory = viewModel::clearProviderChatHistory,
                        description = "Google Cloud enterprise AI with IP-protected inference. Configure project ID and gCloud access token in Settings."
                    )
                }

                DevGateScreen.CLAUDE -> {
                    ProviderScreen(
                        title = "Claude by Anthropic",
                        providerName = "CLAUDE",
                        provider = com.example.devgate.data.api.AiProvider.CLAUDE,
                        chatHistory = providerChatHistory.filter { it.provider == "CLAUDE" },
                        promptInput = uiState.providerPromptInput,
                        selectedModel = uiState.providerSelectedModel,
                        isExecuting = uiState.isProviderExecuting,
                        output = uiState.providerOutput,
                        latencyMs = uiState.providerLatencyMs,
                        availableModels = com.example.devgate.data.api.ProviderRouter.getAvailableModels(com.example.devgate.data.api.AiProvider.CLAUDE),
                        onPromptChange = viewModel::setProviderPromptInput,
                        onSelectModel = viewModel::setProviderModel,
                        onExecute = { viewModel.setSelectedProvider(com.example.devgate.data.api.AiProvider.CLAUDE); viewModel.executeProviderQuery() },
                        onClearHistory = viewModel::clearProviderChatHistory,
                        description = "Anthropic Claude reasoning engine for deep analysis, code review, and architecture design."
                    )
                }

                DevGateScreen.OLLAMA -> {
                    ProviderScreen(
                        title = "Ollama Local LLM",
                        providerName = "OLLAMA",
                        provider = com.example.devgate.data.api.AiProvider.OLLAMA,
                        chatHistory = providerChatHistory.filter { it.provider == "OLLAMA" },
                        promptInput = uiState.providerPromptInput,
                        selectedModel = uiState.providerSelectedModel,
                        isExecuting = uiState.isProviderExecuting,
                        output = uiState.providerOutput,
                        latencyMs = uiState.providerLatencyMs,
                        availableModels = com.example.devgate.data.api.ProviderRouter.getAvailableModels(com.example.devgate.data.api.AiProvider.OLLAMA),
                        onPromptChange = viewModel::setProviderPromptInput,
                        onSelectModel = viewModel::setProviderModel,
                        onExecute = { viewModel.setSelectedProvider(com.example.devgate.data.api.AiProvider.OLLAMA); viewModel.executeProviderQuery() },
                        onClearHistory = viewModel::clearProviderChatHistory,
                        description = "On-device local LLM inference. Install Ollama on your host machine and pull models."
                    )
                }

                DevGateScreen.HERMES -> {
                    ProviderScreen(
                        title = "Hermes Agent Orchestrator",
                        providerName = "HERMES",
                        provider = com.example.devgate.data.api.AiProvider.HERMES,
                        chatHistory = providerChatHistory.filter { it.provider == "HERMES" },
                        promptInput = uiState.providerPromptInput,
                        selectedModel = uiState.providerSelectedModel,
                        isExecuting = uiState.isProviderExecuting,
                        output = uiState.providerOutput,
                        latencyMs = uiState.providerLatencyMs,
                        availableModels = com.example.devgate.data.api.ProviderRouter.getAvailableModels(com.example.devgate.data.api.AiProvider.HERMES),
                        onPromptChange = viewModel::setProviderPromptInput,
                        onSelectModel = viewModel::setProviderModel,
                        onExecute = { viewModel.setSelectedProvider(com.example.devgate.data.api.AiProvider.HERMES); viewModel.executeProviderQuery() },
                        onClearHistory = viewModel::clearProviderChatHistory,
                        description = "Bridge to vertex_orchestrator backend. Routes tasks to CrewAI (analysis), AutoGen (conversation), and Aider (code editing) via Google Vertex AI."
                    )
                }
            }
        }
    }
}
