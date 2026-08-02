package com.example.devgate.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.devgate.data.models.*
import com.example.devgate.ui.viewmodel.DevGateScreen
import com.example.ui.theme.*

@Composable
fun DashboardScreen(
    repos: List<GitRepo>,
    activeRepoId: String,
    recentCommits: List<GitCommit>,
    cliHistory: List<CliEntry>,
    julesTasks: List<JulesTask>,
    snippets: List<SparkSnippet>,
    onSelectRepo: (String) -> Unit,
    onNavigate: (DevGateScreen) -> Unit,
    onQuickPrompt: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeRepo = repos.find { it.id == activeRepoId } ?: repos.firstOrNull()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        // Hero Visual Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .testTag("dashboard_hero_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_dev_hero_1785560176778),
                        contentDescription = "DevGate Workspace Hero",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color.Transparent,
                                        SlateBackgroundDark.copy(alpha = 0.95f)
                                    )
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CyanPrimary.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyanLight)
                        ) {
                            Text(
                                text = "UNIFIED CODEWAY & AI GATEWAY",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyanLight,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Git • Gemini CLI • Gemma • Spark • Jules",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Active Repository Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("active_repo_card"),
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
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(
                                imageVector = Icons.Default.FolderSpecial,
                                contentDescription = "Repo",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column {
                                Text(
                                    text = activeRepo?.name ?: "No Repository Selected",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Branch: ${activeRepo?.activeBranch ?: "main"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Button(
                            onClick = { onNavigate(DevGateScreen.GIT) },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("open_git_button")
                        ) {
                            Text("Git Sync", style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetricBadge(
                            label = "Uncommitted Changes",
                            value = "${activeRepo?.uncommittedChangesCount ?: 0} files",
                            icon = Icons.Default.Difference,
                            color = if ((activeRepo?.uncommittedChangesCount ?: 0) > 0) AmberTertiary else EmeraldSuccess
                        )
                        MetricBadge(
                            label = "Commits",
                            value = "${activeRepo?.totalCommits ?: 0}",
                            icon = Icons.Default.Commit,
                            color = MaterialTheme.colorScheme.primary
                        )
                        MetricBadge(
                            label = "Remote Sync",
                            value = if (activeRepo?.isClean == true) "In Sync" else "Ahead +1",
                            icon = Icons.Default.CloudSync,
                            color = VioletLight
                        )
                    }
                }
            }
        }

        // Quick Workflow Gate Modules Grid
        item {
            Text(
                text = "Integrated Developer Workflows",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ModuleGateCard(
                        title = "Git Repository",
                        badge = "Branch Tree",
                        desc = "Stage diffs, generate AI commit messages, push/pull branch graph",
                        icon = Icons.Default.AccountTree,
                        accentColor = CyanPrimary,
                        modifier = Modifier.weight(1f).testTag("gate_card_git"),
                        onClick = { onNavigate(DevGateScreen.GIT) }
                    )
                    ModuleGateCard(
                        title = "Gemini CLI",
                        badge = "Terminal",
                        desc = "Execute terminal commands, script review presets & prompts",
                        icon = Icons.Default.Terminal,
                        accentColor = VioletSecondary,
                        modifier = Modifier.weight(1f).testTag("gate_card_cli"),
                        onClick = { onNavigate(DevGateScreen.GEMINI_CLI) }
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ModuleGateCard(
                        title = "Gemma OpenSource",
                        badge = "2B/7B OS",
                        desc = "Local model bench, quantization modes & edge test prompts",
                        icon = Icons.Default.Memory,
                        accentColor = AmberTertiary,
                        modifier = Modifier.weight(1f).testTag("gate_card_gemma"),
                        onClick = { onNavigate(DevGateScreen.GEMMA) }
                    )
                    ModuleGateCard(
                        title = "Spark Code Lab",
                        badge = "AI Workbench",
                        desc = "Instant code generator, unit test authoring & snippet bank",
                        icon = Icons.Default.Bolt,
                        accentColor = EmeraldSuccess,
                        modifier = Modifier.weight(1f).testTag("gate_card_spark"),
                        onClick = { onNavigate(DevGateScreen.SPARK) }
                    )
                }

                ModuleGateCard(
                    title = "Jules Autonomous Agent",
                    badge = "Multi-Step Auto",
                    desc = "Autonomous pipeline agent: security audit -> auto lint -> execute unit tests -> create GitHub PR",
                    icon = Icons.Default.SmartToy,
                    accentColor = VioletLight,
                    modifier = Modifier.fillMaxWidth().testTag("gate_card_jules"),
                    onClick = { onNavigate(DevGateScreen.JULES) }
                )
            }
        }

        // Jules Active Task Bar
        item {
            val activeJules = julesTasks.firstOrNull()
            Card(
                modifier = Modifier.fillMaxWidth().testTag("jules_active_task_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(imageVector = Icons.Default.AutoMode, contentDescription = "Jules", tint = VioletLight)
                            Text("Jules Agent Active Pipeline", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        }
                        StatusBadge(status = activeJules?.status?.name ?: "IDLE")
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = activeJules?.title ?: "No pipeline active. Tap to launch automation.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = activeJules?.resultSummary ?: "Ready to execute multi-step CI/CD security audits.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Quick Preset Prompts
        item {
            Text(
                text = "Instant Gateway Quick Actions",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(
                    listOf(
                        "git commit message for latest diff",
                        "spark generate unit test for repo",
                        "jules run security audit & PR",
                        "gemma benchmark INT4 latency",
                        "gemini review code security"
                    )
                ) { prompt ->
                    SuggestionChip(
                        onClick = { onQuickPrompt(prompt) },
                        label = { Text(prompt, style = MaterialTheme.typography.labelSmall) },
                        icon = { Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(14.dp)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricBadge(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold, color = color)
        }
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ModuleGateCard(
    title: String,
    badge: String,
    desc: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = title, tint = accentColor, modifier = Modifier.size(20.dp))
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = accentColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = badge,
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val (color, text) = when (status) {
        "COMPLETED" -> EmeraldSuccess to "COMPLETED"
        "RUNNING" -> CyanPrimary to "RUNNING"
        "AWAITING_APPROVAL" -> AmberTertiary to "APPROVAL REQD"
        "FAILED" -> MaterialTheme.colorScheme.error to "FAILED"
        else -> MaterialTheme.colorScheme.onSurfaceVariant to status
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.2f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            fontWeight = FontWeight.Bold
        )
    }
}
