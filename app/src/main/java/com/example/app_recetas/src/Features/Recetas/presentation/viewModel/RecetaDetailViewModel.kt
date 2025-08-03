package com.example.app_recetas.src.Features.Recetas.presentation.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_recetas.src.Core.di.DataStoreModule
import com.example.app_recetas.src.Features.Recetas.domain.model.RecetasError
import com.example.app_recetas.src.Features.Recetas.domain.model.RecetasResult
import com.example.app_recetas.src.Features.Recetas.domain.usecase.DeleteRecetaUseCase
import com.example.app_recetas.src.Features.Recetas.domain.usecase.GetRecetaUseCase
import com.example.app_recetas.src.Features.Recetas.presentation.view.RecetaDetailUiState
// ✅ AGREGAR IMPORTS DE CONECTIVIDAD
import com.example.app_recetas.src.Core.connectivity.domain.ConnectivityRepository
import com.example.app_recetas.src.Features.Recetas.di.AppNetwork
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RecetaDetailViewModel(
    private val getRecetaUseCase: GetRecetaUseCase,
    private val deleteRecetaUseCase: DeleteRecetaUseCase,
    // ✅ AGREGAR CONECTIVIDAD
    private val connectivityRepository: ConnectivityRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecetaDetailUiState())
    val uiState: StateFlow<RecetaDetailUiState> = _uiState.asStateFlow()

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

    fun eliminarReceta(recetaId: Int, context: Context? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isDeleting = true,
                deleteError = null,
                isDeleted = false
            )

            try {
                val token = DataStoreModule.dataStoreManager.getToken()
                if (token.isNullOrEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        isDeleting = false,
                        deleteError = "No se encontró token de autenticación"
                    )
                    return@launch
                }

                when (val result = deleteRecetaUseCase.execute(token, recetaId)) {
                    is RecetasResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isDeleting = false,
                            isDeleted = true,
                            deleteError = null
                        )

                        // ✅ NUEVO: Iniciar servicio de sincronización tras eliminar
                        context?.let { ctx ->
                            AppNetwork.startSyncService(ctx)
                        }
                    }
                    is RecetasResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isDeleting = false,
                            deleteError = mapError(result.error)
                        )
                    }
                    is RecetasResult.Loading -> {
                        _uiState.value = _uiState.value.copy(isDeleting = true)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isDeleting = false,
                    deleteError = "Error al obtener token: ${e.message}"
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

    fun clearDeleteError() {
        _uiState.value = _uiState.value.copy(deleteError = null)
    }

    fun resetState() {
        _uiState.value = RecetaDetailUiState()
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