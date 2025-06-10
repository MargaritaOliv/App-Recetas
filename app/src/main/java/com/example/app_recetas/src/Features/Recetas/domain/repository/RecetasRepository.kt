package com.example.app_recetas.src.Features.Recetas.domain.repository

import com.example.app_recetas.src.Features.Recetas.domain.model.Recetas
import com.example.app_recetas.src.Features.Recetas.domain.model.RecetasResult

interface RecetasRepository {
    suspend fun crearReceta(token: String, receta: Recetas): RecetasResult<Recetas>
    suspend fun obtenerRecetas(token: String): RecetasResult<List<Recetas>>
    suspend fun obtenerReceta(token: String, recetaId: Int): RecetasResult<Recetas>
    suspend fun actualizarReceta(token: String, receta: Recetas): RecetasResult<Recetas>
    suspend fun eliminarReceta(token: String, recetaId: Int): RecetasResult<Unit>
}