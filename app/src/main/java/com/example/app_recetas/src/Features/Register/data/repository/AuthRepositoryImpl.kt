package com.example.app_recetas.src.Features.Register.data.repository

import com.example.app_recetas.src.Features.Register.data.datasource.remote.AuthApiService
import com.example.app_recetas.src.Features.Register.data.model.RegisterRequest
import com.example.app_recetas.src.Features.Register.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val apiService: AuthApiService
) : AuthRepository {

    override suspend fun register(
        correo: String,
        contrasena: String,
        nombreUsuario: String
    ): Result<String> {
        return try {
            val request = RegisterRequest(
                correo = correo,
                contrasena = contrasena,
                nombre_usuario = nombreUsuario
            )

            val response = apiService.register(request)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body.mensaje)
                } else {
                    Result.failure(Exception("Respuesta vacía del servidor"))
                }
            } else {
                Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
