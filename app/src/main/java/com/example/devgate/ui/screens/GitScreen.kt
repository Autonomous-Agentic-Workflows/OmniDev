package com.example.devgate.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.devgate.data.models.GitCommit
import com.example.devgate.data.models.GitRepo
import com.example.ui.theme.AmberTertiary
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldSuccess
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun GitScreen(
    repos: List<GitRepo>,
    selectedRepoId: String,
    commits: List<GitCommit>,
    commitMessageInput: String,
    isGeneratingMsg: Boolean,
    selectedBranch: String,
    showCloneDialog: Boolean,
    cloneUrlInput: String,
    isCloningRepo: Boolean,
    cloneErrorMessage: String?,
    showBranchDialog: Boolean,
    newBranchInput: String,
    onSelectRepo: (String) -> Unit,
    onCommitMessageChange: (String) -> Unit,
    onGenerateAiCommitMsg: () -> Unit,
    onCommitChanges: () -> Unit,
    onOpenCloneDialog: (Boolean) -> Unit,
    onCloneUrlChange: (String) -> Unit,
    onCloneRepository: () -> Unit,
    onOpenBranchDialog: (Boolean) -> Unit,
    onNewBranchChange: (String) -> Unit,
    onSwitchBranch: (String) -> Unit,
    onCreateBranch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeRepo = repos.find { it.id == selectedRepoId } ?: repos.firstOrNull()
    var selectedTab by remember { mutableStateOf(0) } // 0: Commits, 1: Diff Inspector, 2: Remote Sync
    val availableBranches = listOf("main", "feature/jules-agent", "dev", "release/v1.0")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        // Repository Selector Chips & Clone Button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Git Repositories (${repos.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Button(
                    onClick = { onOpenCloneDialog(true) },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("clone_repo_button")
                ) {
                    Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clone Repo", style = MaterialTheme.typography.labelSmall)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(repos) { repo ->
                    FilterChip(
                        selected = repo.id == selectedRepoId,
                        onClick = { onSelectRepo(repo.id) },
                        label = { Text(repo.name, style = MaterialTheme.typography.labelMedium) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }
        }

        // Branch Bar
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("git_branch_bar"),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.CallSplit, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(18.dp))
                        Text("Branch:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = CyanPrimary.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary)
                        ) {
                            Text(
                                text = activeRepo?.activeBranch ?: selectedBranch,
                                style = MaterialTheme.typography.labelSmall,
                                color = CyanPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(
                            onClick = { onOpenBranchDialog(true) },
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.testTag("switch_branch_button")
                        ) {
                            Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Switch / Create Branch", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        // Commit Workbench Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("git_commit_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.AccountTree, contentDescription = null, tint = CyanPrimary)
                            Text("Stage & Commit Workbench", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = EmeraldSuccess.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = if (activeRepo?.isClean == true) "CLEAN" else "${activeRepo?.uncommittedChangesCount ?: 1} MODIFIED",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (activeRepo?.isClean == true) EmeraldSuccess else AmberTertiary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = commitMessageInput,
                        onValueChange = onCommitMessageChange,
                        placeholder = { Text("Enter commit message or generate via Gemini AI...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("git_commit_input"),
                        minLines = 2,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onGenerateAiCommitMsg,
                            enabled = !isGeneratingMsg,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("ai_commit_msg_button")
                        ) {
                            if (isGeneratingMsg) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("AI Commit Msg", style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        Button(
                            onClick = onCommitChanges,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("commit_changes_button")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Commit Changes", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        // Sub Tabs (Commits, Diff, Sync)
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                    Text("Commits (${commits.size})", modifier = Modifier.padding(10.dp), style = MaterialTheme.typography.labelMedium)
                }
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                    Text("Diff Inspector", modifier = Modifier.padding(10.dp), style = MaterialTheme.typography.labelMedium)
                }
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) {
                    Text("Remote Sync", modifier = Modifier.padding(10.dp), style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        if (selectedTab == 0) {
            // Commit History List
            items(commits) { commit ->
                CommitItemCard(commit = commit)
            }
        } else if (selectedTab == 1) {
            // Diff Inspector
            item {
                DiffInspectorCard(repoName = activeRepo?.name ?: "devgate")
            }
        } else {
            // Remote Sync
            item {
                RemoteSyncCard(
                    repo = activeRepo,
                    onOpenClone = { onOpenCloneDialog(true) }
                )
            }
        }
    }

    // Clone Repository Dialog
    if (showCloneDialog) {
        AlertDialog(
            onDismissRequest = { if (!isCloningRepo) onOpenCloneDialog(false) },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.CloudDownload, contentDescription = null, tint = CyanPrimary)
                    Text("Clone Git Repository")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Enter a GitHub repository URL or 'owner/repo' name to clone commits via Retrofit API:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = cloneUrlInput,
                        onValueChange = onCloneUrlChange,
                        placeholder = { Text("e.g. google/guava or facebook/react") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("clone_url_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    if (isCloningRepo) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Text("Cloning repository & fetching commits via Retrofit...", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    if (cloneErrorMessage != null) {
                        Text(
                            text = cloneErrorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = onCloneRepository,
                    enabled = !isCloningRepo,
                    modifier = Modifier.testTag("confirm_clone_button")
                ) {
                    Text("Clone Repository")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { onOpenCloneDialog(false) },
                    enabled = !isCloningRepo
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Switch / Create Branch Dialog
    if (showBranchDialog) {
        AlertDialog(
            onDismissRequest = { onOpenBranchDialog(false) },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.CallSplit, contentDescription = null, tint = CyanPrimary)
                    Text("Branch Management")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Select Active Branch:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(availableBranches) { b ->
                            FilterChip(
                                selected = b == (activeRepo?.activeBranch ?: selectedBranch),
                                onClick = {
                                    onSwitchBranch(b)
                                    onOpenBranchDialog(false)
                                },
                                label = { Text(b, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }

                    Divider()

                    Text("Or Create New Branch:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = newBranchInput,
                        onValueChange = onNewBranchChange,
                        placeholder = { Text("e.g. feature/my-new-gate") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_branch_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = onCreateBranch,
                    modifier = Modifier.testTag("confirm_create_branch_button")
                ) {
                    Text("Create & Checkout")
                }
            },
            dismissButton = {
                TextButton(onClick = { onOpenBranchDialog(false) }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun CommitItemCard(commit: GitCommit) {
    val dateStr = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(commit.timestamp))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("git_commit_item_${commit.hash}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = CyanPrimary.copy(alpha = 0.2f),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary)
            ) {
                Text(
                    text = commit.hash,
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.labelSmall,
                    color = CyanPrimary,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    fontWeight = FontWeight.Bold
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = commit.message,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "${commit.author} • $dateStr",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${commit.filesChangedCount} files changed",
                        style = MaterialTheme.typography.labelSmall,
                        color = AmberTertiary
                    )
                }
            }
        }
    }
}

@Composable
private fun DiffInspectorCard(repoName: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Diff Preview: $repoName", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("--- a/app/src/main/java/com/example/Gate.kt", fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall)
                    Text("+++ b/app/src/main/java/com/example/Gate.kt", fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall)
                    Text("@@ -14,6 +14,8 @@", fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall, color = CyanPrimary)
                    Text("-  val legacyPipeline = false", fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    Text("+  val legacyPipeline = true", fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall, color = EmeraldSuccess)
                    Text("+  val julesAgentEnabled = true", fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall, color = EmeraldSuccess)
                }
            }
        }
    }
}

@Composable
private fun RemoteSyncCard(
    repo: GitRepo?,
    onOpenClone: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Remote Target: ${repo?.remoteUrl ?: "https://github.com/aistudio/devgate.git"}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {}, shape = RoundedCornerShape(8.dp), modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Git Pull", style = MaterialTheme.typography.labelSmall)
                }
                Button(onClick = {}, shape = RoundedCornerShape(8.dp), modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Git Push", style = MaterialTheme.typography.labelSmall)
                }
                OutlinedButton(onClick = onOpenClone, shape = RoundedCornerShape(8.dp), modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clone", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
