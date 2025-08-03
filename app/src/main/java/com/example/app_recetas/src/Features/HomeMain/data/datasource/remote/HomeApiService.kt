package com.example.app.data.datasource.remote

import com.example.app.data.model.HomeResponse
import retrofit2.Response
import retrofit2.http.GET

interface HomeApiService {
    @GET("api/receta/obtener")
    suspend fun getRecetas(): Response<List<HomeResponse>>
}