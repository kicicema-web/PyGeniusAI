package com.pygeniusai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pygeniusai.ai.BugPrediction
import com.pygeniusai.ui.theme.PyGeniusColors
import kotlinx.coroutines.flow.StateFlow

/**
 * Python code editor with syntax highlighting and AI features
 */
@Composable
fun CodeEditor(
    code: String,
    onCodeChange: (String) -> Unit,
    bugPredictions: List<BugPrediction>,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    val listState = rememberLazyListState()
    val lines = code.lines()
    
    Box(modifier = modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Line numbers
            Column(
                modifier = Modifier
                    .width(48.dp)
                    .fillMaxHeight()
                    .background(PyGeniusColors.SurfaceVariant.copy(alpha = 0.5f))
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                lines.forEachIndexed { index, _ ->
                    val lineNumber = index + 1
                    val hasIssue = bugPredictions.any { it.line == lineNumber }
                    
                    Box(
                        modifier = Modifier.height(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = lineNumber.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (hasIssue) PyGeniusColors.Error 
                                    else PyGeniusColors.OnSurfaceMuted,
                            fontSize = 12.sp
                        )
                    }
                }
            }
            
            // Editor
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                BasicTextField(
                    value = code,
                    onValueChange = onCodeChange,
                    modifier = Modifier
                        .fillMaxSize()
                        .focusRequester(focusRequester)
                        .padding(8.dp),
                    textStyle = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        color = PyGeniusColors.OnSurface,
                        lineHeight = 20.sp
                    ),
                    cursorBrush = SolidColor(PyGeniusColors.Primary),
                    decorationBox = { innerTextField ->
                        Box {
                            // Background highlight for bug lines
                            Column {
                                lines.forEachIndexed { index, _ ->
                                    val lineNumber = index + 1
                                    val prediction = bugPredictions.find { it.line == lineNumber }
                                    Box(
                                        modifier = Modifier
                                            .height(20.dp)
                                            .fillMaxWidth()
                                            .background(
                                                when (prediction?.severity) {
                                                    com.pygeniusai.ai.Severity.HIGH -> PyGeniusColors.Error.copy(alpha = 0.1f)
                                                    com.pygeniusai.ai.Severity.MEDIUM -> PyGeniusColors.Warning.copy(alpha = 0.1f)
                                                    com.pygeniusai.ai.Severity.LOW -> PyGeniusColors.Info.copy(alpha = 0.1f)
                                                    else -> PyGeniusColors.Background
                                                }
                                            )
                                    )
                                }
                            }
                            innerTextField()
                        }
                    }
                )
            }
        }
        
        // Bug prediction indicators
        if (bugPredictions.isNotEmpty()) {
            BugPredictionBar(
                predictions = bugPredictions,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(8.dp)
            )
        }
    }
}

@Composable
private fun BugPredictionBar(
    predictions: List<BugPrediction>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = PyGeniusColors.SurfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = "⚠️ AI Analysis",
                style = MaterialTheme.typography.labelMedium,
                color = PyGeniusColors.Warning
            )
            Spacer(modifier = Modifier.height(4.dp))
            predictions.take(3).forEach { prediction ->
                Row(
                    modifier = Modifier.padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val color = when (prediction.severity) {
                        com.pygeniusai.ai.Severity.HIGH -> PyGeniusColors.Error
                        com.pygeniusai.ai.Severity.MEDIUM -> PyGeniusColors.Warning
                        else -> PyGeniusColors.Info
                    }
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(color, shape = MaterialTheme.shapes.small)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Line ${prediction.line}: ${prediction.message}",
                        style = MaterialTheme.typography.bodySmall,
                        color = PyGeniusColors.OnSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Syntax-highlighted code text
 */
@Composable
fun SyntaxHighlightedCode(
    code: String,
    modifier: Modifier = Modifier
) {
    val annotatedString = remember(code) {
        highlightPythonSyntax(code)
    }
    
    Text(
        text = annotatedString,
        modifier = modifier,
        style = TextStyle(
            fontFamily = FontFamily.Monospace,
            fontSize = 14.sp,
            lineHeight = 20.sp
        )
    )
}

private fun highlightPythonSyntax(code: String): AnnotatedString {
    return buildAnnotatedString {
        val lines = code.lines()
        val keywords = listOf(
            "and", "as", "assert", "break", "class", "continue", "def",
            "del", "elif", "else", "except", "False", "finally", "for",
            "from", "global", "if", "import", "in", "is", "lambda",
            "None", "nonlocal", "not", "or", "pass", "raise", "return",
            "True", "try", "while", "with", "yield"
        )
        
        lines.forEachIndexed { index, line ->
            // Simple highlighting - just color the whole line based on content
            when {
                line.trimStart().startsWith("#") -> {
                    withStyle(SpanStyle(color = PyGeniusColors.CodeComment)) {
                        append(line)
                    }
                }
                line.contains("\"") || line.contains("'") -> {
                    // Very basic string highlighting
                    val parts = line.split("\"")
                    parts.forEachIndexed { i, part ->
                        if (i % 2 == 0) {
                            append(part)
                        } else {
                            withStyle(SpanStyle(color = PyGeniusColors.CodeString)) {
                                append("\"$part\"")
                            }
                        }
                    }
                }
                keywords.any { keyword -> line.contains("\\b$keyword\\b".toRegex()) } -> {
                    withStyle(SpanStyle(color = PyGeniusColors.CodeKeyword)) {
                        append(line)
                    }
                }
                else -> {
                    append(line)
                }
            }
            
            if (index < lines.size - 1) {
                append("\n")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodeCompletionSuggestion(
    suggestion: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = PyGeniusColors.SurfaceVariant
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = suggestion,
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace,
                color = PyGeniusColors.CodeFunction,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = PyGeniusColors.OnSurfaceMuted
            )
        }
    }
}
