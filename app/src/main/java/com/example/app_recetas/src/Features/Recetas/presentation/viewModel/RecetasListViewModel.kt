package com.example.app_recetas.src.Features.Recetas.presentation.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_recetas.src.Features.Recetas.domain.model.Recetas
import com.example.app_recetas.src.Features.Recetas.domain.model.RecetasError
import com.example.app_recetas.src.Features.Recetas.domain.model.RecetasResult
import com.example.app_recetas.src.Features.Recetas.domain.usecase.GetAllRecetasUseCase
import com.example.app_recetas.src.Features.Recetas.domain.usecase.DeleteRecetaUseCase
import com.example.app_recetas.src.Core.connectivity.domain.ConnectivityRepository
import com.example.app_recetas.src.Core.connectivity.domain.SyncStatus
import com.example.app_recetas.src.Features.Recetas.di.AppNetwork
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RecetasListUiState(
    val isLoading: Boolean = false,
    val recetas: List<Recetas> = emptyList(),
    val error: String? = null,
    val isRefreshing: Boolean = false,
    val isEmpty: Boolean = false,
    val searchQuery: String = "",
    val filteredRecetas: List<Recetas> = emptyList(),
    // ✅ Estados de sincronización
    val isSyncing: Boolean = false,
    val syncError: String? = null,
    val hasPendingItems: Boolean = false
)

