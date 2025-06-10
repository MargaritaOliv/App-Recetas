package com.example.app_recetas.src.Features.Recetas.presentation.view

import com.example.app_recetas.src.Features.Recetas.domain.model.Recetas

data class RecetaDetailUiState(
    val isLoading: Boolean = false,
    val receta: Recetas? = null,
    val error: String? = null,
    val isDeleting: Boolean = false,
    val isDeleted: Boolean = false,
    val deleteError: String? = null
)