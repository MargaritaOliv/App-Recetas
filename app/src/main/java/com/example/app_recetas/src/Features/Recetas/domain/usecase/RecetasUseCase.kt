package com.example.app_recetas.src.Features.Recetas.domain.usecase

import com.example.app_recetas.src.Features.Recetas.domain.model.Recetas
import com.example.app_recetas.src.Features.Recetas.domain.model.RecetasError
import com.example.app_recetas.src.Features.Recetas.domain.model.RecetasResult
import com.example.app_recetas.src.Features.Recetas.domain.repository.RecetasRepository

class CreateRecetaUseCase(
    private val repository: RecetasRepository
) {
    suspend fun execute(
        token: String,
        nombre: String,
        ingredientes: List<String>,
        pasos: List<String>,
        tiempoPreparacion: Int
    ): RecetasResult<Recetas> {

        if (token.isBlank()) {
            return RecetasResult.Error(RecetasError.ValidationError("Token requerido"))
        }

        if (nombre.isBlank()) {
            return RecetasResult.Error(RecetasError.ValidationError("El nombre de la receta es obligatorio"))
        }

        if (ingredientes.isEmpty()) {
            return RecetasResult.Error(RecetasError.ValidationError("Los ingredientes son obligatorios"))
        }

        if (pasos.isEmpty()) {
            return RecetasResult.Error(RecetasError.ValidationError("Los pasos son obligatorios"))
        }

        if (tiempoPreparacion <= 0) {
            return RecetasResult.Error(RecetasError.ValidationError("El tiempo de preparación debe ser mayor a 0"))
        }

        val receta = Recetas(
            nombre = nombre,
            ingredientes = ingredientes,
            pasos = pasos,
            tiempoPreparacion = tiempoPreparacion
        )

        return repository.crearReceta(token, receta)
    }
}