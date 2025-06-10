package com.example.app_recetas.src.Features.Register.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_recetas.src.Features.Register.domain.usecase.RegisterUseCase
import com.example.app_recetas.src.Features.Register.presentation.view.RegisterUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    // Estado privado (mutable)
    private val _uiState = MutableStateFlow(RegisterUiState())
    // Estado público (inmutable)
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    // Funciones para actualizar campos individuales
    fun updateCorreo(correo: String) {
        _uiState.value = _uiState.value.copy(correo = correo, errorMessage = null)
    }

    fun updateContrasena(contrasena: String) {
        _uiState.value = _uiState.value.copy(contrasena = contrasena, errorMessage = null)
    }

    fun updateNombreUsuario(nombreUsuario: String) {
        _uiState.value = _uiState.value.copy(nombreUsuario = nombreUsuario, errorMessage = null)
    }

    // Función para registrar
    fun register() {
        val currentState = _uiState.value

        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoading = true, errorMessage = null)

            registerUseCase(
                correo = currentState.correo,
                contrasena = currentState.contrasena,
                nombreUsuario = currentState.nombreUsuario
            )
                .onSuccess { mensaje ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true,
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
