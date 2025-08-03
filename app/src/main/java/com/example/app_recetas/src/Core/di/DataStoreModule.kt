package com.example.app_recetas.src.Core.di

import android.util.Log
import com.example.app_recetas.src.Core.appcontext.AppContextHolder
import com.example.app_recetas.src.Core.datastore.Local.DataStoreManager

object DataStoreModule {

    val dataStoreManager: DataStoreManager by lazy {
        DataStoreManager(AppContextHolder.get())
    }
}