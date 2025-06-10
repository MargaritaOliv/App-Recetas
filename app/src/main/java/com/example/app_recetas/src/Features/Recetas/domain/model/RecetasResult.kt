package com.example.app_recetas.src.Features.Recetas.domain.model

sealed class RecetasResult<out T> {
    data class Success<T>(val data: T) : RecetasResult<T>()
    data class Error(val error: RecetasError) : RecetasResult<Nothing>()
    object Loading : RecetasResult<Nothing>()
}
