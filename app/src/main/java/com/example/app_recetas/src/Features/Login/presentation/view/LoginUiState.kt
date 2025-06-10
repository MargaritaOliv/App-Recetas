package com.example.app_recetas.src.Features.Login.presentation.view

data class LoginUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val correo: String = "",
    val contrasena: String = "",
    val token: String? = null,
    val mensaje: String? = null
)