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
                        apiKeyConfigured = uiState.apiKeyConfigured
                    )
                }
            }
        }
    }
}
