package com.example.app_recetas.src.Features.Register.di

import com.example.app_recetas.src.Features.Register.data.datasource.remote.AuthApiService
import com.example.app_recetas.src.Features.Register.data.repository.AuthRepositoryImpl
import com.example.app_recetas.src.Features.Register.domain.repository.AuthRepository
import com.example.app_recetas.src.Features.Register.domain.usecase.RegisterUseCase
import com.example.app_recetas.src.Features.Register.presentation.viewModel.RegisterViewModel
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

object AppModule {

    private const val BASE_URL = "https://api-recetas.margaritaydidi.xyz/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // API Services
    fun provideAuthApiService(): AuthApiService {
        return retrofit.create(AuthApiService::class.java)
    }

    // Repositories
    fun provideAuthRepository(): AuthRepository {
        return AuthRepositoryImpl(provideAuthApiService())
    }

    // Use Cases
    fun provideRegisterUseCase(): RegisterUseCase {
        return RegisterUseCase(provideAuthRepository())
    }

    // ViewModels
    fun provideRegisterViewModel(): RegisterViewModel {
        return RegisterViewModel(provideRegisterUseCase())
    }
}