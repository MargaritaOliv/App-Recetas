package com.example.app_recetas.src.Features.Recetas.presentation.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_recetas.src.Core.di.DataStoreModule
import com.example.app_recetas.src.Features.Recetas.domain.model.RecetasError
import com.example.app_recetas.src.Features.Recetas.domain.model.RecetasResult
import com.example.app_recetas.src.Features.Recetas.domain.usecase.GetRecetaUseCase
import com.example.app_recetas.src.Features.Recetas.domain.usecase.UpdateRecetaUseCase
import com.example.app_recetas.src.Features.Recetas.presentation.view.RecetaEditUiState
// ✅ AGREGAR IMPORTS DE CONECTIVIDAD
import com.example.app_recetas.src.Core.connectivity.domain.ConnectivityRepository
import com.example.app_recetas.src.Features.Recetas.di.AppNetwork
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RecetaEditViewModel(
    private val getRecetaUseCase: GetRecetaUseCase,
    private val updateRecetaUseCase: UpdateRecetaUseCase,
    // ✅ AGREGAR CONECTIVIDAD
    private val connectivityRepository: ConnectivityRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecetaEditUiState())
    val uiState: StateFlow<RecetaEditUiState> = _uiState.asStateFlow()

    fun cargarReceta(recetaId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            try {
                val token = DataStoreModule.dataStoreManager.getToken()
                if (token.isNullOrEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "No se encontró token de autenticación"
                    )
                    return@launch
                }

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
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error al obtener token: ${e.message}"
                )
            }
        }
    }

    fun actualizarReceta(
        recetaId: Int,
        nombre: String,
        ingredientes: List<String>,
        pasos: List<String>,
        tiempoPreparacion: Int,
        context: Context? = null // ✅ AGREGAR CONTEXT
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isUpdating = true,
                updateError = null,
                isUpdated = false
            )

            try {
                val token = DataStoreModule.dataStoreManager.getToken()
                if (token.isNullOrEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        isUpdating = false,
                        updateError = "No se encontró token de autenticación"
                    )
                    return@launch
                }

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

                        // ✅ NUEVO: Iniciar servicio de sincronización tras actualizar
                        context?.let { ctx ->
                            AppNetwork.startSyncService(ctx)
                        }
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
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isUpdating = false,
                    updateError = "Error al obtener token: ${e.message}"
                )
            }
        }
    }

    // ✅ NUEVO: Función para sincronización manual
    fun sincronizarManualmente() {
        viewModelScope.launch {
            try {
                connectivityRepository.syncPendingData()
            } catch (e: Exception) {
                // Manejar error
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