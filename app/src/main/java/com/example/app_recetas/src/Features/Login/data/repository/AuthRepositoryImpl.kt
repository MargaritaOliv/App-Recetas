package com.example.app_recetas.src.Features.Login.data.repository

import com.example.app_recetas.src.Features.Login.data.datasource.remote.AuthApiService
import com.example.app_recetas.src.Features.Login.data.model.LoginRequest
import com.example.app_recetas.src.Features.Login.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val apiService: AuthApiService
) : AuthRepository {

    override suspend fun login(
        correo: String,
        contrasena: String
    ): Result<Pair<String, String>> {
        return try {
            val request = LoginRequest(
                correo = correo,
                contrasena = contrasena
            )

            val response = apiService.login(request)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(Pair(body.token, body.mensaje))
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
