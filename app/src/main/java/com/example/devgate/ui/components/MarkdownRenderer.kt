package com.example.devgate.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanPrimary

sealed class MarkdownBlock {
    data class Heading(val level: Int, val text: String) : MarkdownBlock()
    data class Code(val language: String, val code: String) : MarkdownBlock()
    data class Quote(val text: String) : MarkdownBlock()
    data class ListItem(val index: Int?, val text: String) : MarkdownBlock()
    object Divider : MarkdownBlock()
    data class Paragraph(val text: String) : MarkdownBlock()
}

@Composable
fun MarkdownRenderer(
    markdownText: String,
    modifier: Modifier = Modifier
) {
    val blocks = parseMarkdownBlocks(markdownText)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        blocks.forEachIndexed { index, block ->
            when (block) {
                is MarkdownBlock.Heading -> {
                    val fontSize = when (block.level) {
                        1 -> 20.sp
                        2 -> 17.sp
                        3 -> 15.sp
                        else -> 14.sp
                    }
                    val textColor = if (block.level == 1) CyanPrimary else MaterialTheme.colorScheme.onSurface
                    Text(
                        text = buildMarkdownAnnotatedString(block.text),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = fontSize,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        ),
                        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                    )
                }
                is MarkdownBlock.Code -> {
                    CodeEditorView(
                        code = block.code,
                        language = block.language.ifBlank { "code" },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("markdown_code_block_$index")
                    )
                }
                is MarkdownBlock.Quote -> {
                    Surface(
                        shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = androidx.compose.foundation.BorderStroke(2.dp, CyanPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    ) {
                        Text(
                            text = buildMarkdownAnnotatedString(block.text),
                            style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                is MarkdownBlock.ListItem -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 6.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (block.index != null) "${block.index}." else "•",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = CyanPrimary
                        )
                        Text(
                            text = buildMarkdownAnnotatedString(block.text),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                is MarkdownBlock.Divider -> {
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    )
                }
                is MarkdownBlock.Paragraph -> {
                    if (block.text.isNotBlank()) {
                        Text(
                            text = buildMarkdownAnnotatedString(block.text),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }
    }
}

fun parseMarkdownBlocks(rawText: String): List<MarkdownBlock> {
    val lines = rawText.lines()
    val blocks = mutableListOf<MarkdownBlock>()
    var inCodeBlock = false
    var currentCodeLang = ""
    val currentCodeBuffer = StringBuilder()

    var i = 0
    while (i < lines.size) {
        val line = lines[i]
        val trimmed = line.trim()

        if (trimmed.startsWith("```")) {
            if (inCodeBlock) {
                // Closing code block
                blocks.add(MarkdownBlock.Code(currentCodeLang, currentCodeBuffer.toString().trimEnd()))
                currentCodeBuffer.clear()
                currentCodeLang = ""
                inCodeBlock = false
            } else {
                // Opening code block
                inCodeBlock = true
                currentCodeLang = trimmed.removePrefix("```").trim()
            }
            i++
            continue
        }

        if (inCodeBlock) {
            currentCodeBuffer.append(line).append("\n")
            i++
            continue
        }

        when {
            trimmed.startsWith("#") -> {
                val hashes = trimmed.takeWhile { it == '#' }
                val level = hashes.length.coerceIn(1, 4)
                val headingText = trimmed.drop(level).trim()
                blocks.add(MarkdownBlock.Heading(level, headingText))
            }
            trimmed.startsWith(">") -> {
                val quoteText = trimmed.removePrefix(">").trim()
                blocks.add(MarkdownBlock.Quote(quoteText))
            }
            trimmed.startsWith("---") || trimmed.startsWith("***") || trimmed.startsWith("___") -> {
                blocks.add(MarkdownBlock.Divider)
            }
            trimmed.startsWith("- ") || trimmed.startsWith("* ") || trimmed.startsWith("+ ") -> {
                val itemText = trimmed.drop(2).trim()
                blocks.add(MarkdownBlock.ListItem(index = null, text = itemText))
            }
            trimmed.matches(Regex("^\\d+\\..*")) -> {
                val dotIndex = trimmed.indexOf('.')
                val indexNum = trimmed.substring(0, dotIndex).toIntOrNull()
                val itemText = trimmed.substring(dotIndex + 1).trim()
                blocks.add(MarkdownBlock.ListItem(index = indexNum, text = itemText))
            }
            else -> {
                if (line.isNotBlank()) {
                    blocks.add(MarkdownBlock.Paragraph(line))
                }
            }
        }
        i++
    }

    if (inCodeBlock && currentCodeBuffer.isNotEmpty()) {
        blocks.add(MarkdownBlock.Code(currentCodeLang, currentCodeBuffer.toString().trimEnd()))
    }

    return blocks
}

@Composable
fun buildMarkdownAnnotatedString(text: String): AnnotatedString {
    val inlineCodeBg = MaterialTheme.colorScheme.surfaceVariant
    val primaryColor = CyanPrimary

    return buildAnnotatedString {
        var cursor = 0
        while (cursor < text.length) {
            val nextBold = text.indexOf("**", cursor)
            val nextCode = text.indexOf("`", cursor)
            val nextItalic = if (nextBold != cursor && text.getOrNull(cursor) == '*') -1 else text.indexOf("*", cursor)

            // Find nearest token
            val tokens = listOf(
                if (nextBold != -1) nextBold to "BOLD" else null,
                if (nextCode != -1) nextCode to "CODE" else null,
                if (nextItalic != -1 && (nextBold == -1 || nextItalic < nextBold)) nextItalic to "ITALIC" else null
            ).filterNotNull().sortedBy { it.first }

            if (tokens.isEmpty()) {
                append(text.substring(cursor))
                break
            }

            val (tokenPos, tokenType) = tokens.first()
            if (tokenPos > cursor) {
                append(text.substring(cursor, tokenPos))
            }

            when (tokenType) {
                "BOLD" -> {
                    val endBold = text.indexOf("**", tokenPos + 2)
                    if (endBold != -1) {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(text.substring(tokenPos + 2, endBold))
                        }
                        cursor = endBold + 2
                    } else {
                        append("**")
                        cursor = tokenPos + 2
                    }
                }
                "CODE" -> {
                    val endCode = text.indexOf("`", tokenPos + 1)
                    if (endCode != -1) {
                        withStyle(
                            SpanStyle(
                                fontFamily = FontFamily.Monospace,
                                background = inlineCodeBg,
                                color = primaryColor,
                                fontSize = 13.sp
                            )
                        ) {
                            append(" ")
                            append(text.substring(tokenPos + 1, endCode))
                            append(" ")
                        }
                        cursor = endCode + 1
                    } else {
                        append("`")
                        cursor = tokenPos + 1
                    }
                }
                "ITALIC" -> {
                    val endItalic = text.indexOf("*", tokenPos + 1)
                    if (endItalic != -1) {
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                            append(text.substring(tokenPos + 1, endItalic))
                        }
                        cursor = endItalic + 1
                    } else {
                        append("*")
                        cursor = tokenPos + 1
                    }
                }
            }
        }
    }
}
