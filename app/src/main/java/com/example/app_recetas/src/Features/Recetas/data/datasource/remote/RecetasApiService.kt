package com.example.app_recetas.src.Features.Recetas.data.datasource.remote

import com.example.app_recetas.src.Features.Recetas.data.model.RecetaDeleteResponse
import com.example.app_recetas.src.Features.Recetas.data.model.RecetaDetailResponse
import com.example.app_recetas.src.Features.Recetas.data.model.RecetaUpdateRequest
import com.example.app_recetas.src.Features.Recetas.data.model.RecetasRequest
import com.example.app_recetas.src.Features.Recetas.data.model.RecetasResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface RecetasApiService {

    @POST("api/receta/crear")
    suspend fun crearReceta(
        @Header("Authorization") token: String,
        @Body request: RecetasRequest
    ): Response<RecetasResponse>

    @GET("api/receta/obtener")
    suspend fun obtenerRecetas(
        @Header("Authorization") token: String
    ): Response<List<RecetaDetailResponse>>

    @GET("api/receta/obtener/{id}")
    suspend fun obtenerReceta(
        @Header("Authorization") token: String,
        @Path("id") recetaId: Int
    ): Response<RecetaDetailResponse>

    @PUT("api/receta/actualizar/{id}")
    suspend fun actualizarReceta(
        @Header("Authorization") token: String,
        @Path("id") recetaId: Int,
        @Body request: RecetaUpdateRequest
    ): Response<RecetaDeleteResponse>

    @DELETE("api/receta/eliminar/{id}")
    suspend fun eliminarReceta(
        @Header("Authorization") token: String,
        @Path("id") recetaId: Int
    ): Response<RecetaDeleteResponse>
}