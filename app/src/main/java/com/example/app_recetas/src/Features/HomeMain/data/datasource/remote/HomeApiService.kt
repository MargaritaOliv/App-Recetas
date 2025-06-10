package com.example.app.data.datasource.remote

import com.example.app.data.model.HomeResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

interface HomeApiService {
    @GET("api/receta/recetas")
    suspend fun getRecetas(
        @Header("Authorization") authorization: String
    ): Response<List<HomeResponse>>
}