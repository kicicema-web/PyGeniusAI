package com.pygeniusai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pygeniusai.ai.DifficultyLevel
import com.pygeniusai.ai.Lesson
import com.pygeniusai.ai.LessonType
import com.pygeniusai.ui.theme.PyGeniusColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearningScreen(
    currentLesson: Lesson?,
    lessonCode: String,
    onLessonCodeChange: (String) -> Unit,
    onLoadLesson: (LessonType, DifficultyLevel) -> Unit,
    onCheckSolution: () -> Unit,
    aiFeedback: String,
    modifier: Modifier = Modifier
) {
    var selectedDifficulty by remember { mutableStateOf(DifficultyLevel.BEGINNER) }
    var showLessonSelector by remember { mutableStateOf(currentLesson == null) }
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        if (showLessonSelector || currentLesson == null) {
            LessonSelector(
                selectedDifficulty = selectedDifficulty,
                onDifficultySelected = { selectedDifficulty = it },
                onLessonSelected = { type, level ->
                    onLoadLesson(type, level)
                    showLessonSelector = false
                },
                onDismiss = { showLessonSelector = false }
            )
        } else {
            LessonContent(
                lesson = currentLesson,
                userCode = lessonCode,
                onUserCodeChange = onLessonCodeChange,
                onBack = { showLessonSelector = true },
                onCheckSolution = onCheckSolution,
                aiFeedback = aiFeedback
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LessonSelector(
    selectedDifficulty: DifficultyLevel,
    onDifficultySelected: (DifficultyLevel) -> Unit,
    onLessonSelected: (LessonType, DifficultyLevel) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
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
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = PyGeniusColors.Accent,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Python Professor",
                            style = MaterialTheme.typography.titleMedium,
                            color = PyGeniusColors.OnSurface
                        )
                        Text(
                            text = "Interactive Python lessons",
                            style = MaterialTheme.typography.bodySmall,
                            color = PyGeniusColors.OnSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Difficulty selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    DifficultyLevel.values().forEach { level ->
                        FilterChip(
                            selected = selectedDifficulty == level,
                            onClick = { onDifficultySelected(level) },
                            label = {
                                Text(level.name.lowercase().replaceFirstChar { it.uppercase() })
                            }
                        )
                    }
                }
            }
        }
        
        // Lesson list
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val lessons = when (selectedDifficulty) {
                DifficultyLevel.BEGINNER -> listOf(
                    LessonType.VARIABLES to "Variables & Data Types",
                    LessonType.LOOPS to "For Loops",
                    LessonType.FUNCTIONS to "Functions",
                    LessonType.DATA_STRUCTURES to "Dictionaries"
                )
                DifficultyLevel.INTERMEDIATE -> listOf(
                    LessonType.LIST_COMPREHENSION to "List Comprehensions",
                    LessonType.CLASSES to "Classes & Objects",
                    LessonType.LOOPS to "Advanced Loops"
                )
                DifficultyLevel.ADVANCED -> listOf(
                    LessonType.LOOPS to "Generator Expressions",
                    LessonType.CLASSES to "Advanced OOP",
                    LessonType.FUNCTIONS to "Decorators"
                )
            }
            
            items(lessons) { (type, title) ->
                LessonCard(
                    title = title,
                    difficulty = selectedDifficulty,
                    onClick = { onLessonSelected(type, selectedDifficulty) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LessonCard(
    title: String,
    difficulty: DifficultyLevel,
    onClick: () -> Unit
) {
    val difficultyColor = when (difficulty) {
        DifficultyLevel.BEGINNER -> PyGeniusColors.Success
        DifficultyLevel.INTERMEDIATE -> PyGeniusColors.Warning
        DifficultyLevel.ADVANCED -> PyGeniusColors.Error
    }
    
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = PyGeniusColors.SurfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        difficultyColor.copy(alpha = 0.2f),
                        shape = MaterialTheme.shapes.medium
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Code,
                    contentDescription = null,
                    tint = difficultyColor
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = PyGeniusColors.OnSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = difficulty.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodySmall,
                    color = difficultyColor
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Start lesson",
                tint = PyGeniusColors.OnSurfaceVariant
            )
        }
    }
}

@Composable
private fun LessonContent(
    lesson: Lesson,
    userCode: String,
    onUserCodeChange: (String) -> Unit,
    onBack: () -> Unit,
    onCheckSolution: () -> Unit,
    aiFeedback: String
) {
    var showHint by remember { mutableStateOf(false) }
    var showSolution by remember { mutableStateOf(false) }
    var currentHintIndex by remember { mutableStateOf(0) }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Lesson header
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
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = lesson.title,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = lesson.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = PyGeniusColors.OnSurfaceVariant
                        )
                    }
                }
            }
        }
        
        // Lesson content
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp)
        ) {
            // Example code
            item {
                Text(
                    text = "Example:",
                    style = MaterialTheme.typography.labelMedium,
                    color = PyGeniusColors.OnSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = PyGeniusColors.CodeBackground,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = lesson.code,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        color = PyGeniusColors.CodeComment,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Challenge
            item {
                Text(
                    text = "Challenge:",
                    style = MaterialTheme.typography.labelMedium,
                    color = PyGeniusColors.OnSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = lesson.challenge,
                    style = MaterialTheme.typography.bodyMedium,
                    color = PyGeniusColors.OnSurface
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Hints
            if (showHint && currentHintIndex < lesson.hints.size) {
                item {
                    Surface(
                        color = PyGeniusColors.Info.copy(alpha = 0.1f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = PyGeniusColors.Info
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Hint ${currentHintIndex + 1}: ${lesson.hints[currentHintIndex]}",
                                style = MaterialTheme.typography.bodySmall,
                                color = PyGeniusColors.OnSurface
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
            
            // Solution
            if (showSolution) {
                item {
                    Surface(
                        color = PyGeniusColors.Success.copy(alpha = 0.1f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "Solution:",
                                style = MaterialTheme.typography.labelMedium,
                                color = PyGeniusColors.Success
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = PyGeniusColors.CodeBackground,
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    text = lesson.solution,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    color = PyGeniusColors.CodeString,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
            
            // AI Feedback
            if (aiFeedback.isNotEmpty()) {
                item {
                    Surface(
                        color = PyGeniusColors.AccentPurple.copy(alpha = 0.1f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.SmartToy,
                                contentDescription = null,
                                tint = PyGeniusColors.AccentPurple
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = aiFeedback,
                                style = MaterialTheme.typography.bodyMedium,
                                color = PyGeniusColors.OnSurface
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
        
        // Action buttons
        Surface(
            color = PyGeniusColors.Surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Code input
                OutlinedTextField(
                    value = userCode,
                    onValueChange = onUserCodeChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Type your solution here...") },
                    minLines = 3,
                    maxLines = 5,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            showHint = true
                            if (currentHintIndex < lesson.hints.size - 1) {
                                currentHintIndex++
                            }
                        }
                    ) {
                        Icon(Icons.Default.Lightbulb, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Hint")
                    }
                    
                    OutlinedButton(
                        onClick = { showSolution = true }
                    ) {
                        Icon(Icons.Default.Visibility, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Solution")
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    Button(
                        onClick = onCheckSolution,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PyGeniusColors.Primary
                        )
                    ) {
                        Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Check")
                    }
                }
            }
        }
    }
}
