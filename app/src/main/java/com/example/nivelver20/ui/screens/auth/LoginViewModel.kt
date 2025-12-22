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

data class LoginUiState(
    val nameUn: String = "",
    val password: String = "",
    val title: String = "INICIAR SESIÓN",
    val nameLabel: String = "Nombre de usuario",
    val passwordLabel: String = "CONTRASEÑA",
    val loginButton: String = "ENTRAR",
    val registerButton: String = "REGISTRO",
    val testButton: String = "TEST",
    val perfilButton: String = "PERFIL",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val repository = SyncRepository.getInstance(application)
    private val sessionManager = SessionManager.getInstance(application)

    fun onEmailChange(newUsername: String) {
        _uiState.update { it.copy(nameUn = newUsername, errorMessage = null, successMessage = null) }
    }

    fun onPasswordChange(newPassword: String) {
        _uiState.update { it.copy(password = newPassword, errorMessage = null, successMessage = null) }
    }

    fun login(onSuccess: () -> Unit) {
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

                // Проверка пароля через SyncRepository
                val user = repository.verifyUserPassword(username, password)

                if (user != null) {
                    sessionManager.login(username)

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "¡Bienvenido, $username!"
                        )
                    }

                    kotlinx.coroutines.delay(1000)
                    onSuccess()
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Usuario o contraseña incorrecta"
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