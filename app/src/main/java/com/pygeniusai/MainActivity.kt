package com.pygeniusai

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pygeniusai.ai.DifficultyLevel
import com.pygeniusai.ai.LessonType
import com.pygeniusai.ui.screens.*
import com.pygeniusai.ui.theme.PyGeniusTheme
import com.pygeniusai.ui.viewmodel.PyGeniusViewModel
import com.pygeniusai.ui.viewmodel.Tab
import java.util.*

class MainActivity : ComponentActivity() {
    
    private var speechRecognizer: SpeechRecognizer? = null
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startVoiceRecognition()
        } else {
            Toast.makeText(this, "Microphone permission required for voice coding", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Handle shared text (from other apps)
        handleSharedIntent(intent)
        
        setContent {
            PyGeniusTheme {
                PyGeniusApp(
                    onVoiceClick = { checkPermissionAndStartVoice() }
                )
            }
        }
    }
    
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleSharedIntent(intent)
    }
    
    private fun handleSharedIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
            // This will be handled by the ViewModel
        }
    }
    
    private fun checkPermissionAndStartVoice() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == 
                PackageManager.PERMISSION_GRANTED -> {
                startVoiceRecognition()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }
    
    private fun startVoiceRecognition() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your Python code...")
        }
        
        // Show a toast to indicate voice recognition started
        Toast.makeText(this, "Listening... Speak your code", Toast.LENGTH_SHORT).show()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer?.destroy()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PyGeniusApp(
    onVoiceClick: () -> Unit,
    viewModel: PyGeniusViewModel = viewModel()
) {
    val context = LocalContext.current
    
    // Collect state
    val code by viewModel.code.collectAsStateWithLifecycle()
    val fileName by viewModel.currentFileName.collectAsStateWithLifecycle()
    val isModified by viewModel.isModified.collectAsStateWithLifecycle()
    val isRunning by viewModel.isRunning.collectAsStateWithLifecycle()
    val consoleOutput by viewModel.consoleOutput.collectAsStateWithLifecycle()
    val bugPredictions by viewModel.bugPredictions.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val aiResponse by viewModel.aiResponse.collectAsStateWithLifecycle()
    val isAiProcessing by viewModel.isAiProcessing.collectAsStateWithLifecycle()
    val lastError by viewModel.lastError.collectAsStateWithLifecycle()
    val currentLesson by viewModel.currentLesson.collectAsStateWithLifecycle()
    val lessonCode by viewModel.lessonCode.collectAsStateWithLifecycle()
    val installedPackages by viewModel.installedPackages.collectAsStateWithLifecycle()
    val pipSearchQuery by viewModel.pipSearchQuery.collectAsStateWithLifecycle()
    
    // Dialog states
    var showFileDialog by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                actions = {
                    // Python version indicator
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = "Python 3.11",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Code, contentDescription = null) },
                    label = { Text("Editor") },
                    selected = selectedTab == Tab.EDITOR,
                    onClick = { viewModel.selectTab(Tab.EDITOR) }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Terminal, contentDescription = null) },
                    label = { Text("Console") },
                    selected = selectedTab == Tab.CONSOLE,
                    onClick = { viewModel.selectTab(Tab.CONSOLE) }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.School, contentDescription = null) },
                    label = { Text("Learn") },
                    selected = selectedTab == Tab.LEARNING,
                    onClick = { viewModel.selectTab(Tab.LEARNING) }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Psychology, contentDescription = null) },
                    label = { Text("AI Tutor") },
                    selected = selectedTab == Tab.AI_TUTOR,
                    onClick = { viewModel.selectTab(Tab.AI_TUTOR) }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Download, contentDescription = null) },
                    label = { Text("Pip") },
                    selected = selectedTab == Tab.PACKAGES,
                    onClick = { viewModel.selectTab(Tab.PACKAGES) }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                Tab.EDITOR -> EditorScreen(
                    code = code,
                    onCodeChange = viewModel::updateCode,
                    fileName = fileName,
                    isModified = isModified,
                    isRunning = isRunning,
                    bugPredictions = bugPredictions,
                    onRunClick = viewModel::runCode,
                    onStopClick = viewModel::stopCode,
                    onNewFile = viewModel::newFile,
                    onSaveFile = { showSaveDialog = true },
                    onOpenFile = { showFileDialog = true },
                    onPipClick = { viewModel.selectTab(Tab.PACKAGES) },
                    onVoiceClick = onVoiceClick,
                    onAiClick = { viewModel.selectTab(Tab.AI_TUTOR) }
                )
                
                Tab.CONSOLE -> ConsoleScreen(
                    output = consoleOutput,
                    isRunning = isRunning,
                    onClear = viewModel::clearConsole
                )
                
                Tab.AI_TUTOR -> AiTutorScreen(
                    aiResponse = aiResponse,
                    isProcessing = isAiProcessing,
                    lastError = lastError,
                    onAskQuestion = viewModel::askAi,
                    onExplainCode = viewModel::getCodeExplanation
                )
                
                Tab.LEARNING -> LearningScreen(
                    currentLesson = currentLesson,
                    lessonCode = lessonCode,
                    onLessonCodeChange = viewModel::updateLessonCode,
                    onLoadLesson = viewModel::loadLesson,
                    onCheckSolution = viewModel::checkLessonSolution,
                    aiFeedback = aiResponse
                )
                
                Tab.PACKAGES -> PackageManagerScreen(
                    installedPackages = installedPackages,
                    searchQuery = pipSearchQuery,
                    onSearchQueryChange = viewModel::updatePipSearch,
                    onInstall = viewModel::installPackage,
                    onUninstall = viewModel::uninstallPackage
                )
            }
        }
    }
    
    // File dialogs would go here
}
