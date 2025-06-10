package com.example.app_recetas.src.Features.Login.domain.repository

interface AuthRepository {
    suspend fun login(
        correo: String,
        contrasena: String
    ): Result<Pair<String, String>> // Pair<Token, Mensaje>
}
