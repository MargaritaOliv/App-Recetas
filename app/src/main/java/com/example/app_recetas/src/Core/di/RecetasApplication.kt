package com.example.app_recetas.src.Core.di

import android.app.Application
import android.util.Log
import com.example.app_recetas.src.Core.connectivity.domain.ConnectivityRepository
import com.example.app_recetas.src.Core.connectivity.domain.SyncRepository

class RecetasApplication : Application() {

    val syncRepository: SyncRepository get() = DatabaseModule.syncRepository
    val connectivityRepository: ConnectivityRepository get() = DatabaseModule.connectivityRepository

    override fun onCreate() {
        super.onCreate()
        Log.d("RecetasApplication", "🚀 Inicializando aplicación...")
        initializeDependencies()
    }

    private fun initializeDependencies() {
        Log.d("RecetasApplication", "🔧 Inicializando dependencias...")

        com.example.app_recetas.src.Core.Http.RetrofitHelper.init()

        DatabaseModule.connectivityRepository

        Log.d("RecetasApplication", "✅ Dependencias inicializadas correctamente")
    }
}