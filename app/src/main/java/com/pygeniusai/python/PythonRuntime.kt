package com.pygeniusai.python

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Python Runtime Manager
 * Currently a mock implementation - Chaquopy integration pending
 */
class PythonRuntime private constructor() {
    
    private lateinit var context: Context
    
    private val _consoleOutput = MutableStateFlow<List<ConsoleLine>>(emptyList())
    val consoleOutput: StateFlow<List<ConsoleLine>> = _consoleOutput.asStateFlow()
    
    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()
    
    private val _installedPackages = MutableStateFlow<List<PyPackage>>(emptyList())
    val installedPackages: StateFlow<List<PyPackage>> = _installedPackages.asStateFlow()
    
    fun initialize(context: Context) {
        this.context = context.applicationContext
        // Mock packages
        _installedPackages.value = listOf(
            PyPackage("numpy", "1.24.3"),
            PyPackage("matplotlib", "3.7.1"),
            PyPackage("pandas", "2.0.3")
        )
    }
    
    suspend fun executeCode(code: String, onLineExecuted: ((Int) -> Unit)? = null): Result<String> = 
        withContext(Dispatchers.IO) {
            try {
                _isRunning.value = true
                _consoleOutput.value = emptyList()
                
                // Mock execution - simulate output
                addConsoleLine(ConsoleLine(">>> Running script...", LineType.PROGRESS))
                
                // Simple mock output based on code content
                when {
                    code.contains("print") -> {
                        val printMatch = "print\\s*\\(([^)]+)\\)".toRegex().find(code)
                        if (printMatch != null) {
                            val content = printMatch.groupValues[1].trim()
                            addConsoleLine(ConsoleLine(content.removeSurrounding("\"").removeSurrounding("'"), LineType.OUTPUT))
                        }
                    }
                    code.contains("import") -> {
                        addConsoleLine(ConsoleLine("Modules imported successfully", LineType.OUTPUT))
                    }
                }
                
                _isRunning.value = false
                Result.success("Execution completed")
                
            } catch (e: Exception) {
                _isRunning.value = false
                addConsoleLine(ConsoleLine(e.message ?: "Unknown error", LineType.ERROR))
                Result.failure(e)
            }
        }
    
    fun stopExecution() {
        _isRunning.value = false
    }
    
    suspend fun installPackage(packageName: String): Result<String> = withContext(Dispatchers.IO) {
        addConsoleLine(ConsoleLine("Installing $packageName...", LineType.PROGRESS))
        // Mock installation
        _installedPackages.value = _installedPackages.value + PyPackage(packageName, "1.0.0")
        Result.success("Installed $packageName")
    }
    
    suspend fun uninstallPackage(packageName: String): Result<String> = withContext(Dispatchers.IO) {
        _installedPackages.value = _installedPackages.value.filter { it.name != packageName }
        Result.success("Uninstalled $packageName")
    }
    
    private fun addConsoleLine(line: ConsoleLine) {
        _consoleOutput.value = _consoleOutput.value + line
    }
    
    fun clearConsole() {
        _consoleOutput.value = emptyList()
    }
    
    companion object {
        @Volatile
        private var instance: PythonRuntime? = null
        
        fun getInstance(): PythonRuntime {
            return instance ?: synchronized(this) {
                instance ?: PythonRuntime().also { instance = it }
            }
        }
    }
}

data class ConsoleLine(
    val text: String,
    val type: LineType
)

enum class LineType {
    OUTPUT, ERROR, PLOT, PROGRESS, AI_SUGGESTION
}

data class PyPackage(
    val name: String,
    val version: String
)
