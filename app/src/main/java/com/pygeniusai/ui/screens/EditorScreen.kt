package com.pygeniusai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pygeniusai.ai.BugPrediction
import com.pygeniusai.ui.components.CodeEditor
import com.pygeniusai.ui.theme.PyGeniusColors

@Composable
fun EditorScreen(
    code: String,
    onCodeChange: (String) -> Unit,
    fileName: String,
    isModified: Boolean,
    isRunning: Boolean,
    bugPredictions: List<BugPrediction>,
    onRunClick: () -> Unit,
    onStopClick: () -> Unit,
    onNewFile: () -> Unit,
    onSaveFile: () -> Unit,
    onOpenFile: () -> Unit,
    onPipClick: () -> Unit,
    onVoiceClick: () -> Unit,
    onAiClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Toolbar
        EditorToolbar(
            fileName = fileName,
            isModified = isModified,
            isRunning = isRunning,
            onRunClick = onRunClick,
            onStopClick = onStopClick,
            onNewFile = onNewFile,
            onSaveFile = onSaveFile,
            onOpenFile = onOpenFile,
            onPipClick = onPipClick,
            onVoiceClick = onVoiceClick,
            onAiClick = onAiClick
        )
        
        // Code editor
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            CodeEditor(
                code = code,
                onCodeChange = onCodeChange,
                bugPredictions = bugPredictions,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun EditorToolbar(
    fileName: String,
    isModified: Boolean,
    isRunning: Boolean,
    onRunClick: () -> Unit,
    onStopClick: () -> Unit,
    onNewFile: () -> Unit,
    onSaveFile: () -> Unit,
    onOpenFile: () -> Unit,
    onPipClick: () -> Unit,
    onVoiceClick: () -> Unit,
    onAiClick: () -> Unit
) {
    Surface(
        color = PyGeniusColors.Surface,
        tonalElevation = 4.dp
    ) {
        Column {
            // Top row - File operations and Run
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // File info and operations
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNewFile) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New file",
                            tint = PyGeniusColors.OnSurface
                        )
                    }
                    IconButton(onClick = onOpenFile) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = "Open file",
                            tint = PyGeniusColors.OnSurface
                        )
                    }
                    IconButton(onClick = onSaveFile) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Save file",
                            tint = PyGeniusColors.OnSurface
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    // File name indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(
                                PyGeniusColors.SurfaceVariant,
                                shape = MaterialTheme.shapes.small
                            )
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = PyGeniusColors.Primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$fileName${if (isModified) " •" else ""}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = PyGeniusColors.OnSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                // Run button
                if (isRunning) {
                    FilledIconButton(
                        onClick = onStopClick,
                        modifier = Modifier.size(40.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = PyGeniusColors.Error
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Stop",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    Button(
                        onClick = onRunClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PyGeniusColors.Primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Run")
                    }
                }
            }
            
            // Bottom row - Tools
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // AI badge
                Surface(
                    color = PyGeniusColors.AccentPurple.copy(alpha = 0.2f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = PyGeniusColors.AccentPurple
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "AI Ready",
                            style = MaterialTheme.typography.labelSmall,
                            color = PyGeniusColors.AccentPurple
                        )
                    }
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Tool buttons
                IconButton(onClick = onAiClick) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = "AI Assist",
                        tint = PyGeniusColors.AccentPurple
                    )
                }
                IconButton(onClick = onVoiceClick) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice coding",
                        tint = PyGeniusColors.OnSurfaceVariant
                    )
                }
                IconButton(onClick = onPipClick) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Pip packages",
                        tint = PyGeniusColors.OnSurfaceVariant
                    )
                }
            }
        }
    }
}
