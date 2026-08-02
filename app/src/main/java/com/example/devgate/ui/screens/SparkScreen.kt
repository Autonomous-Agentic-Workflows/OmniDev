package com.example.devgate.ui.screens

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.devgate.data.models.SparkSnippet
import com.example.devgate.ui.components.CodeEditorView
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldSuccess

@Composable
fun SparkScreen(
    snippets: List<SparkSnippet>,
    prompt: String,
    selectedLanguage: String,
    selectedTaskType: String,
    outputCode: String,
    isGenerating: Boolean,
    onPromptChange: (String) -> Unit,
    onSelectLanguage: (String) -> Unit,
    onSelectTaskType: (String) -> Unit,
    onGenerateCode: () -> Unit,
    onSaveSnippet: (String) -> Unit,
    onDeleteSnippet: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val languages = listOf("Kotlin", "Python", "TypeScript", "Rust", "Go", "SQL")
    val taskTypes = listOf("GENERATE", "REFACTOR", "UNIT_TEST", "BUG_FIX")
    var snippetTitleInput by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }

    val filteredSnippets = remember(snippets, searchQuery) {
        if (searchQuery.isBlank()) snippets
        else snippets.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.language.contains(searchQuery, ignoreCase = true) ||
            it.tags.any { tag -> tag.contains(searchQuery, ignoreCase = true) }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        // Spark AI Generator Workbench Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("spark_workbench_card"),
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
                            Icon(Icons.Default.Bolt, contentDescription = "Spark", tint = EmeraldSuccess)
                            Text("Spark Code Generator Workbench", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = EmeraldSuccess.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldSuccess)
                        ) {
                            Text(
                                text = "SPARK AI",
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldSuccess,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Language Selector
                    Text("Target Language", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(languages) { lang ->
                            FilterChip(
                                selected = lang == selectedLanguage,
                                onClick = { onSelectLanguage(lang) },
                                label = { Text(lang, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Task Type
                    Text("Task Type", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(taskTypes) { type ->
                            FilterChip(
                                selected = type == selectedTaskType,
                                onClick = { onSelectTaskType(type) },
                                label = { Text(type, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = prompt,
                        onValueChange = onPromptChange,
                        placeholder = { Text("Describe the code or component to generate...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("spark_prompt_input"),
                        minLines = 3,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = onGenerateCode,
                        enabled = !isGenerating,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("spark_generate_button")
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Spark Engine Generating...", style = MaterialTheme.typography.labelMedium)
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generate $selectedLanguage Code", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }

        // Generated Output Block
        if (outputCode.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Spark Output Code", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))

                        CodeEditorView(
                            code = outputCode,
                            language = selectedLanguage,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = snippetTitleInput,
                                onValueChange = { snippetTitleInput = it },
                                placeholder = { Text("Snippet Title...") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )

                            Button(
                                onClick = {
                                    onSaveSnippet(snippetTitleInput)
                                    snippetTitleInput = ""
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("spark_save_snippet_button")
                            ) {
                                Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Save Snippet", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }

        // Saved Code Snippets Repository Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Saved Code Snippet Library (${snippets.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search snippets by title, tag, or language...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("spark_search_snippets"),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )
        }

        items(filteredSnippets) { snippet ->
            SnippetCardItem(
                snippet = snippet,
                onDelete = { onDeleteSnippet(snippet.id) }
            )
        }
    }
}

@Composable
private fun SnippetCardItem(
    snippet: SparkSnippet,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("snippet_item_${snippet.id}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = CyanPrimary.copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary)
                    ) {
                        Text(
                            text = snippet.language,
                            style = MaterialTheme.typography.labelSmall,
                            color = CyanPrimary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(snippet.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(snippet.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))

            CodeEditorView(
                code = snippet.code,
                language = snippet.language,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
