package com.example.app_recetas.src.Features.Recetas.data.repository

import com.example.app_recetas.src.Features.Recetas.data.datasource.remote.RecetasApiService
import com.example.app_recetas.src.Features.Recetas.data.model.RecetaUpdateRequest
import com.example.app_recetas.src.Features.Recetas.data.model.RecetasRequest
import com.example.app_recetas.src.Features.Recetas.domain.model.Recetas
import com.example.app_recetas.src.Features.Recetas.domain.model.RecetasError
import com.example.app_recetas.src.Features.Recetas.domain.model.RecetasResult
import com.example.app_recetas.src.Features.Recetas.domain.repository.RecetasRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class RecetasRepositoryImpl(
    private val apiService: RecetasApiService
) : RecetasRepository {

    override suspend fun crearReceta(token: String, receta: Recetas): RecetasResult<Recetas> = withContext(Dispatchers.IO) {
        try {
            val request = RecetasRequest(
                nombre = receta.nombre,
                ingredientes = receta.ingredientes,
                pasos = receta.pasos,
                tiempo_preparacion = receta.tiempoPreparacion
            )

            val response = apiService.crearReceta("Bearer $token", request)

            when {
                response.isSuccessful -> {
                    response.body()?.let { responseBody ->
                        val nuevaReceta = Recetas(
                            id = responseBody.receta.id,
                            nombre = responseBody.receta.nombre,
                            ingredientes = responseBody.receta.ingredientes,
                            pasos = responseBody.receta.pasos,
                            tiempoPreparacion = responseBody.receta.tiempo_preparacion
                        )
                        RecetasResult.Success(nuevaReceta)
                    } ?: RecetasResult.Error(RecetasError.UnknownError("Respuesta vacía del servidor"))
                }
                else -> handleErrorResponse(response.code())
            }
        } catch (e: IOException) {
            RecetasResult.Error(RecetasError.NetworkError)
        } catch (e: Exception) {
            RecetasResult.Error(RecetasError.UnknownError(e.message ?: "Error desconocido"))
        }
    }

    override suspend fun obtenerRecetas(token: String): RecetasResult<List<Recetas>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.obtenerRecetas("Bearer $token")

            when {
                response.isSuccessful -> {
                    response.body()?.let { responseList ->
                        val recetas = responseList.map { recetaResponse ->
                            Recetas(
                                id = recetaResponse.id,
                                nombre = recetaResponse.nombre,
                                ingredientes = recetaResponse.ingredientes,
                                pasos = recetaResponse.pasos,
                                tiempoPreparacion = recetaResponse.tiempo_preparacion
                            )
                        }
                        RecetasResult.Success(recetas)
                    } ?: RecetasResult.Error(RecetasError.UnknownError("Respuesta vacía del servidor"))
                }
                else -> handleErrorResponse(response.code())
            }
        } catch (e: IOException) {
            RecetasResult.Error(RecetasError.NetworkError)
        } catch (e: Exception) {
            RecetasResult.Error(RecetasError.UnknownError(e.message ?: "Error desconocido"))
        }
    }

    override suspend fun obtenerReceta(token: String, recetaId: Int): RecetasResult<Recetas> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.obtenerReceta("Bearer $token", recetaId)

            when {
                response.isSuccessful -> {
                    response.body()?.let { responseBody ->
                        val receta = Recetas(
                            id = responseBody.id,
                            nombre = responseBody.nombre,
                            ingredientes = responseBody.ingredientes,
                            pasos = responseBody.pasos,
                            tiempoPreparacion = responseBody.tiempo_preparacion
                        )
                        RecetasResult.Success(receta)
                    } ?: RecetasResult.Error(RecetasError.UnknownError("Respuesta vacía del servidor"))
                }
                response.code() == 404 -> {
                    RecetasResult.Error(RecetasError.ValidationError("Receta no encontrada"))
                }
                else -> handleErrorResponse(response.code())
            }
        } catch (e: IOException) {
            RecetasResult.Error(RecetasError.NetworkError)
        } catch (e: Exception) {
            RecetasResult.Error(RecetasError.UnknownError(e.message ?: "Error desconocido"))
        }
    }

    override suspend fun actualizarReceta(token: String, receta: Recetas): RecetasResult<Recetas> = withContext(Dispatchers.IO) {
        try {
            val request = RecetaUpdateRequest(
                id = receta.id,
                nombre = receta.nombre,
                ingredientes = receta.ingredientes,
                pasos = receta.pasos,
                tiempo_preparacion = receta.tiempoPreparacion
            )

            val response = apiService.actualizarReceta("Bearer $token", receta.id, request)

            when {
                response.isSuccessful -> {
                    RecetasResult.Success(receta)
                }
                response.code() == 404 -> {
                    RecetasResult.Error(RecetasError.ValidationError("Receta no encontrada"))
                }
                else -> handleErrorResponse(response.code())
            }
        } catch (e: IOException) {
            RecetasResult.Error(RecetasError.NetworkError)
        } catch (e: Exception) {
            RecetasResult.Error(RecetasError.UnknownError(e.message ?: "Error desconocido"))
        }
    }

    override suspend fun eliminarReceta(token: String, recetaId: Int): RecetasResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.eliminarReceta("Bearer $token", recetaId)

            when {
                response.isSuccessful -> {
                    RecetasResult.Success(Unit)
                }
                response.code() == 404 -> {
                    RecetasResult.Error(RecetasError.ValidationError("Receta no encontrada"))
                }
                else -> handleErrorResponse(response.code())
            }
        } catch (e: IOException) {
            RecetasResult.Error(RecetasError.NetworkError)
        } catch (e: Exception) {
            RecetasResult.Error(RecetasError.UnknownError(e.message ?: "Error desconocido"))
        }
    }

    private fun handleErrorResponse(code: Int): RecetasResult.Error {
        return when (code) {
            401 -> RecetasResult.Error(RecetasError.UnauthorizedError)
            in 400..499 -> RecetasResult.Error(RecetasError.ValidationError("Error de validación"))
            in 500..599 -> RecetasResult.Error(RecetasError.ServerError)
            else -> RecetasResult.Error(RecetasError.UnknownError("Error desconocido: $code"))
        }
    }
}