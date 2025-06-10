package com.example.app_recetas.src.Features.Register.data.datasource.remote
import com.example.app_recetas.src.Features.Register.data.model.RegisterRequest
import com.example.app_recetas.src.Features.Register.data.model.RegisterResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>
}