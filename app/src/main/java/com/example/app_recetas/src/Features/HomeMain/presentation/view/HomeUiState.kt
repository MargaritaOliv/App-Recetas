package com.example.app.presentation.view

import com.example.app.domain.model.HomeUser

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(val recetas: List<HomeUser>) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}