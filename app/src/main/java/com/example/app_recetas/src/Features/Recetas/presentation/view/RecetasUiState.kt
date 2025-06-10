package com.example.app_recetas.src.Features.Recetas.presentation.view

import com.example.app_recetas.src.Features.Recetas.domain.model.Recetas

data class RecetasUiState(
    val isLoading: Boolean = false,
    val receta: Recetas? = null,
    val error: String? = null,
    val isSuccess: Boolean = false
)