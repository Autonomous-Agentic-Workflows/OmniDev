package com.example.devgate.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.devgate.data.models.GemmaModelConfig
import com.example.devgate.ui.components.CodeEditorView
import com.example.ui.theme.AmberTertiary
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldSuccess

@Composable
fun GemmaScreen(
    config: GemmaModelConfig,
    promptInput: String,
    output: String,
    isRunning: Boolean,
    latencyMs: Long,
    onUpdateConfig: (GemmaModelConfig) -> Unit,
    onPromptInputChange: (String) -> Unit,
    onRunEvaluation: () -> Unit,
    modifier: Modifier = Modifier
) {
    val modelVariants = listOf("Gemma 2B IT", "Gemma 7B IT", "Gemma 2B Base")
    val quantizations = listOf("INT4 (Optimal)", "INT8", "FP16")

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
                    .testTag("gemma_header_card"),
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
                            Icon(Icons.Default.Memory, contentDescription = "Gemma", tint = AmberTertiary)
                            Column {
                                Text("Gemma Open-Source Workbench", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text("Local Edge Model Playground & Parameter Tester", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AmberTertiary.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AmberTertiary)
                        ) {
                            Text(
                                text = "OPEN SOURCE",
                                style = MaterialTheme.typography.labelSmall,
                                color = AmberTertiary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Configuration Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Model Variant", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(modelVariants) { variant ->
                            FilterChip(
                                selected = variant == config.modelVariant,
                                onClick = { onUpdateConfig(config.copy(modelVariant = variant)) },
                                label = { Text(variant, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Quantization Mode", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(quantizations) { quant ->
                            FilterChip(
                                selected = quant == config.quantization,
                                onClick = { onUpdateConfig(config.copy(quantization = quant)) },
                                label = { Text(quant, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Temperature: ${String.format("%.2f", config.temperature)}", style = MaterialTheme.typography.labelSmall)
                        Slider(
                            value = config.temperature,
                            onValueChange = { onUpdateConfig(config.copy(temperature = it)) },
                            valueRange = 0.0f..1.0f,
                            modifier = Modifier.width(180.dp)
                        )
                    }
                }
            }
        }

        // Prompt Execution Input
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Gemma Evaluation Prompt", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = promptInput,
                        onValueChange = onPromptInputChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("gemma_prompt_input"),
                        minLines = 3,
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = onRunEvaluation,
                        enabled = !isRunning,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("gemma_run_button")
                    ) {
                        if (isRunning) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Benchmarking Gemma Edge...", style = MaterialTheme.typography.labelMedium)
                        } else {
                            Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Run Gemma Inference Benchmark", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }

        // Metrics & Benchmark Bar
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    BenchmarkMetricItem(
                        label = "Inference Latency",
                        value = if (latencyMs > 0) "${latencyMs}ms" else "Ready",
                        icon = Icons.Default.Timer,
                        color = CyanPrimary
                    )
                    BenchmarkMetricItem(
                        label = "Throughput",
                        value = if (latencyMs > 0) "${(512000 / latencyMs.coerceAtLeast(1L))} tok/s" else "42 tok/s",
                        icon = Icons.Default.Speed,
                        color = EmeraldSuccess
                    )
                    BenchmarkMetricItem(
                        label = "RAM Footprint",
                        value = if (config.quantization.contains("INT4")) "1.2 GB" else "3.4 GB",
                        icon = Icons.Default.Analytics,
                        color = AmberTertiary
                    )
                }
            }
        }

        // Generated Output
        if (output.isNotEmpty()) {
            item {
                Text("Gemma Model Response Output", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                CodeEditorView(
                    code = output,
                    language = config.modelVariant,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun BenchmarkMetricItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: androidx.compose.ui.graphics.Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
            Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
        }
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
