package com.pygeniusai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.pygeniusai.python.ConsoleLine
import com.pygeniusai.python.LineType
import com.pygeniusai.ui.theme.PyGeniusColors
import kotlinx.coroutines.flow.StateFlow

/**
 * Console output display with AI annotations
 */
@Composable
fun Console(
    output: List<ConsoleLine>,
    isRunning: Boolean,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    
    // Auto-scroll to bottom
    LaunchedEffect(output.size) {
        if (output.isNotEmpty()) {
            listState.animateScrollToItem(output.size - 1)
        }
    }
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Console toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(PyGeniusColors.SurfaceVariant)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Console",
                    style = MaterialTheme.typography.labelMedium,
                    color = PyGeniusColors.OnSurfaceVariant
                )
                if (isRunning) {
                    Spacer(modifier = Modifier.width(8.dp))
                    CircularProgressIndicator(
                        modifier = Modifier.size(12.dp),
                        strokeWidth = 2.dp,
                        color = PyGeniusColors.Primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Running...",
                        style = MaterialTheme.typography.labelSmall,
                        color = PyGeniusColors.Primary
                    )
                }
            }
            
            IconButton(
                onClick = onClear,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Clear console",
                    tint = PyGeniusColors.OnSurfaceMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        
        // Console output
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(PyGeniusColors.CodeBackground)
                .padding(8.dp)
        ) {
            if (output.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Run your code to see output here...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PyGeniusColors.OnSurfaceMuted
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(output) { line ->
                        ConsoleLineItem(line = line)
                    }
                }
            }
        }
    }
}

@Composable
private fun ConsoleLineItem(line: ConsoleLine) {
    val (icon, color) = when (line.type) {
        LineType.OUTPUT -> "" to PyGeniusColors.ConsoleOutput
        LineType.ERROR -> "✗ " to PyGeniusColors.ConsoleError
        LineType.PLOT -> "📊 " to PyGeniusColors.ConsoleSuccess
        LineType.PROGRESS -> "▶ " to PyGeniusColors.ConsolePrompt
        LineType.AI_SUGGESTION -> "🤖 " to PyGeniusColors.Accent
    }
    
    val annotatedString = buildAnnotatedString {
        withStyle(SpanStyle(color = color)) {
            append(icon)
            append(line.text)
        }
    }
    
    Text(
        text = annotatedString,
        style = MaterialTheme.typography.bodySmall,
        fontFamily = FontFamily.Monospace,
        modifier = Modifier.padding(vertical = 1.dp)
    )
}

@Composable
fun AiAnnotatedConsole(
    output: List<ConsoleLine>,
    aiAnnotations: Map<Int, String>,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f)
        ) {
            items(output) { line ->
                Column {
                    ConsoleLineItem(line = line)
                    // AI annotation if available
                    aiAnnotations[output.indexOf(line)]?.let { annotation ->
                        AiAnnotation(text = annotation)
                    }
                }
            }
        }
    }
}

@Composable
private fun AiAnnotation(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 2.dp)
            .background(
                PyGeniusColors.Accent.copy(alpha = 0.1f),
                shape = MaterialTheme.shapes.small
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "🤖",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = PyGeniusColors.Accent
        )
    }
}

@Composable
fun ExecutionProgressBar(
    currentLine: Int,
    totalLines: Int,
    modifier: Modifier = Modifier
) {
    if (totalLines > 0) {
        LinearProgressIndicator(
            progress = currentLine.toFloat() / totalLines,
            modifier = modifier
                .fillMaxWidth()
                .height(2.dp),
            color = PyGeniusColors.Primary,
            trackColor = PyGeniusColors.SurfaceVariant,
        )
    }
}
