package com.example.app_recetas.src.Features.Recetas.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_recetas.src.Features.Recetas.domain.model.RecetasError
import com.example.app_recetas.src.Features.Recetas.domain.model.RecetasResult
import com.example.app_recetas.src.Features.Recetas.domain.usecase.CreateRecetaUseCase
import com.example.app_recetas.src.Features.Recetas.presentation.view.RecetasUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RecetasViewModel(
    private val createRecetaUseCase: CreateRecetaUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecetasUiState())
    val uiState: StateFlow<RecetasUiState> = _uiState.asStateFlow()

    fun crearReceta(
        token: String, // Agregué el parámetro token
        nombre: String,
        ingredientes: List<String>,
        pasos: List<String>,
        tiempoPreparacion: Int
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                isSuccess = false
            )

            when (val result = createRecetaUseCase.execute(
                token = token, // Pasé el token al use case
                nombre = nombre,
                ingredientes = ingredientes,
                pasos = pasos,
                tiempoPreparacion = tiempoPreparacion
            )) {
                is RecetasResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        receta = result.data,
                        error = null,
                        isSuccess = true
                    )
                }
                is RecetasResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = mapError(result.error),
                        isSuccess = false
                    )
                }
                is RecetasResult.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun resetState() {
        _uiState.value = RecetasUiState()
    }

    private fun mapError(error: RecetasError): String {
        return when (error) {
            is RecetasError.NetworkError -> "Sin conexión a internet"
            is RecetasError.ServerError -> "Error del servidor. Intenta más tarde"
            is RecetasError.UnauthorizedError -> "Sesión expirada. Inicia sesión nuevamente"
            is RecetasError.ValidationError -> error.message
            is RecetasError.UnknownError -> error.message
        }
    }
}