package com.example.app_recetas.src.Features.Register.domain.model

data class RegisterUser(
    val correo: String,
    val contrasena: String,
    val nombreUsuario: String
) {
    fun isValid(): Boolean {
        return correo.isNotBlank() &&
                contrasena.isNotBlank() &&
                nombreUsuario.isNotBlank() &&
                android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches() &&
                contrasena.length >= 6
    }

    fun getValidationError(): String? {
        return when {
            correo.isBlank() -> "El correo es obligatorio"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches() -> "Formato de correo inválido"
            contrasena.isBlank() -> "La contraseña es obligatoria"
            contrasena.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
            nombreUsuario.isBlank() -> "El nombre de usuario es obligatorio"
            else -> null
        }
    }
}