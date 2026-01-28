package com.pygeniusai.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pygeniusai.ui.theme.PyGeniusColors
import com.pygeniusai.ui.viewmodel.ApiKeyStatus

@Composable
fun SettingsScreen(
    apiKeyStatus: ApiKeyStatus,
    modifier: Modifier = Modifier
) {
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "⚙️ Settings",
            style = MaterialTheme.typography.headlineMedium,
            color = PyGeniusColors.OnSurface,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        // API Key Configuration Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = PyGeniusColors.Surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = null,
                        tint = PyGeniusColors.AccentPurple,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "AI Configuration",
                        style = MaterialTheme.typography.titleLarge,
                        color = PyGeniusColors.OnSurface
                    )
                }
                
                // Status indicator
                ApiKeyStatusBadge(status = apiKeyStatus)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "AI features are pre-configured and ready to use!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PyGeniusColors.OnSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                // Status indicator
                Surface(
                    color = PyGeniusColors.Success.copy(alpha = 0.1f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = PyGeniusColors.Success,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI Features Enabled",
                            style = MaterialTheme.typography.labelLarge,
                            color = PyGeniusColors.Success
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Powered by OpenRouter AI",
                    style = MaterialTheme.typography.bodySmall,
                    color = PyGeniusColors.OnSurfaceMuted
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // AI Features Info
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = PyGeniusColors.Surface.copy(alpha = 0.7f)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "✨ Available AI Features",
                    style = MaterialTheme.typography.titleMedium,
                    color = PyGeniusColors.OnSurface,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                val features = listOf(
                    "AI Tutor - Ask Python questions",
                    "Code Explanation - Understand any code",
                    "Error Analysis - Get fix suggestions",
                    "Bug Detection - Find issues early",
                    "Code Optimization - Improve performance",
                    "Voice to Code - Speak to generate code",
                    "Interactive Lessons - AI-generated lessons"
                )
                
                features.forEach { feature ->
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "•",
                            color = PyGeniusColors.AccentPurple,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.width(16.dp)
                        )
                        Text(
                            text = feature,
                            color = PyGeniusColors.OnSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Privacy Notice
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = PyGeniusColors.Surface.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = PyGeniusColors.Success,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Privacy & Security",
                        style = MaterialTheme.typography.titleMedium,
                        color = PyGeniusColors.OnSurface
                    )
                }
                
                Text(
                    text = "• Your code is sent to OpenRouter for AI processing\n" +
                           "• Internet connection is required for AI features\n" +
                           "• Local fallback available when offline\n" +
                           "• Your data is processed securely",
                    style = MaterialTheme.typography.bodySmall,
                    color = PyGeniusColors.OnSurfaceMuted,
                    lineHeight = MaterialTheme.typography.bodySmall.lineHeight * 1.3f
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // About Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = PyGeniusColors.Surface.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "About PyGenius AI",
                    style = MaterialTheme.typography.titleMedium,
                    color = PyGeniusColors.OnSurface,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                Text(
                    text = "Version 1.0.0",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PyGeniusColors.OnSurfaceVariant
                )
                
                Text(
                    text = "AI powered by OpenRouter",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PyGeniusColors.OnSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "A Python IDE with intelligent AI tutoring to help you learn and write better code.",
                    style = MaterialTheme.typography.bodySmall,
                    color = PyGeniusColors.OnSurfaceMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun ApiKeyStatusBadge(status: ApiKeyStatus) {
    val (icon, text, color) = when (status) {
        ApiKeyStatus.CONFIGURED -> Triple(
            Icons.Default.CheckCircle,
            "AI Features Enabled",
            PyGeniusColors.Success
        )
        ApiKeyStatus.NOT_CONFIGURED -> Triple(
            Icons.Default.Warning,
            "API Key Not Set - Limited Features",
            PyGeniusColors.Warning
        )
        ApiKeyStatus.UNKNOWN -> Triple(
            Icons.Default.Help,
            "Checking API Key...",
            PyGeniusColors.OnSurfaceMuted
        )
    }
    
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = color
            )
        }
    }
}
