package com.example.app_recetas.src.Core.connectivity.data

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import com.example.app_recetas.src.Core.connectivity.domain.ConnectivityRepository
import com.example.app_recetas.src.Core.connectivity.domain.SyncRepository
import com.example.app_recetas.src.Core.connectivity.domain.SyncStatus
import com.example.app_recetas.src.Core.connectivity.domain.SyncableItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow

class ConnectivityManager(
    private val context: Context,
    private val syncRepository: SyncRepository
) : ConnectivityRepository {

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val preferences: SharedPreferences = context.getSharedPreferences("connectivity_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    private val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val pendingItems = mutableListOf<SyncableItem>()

    override fun isConnected(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    override fun observeConnectivity(): Flow<Boolean> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Log.d("ConnectivityManager", "🌐 Red disponible")
                trySend(true)
            }

            override fun onLost(network: Network) {
                Log.d("ConnectivityManager", "❌ Red perdida")
                trySend(false)
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                        networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                Log.d("ConnectivityManager", "🔄 Capacidades cambiadas: $hasInternet")
                trySend(hasInternet)
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(request, callback)

        trySend(isConnected())

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }

    override suspend fun syncPendingData(): SyncStatus {
        if (!isConnected()) {
            Log.w("ConnectivityManager", "⚠️ No hay conexión para sincronizar")
            return SyncStatus.Error("Sin conexión a Internet")
        }

        if (!syncRepository.hasPendingChanges() && pendingItems.isEmpty()) {
            Log.d("ConnectivityManager", "✅ No hay datos pendientes para sincronizar")
            return SyncStatus.Success(0)
        }

        _syncStatus.value = SyncStatus.Syncing
        Log.d("ConnectivityManager", "🔄 Iniciando sincronización...")

        try {
            var totalSynced = 0

            syncRepository.syncRecipes().fold(
                onSuccess = { count ->
                    totalSynced += count
                    Log.d("ConnectivityManager", "✅ Sincronizadas $count recetas")
                },
                onFailure = { error ->
                    Log.e("ConnectivityManager", "❌ Error sincronizando recetas: ${error.message}")
                    _syncStatus.value = SyncStatus.Error("Error sincronizando recetas")
                    return SyncStatus.Error("Error sincronizando recetas: ${error.message}")
                }
            )

            syncRepository.syncIngredients().fold(
                onSuccess = { count ->
                    totalSynced += count
                    Log.d("ConnectivityManager", "✅ Sincronizados $count ingredientes")
                },
                onFailure = { error ->
                    Log.e("ConnectivityManager", "❌ Error sincronizando ingredientes: ${error.message}")
                    _syncStatus.value = SyncStatus.Error("Error sincronizando ingredientes")
                    return SyncStatus.Error("Error sincronizando ingredientes: ${error.message}")
                }
            )

            syncRepository.syncCategories().fold(
                onSuccess = { count ->
                    totalSynced += count
                    Log.d("ConnectivityManager", "✅ Sincronizadas $count categorías")
                },
                onFailure = { error ->
                    Log.e("ConnectivityManager", "❌ Error sincronizando categorías: ${error.message}")
                    _syncStatus.value = SyncStatus.Error("Error sincronizando categorías")
                    return SyncStatus.Error("Error sincronizando categorías: ${error.message}")
                }
            )

            pendingItems.clear()
            savePendingItems()

            val result = SyncStatus.Success(totalSynced)
            _syncStatus.value = result
            Log.d("ConnectivityManager", "🎉 Sincronización completada: $totalSynced elementos")

            return result

        } catch (e: Exception) {
            Log.e("ConnectivityManager", "💥 Error inesperado durante sincronización", e)
            val errorResult = SyncStatus.Error("Error inesperado: ${e.message}")
            _syncStatus.value = errorResult
            return errorResult
        }
    }

    override suspend fun markItemForSync(item: SyncableItem) {
        pendingItems.add(item)
        savePendingItems()
        Log.d("ConnectivityManager", "📝 Elemento marcado para sincronización: $item")
    }

    override suspend fun getPendingItems(): List<SyncableItem> {
        return pendingItems.toList()
    }

    override fun observeSyncStatus(): Flow<SyncStatus> = syncStatus

    override fun enableAutoSync(enabled: Boolean) {
        preferences.edit()
            .putBoolean("auto_sync_enabled", enabled)
            .apply()
        Log.d("ConnectivityManager", "🔧 Auto-sync ${if (enabled) "habilitado" else "deshabilitado"}")
    }

    override fun isAutoSyncEnabled(): Boolean {
        return preferences.getBoolean("auto_sync_enabled", true)
    }

    private fun savePendingItems() {
        try {
            val json = gson.toJson(pendingItems)
            preferences.edit()
                .putString("pending_items", json)
                .apply()
            Log.d("ConnectivityManager", "💾 Elementos pendientes guardados: ${pendingItems.size}")
        } catch (e: Exception) {
            Log.e("ConnectivityManager", "❌ Error guardando elementos pendientes", e)
        }
    }

    private fun loadPendingItems() {
        try {
            val json = preferences.getString("pending_items", null)
            if (json != null) {
                val type = object : TypeToken<List<SyncableItem>>() {}.type
                val items = gson.fromJson<List<SyncableItem>>(json, type)
                pendingItems.clear()
                pendingItems.addAll(items)
                Log.d("ConnectivityManager", "📂 Elementos pendientes cargados: ${pendingItems.size}")
            } else {
                Log.d("ConnectivityManager", "📂 No hay elementos pendientes guardados")
            }
        } catch (e: Exception) {
            Log.e("ConnectivityManager", "❌ Error cargando elementos pendientes", e)
            pendingItems.clear()
        }
    }

    init {
        loadPendingItems()
        Log.d("ConnectivityManager", "🎬 ConnectivityManager inicializado")
    }
}