package com.example.app_recetas.src.Core.di

import com.example.app_recetas.src.Core.appcontext.AppContextHolder
import com.example.app_recetas.src.datastore.Local.room.RecetasDB
import com.example.app_recetas.src.datastore.Local.room.daos.RecetaDao
import com.example.app_recetas.src.datastore.Local.room.daos.UsuarioDao
import com.example.app_recetas.src.datastore.Local.room.daos.PendienteSincronizacionDao
import com.example.app_recetas.src.Core.connectivity.data.ConnectivityManager
import com.example.app_recetas.src.Core.connectivity.data.SyncRepositoryImpl
import com.example.app_recetas.src.Core.connectivity.domain.ConnectivityRepository
import com.example.app_recetas.src.Core.connectivity.domain.SyncRepository

object DatabaseModule {

    val recetasDB: RecetasDB by lazy {
        RecetasDB.getDatabase(AppContextHolder.get())
    }

    val recetaDao: RecetaDao by lazy {
        recetasDB.recetaDao()
    }

    val usuarioDao: UsuarioDao by lazy {
        recetasDB.usuarioDao()
    }

    val pendienteSincronizacionDao: PendienteSincronizacionDao by lazy {
        recetasDB.pendienteSincronizacionDao()
    }

    val syncRepository: SyncRepository by lazy {
        SyncRepositoryImpl(
            recetaDao = recetaDao,
            usuarioDao = usuarioDao,
            pendienteSincronizacionDao = pendienteSincronizacionDao
        )
    }

    val connectivityRepository: ConnectivityRepository by lazy {
        ConnectivityManager(
            context = AppContextHolder.get(),
            syncRepository = syncRepository
        )
    }
}