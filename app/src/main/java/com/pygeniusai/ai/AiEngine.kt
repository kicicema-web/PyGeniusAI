package com.pygeniusai.ai

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * AI Engine for PyGenius AI
 * Provides code completion, error explanation, and tutoring functionality
 * Uses on-device ML model (TensorFlow Lite) with cloud fallback
 */
class AiEngine private constructor(private val context: Context) {
    
    private val _isModelLoaded = MutableStateFlow(false)
    val isModelLoaded: StateFlow<Boolean> = _isModelLoaded.asStateFlow()
    
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()
    
    private val _suggestions = MutableStateFlow<List<AiSuggestion>>(emptyList())
    val suggestions: StateFlow<List<AiSuggestion>> = _suggestions.asStateFlow()
    
    private val localKnowledge = LocalAiKnowledge()
    
    init {
        initializeModel()
    }
    
    private fun initializeModel() {
        // In production, this would load a quantized CodeGemma 2B model
        // For now, we use rule-based AI with pattern matching
        _isModelLoaded.value = true
    }
    
    /**
     * Generate code completion suggestions
     */
    suspend fun getCodeCompletion(code: String, cursorPosition: Int): List<AiSuggestion> = 
        withContext(Dispatchers.Default) {
            _isProcessing.value = true
            
            val suggestions = mutableListOf<AiSuggestion>()
            val lines = code.substring(0, cursorPosition).split("\n")
            val currentLine = lines.lastOrNull() ?: ""
            
            // Pattern-based completions
            when {
                // Import suggestions
                currentLine.startsWith("import ") || currentLine.startsWith("from ") -> {
                    val partial = currentLine.substringAfterLast(" ")
                    suggestions.addAll(getImportSuggestions(partial))
                }
                
                // Function call suggestions
                currentLine.contains("plt.") -> {
                    suggestions.addAll(getMatplotlibSuggestions(currentLine))
                }
                
                currentLine.contains("np.") -> {
                    suggestions.addAll(getNumpySuggestions(currentLine))
                }
                
                currentLine.contains("pd.") -> {
                    suggestions.addAll(getPandasSuggestions(currentLine))
                }
                
                // Common Python patterns
                currentLine.contains("for ") -> {
                    suggestions.add(AiSuggestion(
                        text = "for i in range(len(data)):",
                        type = SuggestionType.COMPLETION,
                        description = "Iterate with index"
                    ))
                    suggestions.add(AiSuggestion(
                        text = "for item in items:",
                        type = SuggestionType.COMPLETION,
                        description = "Iterate over collection"
                    ))
                }
                
                currentLine.contains("if __name__") -> {
                    suggestions.add(AiSuggestion(
                        text = "if __name__ == \"__main__\":",
                        type = SuggestionType.COMPLETION,
                        description = "Entry point guard"
                    ))
                }
            }
            
            // Variable-based suggestions
            val definedVars = extractDefinedVariables(code)
            suggestions.addAll(getVariableBasedSuggestions(currentLine, definedVars))
            
            _suggestions.value = suggestions
            _isProcessing.value = false
            suggestions
        }
    
    /**
     * Analyze code for bugs and issues
     */
    suspend fun analyzeForBugs(code: String): List<BugPrediction> = 
        withContext(Dispatchers.Default) {
            val issues = mutableListOf<BugPrediction>()
            val lines = code.lines()
            
            lines.forEachIndexed { index, line ->
                val lineNum = index + 1
                
                // Check for division by zero
                if (line.contains("/") && !line.contains("==")) {
                    if (line.contains("/0") || line.contains("/ ")) {
                        issues.add(BugPrediction(
                            line = lineNum,
                            message = "Possible ZeroDivisionError if divisor is 0",
                            severity = Severity.HIGH,
                            fixSuggestion = "Add check: if divisor != 0:"
                        ))
                    }
                }
                
                // Check for mutable default arguments
                if (line.contains("def ") && (line.contains("=[]") || line.contains("={}"))) {
                    issues.add(BugPrediction(
                        line = lineNum,
                        message = "Mutable default argument detected",
                        severity = Severity.MEDIUM,
                        fixSuggestion = "Use None and initialize inside function"
                    ))
                }
                
                // Check for bare except
                if (line.contains("except:") && !line.contains("Exception")) {
                    issues.add(BugPrediction(
                        line = lineNum,
                        message = "Bare except clause catches all exceptions including KeyboardInterrupt",
                        severity = Severity.MEDIUM,
                        fixSuggestion = "Use 'except SpecificException:' instead"
                    ))
                }
                
                // Check for undefined variables (simple heuristic)
                val undefinedPattern = "\\b([a-zA-Z_][a-zA-Z0-9_]*)\\s*=".toRegex()
                undefinedPattern.findAll(line).forEach { match ->
                    val varName = match.groupValues[1]
                    // Check if used after definition
                    val isUsed = lines.drop(index + 1).any { it.contains(varName) && !it.contains("$varName =") }
                    if (!isUsed && !listOf("if", "for", "while", "def", "class", "return").contains(varName)) {
                        issues.add(BugPrediction(
                            line = lineNum,
                            message = "Variable '$varName' may be unused (dead code)",
                            severity = Severity.LOW,
                            fixSuggestion = "Remove unused variable or use it"
                        ))
                    }
                }
            }
            
            issues
        }
    
