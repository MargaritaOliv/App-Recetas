package com.example.app_recetas.src.Features.Recetas.presentation.viewModel
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_recetas.src.Core.di.DataStoreModule
import com.example.app_recetas.src.Core.Hardware.Vibracion.domain.VibratorRepository
import com.example.app_recetas.src.Features.Recetas.domain.model.RecetasError
import com.example.app_recetas.src.Features.Recetas.domain.model.RecetasResult
import com.example.app_recetas.src.Features.Recetas.domain.usecase.CreateRecetaUseCase
import com.example.app_recetas.src.Features.Recetas.presentation.view.RecetasUiState
import com.example.app_recetas.src.Core.connectivity.domain.ConnectivityRepository
import com.example.app_recetas.src.Features.Recetas.di.AppNetwork
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RecetasViewModel(
    private val createRecetaUseCase: CreateRecetaUseCase,
    private val vibratorRepository: VibratorRepository,
    private val connectivityRepository: ConnectivityRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecetasUiState())
    val uiState: StateFlow<RecetasUiState> = _uiState.asStateFlow()

    fun crearReceta(
        nombre: String,
        ingredientes: List<String>,
        pasos: List<String>,
        tiempoPreparacion: Int,
        imagenPath: String? = null,
        context: Context? = null
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                isSuccess = false
            )

            try {
                val token = DataStoreModule.dataStoreManager.getToken()
                if (token.isNullOrEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "No se encontró token de autenticación",
                        isSuccess = false
                    )
                    return@launch
                }

                when (val result = createRecetaUseCase.execute(
                    token = token,
                    nombre = nombre,
                    ingredientes = ingredientes,
                    pasos = pasos,
                    tiempoPreparacion = tiempoPreparacion,
                    imagenPath = imagenPath
                )) {
                    is RecetasResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            receta = result.data,
                            error = null,
                            isSuccess = true
                        )

                        vibratorRepository.vibrateRecipeCreated()

                        context?.let { ctx ->
                            try {
                                AppNetwork.startSyncService(ctx)
                            } catch (e: Exception) {
                            }
                        }
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
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error al obtener token: ${e.message}",
                    isSuccess = false
                )
            }
        }
    }

    fun sincronizarManualmente() {
        viewModelScope.launch {
            try {
                connectivityRepository?.syncPendingData()
            } catch (e: Exception) {
            }
        }
    }

    fun iniciarServicioSincronizacion(context: Context) {
        try {
            AppNetwork.startSyncService(context)
        } catch (e: Exception) {
        }
    }

    fun detenerServicioSincronizacion(context: Context) {
        try {
            AppNetwork.stopSyncService(context)
        } catch (e: Exception) {
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