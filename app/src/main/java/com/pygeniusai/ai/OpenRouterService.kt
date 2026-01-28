package com.pygeniusai.ai

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * OpenRouter AI Service
 * Provides real AI capabilities using OpenRouter API (supports multiple models)
 */
class OpenRouterService private constructor(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // Hardcoded API key - not stored in user preferences
    private val apiKey = ApiKeys.OPENROUTER_API_KEY

    val hasApiKey: Boolean = apiKey.isNotBlank()

    /**
     * Ask the AI tutor a question about Python
     */
    suspend fun askTutor(question: String, codeContext: String = ""): String =
        withContext(Dispatchers.IO) {
            val systemPrompt = """You are PyGenius AI, a helpful Python programming tutor. 
            |You help users learn Python, fix errors, and write better code.
            |Be concise but thorough. Provide code examples when helpful.
            |If there's a code context, analyze it in your response.""".trimMargin()

            val userPrompt = buildString {
                if (codeContext.isNotBlank()) {
                    appendLine("Here's my current code:")
                    appendLine("```python")
                    appendLine(codeContext)
                    appendLine("```")
                    appendLine()
                }
                appendLine(question)
            }

            callOpenRouterApi(systemPrompt, userPrompt)
        }

    /**
     * Explain Python code
     */
    suspend fun explainCode(code: String): String =
        withContext(Dispatchers.IO) {
            val systemPrompt = """You are a Python code explainer. 
            |Explain the provided Python code clearly and concisely.
            |Break down:
            |1. What the code does overall
            |2. Key concepts used
            |3. Important functions/classes
            |4. Any potential issues or improvements
            |Use emoji icons to make it engaging.""".trimMargin()

            val userPrompt = """Please explain this Python code:
            |```python
            |$code
            |```""".trimMargin()

            callOpenRouterApi(systemPrompt, userPrompt)
        }

    /**
     * Explain an error with context
     */
    suspend fun explainError(errorMessage: String, codeContext: String): ErrorExplanation {
        val systemPrompt = """You are a Python error expert. Analyze the error and provide:
        |1. Error type (one of: SyntaxError, NameError, TypeError, ValueError, IndexError, KeyError, AttributeError, ImportError, RuntimeError, ZeroDivisionError, FileNotFoundError, Other)
        |2. A clear explanation in plain English
        |3. A specific suggestion to fix the error
        |4. A corrected code example if applicable
        |
        |Respond in this exact JSON format:
        |{
        |  "errorType": "ErrorName",
        |  "explanation": "explanation here",
        |  "suggestion": "fix suggestion here",
        |  "example": "corrected code here (optional)"
        |}""".trimMargin()

        val userPrompt = """Error message: $errorMessage
            |
            |Code context:
            |```python
            |$codeContext
            |```""".trimMargin()

        return try {
            val response = callOpenRouterApi(systemPrompt, userPrompt)
            parseErrorExplanation(response)
        } catch (e: Exception) {
            ErrorExplanation(
                errorType = "Unknown",
                explanation = "Failed to analyze error: ${e.message}",
                suggestion = "Please check your internet connection."
            )
        }
    }

    /**
     * Analyze code for bugs and issues
     */
    suspend fun analyzeForBugs(code: String): List<BugPrediction> =
        withContext(Dispatchers.IO) {
            val systemPrompt = """You are a Python code reviewer. Analyze the code for:
            |- Syntax errors
            |- Logic bugs
            |- Performance issues
            |- Security concerns
            |- Best practice violations
            |
            |For each issue found, provide:
            |1. Line number (or approximate)
            |2. Issue description
            |3. Severity (LOW, MEDIUM, HIGH, CRITICAL)
            |4. Fix suggestion
            |
            |Respond in this exact JSON format:
            |{
            |  "issues": [
            |    {
            |      "line": 5,
            |      "message": "description of issue",
            |      "severity": "HIGH",
            |      "fix": "suggested fix"
            |    }
            |  ]
            |}
            |If no issues found, return {"issues": []}""".trimMargin()

            val userPrompt = """Please analyze this Python code for bugs:
            |```python
            |$code
            |```""".trimMargin()

            try {
                val response = callOpenRouterApi(systemPrompt, userPrompt)
                parseBugPredictions(response)
            } catch (e: Exception) {
                emptyList()
            }
        }

    /**
     * Generate code from voice/text description
     */
    suspend fun generateCode(description: String): String =
        withContext(Dispatchers.IO) {
            val systemPrompt = """You are a Python code generator. 
            |Generate clean, well-commented Python code based on the user's description.
            |Use best practices and include error handling where appropriate.
            |Only return the code, no explanations unless requested.""".trimMargin()

            callOpenRouterApi(systemPrompt, description)
        }

    /**
     * Generate a lesson based on type and difficulty
     */
    suspend fun generateLesson(lessonType: LessonType, level: DifficultyLevel): Lesson =
        withContext(Dispatchers.IO) {
            val systemPrompt = """You are a Python curriculum designer. Create an interactive coding lesson.
            |Respond in this exact JSON format:
            |{
            |  "title": "Lesson Title",
            |  "description": "Lesson description",
            |  "code": "starter code with TODO comments",
            |  "challenge": "what the user needs to do",
            |  "hints": ["hint 1", "hint 2", "hint 3"],
            |  "solution": "complete working solution"
            |}""".trimMargin()

            val userPrompt = """Create a $level lesson about $lessonType in Python.
            |Make it engaging and practical with a real coding challenge.""".trimMargin()

            try {
                val response = callOpenRouterApi(systemPrompt, userPrompt)
                parseLesson(response, level)
            } catch (e: Exception) {
                // Fallback to local knowledge
                LocalAiKnowledge().getLesson(lessonType, level)
            }
        }

    /**
     * Optimize code
     */
    suspend fun optimizeCode(code: String): String =
        withContext(Dispatchers.IO) {
            val systemPrompt = """You are a Python optimization expert. 
            |Analyze the provided code and suggest optimizations for:
            |- Performance
            |- Readability
            |- Pythonic style
            |- Memory usage
            |
            |Provide the optimized code with comments explaining the changes.""".trimMargin()

            val userPrompt = """Please optimize this Python code:
            |```python
            |$code
            |```""".trimMargin()

            callOpenRouterApi(systemPrompt, userPrompt)
        }

    /**
     * Stream AI response for real-time typing effect
     */
    fun streamTutorResponse(question: String, codeContext: String = ""): Flow<String> = flow {
        if (apiKey.isBlank()) {
            emit("⚠️ API key not configured.")
            return@flow
        }

        val systemPrompt = """You are PyGenius AI, a helpful Python programming tutor. 
            |Be concise but thorough. Provide code examples when helpful.""".trimMargin()

        val userPrompt = buildString {
            if (codeContext.isNotBlank()) {
                appendLine("Here's my current code:")
                appendLine("```python")
                appendLine(codeContext)
                appendLine("```")
                appendLine()
            }
            appendLine(question)
        }

        val messages = JSONArray().apply {
            put(JSONObject().apply {
                put("role", "system")
                put("content", systemPrompt)
            })
            put(JSONObject().apply {
                put("role", "user")
                put("content", userPrompt)
            })
        }

        val jsonBody = JSONObject().apply {
            put("model", "openai/gpt-3.5-turbo")  // Using OpenRouter's unified API
            put("messages", messages)
            put("stream", true)
            put("temperature", 0.7)
            put("max_tokens", 2000)
        }

        val request = Request.Builder()
            .url("https://openrouter.ai/api/v1/chat/completions")
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .addHeader("HTTP-Referer", "https://pygenius.ai")  // Required by OpenRouter
            .addHeader("X-Title", "PyGenius AI")  // Required by OpenRouter
            .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    emit("Error: ${response.code} - ${response.message}")
                    return@use
                }

                response.body?.source()?.use { source ->
                    while (!source.exhausted()) {
                        val line = source.readUtf8Line() ?: continue
                        if (line.startsWith("data: ")) {
                            val data = line.substring(6)
                            if (data == "[DONE]") break

                            try {
                                val json = JSONObject(data)
                                val choices = json.getJSONArray("choices")
                                if (choices.length() > 0) {
                                    val delta = choices.getJSONObject(0).optJSONObject("delta")
                                    val content = delta?.optString("content", "")
                                    if (!content.isNullOrBlank()) {
                                        emit(content)
                                    }
                                }
                            } catch (e: Exception) {
                                // Skip malformed JSON
                            }
                        }
                    }
                }
            }
        } catch (e: IOException) {
            emit("\n\n❌ Network error: ${e.message}")
        }
    }.flowOn(Dispatchers.IO)

    // Private helper methods

    private suspend fun callOpenRouterApi(systemPrompt: String, userPrompt: String): String {
        if (apiKey.isBlank()) {
            return """⚠️ OpenRouter API key not configured.
                |
                |Please configure the API key to use AI features.""".trimMargin()
        }

        val messages = JSONArray().apply {
            put(JSONObject().apply {
                put("role", "system")
                put("content", systemPrompt)
            })
            put(JSONObject().apply {
                put("role", "user")
                put("content", userPrompt)
            })
        }

        val jsonBody = JSONObject().apply {
            put("model", "openai/gpt-3.5-turbo")  // Using GPT-3.5 via OpenRouter
            put("messages", messages)
            put("temperature", 0.7)
            put("max_tokens", 2000)
        }

        val request = Request.Builder()
            .url("https://openrouter.ai/api/v1/chat/completions")
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .addHeader("HTTP-Referer", "https://pygenius.ai")  // Required by OpenRouter
            .addHeader("X-Title", "PyGenius AI")  // Required by OpenRouter
            .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        return withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errorBody = response.body?.string()
                    throw IOException("API Error ${response.code}: $errorBody")
                }

                val responseBody = response.body?.string()
                    ?: throw IOException("Empty response")

                val json = JSONObject(responseBody)
                val choices = json.getJSONArray("choices")
                if (choices.length() > 0) {
                    choices.getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content")
                } else {
                    throw IOException("No response from AI")
                }
            }
        }
    }

    private fun parseErrorExplanation(response: String): ErrorExplanation {
        return try {
            // Try to parse as JSON
            val json = JSONObject(response)
            ErrorExplanation(
                errorType = json.optString("errorType", "Unknown"),
                explanation = json.optString("explanation", response),
                suggestion = json.optString("suggestion", ""),
                example = json.optString("example", "")
            )
        } catch (e: Exception) {
            // Fallback: parse from markdown
            ErrorExplanation(
                errorType = "Analysis",
                explanation = response.take(500),
                suggestion = "Review the analysis above.",
                example = ""
            )
        }
    }

    private fun parseBugPredictions(response: String): List<BugPrediction> {
        val predictions = mutableListOf<BugPrediction>()
        return try {
            val json = JSONObject(response)
            val issues = json.getJSONArray("issues")
            for (i in 0 until issues.length()) {
                val issue = issues.getJSONObject(i)
                predictions.add(BugPrediction(
                    line = issue.optInt("line", 0),
                    message = issue.optString("message", ""),
                    severity = Severity.valueOf(
                        issue.optString("severity", "LOW").uppercase()
                    ),
                    fixSuggestion = issue.optString("fix", "")
                ))
            }
            predictions
        } catch (e: Exception) {
            // If JSON parsing fails, return empty list
            emptyList()
        }
    }

    private fun parseLesson(response: String, level: DifficultyLevel): Lesson {
        return try {
            val json = JSONObject(response)
            Lesson(
                title = json.optString("title", "Python Lesson"),
                description = json.optString("description", ""),
                code = json.optString("code", ""),
                challenge = json.optString("challenge", ""),
                hints = json.optJSONArray("hints")?.let { arr ->
                    List(arr.length()) { arr.getString(it) }
                } ?: emptyList(),
                solution = json.optString("solution", ""),
                difficulty = level
            )
        } catch (e: Exception) {
            // Fallback lesson
            LocalAiKnowledge().getLesson(LessonType.VARIABLES, level)
        }
    }

    companion object {
        @Volatile
        private var instance: OpenRouterService? = null

        fun getInstance(context: Context): OpenRouterService {
            return instance ?: synchronized(this) {
                instance ?: OpenRouterService(context.applicationContext).also { instance = it }
            }
        }
    }
}
