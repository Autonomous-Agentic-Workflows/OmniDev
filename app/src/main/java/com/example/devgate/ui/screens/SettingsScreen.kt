package com.example.devgate.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.devgate.data.models.ProviderSettingsState
import com.example.ui.theme.AmberTertiary
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.VioletSecondary

@Composable
fun SettingsScreen(
    apiKeyConfigured: Boolean,
    providerSettings: ProviderSettingsState = ProviderSettingsState(),
    onUpdateProviderSettings: (ProviderSettingsState) -> Unit = {},
    hermesBackendStatus: Boolean = false,
    isCheckingHermesHealth: Boolean = false,
    onCheckHermesHealth: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var geminiKey by remember(providerSettings) { mutableStateOf(providerSettings.geminiApiKey) }
    var vertexProject by remember(providerSettings) { mutableStateOf(providerSettings.vertexProjectId) }
    var vertexToken by remember(providerSettings) { mutableStateOf(providerSettings.vertexAccessToken) }
    var vertexLocation by remember(providerSettings) { mutableStateOf(providerSettings.vertexLocation) }
    var claudeKey by remember(providerSettings) { mutableStateOf(providerSettings.claudeApiKey) }
    var ollamaUrl by remember(providerSettings) { mutableStateOf(providerSettings.ollamaBaseUrl) }
    var hermesUrl by remember(providerSettings) { mutableStateOf(providerSettings.hermesBackendUrl) }
    var hermesKey by remember(providerSettings) { mutableStateOf(providerSettings.hermesApiKey) }

    fun updateSettings() {
        onUpdateProviderSettings(
            ProviderSettingsState(
                geminiApiKey = geminiKey,
                vertexProjectId = vertexProject,
                vertexAccessToken = vertexToken,
                vertexLocation = vertexLocation,
                claudeApiKey = claudeKey,
                ollamaBaseUrl = ollamaUrl,
                hermesBackendUrl = hermesUrl,
                hermesApiKey = hermesKey
            )
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        // API Key Status Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_key_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Key, contentDescription = "API Key", tint = CyanPrimary)
                            Text("Gemini API Key Status", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = EmeraldSuccess.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, EmeraldSuccess)
                        ) {
                            Text(
                                text = if (apiKeyConfigured) "CONFIGURED" else "NOT SET",
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldSuccess,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "API Key injected via Secrets panel & BuildConfig. GEMINI_API_KEY is active for live REST calls.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Vertex AI Settings
        item {
            SettingsSectionCard(title = "Vertex AI (Google Cloud Enterprise)", icon = Icons.Default.Cloud, color = CyanPrimary) {
                OutlinedTextField(
                    value = vertexProject,
                    onValueChange = { vertexProject = it; updateSettings() },
                    label = { Text("Project ID") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = vertexLocation,
                    onValueChange = { vertexLocation = it; updateSettings() },
                    label = { Text("Location") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = vertexToken,
                    onValueChange = { vertexToken = it; updateSettings() },
                    label = { Text("Access Token (gcloud auth print-access-token)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Run: gcloud auth application-default login\nThen: gcloud auth print-access-token",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Claude Settings
        item {
            SettingsSectionCard(title = "Claude (Anthropic)", icon = Icons.Default.Psychology, color = VioletSecondary) {
                OutlinedTextField(
                    value = claudeKey,
                    onValueChange = { claudeKey = it; updateSettings() },
                    label = { Text("Anthropic API Key") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Get your API key at: https://console.anthropic.com",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Ollama Settings
        item {
            SettingsSectionCard(title = "Ollama (Local LLM)", icon = Icons.Default.Memory, color = AmberTertiary) {
                OutlinedTextField(
                    value = ollamaUrl,
                    onValueChange = { ollamaUrl = it; updateSettings() },
                    label = { Text("Ollama Server URL") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Emulator: http://10.0.2.2:11434/\nDevice: http://192.168.x.x:11434/\nInstall: https://ollama.com",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Hermes Bridge Settings
        item {
            SettingsSectionCard(title = "Hermes Agent Bridge (vertex_orchestrator)", icon = Icons.Default.Hub, color = EmeraldSuccess) {
                OutlinedTextField(
                    value = hermesUrl,
                    onValueChange = { hermesUrl = it; updateSettings() },
                    label = { Text("Backend URL") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = hermesKey,
                    onValueChange = { hermesKey = it; updateSettings() },
                    label = { Text("API Key (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onCheckHermesHealth,
                        enabled = !isCheckingHermesHealth
                    ) {
                        if (isCheckingHermesHealth) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Checking...")
                        } else {
                            Text("Check Health")
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (hermesBackendStatus) EmeraldSuccess.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.errorContainer,
                        border = BorderStroke(1.dp, if (hermesBackendStatus) EmeraldSuccess else MaterialTheme.colorScheme.error)
                    ) {
                        Text(
                            text = if (hermesBackendStatus) "ONLINE" else "OFFLINE",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (hermesBackendStatus) EmeraldSuccess else MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Integrated Ecosystem Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("DevGate Architecture & Integrated Engines", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    EcosystemItem("Git Repository Sync", "Stage diffs, AI commit messages, branch graph & remote sync", Icons.Default.AccountTree, CyanPrimary)
                    EcosystemItem("Gemini CLI Terminal", "Interactive terminal with model selection & execution log", Icons.Default.Terminal, VioletSecondary)
                    EcosystemItem("Gemma Open-Source", "Local model bench with 2B/7B IT, INT4 quantization & latency metrics", Icons.Default.Memory, AmberTertiary)
                    EcosystemItem("Spark Code Workbench", "Multi-language code generation, unit tests & snippet library", Icons.Default.Bolt, EmeraldSuccess)
                    EcosystemItem("Jules Autonomous Agent", "Multi-step pipeline: analysis -> tests -> draft PR", Icons.Default.SmartToy, CyanPrimary)
                    EcosystemItem("Vertex AI Enterprise", "Google Cloud IP-protected inference via gCloud ADC", Icons.Default.Cloud, CyanPrimary)
                    EcosystemItem("Claude by Anthropic", "Deep reasoning for analysis, review & architecture", Icons.Default.Psychology, VioletSecondary)
                    EcosystemItem("Ollama Local LLM", "On-device llama3, gemma2, mistral & codellama", Icons.Default.Memory, AmberTertiary)
                    EcosystemItem("Hermes Orchestrator", "CrewAI + AutoGen + Aider bridge via vertex_orchestrator", Icons.Default.Hub, EmeraldSuccess)
                }
            }
        }

        // About
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("DevGate Developer Hub v2.0.0", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Multi-provider AI gateway: Gemini, Vertex AI, Claude, Ollama, Hermes\nBuilt with Jetpack Compose, Material 3, Room, Retrofit", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: androidx.compose.ui.graphics.Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun EcosystemItem(
    title: String,
    desc: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: androidx.compose.ui.graphics.Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            Text(desc, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}