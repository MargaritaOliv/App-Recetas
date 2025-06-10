package com.example.app_recetas.src.Features.Login.domain.model

import android.util.Patterns

data class LoginUser(
    val correo: String,
    val contrasena: String
) {
    fun isValid(): Boolean {
        return correo.isNotBlank() &&
                contrasena.isNotBlank() &&
                Patterns.EMAIL_ADDRESS.matcher(correo).matches() &&
                contrasena.length >= 6
    }

    fun getValidationError(): String? {
        return when {
            correo.isBlank() -> "El correo es obligatorio"
            !Patterns.EMAIL_ADDRESS.matcher(correo).matches() -> "Formato de correo inválido"
            contrasena.isBlank() -> "La contraseña es obligatoria"
            contrasena.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
            else -> null
        }
    }
}