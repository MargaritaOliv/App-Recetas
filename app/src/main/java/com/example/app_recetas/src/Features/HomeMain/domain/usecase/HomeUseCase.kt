package com.example.app.domain.usecase

import com.example.app.domain.model.HomeUser
import com.example.app.domain.repository.HomeRepository

class HomeUseCase(
    private val repository: HomeRepository
) {
    suspend fun getRecetas(token: String): Result<List<HomeUser>> {
        return repository.getRecetas(token)
    }
}