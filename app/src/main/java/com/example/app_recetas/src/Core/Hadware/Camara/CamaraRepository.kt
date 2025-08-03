package com.example.app_recetas.src.Core.Hardware.Camara.domain

import java.io.File

interface CameraRepository {
    fun isCameraAvailable(): Boolean
    suspend fun takePhoto(outputDirectory: File): Result<File>
    suspend fun takePhotoCompressed(
        outputDirectory: File,
        compressionQuality: Int = 80
    ): Result<File>
}

