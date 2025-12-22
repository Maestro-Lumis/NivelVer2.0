package com.example.nivelver20.ui.screens.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nivelver20.data.repository.SyncRepository // ИЗМЕНЕНО!
import com.example.nivelver20.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegisterUiState(
    val title: String = "REGISTRO",
    val nameLabel: String = "Nombre de usuario",
    val passwordLabel: String = "Contraseña",
    val registerButton: String = "GUARDAR",
    val backButton: String = "ENTRADA",
    val testButton: String = "TEST",
    val perfilButton: String = "PERFIL",
    val nameUn: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class RegisterViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    // Используем SyncRepository - он координирует Realm и Firestore
    private val repository = SyncRepository.getInstance(application)
    private val sessionManager = SessionManager.getInstance(application)

    fun onNameChange(newName: String) {
        _uiState.update { it.copy(nameUn = newName, errorMessage = null, successMessage = null) }
    }

    fun onPasswordChange(newPassword: String) {
        _uiState.update { it.copy(password = newPassword, errorMessage = null, successMessage = null) }
    }

    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }

    fun registerUser() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }

            try {
                val username = _uiState.value.nameUn.trim()
                val password = _uiState.value.password

                if (username.isBlank() || password.isBlank()) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Llene todos los campos"
                        )
                    }
                    return@launch
                }

                // Проверка существования пользователя
                val existingUser = repository.getUserByUsername(username)
                if (existingUser != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Este usuario ya está registrado"
                        )
                    }
                    return@launch
                }

                // Создание пользователя через SyncRepository
                // Он автоматически сохранит локально и синхронизирует с облаком
                val result = repository.createUser(username, password)

                if (result.isSuccess) {
                    sessionManager.login(username)

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "¡Registro exitoso! Estás autorizado"
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Error al crear usuario"
                        )
                    }
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Ha ocurrido un error: ${e.message}"
                    )
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
    }
}