package com.example.devgate.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
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
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TerminalBackground

@Composable
fun CodeEditorView(
    code: String,
    language: String = "Kotlin",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lines = code.lines()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(TerminalBackground)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(modifier = Modifier.size(8.dp).background(MaterialTheme.colorScheme.error, RoundedCornerShape(50)))
                Box(modifier = Modifier.size(8.dp).background(MaterialTheme.colorScheme.tertiary, RoundedCornerShape(50)))
                Box(modifier = Modifier.size(8.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(50)))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = language.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Code", code))
                    Toast.makeText(context, "Code copied to clipboard", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .size(28.dp)
                    .testTag("copy_code_button")
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy Code",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Code Area
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .horizontalScroll(rememberScrollState())
        ) {
            // Line numbers
            Column(modifier = Modifier.padding(end = 12.dp)) {
                lines.indices.forEach { index ->
                    Text(
                        text = "${index + 1}",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        fontSize = 12.sp
                    )
                }
            }

            // Code Text
            Column {
                lines.forEach { line ->
                    Text(
                        text = line,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
