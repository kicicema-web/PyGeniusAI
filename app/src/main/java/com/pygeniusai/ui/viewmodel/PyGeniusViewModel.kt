package com.pygeniusai.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pygeniusai.PyGeniusApplication
import com.pygeniusai.ai.AiEngine
import com.pygeniusai.ai.BugPrediction
import com.pygeniusai.ai.DifficultyLevel
import com.pygeniusai.ai.ErrorExplanation
import com.pygeniusai.ai.Lesson
import com.pygeniusai.ai.LessonType
import com.pygeniusai.data.ScriptEntry
import com.pygeniusai.data.UserProgressRepository
import com.pygeniusai.python.ConsoleLine
import com.pygeniusai.python.LineType
import com.pygeniusai.python.PythonRuntime
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class PyGeniusViewModel(application: Application) : AndroidViewModel(application) {
    
    private val pythonRuntime: PythonRuntime = PyGeniusApplication.getInstance(application).pythonRuntime
    private val aiEngine: AiEngine = AiEngine.getInstance(application)
    private val userProgress: UserProgressRepository = PyGeniusApplication.getInstance(application).userProgressRepository
    
    // Editor state
    private val _code = MutableStateFlow(DEFAULT_CODE)
    val code: StateFlow<String> = _code.asStateFlow()
    
    private val _currentFileName = MutableStateFlow("untitled.py")
    val currentFileName: StateFlow<String> = _currentFileName.asStateFlow()
    
    private val _isModified = MutableStateFlow(false)
    val isModified: StateFlow<Boolean> = _isModified.asStateFlow()
    
    // Console state
    val consoleOutput: StateFlow<List<ConsoleLine>> = pythonRuntime.consoleOutput
    val isRunning: StateFlow<Boolean> = pythonRuntime.isRunning
    
    // AI state
    private val _aiSuggestions = MutableStateFlow<List<String>>(emptyList())
    val aiSuggestions: StateFlow<List<String>> = _aiSuggestions.asStateFlow()
    
    private val _bugPredictions = MutableStateFlow<List<BugPrediction>>(emptyList())
    val bugPredictions: StateFlow<List<BugPrediction>> = _bugPredictions.asStateFlow()
    
    private val _lastError = MutableStateFlow<ErrorExplanation?>(null)
    val lastError: StateFlow<ErrorExplanation?> = _lastError.asStateFlow()
    
    private val _aiResponse = MutableStateFlow<String>("")
    val aiResponse: StateFlow<String> = _aiResponse.asStateFlow()
    
    private val _isAiProcessing = MutableStateFlow(false)
    val isAiProcessing: StateFlow<Boolean> = _isAiProcessing.asStateFlow()
    
    // API Key state
    private val _apiKeyStatus = MutableStateFlow<ApiKeyStatus>(ApiKeyStatus.UNKNOWN)
    val apiKeyStatus: StateFlow<ApiKeyStatus> = _apiKeyStatus.asStateFlow()
    
    // Learning mode state
    private val _currentLesson = MutableStateFlow<Lesson?>(null)
    val currentLesson: StateFlow<Lesson?> = _currentLesson.asStateFlow()
    
    private val _lessonCode = MutableStateFlow("")
    val lessonCode: StateFlow<String> = _lessonCode.asStateFlow()
    
    // Pip state
    val installedPackages = pythonRuntime.installedPackages
    
    private val _pipSearchQuery = MutableStateFlow("")
    val pipSearchQuery: StateFlow<String> = _pipSearchQuery.asStateFlow()
    
    // Saved scripts
    val savedScripts: StateFlow<Map<String, ScriptEntry>> = MutableStateFlow(emptyMap())
    
    // Tab selection
    private val _selectedTab = MutableStateFlow(Tab.EDITOR)
    val selectedTab: StateFlow<Tab> = _selectedTab.asStateFlow()
    
    private var aiJob: Job? = null
    private var analysisJob: Job? = null
    
    init {
        loadSavedScripts()
        checkApiKeyStatus()
    }
    
    // Editor operations
    fun updateCode(newCode: String) {
        _code.value = newCode
        _isModified.value = true
        
        // Trigger AI analysis after a delay
        analysisJob?.cancel()
        analysisJob = viewModelScope.launch {
            delay(800) // Wait for user to stop typing
            analyzeCodeForBugs()
        }
    }
    
    fun setCurrentFileName(name: String) {
        _currentFileName.value = name
    }
    
    fun newFile() {
        _code.value = DEFAULT_CODE
        _currentFileName.value = "untitled.py"
        _isModified.value = false
        _bugPredictions.value = emptyList()
    }
    
    fun saveCurrentFile() {
        viewModelScope.launch {
            userProgress.saveScript(_currentFileName.value, _code.value)
            _isModified.value = false
            loadSavedScripts()
        }
    }
    
    fun loadFile(name: String) {
        val scripts = userProgress.getScripts()
        scripts[name]?.let { entry ->
            _code.value = entry.content
            _currentFileName.value = name
            _isModified.value = false
        }
    }
    
    fun deleteFile(name: String) {
        viewModelScope.launch {
            userProgress.deleteScript(name)
            loadSavedScripts()
        }
    }
    
    private fun loadSavedScripts() {
        (savedScripts as MutableStateFlow).value = userProgress.getScripts()
    }
    
    // Code execution
    fun runCode() {
        viewModelScope.launch {
            userProgress.incrementCodeRuns()
            _lastError.value = null
            
            val result = pythonRuntime.executeCode(_code.value) { line ->
                // Real-time line analysis could go here
            }
            
            result.onFailure { error ->
                _lastError.value = aiEngine.explainError(
                    error.message ?: "Unknown error",
                    _code.value
                )
            }
        }
    }
    
    fun stopCode() {
        pythonRuntime.stopExecution()
    }
    
    fun clearConsole() {
        pythonRuntime.clearConsole()
    }
    
    // AI operations
    private suspend fun analyzeCodeForBugs() {
        _isAiProcessing.value = true
        val predictions = aiEngine.analyzeForBugs(_code.value)
        _bugPredictions.value = predictions
        _isAiProcessing.value = false
    }
    
    fun getCodeExplanation() {
        aiJob?.cancel()
        aiJob = viewModelScope.launch {
            _isAiProcessing.value = true
            _aiResponse.value = "Analyzing your code..."
            
            _aiResponse.value = aiEngine.explainCode(_code.value)
            _isAiProcessing.value = false
        }
    }
    
    fun askAi(question: String) {
        aiJob?.cancel()
        aiJob = viewModelScope.launch {
            _isAiProcessing.value = true
            _aiResponse.value = "Thinking..."
            
            val response = aiEngine.askTutor(question, _code.value)
            _aiResponse.value = response
            _isAiProcessing.value = false
        }
    }
    
    fun optimizeCode() {
        aiJob?.cancel()
        aiJob = viewModelScope.launch {
            _isAiProcessing.value = true
            _aiResponse.value = "Analyzing for optimizations..."
            
            val response = aiEngine.optimizeCode(_code.value)
            _aiResponse.value = response
            _isAiProcessing.value = false
        }
    }
    
    fun voiceToCode(spokenText: String) {
        viewModelScope.launch {
            _isAiProcessing.value = true
            val generatedCode = aiEngine.voiceToCode(spokenText)
            _code.value = _code.value + "\n" + generatedCode
            _isAiProcessing.value = false
        }
    }
    
    fun fixCodeAtLine(lineNumber: Int) {
        val predictions = _bugPredictions.value.filter { it.line == lineNumber }
        if (predictions.isNotEmpty()) {
            val fix = predictions.first()
            _aiResponse.value = """🐛 Issue on line ${fix.line}: ${fix.message}
                |
                |💡 Suggested fix:
                |${fix.fixSuggestion}
                |
                |⚠️ Severity: ${fix.severity}""".trimMargin()
        }
    }
    
    // API Key Management
    fun saveApiKey(apiKey: String) {
        viewModelScope.launch {
            _isAiProcessing.value = true
            aiEngine.saveApiKey(apiKey)
            checkApiKeyStatus()
            _isAiProcessing.value = false
        }
    }
    
    private fun checkApiKeyStatus() {
        viewModelScope.launch {
            _apiKeyStatus.value = if (aiEngine.isAiAvailable) {
                ApiKeyStatus.CONFIGURED
            } else {
                ApiKeyStatus.NOT_CONFIGURED
            }
        }
    }
    
    // Learning mode
    fun loadLesson(type: LessonType, level: DifficultyLevel) {
        viewModelScope.launch {
            _isAiProcessing.value = true
            val lesson = aiEngine.generateLesson(type, level)
            _currentLesson.value = lesson
            _lessonCode.value = lesson.code
            _isAiProcessing.value = false
        }
    }
    
    fun updateLessonCode(newCode: String) {
        _lessonCode.value = newCode
    }
    
    fun checkLessonSolution() {
        val lesson = _currentLesson.value ?: return
        viewModelScope.launch {
            _isAiProcessing.value = true
            
            // Use AI to check if the solution is correct
            val prompt = """Check if this code solves the challenge:
                |
                |Challenge: ${lesson.challenge}
                |
                |User's code:
                |```python
                |${_lessonCode.value}
                |```
                |
                |Expected solution:
                |```python
                |${lesson.solution}
                |```
                |
                |Is this correct? Provide brief feedback.""".trimMargin()
            
            val feedback = aiEngine.askTutor(prompt, "")
            
            if (feedback.contains("correct", ignoreCase = true) || 
                feedback.contains("✓", ignoreCase = true) ||
                _lessonCode.value.trim().equals(lesson.solution.trim(), ignoreCase = true)) {
                _aiResponse.value = "✓ $feedback"
                userProgress.markLessonCompleted(lesson.title)
            } else {
                _aiResponse.value = feedback
            }
            
            _isAiProcessing.value = false
        }
    }
    
    // Pip operations
    fun installPackage(packageName: String) {
        viewModelScope.launch {
            pythonRuntime.installPackage(packageName)
        }
    }
    
    fun uninstallPackage(packageName: String) {
        viewModelScope.launch {
            pythonRuntime.uninstallPackage(packageName)
        }
    }
    
    fun updatePipSearch(query: String) {
        _pipSearchQuery.value = query
    }
    
    // Tab selection
    fun selectTab(tab: Tab) {
        _selectedTab.value = tab
    }
    
    // Clear AI response
    fun clearAiResponse() {
        _aiResponse.value = ""
    }
    
    override fun onCleared() {
        super.onCleared()
        aiJob?.cancel()
        analysisJob?.cancel()
    }
    
    companion object {
        val DEFAULT_CODE = """
            # Welcome to PyGenius AI!
            # This is your Python coding environment with AI assistance
            
            def greet(name):
                return f"Hello, {name}!"
            
            # Try running this code
            message = greet("Python Developer")
            print(message)
            
            # Tap the AI Tutor tab for intelligent help!
            # AI features are ready to use!
        """.trimIndent()
    }
}

enum class Tab {
    EDITOR, CONSOLE, AI_TUTOR, LEARNING, PACKAGES, SETTINGS
}

enum class ApiKeyStatus {
    UNKNOWN, NOT_CONFIGURED, CONFIGURED
}
