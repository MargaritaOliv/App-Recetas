package com.example.app_recetas.src.Features.Recetas.domain.model

sealed class RecetasError {
    object NetworkError : RecetasError()
    object ServerError : RecetasError()
    object UnauthorizedError : RecetasError()
    data class ValidationError(val message: String) : RecetasError()
    data class UnknownError(val message: String) : RecetasError()
}