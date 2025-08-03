package com.example.app_recetas.src.Features.Recetas.data.datasource.remote

import com.example.app_recetas.src.Features.Recetas.data.model.RecetaDeleteResponse
import com.example.app_recetas.src.Features.Recetas.data.model.RecetaDetailResponse
import com.example.app_recetas.src.Features.Recetas.data.model.RecetaUpdateRequest
import com.example.app_recetas.src.Features.Recetas.data.model.RecetasRequest
import com.example.app_recetas.src.Features.Recetas.data.model.RecetasResponse
import retrofit2.Response
import retrofit2.http.*

interface RecetasApiService {

    @POST("api/receta/crear")
    suspend fun crearReceta(
        @Body request: RecetasRequest
    ): Response<RecetasResponse>

    @GET("api/receta/obtener")
    suspend fun obtenerRecetas(): Response<List<RecetaDetailResponse>>

    @GET("api/receta/obtener/{id}")
    suspend fun obtenerReceta(
        @Path("id") recetaId: Int
    ): Response<RecetaDetailResponse>

    @PUT("api/receta/actualizar/{id}")
    suspend fun actualizarReceta(
        @Path("id") recetaId: Int,
        @Body request: RecetaUpdateRequest
    ): Response<RecetaDeleteResponse>

    @DELETE("api/receta/eliminar/{id}")
    suspend fun eliminarReceta(
        @Path("id") recetaId: Int
    ): Response<RecetaDeleteResponse>
}