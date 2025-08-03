package com.example.app_recetas.src.Core.connectivity.domain

import kotlinx.coroutines.flow.Flow

// Estados de sincronización
sealed class SyncStatus {
    object Idle : SyncStatus()
    object Syncing : SyncStatus()
    data class Success(val itemsSynced: Int) : SyncStatus()
    data class Error(val message: String) : SyncStatus()
}

// Tipo de datos a sincronizar
sealed class SyncableItem {
    data class Recipe(val id: Long, val title: String) : SyncableItem()
    data class Ingredient(val id: Long, val name: String) : SyncableItem()
    data class Category(val id: Long, val name: String) : SyncableItem()
    // Agrega más tipos según tu app
}

interface ConnectivityRepository {

    // Conectividad básica
    fun isConnected(): Boolean
    fun observeConnectivity(): Flow<Boolean>

    // Sistema de sincronización
    suspend fun syncPendingData(): SyncStatus
    suspend fun markItemForSync(item: SyncableItem)
    suspend fun getPendingItems(): List<SyncableItem>
    fun observeSyncStatus(): Flow<SyncStatus>

    // Configuración de auto-sync
    fun enableAutoSync(enabled: Boolean)
    fun isAutoSyncEnabled(): Boolean
}

// Interfaz para sincronizar diferentes tipos de datos
interface SyncRepository {
    suspend fun syncRecipes(): Result<Int>
    suspend fun syncIngredients(): Result<Int>
    suspend fun syncCategories(): Result<Int>
    suspend fun hasPendingChanges(): Boolean
}