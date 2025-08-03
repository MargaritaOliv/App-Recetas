package com.example.app_recetas.src.Core.Hardware.Vibracion.domain

interface VibratorRepository {
    fun isVibratorAvailable(): Boolean
    fun vibrateRecipeCreated()
}