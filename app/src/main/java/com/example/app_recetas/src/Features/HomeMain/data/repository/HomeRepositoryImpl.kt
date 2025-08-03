package com.example.app.data.repository

import com.example.app.data.datasource.remote.HomeApiService
import com.example.app.data.model.HomeResponse
import com.example.app.domain.model.HomeUser
import com.example.app.domain.repository.HomeRepository

class HomeRepositoryImpl(
    private val apiService: HomeApiService
) : HomeRepository {

    override suspend fun getRecetas(): Result<List<HomeUser>> {
        return try {
            val response = apiService.getRecetas()
            if (response.isSuccessful) {
                val recetas = response.body()?.map { it.toDomainModel() } ?: emptyList()
                Result.success(recetas)
            } else {
                Result.failure(Exception("Error: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun HomeResponse.toDomainModel(): HomeUser {
        return HomeUser(
            id = this.id,
            nombre = this.nombre,
            ingredientes = this.ingredientes,
            pasos = this.pasos,
            tiempoPreparacion = this.tiempoPreparacion,
            imagenReceta = this.imagenReceta
        )
    }
}