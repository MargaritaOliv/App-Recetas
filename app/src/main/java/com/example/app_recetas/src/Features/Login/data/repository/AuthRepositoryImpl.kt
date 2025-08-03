package com.example.app_recetas.src.Features.Login.data.repository

import android.util.Log
import com.example.app_recetas.src.Features.Login.data.datasource.remote.AuthApiService
import com.example.app_recetas.src.Features.Login.data.model.LoginRequest
import com.example.app_recetas.src.Features.Login.domain.repository.AuthRepository
import com.example.app_recetas.src.Features.Login.domain.repository.TokenRepository

class AuthRepositoryImpl(
    private val apiService: AuthApiService,
    private val tokenRepository: TokenRepository
) : AuthRepository {

    override suspend fun login(
        correo: String,
        contrasena: String
    ): Result<Pair<String, String>> {
        return try {
            Log.d("AuthRepository", "🔐 Iniciando login para: $correo")

            val request = LoginRequest(
                correo = correo,
                contrasena = contrasena
            )

            Log.d("AuthRepository", "📤 Enviando request de login...")
            val response = apiService.login(request)

            Log.d("AuthRepository", "📥 Respuesta recibida - Código: ${response.code()}")
            Log.d("AuthRepository", "📥 Respuesta exitosa: ${response.isSuccessful}")

            if (response.isSuccessful) {
                val body = response.body()
                Log.d("AuthRepository", "📄 Body de respuesta: $body")

                if (body != null) {
                    Log.d("AuthRepository", "🎫 Token recibido: ${body.token}")
                    Log.d("AuthRepository", "💬 Mensaje recibido: ${body.mensaje}")

                    // Intentar guardar el token
                    try {
                        Log.d("AuthRepository", "💾 Intentando guardar token...")
                        tokenRepository.saveToken(body.token)
                        Log.d("AuthRepository", "✅ Token guardado exitosamente!")

                        // Verificar que se guardó correctamente
                        val savedToken = tokenRepository.getToken()
                        Log.d("AuthRepository", "🔍 Token verificado desde storage: $savedToken")

                        if (savedToken == body.token) {
                            Log.d("AuthRepository", "✅ Verificación exitosa - tokens coinciden")
                        } else {
                            Log.e("AuthRepository", "❌ ERROR: Tokens no coinciden!")
                            Log.e("AuthRepository", "❌ Token original: ${body.token}")
                            Log.e("AuthRepository", "❌ Token guardado: $savedToken")
                        }

                    } catch (saveException: Exception) {
                        Log.e("AuthRepository", "❌ ERROR al guardar token: ${saveException.message}", saveException)
                        return Result.failure(Exception("Error al guardar token: ${saveException.message}"))
                    }

                    Log.d("AuthRepository", "🎉 Login completado exitosamente")
                    Result.success(Pair(body.token, body.mensaje))
                } else {
                    Log.e("AuthRepository", "❌ ERROR: Respuesta vacía del servidor")
                    Result.failure(Exception("Respuesta vacía del servidor"))
                }
            } else {
                Log.e("AuthRepository", "❌ ERROR en login - Código: ${response.code()}")
                Log.e("AuthRepository", "❌ Mensaje de error: ${response.message()}")
                Log.e("AuthRepository", "❌ Error body: ${response.errorBody()?.string()}")
                Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "❌ EXCEPCIÓN en login: ${e.message}", e)
            Result.failure(e)
        }
    }
}