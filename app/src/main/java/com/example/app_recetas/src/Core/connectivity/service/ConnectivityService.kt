package com.example.app_recetas.src.Core.connectivity.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.IBinder
import androidx.core.app.NotificationCompat

class ConnectivityService : Service() {

    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback
    private var isMonitoring = false

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "sync_channel"
    }

    override fun onCreate() {
        super.onCreate()
        println("🚀 [SERVICE] ConnectivityService onCreate() - INICIANDO...")

        try {
            connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            createNotificationChannel()
            println("✅ [SERVICE] ConnectivityManager y canal de notificación creados")

            setupNetworkCallback()
            println("✅ [SERVICE] NetworkCallback configurado")

        } catch (e: Exception) {
            println("💥 [SERVICE] Error en onCreate(): ${e.message}")
            e.printStackTrace()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        println("🚀 [SERVICE] onStartCommand() - INICIANDO FOREGROUND")

        try {
            startForeground(NOTIFICATION_ID, createNotification("Servicio de sincronización iniciado"))
            println("✅ [SERVICE] Foreground iniciado exitosamente")

            startNetworkMonitoring()
            println("✅ [SERVICE] Monitoreo de red iniciado")

        } catch (e: Exception) {
            println("💥 [SERVICE] Error en onStartCommand(): ${e.message}")
            e.printStackTrace()
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun setupNetworkCallback() {
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                println("🌐 [SERVICE] ===== RED DISPONIBLE =====")
                println("🌐 [SERVICE] Network: $network")
                updateNotification("🌐 Conectado - Red disponible")
                simulateSyncProcess()
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                println("📴 [SERVICE] ===== RED PERDIDA =====")
                println("📴 [SERVICE] Network perdida: $network")
                updateNotification("📴 Sin conexión - Datos en espera")
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                super.onCapabilitiesChanged(network, networkCapabilities)
                val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                val isValidated = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)

                println("🔄 [SERVICE] Capacidades cambiadas:")
                println("🔄 [SERVICE] - Internet: $hasInternet")
                println("🔄 [SERVICE] - Validada: $isValidated")

                if (hasInternet && isValidated) {
                    println("✅ [SERVICE] Red completamente funcional")
                    updateNotification("✅ Internet validado - Listo para sincronizar")
                    simulateSyncProcess()
                }
            }
        }
    }

    private fun startNetworkMonitoring() {
        if (isMonitoring) {
            println("⚠️ [SERVICE] Ya está monitoreando la red")
            return
        }

        try {
            val networkRequest = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                .build()

            connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
            isMonitoring = true
            println("✅ [SERVICE] Callback de red registrado exitosamente")

            // Verificar estado inicial
            checkInitialNetworkState()

        } catch (e: Exception) {
            println("❌ [SERVICE] Error registrando callback: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun checkInitialNetworkState() {
        try {
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)

            if (network != null && capabilities != null) {
                val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                val isValidated = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)

                println("📊 [SERVICE] Estado inicial de red:")
                println("📊 [SERVICE] - Network: $network")
                println("📊 [SERVICE] - Internet: $hasInternet")
                println("📊 [SERVICE] - Validada: $isValidated")

                if (hasInternet && isValidated) {
                    println("✅ [SERVICE] Estado inicial: CONECTADO")
                    updateNotification("✅ Conectado - Monitoreo activo")
                } else {
                    println("❌ [SERVICE] Estado inicial: SIN CONEXIÓN VÁLIDA")
                    updateNotification("❌ Sin conexión válida")
                }
            } else {
                println("❌ [SERVICE] Estado inicial: SIN RED")
                updateNotification("❌ Sin red disponible")
            }
        } catch (e: Exception) {
            println("💥 [SERVICE] Error verificando estado inicial: ${e.message}")
        }
    }

    private fun simulateSyncProcess() {
        // Simulación simple de sincronización
        Thread {
            try {
                println("📤 [SERVICE] ===== INICIANDO SIMULACIÓN DE SYNC =====")
                updateNotification("🔄 Sincronizando datos...")

                Thread.sleep(3000) // Simular trabajo de sincronización

                println("✅ [SERVICE] ===== SYNC SIMULADO COMPLETADO =====")
                updateNotification("✅ Sincronización completada")

                // Mostrar notificación de éxito
                showSuccessNotification("✅ Datos sincronizados", "2 recetas subidas al servidor")

                // Volver a notificación normal después de 5 segundos
                Thread.sleep(5000)
                updateNotification("🌐 Conectado - Monitoreo activo")

            } catch (e: Exception) {
                println("❌ [SERVICE] Error en simulación: ${e.message}")
                updateNotification("❌ Error en sincronización")
            }
        }.start()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Sincronización de Recetas",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Notificaciones del servicio de sincronización"
            setShowBadge(false)
        }

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
        println("📱 [SERVICE] Canal de notificación creado")
    }

    private fun createNotification(text: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("App Recetas - Sync")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun updateNotification(text: String) {
        try {
            val notification = createNotification(text)
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.notify(NOTIFICATION_ID, notification)
            println("🔔 [SERVICE] Notificación actualizada: '$text'")
        } catch (e: Exception) {
            println("❌ [SERVICE] Error actualizando notificación: ${e.message}")
        }
    }

    private fun showSuccessNotification(title: String, message: String) {
        try {
            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.notify(NOTIFICATION_ID + 1, notification)
            println("🎉 [SERVICE] Notificación de éxito mostrada: '$title - $message'")
        } catch (e: Exception) {
            println("❌ [SERVICE] Error mostrando notificación de éxito: ${e.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        println("🛑 [SERVICE] onDestroy() - DETENIENDO SERVICIO")

        try {
            if (isMonitoring) {
                connectivityManager.unregisterNetworkCallback(networkCallback)
                isMonitoring = false
                println("✅ [SERVICE] Callback de red desregistrado")
            }
        } catch (e: Exception) {
            println("❌ [SERVICE] Error en onDestroy(): ${e.message}")
        }
    }
}