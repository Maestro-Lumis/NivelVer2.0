package com.example.nivelver20.data.session

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class TestResult(
    val nivel: String = "A1",
    val correctCount: Int = 0,
    val incorrectCount: Int = 0
)

class SessionManager private constructor(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "user_session",
        Context.MODE_PRIVATE
    )

    private val _isLoggedIn = MutableStateFlow(isUserLoggedIn())
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _currentUsername = MutableStateFlow(getCurrentUsername())
    val currentUsername: StateFlow<String?> = _currentUsername.asStateFlow()

    // StateFlows для результатов тестов
    private val _vocabularioResult = MutableStateFlow(loadVocabularioResult())
    val vocabularioResult: StateFlow<TestResult> = _vocabularioResult.asStateFlow()

    private val _lecturaResult = MutableStateFlow(loadLecturaResult())
    val lecturaResult: StateFlow<TestResult> = _lecturaResult.asStateFlow()

    private val _audioResult = MutableStateFlow(loadAudioResult())
    val audioResult: StateFlow<TestResult> = _audioResult.asStateFlow()

    // NEW: Grammar result
    private val _grammarResult = MutableStateFlow(loadGrammarResult())
    val grammarResult: StateFlow<TestResult> = _grammarResult.asStateFlow()

    companion object {
        @Volatile
        private var INSTANCE: SessionManager? = null

        fun getInstance(context: Context): SessionManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SessionManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    // Проверка авторизации
    private fun isUserLoggedIn(): Boolean {
        return prefs.getBoolean("is_logged_in", false)
    }

    // Получить текущего пользователя
    private fun getCurrentUsername(): String? {
        return prefs.getString("current_username", null)
    }

    fun getCurrentUser(): String? {
        return getCurrentUsername()
    }

    // Войти в систему
    fun login(username: String) {
        prefs.edit().apply {
            putBoolean("is_logged_in", true)
            putString("current_username", username)
            apply()
        }
        _isLoggedIn.value = true
        _currentUsername.value = username
        Log.d("SessionManager", "User logged in: $username")
    }

    // Выйти из системы
    fun logout() {
        prefs.edit().apply {
            putBoolean("is_logged_in", false)
            remove("current_username")
            apply()
        }
        _isLoggedIn.value = false
        _currentUsername.value = null
        Log.d("SessionManager", "User logged out")
    }

    // ========== VOCABULARIO ==========

    private fun loadVocabularioResult(): TestResult {
        return TestResult(
            nivel = prefs.getString("vocabulario_nivel", "A1") ?: "A1",
            correctCount = prefs.getInt("vocabulario_correct", 0),
            incorrectCount = prefs.getInt("vocabulario_incorrect", 0)
        )
    }

    fun saveVocabularioResult(nivel: String, correctCount: Int, incorrectCount: Int) {
        prefs.edit().apply {
            putString("vocabulario_nivel", nivel)
            putInt("vocabulario_correct", correctCount)
            putInt("vocabulario_incorrect", incorrectCount)
            apply()
        }

        _vocabularioResult.value = TestResult(nivel, correctCount, incorrectCount)
        Log.d("SessionManager", "Saved Vocabulario: $nivel, $correctCount/$incorrectCount")
    }

    // ========== LECTURA ==========

    private fun loadLecturaResult(): TestResult {
        return TestResult(
            nivel = prefs.getString("lectura_nivel", "A1") ?: "A1",
            correctCount = prefs.getInt("lectura_correct", 0),
            incorrectCount = prefs.getInt("lectura_incorrect", 0)
        )
    }

    fun saveLecturaResult(nivel: String, correctCount: Int, incorrectCount: Int) {
        prefs.edit().apply {
            putString("lectura_nivel", nivel)
            putInt("lectura_correct", correctCount)
            putInt("lectura_incorrect", incorrectCount)
            apply()
        }

        _lecturaResult.value = TestResult(nivel, correctCount, incorrectCount)
        Log.d("SessionManager", "Saved Lectura: $nivel, $correctCount/$incorrectCount")
    }

    // ========== AUDIO ==========

    private fun loadAudioResult(): TestResult {
        return TestResult(
            nivel = prefs.getString("audio_nivel", "A1") ?: "A1",
            correctCount = prefs.getInt("audio_correct", 0),
            incorrectCount = prefs.getInt("audio_incorrect", 0)
        )
    }

    fun saveAudioResult(nivel: String, correctCount: Int, incorrectCount: Int) {
        prefs.edit().apply {
            putString("audio_nivel", nivel)
            putInt("audio_correct", correctCount)
            putInt("audio_incorrect", incorrectCount)
            apply()
        }

        _audioResult.value = TestResult(nivel, correctCount, incorrectCount)
        Log.d("SessionManager", "Saved Audio: $nivel, $correctCount/$incorrectCount")
    }

    // ========== GRAMMAR ==========

    private fun loadGrammarResult(): TestResult {
        return TestResult(
            nivel = prefs.getString("grammar_nivel", "A1") ?: "A1",
            correctCount = prefs.getInt("grammar_correct", 0),
            incorrectCount = prefs.getInt("grammar_incorrect", 0)
        )
    }

    fun saveGrammarResult(nivel: String, correctCount: Int, incorrectCount: Int) {
        prefs.edit().apply {
            putString("grammar_nivel", nivel)
            putInt("grammar_correct", correctCount)
            putInt("grammar_incorrect", incorrectCount)
            apply()
        }

        _grammarResult.value = TestResult(nivel, correctCount, incorrectCount)
        Log.d("SessionManager", "Saved Grammar: $nivel, $correctCount/$incorrectCount")
    }
}