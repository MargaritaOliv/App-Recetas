package com.example.app.domain.repository

import com.example.app.domain.model.HomeUser

interface HomeRepository {
    suspend fun getRecetas(): Result<List<HomeUser>>
}