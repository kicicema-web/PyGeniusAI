package com.pygeniusai.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Date

/**
 * Repository for storing and retrieving user progress
 */
class UserProgressRepository(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, 
        Context.MODE_PRIVATE
    )
    
    private val _codingStreak = MutableStateFlow(0)
    val codingStreak: StateFlow<Int> = _codingStreak.asStateFlow()
    
    private val _completedLessons = MutableStateFlow<Set<String>>(emptySet())
    val completedLessons: StateFlow<Set<String>> = _completedLessons.asStateFlow()
    
    private val _totalCodeRuns = MutableStateFlow(0)
    val totalCodeRuns: StateFlow<Int> = _totalCodeRuns.asStateFlow()
    
    init {
        loadProgress()
        checkAndUpdateStreak()
    }
    
    private fun loadProgress() {
        _codingStreak.value = prefs.getInt(KEY_STREAK, 0)
        _completedLessons.value = prefs.getStringSet(KEY_COMPLETED_LESSONS, emptySet()) ?: emptySet()
        _totalCodeRuns.value = prefs.getInt(KEY_TOTAL_RUNS, 0)
    }
    
    private fun checkAndUpdateStreak() {
        val lastActive = prefs.getLong(KEY_LAST_ACTIVE, 0)
        val today = System.currentTimeMillis() / (1000 * 60 * 60 * 24) // Days since epoch
        val lastDay = lastActive / (1000 * 60 * 60 * 24)
        
        when {
            today == lastDay -> {} // Already active today, no change
            today - lastDay == 1L -> incrementStreak() // Consecutive day
            today > lastDay -> resetStreak() // Streak broken
        }
        
        prefs.edit().putLong(KEY_LAST_ACTIVE, System.currentTimeMillis()).apply()
    }
    
    fun incrementStreak() {
        _codingStreak.value = _codingStreak.value + 1
        prefs.edit().putInt(KEY_STREAK, _codingStreak.value).apply()
    }
    
    fun resetStreak() {
        _codingStreak.value = 0
        prefs.edit().putInt(KEY_STREAK, 0).apply()
    }
    
    fun markLessonCompleted(lessonId: String) {
        val updated = _completedLessons.value + lessonId
        _completedLessons.value = updated
        prefs.edit().putStringSet(KEY_COMPLETED_LESSONS, updated).apply()
    }
    
    fun isLessonCompleted(lessonId: String): Boolean {
        return lessonId in _completedLessons.value
    }
    
    fun incrementCodeRuns() {
        _totalCodeRuns.value = _totalCodeRuns.value + 1
        prefs.edit().putInt(KEY_TOTAL_RUNS, _totalCodeRuns.value).apply()
    }
    
    fun saveScript(name: String, content: String) {
        val scripts = getScripts().toMutableMap()
        scripts[name] = ScriptEntry(content, System.currentTimeMillis())
        prefs.edit().putString(KEY_SCRIPTS, serializeScripts(scripts)).apply()
    }
    
    fun getScripts(): Map<String, ScriptEntry> {
        val json = prefs.getString(KEY_SCRIPTS, "{}") ?: "{}"
        return deserializeScripts(json)
    }
    
    fun deleteScript(name: String) {
        val scripts = getScripts().toMutableMap()
        scripts.remove(name)
        prefs.edit().putString(KEY_SCRIPTS, serializeScripts(scripts)).apply()
    }
    
    private fun serializeScripts(scripts: Map<String, ScriptEntry>): String {
        val entries = scripts.map { (name, entry) ->
            "${name}|${entry.timestamp}|${entry.content.replace("|", "\\|")}"
        }
        return entries.joinToString("\n")
    }
    
    private fun deserializeScripts(json: String): Map<String, ScriptEntry> {
        if (json == "{}") return emptyMap()
        
        return json.split("\n").associate { line ->
            val parts = line.split("|", limit = 3)
            val name = parts[0]
            val timestamp = parts.getOrNull(1)?.toLongOrNull() ?: 0L
            val content = parts.getOrNull(2)?.replace("\\|", "|") ?: ""
            name to ScriptEntry(content, timestamp)
        }
    }
    
    companion object {
        private const val PREFS_NAME = "pygenius_prefs"
        private const val KEY_STREAK = "coding_streak"
        private const val KEY_LAST_ACTIVE = "last_active"
        private const val KEY_COMPLETED_LESSONS = "completed_lessons"
        private const val KEY_TOTAL_RUNS = "total_runs"
        private const val KEY_SCRIPTS = "saved_scripts"
    }
}

data class ScriptEntry(
    val content: String,
    val timestamp: Long
) {
    fun getFormattedDate(): String {
        val date = java.util.Date(timestamp)
        return java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault()).format(date)
    }
}
