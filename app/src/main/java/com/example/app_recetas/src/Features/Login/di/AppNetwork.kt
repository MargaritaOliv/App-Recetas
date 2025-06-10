package com.example.app_recetas.src.Features.Login.di

import com.example.app_recetas.src.Features.Login.data.datasource.remote.AuthApiService
import com.example.app_recetas.src.Features.Login.data.repository.AuthRepositoryImpl
import com.example.app_recetas.src.Features.Login.domain.repository.AuthRepository
import com.example.app_recetas.src.Features.Login.domain.usecase.LoginUseCase
import com.example.app_recetas.src.Features.Login.presentation.viewModel.LoginViewModel
import com.example.app_recetas.src.Features.Login.presentation.viewModel.LoginViewModelFactory
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

object AppNetwork {

    private const val BASE_URL = "https://api-recetas.margaritaydidi.xyz/"

    // Network Configuration
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
    fun provideLoginUseCase(): LoginUseCase {
        return LoginUseCase(provideAuthRepository())
    }

    // ViewModels
    fun provideLoginViewModel(): LoginViewModel {
        return LoginViewModel(provideLoginUseCase())
    }

    // ViewModelFactory
    fun provideLoginViewModelFactory(): LoginViewModelFactory {
        return LoginViewModelFactory(provideLoginUseCase())
    }
}
