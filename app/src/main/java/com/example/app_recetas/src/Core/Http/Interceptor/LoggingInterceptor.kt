package com.example.app_recetas.src.Core.Http.Interceptor

import okhttp3.logging.HttpLoggingInterceptor

fun provideLoggingInterceptor(): HttpLoggingInterceptor {
    return HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
}