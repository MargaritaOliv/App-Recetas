package com.example.app_recetas.src.Features.Recetas.domain.usecase

import com.example.app_recetas.src.Features.Recetas.domain.model.RecetasError
import com.example.app_recetas.src.Features.Recetas.domain.model.RecetasResult
import com.example.app_recetas.src.Features.Recetas.domain.repository.RecetasRepository

class DeleteRecetaUseCase(
    private val repository: RecetasRepository
) {
    suspend fun execute(token: String, recetaId: Int): RecetasResult<Unit> {
        if (token.isBlank()) {
            return RecetasResult.Error(RecetasError.ValidationError("Token requerido"))
        }

        if (recetaId <= 0) {
            return RecetasResult.Error(RecetasError.ValidationError("ID de receta inválido"))
        }

        return repository.eliminarReceta(token, recetaId)
    }
}