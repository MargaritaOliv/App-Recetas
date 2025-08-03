package com.example.app_recetas.src.Features.Recetas.domain.repository

import com.example.app_recetas.src.Features.Recetas.domain.model.Recetas
import com.example.app_recetas.src.Features.Recetas.domain.model.RecetasResult
import kotlinx.coroutines.flow.Flow

interface RecetasRepository {
    suspend fun crearReceta(receta: Recetas): RecetasResult<Recetas>
    suspend fun obtenerRecetas(): RecetasResult<List<Recetas>>
    suspend fun obtenerReceta(recetaId: Int): RecetasResult<Recetas>
    suspend fun actualizarReceta(receta: Recetas): RecetasResult<Recetas>
    suspend fun eliminarReceta(recetaId: Int): RecetasResult<Unit>
    fun obtenerRecetasFlow(): Flow<List<Recetas>>
}