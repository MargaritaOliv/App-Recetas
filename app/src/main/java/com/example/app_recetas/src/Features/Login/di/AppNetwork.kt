package com.example.app_recetas.src.Features.Login.di

import android.content.Context
import com.example.app_recetas.src.Core.Http.RetrofitHelper
import com.example.app_recetas.src.Core.di.DataStoreModule
import com.example.app_recetas.src.Features.Login.data.datasource.remote.AuthApiService
import com.example.app_recetas.src.Features.Login.data.repository.AuthRepositoryImpl
import com.example.app_recetas.src.Features.Login.data.repository.TokenRepositoryImpl
import com.example.app_recetas.src.Features.Login.domain.repository.AuthRepository
import com.example.app_recetas.src.Features.Login.domain.repository.TokenRepository
import com.example.app_recetas.src.Features.Login.domain.usecase.LoginUseCase
import com.example.app_recetas.src.Features.Login.presentation.viewModel.LoginViewModelFactory

object AppNetwork {

    private var isInitialized = false

    fun init(applicationContext: Context) {
        if (!isInitialized) {
            RetrofitHelper.init()
            isInitialized = true
        }
    }

    private val tokenRepository: TokenRepository by lazy {
        TokenRepositoryImpl(DataStoreModule.dataStoreManager)
    }

    private val authApiService: AuthApiService by lazy {
        RetrofitHelper.getService(AuthApiService::class.java)
    }

    private val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(authApiService, tokenRepository)
    }

    val loginUseCase: LoginUseCase by lazy {
        LoginUseCase(authRepository)
    }

    val loginViewModelFactory: LoginViewModelFactory by lazy {
        LoginViewModelFactory(loginUseCase)
    }
}