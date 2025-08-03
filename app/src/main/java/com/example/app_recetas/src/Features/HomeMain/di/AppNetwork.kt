package com.example.app.di

import com.example.app.data.datasource.remote.HomeApiService
import com.example.app.data.repository.HomeRepositoryImpl
import com.example.app.domain.repository.HomeRepository
import com.example.app.domain.usecase.HomeUseCase
import com.example.app.presentation.viewModel.HomeViewModelFactory
import com.example.app_recetas.src.Core.Http.RetrofitHelper

object AppNetwork {

    // Proveer API Service
    private fun provideHomeApiService(): HomeApiService {
        return RetrofitHelper.getService(HomeApiService::class.java)
    }

    // Proveer Repository con DI - SOLO dependencias de dominio
    private fun provideHomeRepository(): HomeRepository {
        return HomeRepositoryImpl(
            apiService = provideHomeApiService()
            // ⬅️ Sin DataStoreManager - el interceptor maneja la autenticación
        )
    }

    // Proveer UseCase
    private fun provideHomeUseCase(): HomeUseCase {
        return HomeUseCase(provideHomeRepository())
    }

    // Proveer ViewModelFactory
    fun provideHomeViewModelFactory(): HomeViewModelFactory {
        return HomeViewModelFactory(provideHomeUseCase())
    }
}