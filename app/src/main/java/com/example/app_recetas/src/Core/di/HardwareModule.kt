package com.example.app_recetas.src.Core.di

import androidx.lifecycle.LifecycleOwner
import com.example.app_recetas.src.Core.appcontext.AppContextHolder
import com.example.app_recetas.src.Core.Hardware.Camara.data.CameraManager
import com.example.app_recetas.src.Core.Hardware.Camara.domain.CameraRepository
import com.example.app_recetas.src.Core.Hardware.Vibracion.data.VibratorManager
import com.example.app_recetas.src.Core.Hardware.Vibracion.domain.VibratorRepository

object HardwareModule {

    fun cameraManager(lifecycleOwner: LifecycleOwner): CameraRepository {
        return CameraManager(AppContextHolder.get(), lifecycleOwner)
    }

    fun vibratorManager(): VibratorRepository {
        return VibratorManager(AppContextHolder.get())
    }
}