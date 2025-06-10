package com.example.app_recetas.src.Features.Recetas.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_recetas.src.Features.Recetas.domain.model.RecetasError
import com.example.app_recetas.src.Features.Recetas.domain.model.RecetasResult
import com.example.app_recetas.src.Features.Recetas.domain.usecase.GetRecetaUseCase
import com.example.app_recetas.src.Features.Recetas.domain.usecase.UpdateRecetaUseCase
import com.example.app_recetas.src.Features.Recetas.presentation.view.RecetaEditUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RecetaEditViewModel(
    private val getRecetaUseCase: GetRecetaUseCase,
    private val updateRecetaUseCase: UpdateRecetaUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecetaEditUiState())
    val uiState: StateFlow<RecetaEditUiState> = _uiState.asStateFlow()

    fun cargarReceta(token: String, recetaId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            when (val result = getRecetaUseCase.execute(token, recetaId)) {
                is RecetasResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        receta = result.data,
                        error = null
                    )
                }
                is RecetasResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = mapError(result.error)
                    )
                }
                is RecetasResult.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
            }
        }
    }

    fun actualizarReceta(
        token: String,
        recetaId: Int,
        nombre: String,
        ingredientes: List<String>,
        pasos: List<String>,
        tiempoPreparacion: Int
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isUpdating = true,
                updateError = null,
                isUpdated = false
            )

            when (val result = updateRecetaUseCase.execute(
                token = token,
                recetaId = recetaId,
                nombre = nombre,
                ingredientes = ingredientes,
                pasos = pasos,
                tiempoPreparacion = tiempoPreparacion
            )) {
                is RecetasResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isUpdating = false,
                        isUpdated = true,
                        updateError = null
                    )
                }
                is RecetasResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isUpdating = false,
                        updateError = mapError(result.error)
                    )
                }
                is RecetasResult.Loading -> {
                    _uiState.value = _uiState.value.copy(isUpdating = true)
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearUpdateError() {
        _uiState.value = _uiState.value.copy(updateError = null)
    }

    fun resetState() {
        _uiState.value = RecetaEditUiState()
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