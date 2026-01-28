package com.pygeniusai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pygeniusai.ai.ErrorExplanation
import com.pygeniusai.ui.theme.PyGeniusColors

@Composable
fun AiTutorScreen(
    aiResponse: String,
    isProcessing: Boolean,
    lastError: ErrorExplanation?,
    apiKeyStatus: com.pygeniusai.ui.viewmodel.ApiKeyStatus = com.pygeniusai.ui.viewmodel.ApiKeyStatus.UNKNOWN,
    onAskQuestion: (String) -> Unit,
    onExplainCode: () -> Unit,
    onOptimizeCode: () -> Unit = {},
    onGoToSettings: () -> Unit = {},
    onClearResponse: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var question by remember { mutableStateOf("") }
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Header
        Surface(
            color = PyGeniusColors.Surface,
            tonalElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = null,
                        tint = PyGeniusColors.AccentPurple,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "AI Tutor",
                            style = MaterialTheme.typography.titleMedium,
                            color = PyGeniusColors.OnSurface
                        )
                        Text(
                            text = "Your Python learning companion",
                            style = MaterialTheme.typography.bodySmall,
                            color = PyGeniusColors.OnSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Quick actions
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AssistChip(
                        onClick = onExplainCode,
                        label = { Text("Explain Code") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Description,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                    AssistChip(
                        onClick = { onAskQuestion("How do I fix errors?") },
                        label = { Text("Fix Errors") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.BugReport,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                    AssistChip(
                        onClick = { onAskQuestion("Optimize my code") },
                        label = { Text("Optimize") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Speed,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }
            }
        }
        
        // Error explanation card (if available)
        lastError?.let { error ->
            ErrorExplanationCard(
                error = error,
                modifier = Modifier.padding(16.dp)
            )
        }
        
        // AI Response area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            if (isProcessing) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = PyGeniusColors.AccentPurple)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "AI is thinking...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = PyGeniusColors.OnSurfaceVariant
                        )
                    }
                }
            } else if (aiResponse.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxSize(),
                    colors = CardDefaults.cardColors(
                        containerColor = PyGeniusColors.SurfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    LazyColumn(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        item {
                            Text(
                                text = aiResponse,
                                style = MaterialTheme.typography.bodyMedium,
                                color = PyGeniusColors.OnSurface
                            )
                        }
                    }
                }
            } else {
                // Empty state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = PyGeniusColors.OnSurfaceMuted
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Ask the AI Tutor anything!",
                            style = MaterialTheme.typography.bodyLarge,
                            color = PyGeniusColors.OnSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "• Explain your code\n• Fix errors\n• Learn Python concepts",
                            style = MaterialTheme.typography.bodySmall,
                            color = PyGeniusColors.OnSurfaceMuted
                        )
                    }
                }
            }
        }
        
        // Input field
        Surface(
            color = PyGeniusColors.Surface,
            tonalElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = question,
                    onValueChange = { question = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Ask the AI Tutor...") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PyGeniusColors.AccentPurple,
                        focusedLabelColor = PyGeniusColors.AccentPurple
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (question.isNotBlank()) {
                            onAskQuestion(question)
                            question = ""
                        }
                    },
                    enabled = question.isNotBlank() && !isProcessing
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = if (question.isNotBlank()) PyGeniusColors.AccentPurple 
                               else PyGeniusColors.OnSurfaceMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorExplanationCard(
    error: ErrorExplanation,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = PyGeniusColors.Error.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = null,
                    tint = PyGeniusColors.Error
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Error: ${error.errorType}",
                    style = MaterialTheme.typography.titleSmall,
                    color = PyGeniusColors.Error
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = error.explanation,
                style = MaterialTheme.typography.bodyMedium,
                color = PyGeniusColors.OnSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Surface(
                color = PyGeniusColors.Surface,
                shape = RoundedCornerShape(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "💡 Suggestion:",
                        style = MaterialTheme.typography.labelMedium,
                        color = PyGeniusColors.Primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = error.suggestion,
                        style = MaterialTheme.typography.bodySmall,
                        color = PyGeniusColors.OnSurfaceVariant
                    )
                }
            }
            
            if (error.example.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = PyGeniusColors.CodeBackground,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = error.example,
                        style = MaterialTheme.typography.bodySmall,
                        color = PyGeniusColors.CodeComment,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}
