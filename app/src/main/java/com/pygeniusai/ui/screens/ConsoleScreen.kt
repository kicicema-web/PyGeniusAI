package com.pygeniusai.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.pygeniusai.python.ConsoleLine
import com.pygeniusai.ui.components.Console
import kotlinx.coroutines.flow.StateFlow

@Composable
fun ConsoleScreen(
    output: List<ConsoleLine>,
    isRunning: Boolean,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    Console(
        output = output,
        isRunning = isRunning,
        onClear = onClear,
        modifier = modifier.fillMaxSize()
    )
}