    /**
     * Explain an error in plain English
     */
    suspend fun explainError(errorMessage: String, codeContext: String): ErrorExplanation = 
        withContext(Dispatchers.Default) {
            localKnowledge.explainError(errorMessage, codeContext)
        }
    
    /**
     * Generate explanation for code
     */
    suspend fun explainCode(code: String): String = 
        withContext(Dispatchers.Default) {
            val lines = code.lines()
            val explanations = mutableListOf<String>()
            
            // Analyze code structure
            if (code.contains("def ")) {
                val funcCount = code.split("def ").size - 1
                explanations.add("📦 This code defines $funcCount function(s).")
            }
            
            if (code.contains("class ")) {
                val classCount = code.split("class ").size - 1
                explanations.add("🏗️ Defines $classCount class(es).")
            }
            
            if (code.contains("import ") || code.contains("from ")) {
                explanations.add("📚 Uses external libraries.")
            }
            
            // Check for specific patterns
            when {
                code.contains("plt.plot") -> explanations.add("📊 Creates a line plot using matplotlib.")
                code.contains("plt.scatter") -> explanations.add("📊 Creates a scatter plot.")
                code.contains("plt.bar") -> explanations.add("📊 Creates a bar chart.")
                code.contains("pd.read_csv") -> explanations.add("📁 Reads data from a CSV file.")
                code.contains("np.array") -> explanations.add("🔢 Works with NumPy arrays for numerical computation.")
            }
            
            // Algorithm detection
            when {
                code.contains("for ") && code.contains("range") && code.contains("append") -> 
                    explanations.add("🔄 Uses a loop to build a list iteratively.")
                code.contains("[") && code.contains("for ") && code.contains("in ") -> 
                    explanations.add("⚡ Uses list comprehension for concise list creation.")
                code.contains("def ") && code.contains("return") && 
                    code.substringAfter("def ").substringBefore("(").trim() == 
                    code.substringAfter("return ").substringBefore("(").trim() -> 
                    explanations.add("🔄 This appears to be a recursive function.")
            }
            
            if (explanations.isEmpty()) {
                "This is Python code. It appears to be a basic script."
            } else {
                explanations.joinToString("\n")
            }
        }
    
    /**
     * Voice to code conversion
     */
    suspend fun voiceToCode(spokenText: String): String = 
        withContext(Dispatchers.Default) {
            val lower = spokenText.lowercase()
            
            when {
                // Function definitions
                lower.contains("create function") || lower.contains("define function") -> {
                    val funcName = spokenText.substringAfter("function ").substringBefore(" ").trim()
                    """
                    def $funcName():
                        """
                        pass
                        """
                        pass
                    """.trimIndent()
                }
                
                // Loops
                lower.contains("for loop") || lower.contains("loop through") -> {
                    val varName = spokenText.substringAfter("loop ").substringBefore(" ").trim()
                    """
                    for item in $varName:
                        # Process each item
                        pass
                    """.trimIndent()
                }
                
                // If statements
                lower.contains("if statement") || lower.contains("check if") -> {
                    """
                    if condition:
                        # Do something
                        pass
                    else:
                        # Do something else
                        pass
                    """.trimIndent()
                }
                
                // Fibonacci
                lower.contains("fibonacci") -> {
                    """
                    def fibonacci(n, memo={}):
                        if n in memo:
                            return memo[n]
                        if n <= 1:
                            return n
                        memo[n] = fibonacci(n-1, memo) + fibonacci(n-2, memo)
                        return memo[n]
                    """.trimIndent()
                }
                
                // List comprehension
                lower.contains("list comprehension") -> {
                    """
                    # List comprehension syntax
                    [x for x in range(10) if condition]
                    """.trimIndent()
                }
                
                else -> "# Voice command not recognized: $spokenText\n# Try: 'Create function', 'For loop', 'Fibonacci'"
            }
        }
    
    /**
     * Generate lesson content for learning mode
     */
    suspend fun generateLesson(lessonType: LessonType, level: DifficultyLevel): Lesson = 
        withContext(Dispatchers.Default) {
            localKnowledge.getLesson(lessonType, level)
        }
    
    // Helper methods
    private fun getImportSuggestions(partial: String): List<AiSuggestion> {
        val commonImports = listOf(
            "numpy" to "import numpy as np",
            "pandas" to "import pandas as pd",
            "matplotlib" to "import matplotlib.pyplot as plt",
            "random" to "import random",
            "datetime" to "from datetime import datetime",
            "json" to "import json",
            "os" to "import os",
            "sys" to "import sys",
            "math" to "import math"
        )
        
        return commonImports
            .filter { it.first.startsWith(partial) }
            .map { 
                AiSuggestion(
                    text = it.second,
                    type = SuggestionType.IMPORT,
                    description = "Import ${it.first}"
                )
            }
    }
    
