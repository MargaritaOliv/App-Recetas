package com.example.app_recetas.src.Features.Recetas.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_recetas.src.Features.Recetas.domain.model.Recetas
import com.example.app_recetas.src.Features.Recetas.domain.model.RecetasError
import com.example.app_recetas.src.Features.Recetas.domain.model.RecetasResult
import com.example.app_recetas.src.Features.Recetas.domain.usecase.GetAllRecetasUseCase
import com.example.app_recetas.src.Features.Recetas.domain.usecase.DeleteRecetaUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RecetasListUiState(
    val isLoading: Boolean = false,
    val recetas: List<Recetas> = emptyList(),
    val error: String? = null,
    val isRefreshing: Boolean = false,
    val isEmpty: Boolean = false,
    val searchQuery: String = "",
    val filteredRecetas: List<Recetas> = emptyList()
)

class RecetasListViewModel(
    private val getAllRecetasUseCase: GetAllRecetasUseCase,
    private val deleteRecetaUseCase: DeleteRecetaUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecetasListUiState())
    val uiState: StateFlow<RecetasListUiState> = _uiState.asStateFlow()

    // Removí el init{} porque ahora necesitamos el token para cargar

    fun cargarRecetas(token: String) { // Agregué el parámetro token
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            when (val result = getAllRecetasUseCase.execute(token)) { // Pasé el token
                is RecetasResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        recetas = result.data,
                        filteredRecetas = filterRecetas(result.data, _uiState.value.searchQuery),
                        isEmpty = result.data.isEmpty(),
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

    fun refreshRecetas(token: String) { // Agregué el parámetro token
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isRefreshing = true,
                error = null
            )

            when (val result = getAllRecetasUseCase.execute(token)) { // Pasé el token
                is RecetasResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isRefreshing = false,
                        recetas = result.data,
                        filteredRecetas = filterRecetas(result.data, _uiState.value.searchQuery),
                        isEmpty = result.data.isEmpty(),
                        error = null
                    )
                }
                is RecetasResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isRefreshing = false,
                        error = mapError(result.error)
                    )
                }
                is RecetasResult.Loading -> {
                    _uiState.value = _uiState.value.copy(isRefreshing = true)
                }
            }
        }
    }

    fun eliminarReceta(token: String, recetaId: Int) { // Agregué el parámetro token
        viewModelScope.launch {
            when (val result = deleteRecetaUseCase.execute(token, recetaId)) { // Pasé el token
                is RecetasResult.Success -> {
                    // Actualizar la lista local removiendo la receta eliminada
                    val updatedRecetas = _uiState.value.recetas.filter { it.id != recetaId }
                    _uiState.value = _uiState.value.copy(
                        recetas = updatedRecetas,
                        filteredRecetas = filterRecetas(updatedRecetas, _uiState.value.searchQuery),
                        isEmpty = updatedRecetas.isEmpty()
                    )
                }
                is RecetasResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        error = mapError(result.error)
                    )
                }
                is RecetasResult.Loading -> {
                    // Opcional: mostrar loading en el item específico
                }
            }
        }
    }

    fun buscarRecetas(query: String) {
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            filteredRecetas = filterRecetas(_uiState.value.recetas, query)
        )
    }

    fun clearSearch() {
        _uiState.value = _uiState.value.copy(
            searchQuery = "",
            filteredRecetas = _uiState.value.recetas
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun resetState() {
        _uiState.value = RecetasListUiState()
    }

    private fun filterRecetas(recetas: List<Recetas>, query: String): List<Recetas> {
        if (query.isBlank()) return recetas

        return recetas.filter { receta ->
            receta.nombre.contains(query, ignoreCase = true) ||
                    receta.ingredientes.any { it.contains(query, ignoreCase = true) }
        }
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