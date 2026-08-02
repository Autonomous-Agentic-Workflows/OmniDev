package com.example.devgate.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.devgate.data.models.JulesStep
import com.example.devgate.data.models.JulesStepStatus
import com.example.devgate.data.models.JulesTask
import com.example.devgate.ui.components.MarkdownRenderer
import com.example.ui.theme.AmberTertiary
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.VioletLight

@Composable
fun JulesScreen(
    tasks: List<JulesTask>,
    selectedTaskId: String,
    steps: List<JulesStep>,
    isLaunchingTask: Boolean,
    showNewTaskDialog: Boolean,
    newTaskTitle: String,
    newTaskDesc: String,
    onSelectTask: (String) -> Unit,
    onOpenNewTaskDialog: (Boolean) -> Unit,
    onNewTaskTitleChange: (String) -> Unit,
    onNewTaskDescChange: (String) -> Unit,
    onLaunchTask: () -> Unit,
    onAdvanceStep: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeTask = tasks.find { it.id == selectedTaskId } ?: tasks.firstOrNull()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        // Header Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("jules_header_card"),
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
                            Icon(Icons.Default.SmartToy, contentDescription = "Jules", tint = VioletLight)
                            Column {
                                Text("Jules Autonomous Agent", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text("Multi-step Workflow Orchestrator & Code Reviewer", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Button(
                            onClick = { onOpenNewTaskDialog(true) },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("jules_new_task_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New Agent Run", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        // Active Task Pipeline Flow
        if (activeTask != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(activeTask.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            StatusBadge(status = activeTask.status.name)
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(activeTask.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))

                        // Progress Bar
                        val progress = if (steps.isNotEmpty()) {
                            val completedCount = steps.count { it.status == JulesStepStatus.COMPLETED }
                            completedCount.toFloat() / steps.size.toFloat()
                        } else 0f

                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = CyanPrimary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Pipeline Status: ${activeTask.resultSummary}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )

                            if (activeTask.status == JulesStepStatus.RUNNING) {
                                Button(
                                    onClick = onAdvanceStep,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("jules_advance_step_button")
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Step Exec", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Step Execution List
        item {
            Text("Step Execution Pipeline (${steps.size} Steps)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        }

        items(steps) { step ->
            StepExecutionItemCard(step = step)
        }

        // Task History List
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Automation Pipeline History (${tasks.size})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        }

        items(tasks) { task ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("jules_task_history_item_${task.id}"),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (task.id == selectedTaskId) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(task.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Text(task.resultSummary, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Button(
                        onClick = { onSelectTask(task.id) },
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("View Pipeline", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }

    // New Task Launcher Dialog
    if (showNewTaskDialog) {
        AlertDialog(
            onDismissRequest = { onOpenNewTaskDialog(false) },
            title = { Text("Launch Jules Agent Pipeline") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newTaskTitle,
                        onValueChange = onNewTaskTitleChange,
                        label = { Text("Pipeline Title") },
                        placeholder = { Text("e.g. Code Review & PR Draft") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newTaskDesc,
                        onValueChange = onNewTaskDescChange,
                        label = { Text("Task Description") },
                        placeholder = { Text("e.g. Audit security, generate unit tests, format code") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = onLaunchTask,
                    modifier = Modifier.testTag("confirm_launch_jules_task")
                ) {
                    Text("Launch Jules Pipeline")
                }
            },
            dismissButton = {
                TextButton(onClick = { onOpenNewTaskDialog(false) }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun StepExecutionItemCard(step: JulesStep) {
    val (statusColor, icon) = when (step.status) {
        JulesStepStatus.COMPLETED -> EmeraldSuccess to Icons.Default.CheckCircle
        JulesStepStatus.RUNNING -> CyanPrimary to Icons.Default.Sync
        JulesStepStatus.AWAITING_APPROVAL -> AmberTertiary to Icons.Default.HourglassTop
        JulesStepStatus.FAILED -> MaterialTheme.colorScheme.error to Icons.Default.Error
        else -> MaterialTheme.colorScheme.onSurfaceVariant to Icons.Default.RadioButtonUnchecked
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(imageVector = icon, contentDescription = null, tint = statusColor, modifier = Modifier.size(18.dp))
                    Text("Step ${step.stepIndex + 1}: ${step.name}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                }
                StatusBadge(status = step.status.name)
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(step.detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            if (step.logOutput.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(8.dp)) {
                        MarkdownRenderer(markdownText = step.logOutput)
                    }
                }
            }
        }
    }
}
