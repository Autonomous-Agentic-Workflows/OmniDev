package com.example.devgate.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.devgate.data.models.CliEntry
import com.example.devgate.ui.components.CodeEditorView
import com.example.devgate.ui.components.MarkdownRenderer
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.TerminalBackground
import com.example.ui.theme.VioletSecondary
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun GeminiCliScreen(
    cliHistory: List<CliEntry>,
    cliInput: String,
    selectedModel: String,
    isExecuting: Boolean,
    onCliInputChange: (String) -> Unit,
    onSelectModel: (String) -> Unit,
    onExecuteCommand: () -> Unit,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val presets = listOf(
        "gemini review --diff main",
        "gemini test --generate unit",
        "gemini refactor --optimize",
        "gemini explain --architecture",
        "gemini docs --markdown"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        // Top Terminal Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.Terminal, contentDescription = "CLI", tint = VioletSecondary)
                Text("Gemini CLI Terminal", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                IconButton(
                    onClick = onClearHistory,
                    modifier = Modifier.testTag("cli_clear_button")
                ) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = "Clear Terminal History", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Model Selector Chips
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(listOf("gemini-3.5-flash", "gemini-3.1-pro-preview")) { model ->
                FilterChip(
                    selected = model == selectedModel,
                    onClick = { onSelectModel(model) },
                    label = { Text(model, style = MaterialTheme.typography.labelSmall) },
                    leadingIcon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(12.dp)) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Quick Command Presets
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(presets) { preset ->
                SuggestionChip(
                    onClick = {
                        onCliInputChange(preset)
                    },
                    label = { Text(preset, style = MaterialTheme.typography.labelSmall) }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Terminal Log History
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(TerminalBackground)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (cliHistory.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Terminal, contentDescription = null, tint = VioletSecondary, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Gemini CLI Terminal v3.5 Ready",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Type 'gemini review', 'gemini test', or custom prompt commands below.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(cliHistory) { entry ->
                    CliLogItem(entry = entry)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Command Input Field Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = cliInput,
                onValueChange = onCliInputChange,
                placeholder = { Text("$ gemini ...", fontFamily = FontFamily.Monospace) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("cli_input_field"),
                shape = RoundedCornerShape(10.dp),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace)
            )

            Button(
                onClick = onExecuteCommand,
                enabled = !isExecuting,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .height(56.dp)
                    .testTag("cli_run_button")
            ) {
                if (isExecuting) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Send, contentDescription = "Run", modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun CliLogItem(entry: CliEntry) {
    val context = LocalContext.current
    val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(entry.timestamp))

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "$",
                    color = CyanPrimary,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = entry.command,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "${entry.executionTimeMs}ms • $timeStr",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = FontFamily.Monospace
                )
                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Terminal Log", entry.response))
                        Toast.makeText(context, "Copied response to clipboard", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        MarkdownRenderer(
            markdownText = entry.response,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
