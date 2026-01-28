package com.pygeniusai.ai

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/**
 * AI Engine for PyGenius AI
 * Provides code completion, error explanation, and tutoring functionality
 * Uses OpenRouter AI API with local fallback for offline functionality
 */
class AiEngine private constructor(private val context: Context) {
    
    private val openRouterService = OpenRouterService.getInstance(context)
    private val localKnowledge = LocalAiKnowledge()
    
    private val _isModelLoaded = MutableStateFlow(false)
    val isModelLoaded: StateFlow<Boolean> = _isModelLoaded.asStateFlow()
    
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()
    
    private val _suggestions = MutableStateFlow<List<AiSuggestion>>(emptyList())
    val suggestions: StateFlow<List<AiSuggestion>> = _suggestions.asStateFlow()
    
    // Track if OpenRouter is available (has API key)
    val isAiAvailable: Boolean
        get() = openRouterService.hasApiKey
    
    init {
        _isModelLoaded.value = true
    }
    
    /**
     * API key is hardcoded - no need to save
     */
    suspend fun saveApiKey(apiKey: String) {
        // API key is hardcoded in ApiKeys.kt
    }
    
    /**
     * Generate code completion suggestions
     * Uses local pattern matching (fast) for immediate suggestions
     * DeepSeek could be used for more intelligent completions in the future
     */
    suspend fun getCodeCompletion(code: String, cursorPosition: Int): List<AiSuggestion> = 
        withContext(Dispatchers.Default) {
            _isProcessing.value = true
            
            val suggestions = mutableListOf<AiSuggestion>()
            val lines = code.substring(0, cursorPosition).split("\n")
            val currentLine = lines.lastOrNull() ?: ""
            
            // Pattern-based completions (fast, works offline)
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
     * Uses DeepSeek AI if available, otherwise local analysis
     */
    suspend fun analyzeForBugs(code: String): List<BugPrediction> = 
        withContext(Dispatchers.Default) {
            _isProcessing.value = true
            
            val predictions = try {
                if (isAiAvailable) {
                    // Use OpenRouter for intelligent analysis
                    openRouterService.analyzeForBugs(code)
                } else {
                    // Fallback to local analysis
                    analyzeForBugsLocal(code)
                }
            } catch (e: Exception) {
                // Fallback on error
                analyzeForBugsLocal(code)
            }
            
            _isProcessing.value = false
            predictions
        }
    
    /**
     * Local bug analysis (fallback when OpenRouter is unavailable)
     */
    private fun analyzeForBugsLocal(code: String): List<BugPrediction> {
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
        
        return issues
    }
    
    /**
     * Explain an error in plain English
     * Uses OpenRouter AI if available, otherwise local knowledge
     */
    suspend fun explainError(errorMessage: String, codeContext: String): ErrorExplanation = 
        withContext(Dispatchers.Default) {
            try {
                if (isAiAvailable) {
                    openRouterService.explainError(errorMessage, codeContext)
                } else {
                    localKnowledge.explainError(errorMessage, codeContext)
                }
            } catch (e: Exception) {
                localKnowledge.explainError(errorMessage, codeContext)
            }
        }
    
    /**
     * Generate explanation for code
     * Uses OpenRouter AI if available, otherwise local analysis
     */
    suspend fun explainCode(code: String): String = 
        withContext(Dispatchers.Default) {
            _isProcessing.value = true
            
            val explanation = try {
                if (isAiAvailable) {
                    openRouterService.explainCode(code)
                } else {
                    explainCodeLocal(code)
                }
            } catch (e: Exception) {
                explainCodeLocal(code)
            }
            
            _isProcessing.value = false
            explanation
        }
    
    /**
     * Local code explanation (fallback)
     */
    private fun explainCodeLocal(code: String): String {
        val lines = code.lines()
        val explanations = mutableListOf<String>()
        
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
        
        when {
            code.contains("plt.plot") -> explanations.add("📊 Creates a line plot using matplotlib.")
            code.contains("plt.scatter") -> explanations.add("📊 Creates a scatter plot.")
            code.contains("plt.bar") -> explanations.add("📊 Creates a bar chart.")
            code.contains("pd.read_csv") -> explanations.add("📁 Reads data from a CSV file.")
            code.contains("np.array") -> explanations.add("🔢 Works with NumPy arrays for numerical computation.")
        }
        
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
        
        return if (explanations.isEmpty()) {
            "This is Python code. It appears to be a basic script.\n\n" +
            "💡 Connect your DeepSeek API key for detailed AI explanations!"
        } else {
            explanations.joinToString("\n")
        }
    }
    
    /**
     * Ask the AI tutor a question
     * This is the NEW method for the AI Tutor chat functionality
     */
    suspend fun askTutor(question: String, codeContext: String = ""): String =
        withContext(Dispatchers.Default) {
            _isProcessing.value = true
            
            val response = try {
                if (isAiAvailable) {
                    openRouterService.askTutor(question, codeContext)
                } else {
                    """⚠️ AI service not available.
                        |
                        |Please check your internet connection.
                        |
                        |---
                        |
                        |Meanwhile, I can still help with basic pattern matching:
                        |
                        |${provideBasicResponse(question, codeContext)}""".trimMargin()
                }
            } catch (e: Exception) {
                "❌ Error: ${e.message}\n\nPlease check your internet connection and API key."
            }
            
            _isProcessing.value = false
            response
        }
    
    /**
     * Basic response for when OpenRouter is not available
     */
    private fun provideBasicResponse(question: String, codeContext: String): String {
        return when {
            question.contains("hello", ignoreCase = true) || 
            question.contains("hi ", ignoreCase = true) ->
                "Hello! I'm PyGenius AI. I can help you learn Python!"
            
            question.contains("error", ignoreCase = true) || 
            question.contains("fix", ignoreCase = true) ->
                "I can help fix errors! Please make sure your code is in the editor, " +
                "and I'll analyze it when you run it."
            
            question.contains("explain", ignoreCase = true) ->
                if (codeContext.isNotBlank()) {
                    explainCodeLocal(codeContext)
                } else {
                    "Please write some code in the editor first, then I can explain it!"
                }
            
            question.contains("optimize", ignoreCase = true) ||
            question.contains("improve", ignoreCase = true) ->
                "To optimize code:\n" +
                "• Use list comprehensions instead of loops\n" +
                "• Use built-in functions like map(), filter()\n" +
                "• Avoid repeated calculations\n" +
                "• Use appropriate data structures"
            
            question.contains("help", ignoreCase = true) ->
                "I can help you with:\n" +
                "• Explaining Python code\n" +
                "• Fixing errors\n" +
                "• Learning Python concepts\n" +
                "• Code optimization tips\n\n" +
                "Connect your DeepSeek API key for AI-powered responses!"
            
            else ->
                "I'm your AI Python tutor! Ask me about:\n" +
                "• Code explanations\n" +
                "• Error fixing\n" +
                "• Python concepts\n\n" +
                "💡 Connect DeepSeek API key for more intelligent responses!"
        }
    }
    
    /**
     * Optimize code
     * Uses OpenRouter AI if available
     */
    suspend fun optimizeCode(code: String): String =
        withContext(Dispatchers.Default) {
            _isProcessing.value = true
            
            val response = try {
                if (isAiAvailable) {
                    openRouterService.optimizeCode(code)
                } else {
                    """⚠️ AI service not available.
                        |
                        |General optimization tips:
                        |• Use list comprehensions: [x*2 for x in items]
                        |• Use built-in functions: sum(), max(), min()
                        |• Use generators for large datasets
                        |• Avoid repeated calculations - cache results
                        |• Use sets for membership testing (O(1))
                        |
                        |Connect to the internet for code-specific optimization!""".trimMargin()
                }
            } catch (e: Exception) {
                "Error optimizing code: ${e.message}"
            }
            
            _isProcessing.value = false
            response
        }
    
    /**
     * Voice to code conversion
     * Uses OpenRouter AI if available, otherwise local patterns
     */
    suspend fun voiceToCode(spokenText: String): String = 
        withContext(Dispatchers.Default) {
            _isProcessing.value = true
            
            val code = try {
                if (isAiAvailable) {
                    openRouterService.generateCode(spokenText)
                } else {
                    voiceToCodeLocal(spokenText)
                }
            } catch (e: Exception) {
                voiceToCodeLocal(spokenText)
            }
            
            _isProcessing.value = false
            code
        }
    
    /**
     * Local voice to code conversion
     */
    private fun voiceToCodeLocal(spokenText: String): String {
        val lower = spokenText.lowercase()
        
        return when {
            lower.contains("create function") || lower.contains("define function") -> {
                val funcName = spokenText.substringAfter("function ").substringBefore(" ").trim()
                    .takeIf { it.isNotBlank() } ?: "my_function"
                """
                def $funcName():
                    pass
                """.trimIndent()
            }
            
            lower.contains("for loop") || lower.contains("loop through") -> {
                val varName = spokenText.substringAfter("loop ").substringBefore(" ").trim()
                    .takeIf { it.isNotBlank() } ?: "items"
                """
                for item in $varName:
                    # Process each item
                    pass
                """.trimIndent()
            }
            
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
            
            lower.contains("list comprehension") -> {
                """
                # List comprehension syntax
                [x for x in range(10) if condition]
                """.trimIndent()
            }
            
            else -> """# Voice command: "$spokenText"
                |# Try: 'Create function hello', 'For loop items', 'Fibonacci'
                |# Or connect to the internet for AI code generation!
                |pass""".trimMargin()
        }
    }
    
    /**
     * Generate lesson content for learning mode
     * Uses OpenRouter AI if available, otherwise local knowledge
     */
    suspend fun generateLesson(lessonType: LessonType, level: DifficultyLevel): Lesson = 
        withContext(Dispatchers.Default) {
            _isProcessing.value = true
            
            val lesson = try {
                if (isAiAvailable) {
                    openRouterService.generateLesson(lessonType, level)
                } else {
                    localKnowledge.getLesson(lessonType, level)
                }
            } catch (e: Exception) {
                localKnowledge.getLesson(lessonType, level)
            }
            
            _isProcessing.value = false
            lesson
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
