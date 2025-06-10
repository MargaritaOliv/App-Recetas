package com.example.app_recetas.src.Features.Register.domain.repository

interface AuthRepository {
    suspend fun register(
        correo: String,
        contrasena: String,
        nombreUsuario: String
    ): Result<String>
}