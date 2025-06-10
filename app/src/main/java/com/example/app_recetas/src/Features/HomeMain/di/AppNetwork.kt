package com.example.app.di

import com.example.app.data.datasource.remote.HomeApiService
import com.example.app.data.repository.HomeRepositoryImpl
import com.example.app.domain.repository.HomeRepository
import com.example.app.domain.usecase.HomeUseCase
import com.example.app.presentation.viewModel.HomeViewModelFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object AppNetwork {

    private fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api-recetas.margaritaydidi.xyz/")
            .client(provideOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private fun provideHomeApiService(): HomeApiService {
        return provideRetrofit().create(HomeApiService::class.java)
    }

    private fun provideHomeRepository(): HomeRepository {
        return HomeRepositoryImpl(provideHomeApiService())
    }

    private fun provideHomeUseCase(): HomeUseCase {
        return HomeUseCase(provideHomeRepository())
    }

    fun provideHomeViewModelFactory(): HomeViewModelFactory {
        return HomeViewModelFactory(provideHomeUseCase())
    }
}