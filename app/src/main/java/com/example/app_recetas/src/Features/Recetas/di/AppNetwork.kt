package com.example.app_recetas.src.Features.Recetas.di

import androidx.lifecycle.LifecycleOwner
import com.example.app_recetas.src.Core.Http.RetrofitHelper
import com.example.app_recetas.src.Core.di.HardwareModule
import com.example.app_recetas.src.Core.di.DatabaseModule
import com.example.app_recetas.src.Core.Hardware.Camara.domain.CameraRepository
import com.example.app_recetas.src.Core.Hardware.Vibracion.domain.VibratorRepository
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
// ✅ AGREGAR IMPORTS DE CONECTIVIDAD
import com.example.app_recetas.src.Core.connectivity.domain.ConnectivityRepository
import com.example.app_recetas.src.Core.connectivity.domain.SyncRepository

object AppNetwork {

    private val recetasApiService: RecetasApiService by lazy {
        RetrofitHelper.getService(RecetasApiService::class.java)
    }

    private val recetasRepository: RecetasRepository by lazy {
        RecetasRepositoryImpl(recetasApiService)
    }

    private val vibratorRepository: VibratorRepository by lazy {
        HardwareModule.vibratorManager()
    }

    // ✅ AGREGAR DEPENDENCIAS DE CONECTIVIDAD
    private val syncRepository: SyncRepository by lazy {
        DatabaseModule.syncRepository
    }

    private val connectivityRepository: ConnectivityRepository by lazy {
        DatabaseModule.connectivityRepository
    }

    // ✅ USE CASES EXISTENTES - HACER PÚBLICOS
    val createRecetaUseCase: CreateRecetaUseCase by lazy {
        CreateRecetaUseCase(recetasRepository)
    }

    val getAllRecetasUseCase: GetAllRecetasUseCase by lazy {
        GetAllRecetasUseCase(recetasRepository)
    }

    val getRecetaUseCase: GetRecetaUseCase by lazy {
        GetRecetaUseCase(recetasRepository)
    }

    val updateRecetaUseCase: UpdateRecetaUseCase by lazy {
        UpdateRecetaUseCase(recetasRepository)
    }

    val deleteRecetaUseCase: DeleteRecetaUseCase by lazy {
        DeleteRecetaUseCase(recetasRepository)
    }

    // ✅ FUNCIONES EXISTENTES - ACTUALIZAR TODAS CON CONECTIVIDAD
    fun provideRecetasViewModel(): RecetasViewModel {
        return RecetasViewModel(
            createRecetaUseCase = createRecetaUseCase,
            vibratorRepository = provideVibratorRepository(),
            connectivityRepository = provideConnectivityRepository()
        )
    }

    fun provideRecetasListViewModel(): RecetasListViewModel {
        return RecetasListViewModel(
            getAllRecetasUseCase = getAllRecetasUseCase,
            deleteRecetaUseCase = deleteRecetaUseCase,
            connectivityRepository = provideConnectivityRepository() // ✅ AGREGAR
        )
    }

    fun provideRecetaDetailViewModel(): RecetaDetailViewModel {
        return RecetaDetailViewModel(
            getRecetaUseCase = getRecetaUseCase,
            deleteRecetaUseCase = deleteRecetaUseCase,
            connectivityRepository = provideConnectivityRepository() // ✅ AGREGAR
        )
    }

    fun provideRecetaEditViewModel(): RecetaEditViewModel {
        return RecetaEditViewModel(
            getRecetaUseCase = getRecetaUseCase,
            updateRecetaUseCase = updateRecetaUseCase,
            connectivityRepository = provideConnectivityRepository() // ✅ AGREGAR
        )
    }

    fun provideCameraRepository(lifecycleOwner: LifecycleOwner): CameraRepository {
        return HardwareModule.cameraManager(lifecycleOwner)
    }

    fun provideVibratorRepository(): VibratorRepository {
        return vibratorRepository
    }

    // ✅ AGREGAR NUEVAS FUNCIONES PARA CONECTIVIDAD
    fun provideSyncRepository(): SyncRepository {
        return syncRepository
    }

    fun provideConnectivityRepository(): ConnectivityRepository {
        return connectivityRepository
    }

    // ✅ FUNCIÓN PARA INICIAR/DETENER SERVICIO DESDE VIEWMODEL
    fun startSyncService(context: android.content.Context) {
        val intent = android.content.Intent(context, com.example.app_recetas.src.Core.connectivity.service.ConnectivityService::class.java)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    fun stopSyncService(context: android.content.Context) {
        val intent = android.content.Intent(context, com.example.app_recetas.src.Core.connectivity.service.ConnectivityService::class.java)
        context.stopService(intent)
    }
}