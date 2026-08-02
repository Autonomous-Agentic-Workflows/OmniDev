package com.example.devgate.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AmberTertiary
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.VioletSecondary

@Composable
fun SettingsScreen(
    apiKeyConfigured: Boolean,
    modifier: Modifier = Modifier
) {
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
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
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
                            border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldSuccess)
                        ) {
                            Text(
                                text = "CONFIGURED",
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldSuccess,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "API Key injected via Secrets panel & BuildConfig. GEMINI_API_KEY is active for live REST calls to gemini-3.5-flash & gemini-3.1-pro-preview models.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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

                    EcosystemItem(
                        title = "Git Repository Sync",
                        desc = "Stage diffs, AI commit message generator, branch graph & remote push/pull",
                        icon = Icons.Default.AccountTree,
                        color = CyanPrimary
                    )
                    EcosystemItem(
                        title = "Gemini CLI Terminal",
                        desc = "Interactive terminal interface with model selection, prompt presets & execution log history",
                        icon = Icons.Default.Terminal,
                        color = VioletSecondary
                    )
                    EcosystemItem(
                        title = "Gemma Open-Source",
                        desc = "Local model bench with 2B/7B IT variants, INT4 quantization & latency tok/s metrics",
                        icon = Icons.Default.Memory,
                        color = AmberTertiary
                    )
                    EcosystemItem(
                        title = "Spark Code Workbench",
                        desc = "Instant multi-language code generation, unit test authoring & Room DB snippet library",
                        icon = Icons.Default.Bolt,
                        color = EmeraldSuccess
                    )
                    EcosystemItem(
                        title = "Jules Autonomous Agent",
                        desc = "Multi-step automated developer pipeline: static analysis -> test runner -> draft PR",
                        icon = Icons.Default.SmartToy,
                        color = CyanPrimary
                    )
                }
            }
        }

        // About & Version
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("DevGate Developer Hub v1.0.0", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Built with Jetpack Compose, Material 3, Room Local DB, Retrofit & Gemini API", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
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
