package com.example.app_recetas.src.Features.Login.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_recetas.src.Features.Login.domain.usecase.LoginUseCase
import com.example.app_recetas.src.Features.Login.presentation.view.LoginUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    // Estado privado (mutable)
    private val _uiState = MutableStateFlow(LoginUiState())
    // Estado público (inmutable)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    // Funciones para actualizar campos individuales
    fun updateCorreo(correo: String) {
        _uiState.value = _uiState.value.copy(correo = correo, errorMessage = null)
    }

    fun updateContrasena(contrasena: String) {
        _uiState.value = _uiState.value.copy(contrasena = contrasena, errorMessage = null)
    }

    // Función para hacer login
    fun login() {
        val currentState = _uiState.value

        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoading = true, errorMessage = null)

            loginUseCase(
                correo = currentState.correo,
                contrasena = currentState.contrasena
            )
                .onSuccess { (token, mensaje) ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        token = token,
                        mensaje = mensaje,
                        errorMessage = null
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = false,
                        errorMessage = exception.message ?: "Error desconocido"
                    )
                }
        }
    }

    // Funciones de utilidad
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun resetSuccess() {
        _uiState.value = _uiState.value.copy(isSuccess = false)
    }
}