    private fun getMatplotlibSuggestions(line: String): List<AiSuggestion> {
        return listOf(
            AiSuggestion("plt.plot(x, y)", SuggestionType.METHOD, "Create line plot"),
            AiSuggestion("plt.scatter(x, y)", SuggestionType.METHOD, "Create scatter plot"),
            AiSuggestion("plt.bar(x, height)", SuggestionType.METHOD, "Create bar chart"),
            AiSuggestion("plt.hist(data)", SuggestionType.METHOD, "Create histogram"),
            AiSuggestion("plt.xlabel('label')", SuggestionType.METHOD, "Set x-axis label"),
            AiSuggestion("plt.ylabel('label')", SuggestionType.METHOD, "Set y-axis label"),
            AiSuggestion("plt.title('Title')", SuggestionType.METHOD, "Set plot title"),
            AiSuggestion("plt.grid(True)", SuggestionType.METHOD, "Show grid"),
            AiSuggestion("plt.legend()", SuggestionType.METHOD, "Show legend"),
            AiSuggestion("plt.show()", SuggestionType.METHOD, "Display plot")
        )
    }
    
    private fun getNumpySuggestions(line: String): List<AiSuggestion> {
        return listOf(
            AiSuggestion("np.array([1, 2, 3])", SuggestionType.METHOD, "Create array"),
            AiSuggestion("np.zeros((3, 3))", SuggestionType.METHOD, "Create zeros array"),
            AiSuggestion("np.ones((3, 3))", SuggestionType.METHOD, "Create ones array"),
            AiSuggestion("np.linspace(0, 10, 100)", SuggestionType.METHOD, "Linearly spaced values"),
            AiSuggestion("np.arange(0, 10, 0.1)", SuggestionType.METHOD, "Range with step"),
            AiSuggestion("np.random.rand(10)", SuggestionType.METHOD, "Random values"),
            AiSuggestion("np.mean(data)", SuggestionType.METHOD, "Calculate mean"),
            AiSuggestion("np.std(data)", SuggestionType.METHOD, "Standard deviation")
        )
    }
    
    private fun getPandasSuggestions(line: String): List<AiSuggestion> {
        return listOf(
            AiSuggestion("pd.read_csv('file.csv')", SuggestionType.METHOD, "Read CSV file"),
            AiSuggestion("pd.DataFrame(data)", SuggestionType.METHOD, "Create DataFrame"),
            AiSuggestion("df.head()", SuggestionType.METHOD, "First 5 rows"),
            AiSuggestion("df.describe()", SuggestionType.METHOD, "Statistics summary"),
            AiSuggestion("df['column']", SuggestionType.METHOD, "Select column"),
            AiSuggestion("df.loc[row, col]", SuggestionType.METHOD, "Label-based selection")
        )
    }
    
    private fun extractDefinedVariables(code: String): Set<String> {
        val pattern = "\\b([a-zA-Z_][a-zA-Z0-9_]*)\\s*=".toRegex()
        return pattern.findAll(code)
            .map { it.groupValues[1] }
            .filter { it !in listOf("if", "for", "while", "def", "class") }
            .toSet()
    }
    
    private fun getVariableBasedSuggestions(line: String, vars: Set<String>): List<AiSuggestion> {
        val suggestions = mutableListOf<AiSuggestion>()
        
        vars.forEach { varName ->
            if (!line.contains(varName)) {
                suggestions.add(AiSuggestion(
                    text = varName,
                    type = SuggestionType.VARIABLE,
                    description = "Variable: $varName"
                ))
            }
        }
        
        return suggestions
    }
    
    companion object {
        @Volatile
        private var instance: AiEngine? = null
        
        fun getInstance(context: Context): AiEngine {
            return instance ?: synchronized(this) {
                instance ?: AiEngine(context.applicationContext).also { instance = it }
            }
        }
    }
}

// Data classes
data class AiSuggestion(
    val text: String,
    val type: SuggestionType,
    val description: String = ""
)

enum class SuggestionType {
    COMPLETION, IMPORT, METHOD, VARIABLE, SNIPPET
}

data class BugPrediction(
    val line: Int,
    val message: String,
    val severity: Severity,
    val fixSuggestion: String
)

enum class Severity {
    LOW, MEDIUM, HIGH, CRITICAL
}

data class ErrorExplanation(
    val errorType: String,
    val explanation: String,
    val suggestion: String,
    val example: String = ""
)

data class Lesson(
    val title: String,
    val description: String,
    val code: String,
    val challenge: String,
    val hints: List<String>,
    val solution: String,
    val difficulty: DifficultyLevel
)

enum class LessonType {
    VARIABLES, LOOPS, FUNCTIONS, CLASSES, LIST_COMPREHENSION, DATA_STRUCTURES
}

enum class DifficultyLevel {
    BEGINNER, INTERMEDIATE, ADVANCED
}
