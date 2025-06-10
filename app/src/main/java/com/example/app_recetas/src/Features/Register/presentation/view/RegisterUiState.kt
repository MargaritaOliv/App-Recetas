package com.example.app_recetas.src.Features.Register.presentation.view

data class RegisterUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val correo: String = "",
    val contrasena: String = "",
    val nombreUsuario: String = ""
)