package com.example.nivelver20.data.session

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SessionManager private constructor(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "user_session",
        Context.MODE_PRIVATE
    )

    private val _isLoggedIn = MutableStateFlow(isUserLoggedIn())
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _currentUsername = MutableStateFlow(getCurrentUsername())
    val currentUsername: StateFlow<String?> = _currentUsername.asStateFlow()

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
    }
}