class RecetasListViewModel(
    private val getAllRecetasUseCase: GetAllRecetasUseCase,
    private val deleteRecetaUseCase: DeleteRecetaUseCase,
    private val connectivityRepository: ConnectivityRepository? = null
) : ViewModel() {

    // ✅ ESTADOS INTERNOS
    private val _searchQuery = MutableStateFlow("")
    private val _isLoading = MutableStateFlow(false)
    private val _isRefreshing = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)
    private val _syncError = MutableStateFlow<String?>(null)

    private val recetasFlow = getAllRecetasUseCase.executeFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // ✅ SOLUCIÓN 1: Agrupar flows relacionados para no exceder 5 parámetros
    private val loadingStates = combine(
        _isLoading,
        _isRefreshing,
        _error
    ) { isLoading, isRefreshing, error ->
        Triple(isLoading, isRefreshing, error)
    }

    // ✅ Flow de conectividad (reactivo o por defecto)
    private val connectivityFlow = connectivityRepository?.observeSyncStatus()
        ?: flowOf(SyncStatus.Idle)

    private val syncStates = combine(
        _syncError,
        connectivityFlow
    ) { syncError, syncStatus ->
        val isSyncing = when (syncStatus) {
            is SyncStatus.Syncing -> true
            else -> false
        }
        val hasPendingItems = when (syncStatus) {
            is SyncStatus.Error -> true // Asumimos que hay items pendientes en error
            else -> false // Por ahora simple, se puede expandir según tu SyncStatus
        }
        Triple(syncError, isSyncing, hasPendingItems)
    }

    // ✅ COMBINAR TODO (solo 4 flows, dentro del límite)
    val uiState: StateFlow<RecetasListUiState> = combine(
        recetasFlow,
        _searchQuery,
        loadingStates,
        syncStates
    ) { recetas, searchQuery, loadingState, syncState ->

        println("🔄 [VIEWMODEL] Flow update - Recetas: ${recetas.size}, Query: '$searchQuery'")

        val filteredRecetas = filterRecetas(recetas, searchQuery)
        val (isLoading, isRefreshing, error) = loadingState
        val (syncError, isSyncing, hasPendingItems) = syncState

        RecetasListUiState(
            isLoading = isLoading,
            isRefreshing = isRefreshing,
            recetas = recetas,
            filteredRecetas = filteredRecetas,
            isEmpty = recetas.isEmpty() && !isLoading,
            error = error,
            searchQuery = searchQuery,
            // ✅ Estados de sincronización reactivos
            isSyncing = isSyncing,
            syncError = syncError,
            hasPendingItems = hasPendingItems
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = RecetasListUiState()
    )

    init {
        println("🔄 [VIEWMODEL] Inicializando RecetasListViewModel")
        cargarRecetasIniciales()
        connectivityRepository?.let { observarEstadoSincronizacion() }
    }

    // ✅ Observar estado de sincronización
    private fun observarEstadoSincronizacion() {
        connectivityRepository?.let { repository ->
            viewModelScope.launch {
                repository.observeSyncStatus().collect { status ->
                    when (status) {
                        is SyncStatus.Success -> {
                            if (status.itemsSynced > 0) {
                                println("✅ [VIEWMODEL] Sincronización exitosa: ${status.itemsSynced} elementos")
                            }
                            _syncError.value = null
                        }
                        is SyncStatus.Error -> {
                            println("❌ [VIEWMODEL] Error de sincronización: ${status.message}")
                            _syncError.value = status.message
                        }
                        is SyncStatus.Syncing -> {
                            println("🔄 [VIEWMODEL] Sincronización en progreso...")
                            _syncError.value = null
                        }
                        else -> {
                            _syncError.value = null
                        }
                    }
                }
            }
        }
    }

    // ✅ CARGA INICIAL
    private fun cargarRecetasIniciales() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                when (val result = getAllRecetasUseCase.executeOffline()) {
                    is RecetasResult.Success -> {
                        println("✅ [VIEWMODEL] Carga inicial exitosa: ${result.data.size} recetas")
                        _isLoading.value = false
                        _error.value = null
                    }
                    is RecetasResult.Error -> {
                        println("❌ [VIEWMODEL] Error en carga inicial: ${result.error}")
                        _isLoading.value = false
                        _error.value = mapError(result.error)
                    }
                    is RecetasResult.Loading -> {
                        _isLoading.value = true
                    }
                }
            } catch (e: Exception) {
                println("❌ [VIEWMODEL] Excepción en carga inicial: ${e.message}")
                _isLoading.value = false
                _error.value = "Error inesperado: ${e.message}"
            }
        }
    }

    // ✅ CARGAR CON TOKEN
    fun cargarRecetas(token: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            when (val result = getAllRecetasUseCase.execute(token)) {
                is RecetasResult.Success -> {
                    println("✅ [VIEWMODEL] Carga con token exitosa: ${result.data.size} recetas")
                    _isLoading.value = false
                    _error.value = null
                }
                is RecetasResult.Error -> {
                    println("❌ [VIEWMODEL] Error con token: ${result.error}")
                    _isLoading.value = false
                    _error.value = mapError(result.error)
                }
                is RecetasResult.Loading -> {
                    _isLoading.value = true
                }
            }
        }
    }

    // ✅ REFRESH CON INDICADOR VISUAL
    fun refreshRecetas(token: String = "") {
        viewModelScope.launch {
            _isRefreshing.value = true
            _error.value = null

            try {
                val result = if (token.isNotBlank()) {
                    getAllRecetasUseCase.execute(token)
                } else {
                    getAllRecetasUseCase.executeOffline()
                }

                when (result) {
                    is RecetasResult.Success -> {
                        println("✅ [VIEWMODEL] Refresh exitoso: ${result.data.size} recetas")
                        _isRefreshing.value = false
                        _error.value = null
                    }
                    is RecetasResult.Error -> {
                        println("❌ [VIEWMODEL] Error en refresh: ${result.error}")
                        _isRefreshing.value = false
                        _error.value = mapError(result.error)
                    }
                    is RecetasResult.Loading -> {
                        // Mantener refreshing = true
                    }
                }
            } catch (e: Exception) {
                println("❌ [VIEWMODEL] Excepción en refresh: ${e.message}")
                _isRefreshing.value = false
                _error.value = "Error en actualización: ${e.message}"
            }
        }
    }

    // ✅ ELIMINAR RECETA
    fun eliminarReceta(token: String, recetaId: Int, context: Context? = null) {
        viewModelScope.launch {
            when (val result = deleteRecetaUseCase.execute(token, recetaId)) {
                is RecetasResult.Success -> {
                    println("✅ [VIEWMODEL] Receta eliminada: $recetaId")
                    _error.value = null

                    context?.let { ctx ->
                        try {
                            AppNetwork.startSyncService(ctx)
                        } catch (e: Exception) {
                            // Si falla, no es crítico
                        }
                    }
                }
                is RecetasResult.Error -> {
                    println("❌ [VIEWMODEL] Error eliminando receta: ${result.error}")
                    _error.value = "Error al eliminar: ${mapError(result.error)}"
                }
                is RecetasResult.Loading -> {
                    // Loading state si es necesario
                }
            }
        }
    }

    // ✅ FUNCIONES DE CONECTIVIDAD
    fun sincronizarManualmente() {
        viewModelScope.launch {
            try {
                connectivityRepository?.let { repository ->
                    _syncError.value = null
                    val result = repository.syncPendingData()
                    when (result) {
                        is SyncStatus.Success -> {
                            println("✅ [VIEWMODEL] Sincronización manual exitosa: ${result.itemsSynced} elementos")
                        }
                        is SyncStatus.Error -> {
                            println("❌ [VIEWMODEL] Error en sincronización manual: ${result.message}")
                            _syncError.value = result.message
                        }
                        else -> {
                            // Otros estados se manejan en observarEstadoSincronizacion()
                        }
                    }
                }
            } catch (e: Exception) {
                println("❌ [VIEWMODEL] Excepción en sincronización manual: ${e.message}")
                _syncError.value = "Error en sincronización: ${e.message}"
            }
        }
    }

    fun iniciarServicioAutomatico(context: Context) {
        try {
            AppNetwork.startSyncService(context)
            println("🚀 [VIEWMODEL] Servicio automático iniciado")
        } catch (e: Exception) {
            println("❌ [VIEWMODEL] Error iniciando servicio: ${e.message}")
        }
    }

    fun detenerServicioAutomatico(context: Context) {
        try {
            AppNetwork.stopSyncService(context)
            println("🛑 [VIEWMODEL] Servicio automático detenido")
        } catch (e: Exception) {
            println("❌ [VIEWMODEL] Error deteniendo servicio: ${e.message}")
        }
    }

    fun habilitarAutoSync(enabled: Boolean) {
        viewModelScope.launch {
            try {
                connectivityRepository?.enableAutoSync(enabled)
                println("🔧 [VIEWMODEL] Auto-sync ${if (enabled) "habilitado" else "deshabilitado"}")
            } catch (e: Exception) {
                println("❌ [VIEWMODEL] Error configurando auto-sync: ${e.message}")
            }
        }
    }

    // ✅ BÚSQUEDA REACTIVA
    fun buscarRecetas(query: String) {
        println("🔍 [VIEWMODEL] Buscando: '$query'")
        _searchQuery.value = query
    }

    fun clearSearch() {
        println("🔍 [VIEWMODEL] Limpiando búsqueda")
        _searchQuery.value = ""
    }

    fun clearError() {
        _error.value = null
    }

    fun clearSyncError() {
        _syncError.value = null
    }

    fun resetState() {
        _searchQuery.value = ""
        _isLoading.value = false
        _isRefreshing.value = false
        _error.value = null
        _syncError.value = null
    }

    // ✅ FILTRADO DE RECETAS
    private fun filterRecetas(recetas: List<Recetas>, query: String): List<Recetas> {
        if (query.isBlank()) return recetas

        return recetas.filter { receta ->
            receta.nombre.contains(query, ignoreCase = true) ||
                    receta.ingredientes.any { it.contains(query, ignoreCase = true) }
        }
    }

    // ✅ MAPEO DE ERRORES
    private fun mapError(error: RecetasError): String {
        return when (error) {
            is RecetasError.NetworkError -> "Sin conexión a internet"
            is RecetasError.ServerError -> "Error del servidor. Intenta más tarde"
            is RecetasError.UnauthorizedError -> "Sesión expirada. Inicia sesión nuevamente"
            is RecetasError.ValidationError -> error.message
            is RecetasError.UnknownError -> error.message
        }
    }

    // ✅ MÉTODO PARA DEBUGGING
    fun debugEstado() {
        viewModelScope.launch {
            val currentState = uiState.value
            println("🔍 [DEBUG] Estado actual:")
            println("🔍 [DEBUG] - Loading: ${currentState.isLoading}")
            println("🔍 [DEBUG] - Recetas: ${currentState.recetas.size}")
            println("🔍 [DEBUG] - Filtered: ${currentState.filteredRecetas.size}")
            println("🔍 [DEBUG] - Error: ${currentState.error}")
            println("🔍 [DEBUG] - Query: '${currentState.searchQuery}'")
            println("🔍 [DEBUG] - Syncing: ${currentState.isSyncing}")
            println("🔍 [DEBUG] - Sync Error: ${currentState.syncError}")
            println("🔍 [DEBUG] - Has Pending: ${currentState.hasPendingItems}")
        }
    }
}