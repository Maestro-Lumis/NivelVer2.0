package com.example.nivelver20.ui.screens.auth

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nivelver20.data.repository.FirestoreRepository
import com.example.nivelver20.data.session.SessionManager
import com.example.nivelver20.utils.NetworkUtils
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
    val successMessage: String? = null,
    val isOfflineMode: Boolean = false
)

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val repository = FirestoreRepository.getInstance()
    private val sessionManager = SessionManager.getInstance(application)
    @SuppressLint("StaticFieldLeak")
    private val context = application.applicationContext

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

                // Валидация
                if (username.isBlank() || password.isBlank()) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Llene todos los campos"
                        )
                    }
                    return@launch
                }

                val isOffline = !NetworkUtils.isNetworkAvailable(context)

                // Проверка пароля (работает с offline кэшем Firebase)
                val verifyResult = repository.verifyUserPassword(username, password)

                if (verifyResult.isSuccess && verifyResult.getOrNull() != null) {
                    sessionManager.login(username)

                    val message = if (isOffline) {
                        "¡Bienvenido, $username! (Modo sin conexión)"
                    } else {
                        "¡Bienvenido, $username!"
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = message,
                            isOfflineMode = isOffline
                        )
                    }

                    kotlinx.coroutines.delay(1000)
                    onSuccess()

                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = if (isOffline) {
                                "Sin conexión. Conéctate para iniciar sesión por primera vez."
                            } else {
                                "Usuario o contraseña incorrecta"
                            }
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
