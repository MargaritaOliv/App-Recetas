package com.example.app_recetas

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.app_recetas.src.Core.appcontext.AppContextHolder
import com.example.app_recetas.src.Features.Login.di.AppNetwork
import com.example.app_recetas.src.Features.Navigate.AppNavigate
import com.example.app_recetas.src.Core.connectivity.service.ConnectivityService
import com.example.app_recetas.ui.theme.AppRecetasTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppContextHolder.init(this)
        AppNetwork.init(applicationContext)

        try {
            println("🚀 [MAIN] Iniciando servicio de conectividad...")
            val serviceIntent = Intent(this, ConnectivityService::class.java)
            println("🔍 [DEBUG] Intent creado: $serviceIntent")
            val result = startForegroundService(serviceIntent)
            println("🔍 [DEBUG] Resultado startForegroundService: $result")
            println("✅ [MAIN] Servicio de conectividad iniciado")
        } catch (e: Exception) {
            println("❌ [MAIN] Error iniciando servicio: ${e.message}")
            println("❌ [MAIN] Stack trace completo:")
            e.printStackTrace()
        }

        enableEdgeToEdge()
        setContent {
            AppRecetasTheme {
                AppNavigate()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            println("🛑 [MAIN] Deteniendo servicio de conectividad...")
            val serviceIntent = Intent(this, ConnectivityService::class.java)
            stopService(serviceIntent)
            println("✅ [MAIN] Servicio detenido")
        } catch (e: Exception) {
            println("❌ [MAIN] Error deteniendo servicio: ${e.message}")
        }
    }
}