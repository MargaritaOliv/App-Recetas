package com.example.app_recetas.src.Features.Recetas.di

import com.example.app_recetas.src.Features.Recetas.data.datasource.remote.RecetasApiService
import com.example.app_recetas.src.Features.Recetas.data.repository.RecetasRepositoryImpl
import com.example.app_recetas.src.Features.Recetas.domain.repository.RecetasRepository
import com.example.app_recetas.src.Features.Recetas.domain.usecase.CreateRecetaUseCase
import com.example.app_recetas.src.Features.Recetas.domain.usecase.DeleteRecetaUseCase
import com.example.app_recetas.src.Features.Recetas.domain.usecase.GetAllRecetasUseCase
import com.example.app_recetas.src.Features.Recetas.domain.usecase.GetRecetaUseCase
import com.example.app_recetas.src.Features.Recetas.domain.usecase.UpdateRecetaUseCase
import com.example.app_recetas.src.Features.Recetas.presentation.viewModel.RecetaDetailViewModel
import com.example.app_recetas.src.Features.Recetas.presentation.viewModel.RecetaEditViewModel
import com.example.app_recetas.src.Features.Recetas.presentation.viewModel.RecetasListViewModel
import com.example.app_recetas.src.Features.Recetas.presentation.viewModel.RecetasViewModel
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object AppNetwork {

    private const val BASE_URL = "https://api-recetas.margaritaydidi.xyz/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val recetasApiService: RecetasApiService by lazy {
        retrofit.create(RecetasApiService::class.java)
    }

    private val recetasRepository: RecetasRepository by lazy {
        RecetasRepositoryImpl(recetasApiService)
    }

    // Use Cases
    private val createRecetaUseCase: CreateRecetaUseCase by lazy {
        CreateRecetaUseCase(recetasRepository)
    }

    private val getAllRecetasUseCase: GetAllRecetasUseCase by lazy {
        GetAllRecetasUseCase(recetasRepository)
    }

    private val getRecetaUseCase: GetRecetaUseCase by lazy {
        GetRecetaUseCase(recetasRepository)
    }

    private val updateRecetaUseCase: UpdateRecetaUseCase by lazy {
        UpdateRecetaUseCase(recetasRepository)
    }

    private val deleteRecetaUseCase: DeleteRecetaUseCase by lazy {
        DeleteRecetaUseCase(recetasRepository)
    }

    fun provideRecetasViewModel(): RecetasViewModel {
        return RecetasViewModel(createRecetaUseCase)
    }

    fun provideRecetasListViewModel(): RecetasListViewModel {
        return RecetasListViewModel(getAllRecetasUseCase, deleteRecetaUseCase)
    }

    fun provideRecetaDetailViewModel(): RecetaDetailViewModel {
        return RecetaDetailViewModel(getRecetaUseCase, deleteRecetaUseCase)
    }

    fun provideRecetaEditViewModel(): RecetaEditViewModel {
        return RecetaEditViewModel(getRecetaUseCase, updateRecetaUseCase)
    }
}