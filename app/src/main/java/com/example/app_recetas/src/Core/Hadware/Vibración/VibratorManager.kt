package com.example.app_recetas.src.Core.Hardware.Vibracion.data

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.app_recetas.src.Core.Hardware.Vibracion.domain.VibratorRepository

class VibratorManager(
    private val context: Context
) : VibratorRepository {

    private val vibrator: Vibrator by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    override fun isVibratorAvailable(): Boolean {
        return try {
            vibrator.hasVibrator()
        } catch (e: Exception) {
            false
        }
    }

    override fun vibrateRecipeCreated() {
        if (!isVibratorAvailable()) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(500L, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(500L)
            }
        } catch (e: Exception) {
        }
    }
}