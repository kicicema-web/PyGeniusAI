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
            updateAiSuggestions()
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
            
            val result = pythonRuntime.executeCode(_code.value) { line, type ->
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
        val predictions = aiEngine.analyzeForBugs(_code.value)
        _bugPredictions.value = predictions
    }
    
    private suspend fun updateAiSuggestions() {
        // Get contextual suggestions based on cursor position
        // For now, we use simple pattern matching
    }
    
    fun getCodeExplanation() {
        viewModelScope.launch {
            _isAiProcessing.value = true
            _aiResponse.value = aiEngine.explainCode(_code.value)
            _isAiProcessing.value = false
        }
    }
    
    fun askAi(question: String) {
        viewModelScope.launch {
            _isAiProcessing.value = true
            _aiResponse.value = "Thinking..."
            delay(500) // Simulate processing
            
            _aiResponse.value = when {
                question.contains("error") || question.contains("fix") -> {
                    "I can help you fix errors! Let me analyze your code..."
                }
                question.contains("explain") -> {
                    aiEngine.explainCode(_code.value)
                }
                question.contains("optimize") -> {
                    "Your code looks good! Consider using list comprehensions for cleaner syntax."
                }
                else -> {
                    "I'm your AI Python tutor! I can:\n" +
                    "• Explain your code\n" +
                    "• Help fix errors\n" +
                    "• Suggest improvements\n" +
                    "• Guide you through lessons"
                }
            }
            _isAiProcessing.value = false
        }
    }
    
    fun voiceToCode(spokenText: String) {
        viewModelScope.launch {
            val generatedCode = aiEngine.voiceToCode(spokenText)
            _code.value = _code.value + "\n" + generatedCode
        }
    }
    
    fun fixCodeAtLine(lineNumber: Int) {
        val predictions = _bugPredictions.value.filter { it.line == lineNumber }
        if (predictions.isNotEmpty()) {
            // Apply the first fix suggestion
            val fix = predictions.first().fixSuggestion
            _aiResponse.value = "Suggested fix: $fix"
        }
    }
    
    // Learning mode
    fun loadLesson(type: LessonType, level: DifficultyLevel) {
        viewModelScope.launch {
            val lesson = aiEngine.generateLesson(type, level)
            _currentLesson.value = lesson
            _lessonCode.value = lesson.code
        }
    }
    
    fun updateLessonCode(newCode: String) {
        _lessonCode.value = newCode
    }
    
    fun checkLessonSolution() {
        val lesson = _currentLesson.value ?: return
        // Simple check - in production, use more sophisticated validation
        if (_lessonCode.value.trim().equals(lesson.solution.trim(), ignoreCase = true)) {
            _aiResponse.value = "✓ Correct! Well done!"
            userProgress.markLessonCompleted(lesson.title)
        } else {
            _aiResponse.value = "Keep trying! Check the hints if you need help."
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
    
    override fun onCleared() {
        super.onCleared()
        aiJob?.cancel()
        analysisJob?.cancel()
    }
    
    companion object {
        val DEFAULT_CODE = """
            # Welcome to PyGenius AI!
            # This is your Python coding environment
            
            def greet(name):
                return f"Hello, {name}!"
            
            # Try running this code
            message = greet("Python Developer")
            print(message)
            
            # Tap the AI Tutor tab for help!
        """.trimIndent()
    }
}

enum class Tab {
    EDITOR, CONSOLE, AI_TUTOR, LEARNING, PACKAGES
}
