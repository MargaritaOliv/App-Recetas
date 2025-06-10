package com.example.app_recetas.src.Features.Recetas.domain.usecase

import com.example.app_recetas.src.Features.Recetas.domain.model.Recetas
import com.example.app_recetas.src.Features.Recetas.domain.model.RecetasError
import com.example.app_recetas.src.Features.Recetas.domain.model.RecetasResult
import com.example.app_recetas.src.Features.Recetas.domain.repository.RecetasRepository

class GetAllRecetasUseCase(
    private val repository: RecetasRepository
) {
    suspend fun execute(token: String): RecetasResult<List<Recetas>> {
        if (token.isBlank()) {
            return RecetasResult.Error(RecetasError.ValidationError("Token requerido"))
        }

        return repository.obtenerRecetas(token)
    }
}