package com.example.app_recetas.src.Features.Register.domain.usecase

import com.example.app_recetas.src.Features.Register.domain.repository.AuthRepository

class RegisterUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        correo: String,
        contrasena: String,
        nombreUsuario: String
    ): Result<String> {
        if (correo.isBlank() || contrasena.isBlank() || nombreUsuario.isBlank()) {
            return Result.failure(Exception("Todos los campos son obligatorios"))
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            return Result.failure(Exception("Formato de correo inválido"))
        }

        if (contrasena.length < 6) {
            return Result.failure(Exception("La contraseña debe tener al menos 6 caracteres"))
        }

        return authRepository.register(correo, contrasena, nombreUsuario)
    }
}