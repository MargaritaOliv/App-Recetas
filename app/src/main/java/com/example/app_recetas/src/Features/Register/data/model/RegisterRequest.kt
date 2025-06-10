package com.example.app_recetas.src.Features.Register.data.model

data class RegisterRequest(
    val correo: String,
    val contrasena: String,
    val nombre_usuario: String
)