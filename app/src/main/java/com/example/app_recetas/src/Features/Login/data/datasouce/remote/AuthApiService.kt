package com.example.app_recetas.src.Features.Login.data.datasource.remote

import com.example.app_recetas.src.Features.Login.data.model.LoginRequest
import com.example.app_recetas.src.Features.Login.data.model.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
}