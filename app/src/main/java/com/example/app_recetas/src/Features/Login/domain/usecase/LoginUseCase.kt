package com.example.app_recetas.src.Features.Login.domain.usecase

import android.util.Patterns
import com.example.app_recetas.src.Features.Login.domain.repository.AuthRepository

class LoginUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        correo: String,
        contrasena: String
    ): Result<Pair<String, String>> {
        // Validaciones de negocio
        if (correo.isBlank() || contrasena.isBlank()) {
            return Result.failure(Exception("Todos los campos son obligatorios"))
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            return Result.failure(Exception("Formato de correo inválido"))
        }

        if (contrasena.length < 6) {
            return Result.failure(Exception("La contraseña debe tener al menos 6 caracteres"))
        }

        return authRepository.login(correo, contrasena)
    }
}