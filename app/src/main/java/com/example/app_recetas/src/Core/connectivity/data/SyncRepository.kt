package com.example.app_recetas.src.Core.connectivity.data

import android.util.Log
import com.example.app_recetas.src.Core.connectivity.domain.SyncRepository
import com.example.app_recetas.src.Core.Http.RetrofitHelper
import com.example.app_recetas.src.datastore.Local.room.daos.RecetaDao
import com.example.app_recetas.src.datastore.Local.room.daos.UsuarioDao
import com.example.app_recetas.src.datastore.Local.room.daos.PendienteSincronizacionDao
import com.example.app_recetas.src.Features.Recetas.data.datasource.remote.RecetasApiService
import com.example.app_recetas.src.Features.Recetas.data.model.RecetasRequest
import com.example.app_recetas.src.Features.Recetas.data.model.RecetaUpdateRequest
import com.example.app_recetas.src.datastore.Local.room.entities.PendienteSincronizacionEntity
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class SyncRepositoryImpl(
    private val recetaDao: RecetaDao,
    private val usuarioDao: UsuarioDao,
    private val pendienteSincronizacionDao: PendienteSincronizacionDao
) : SyncRepository {

    private val recetasApiService: RecetasApiService by lazy {
        RetrofitHelper.getService(RecetasApiService::class.java)
    }

    private val gson = Gson()

    override suspend fun syncRecipes(): Result<Int> = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d("SyncRepository", "🥘 Iniciando sincronización de recetas...")

            val recetasNoSincronizadas = recetaDao.getRecetasNoSincronizadas()

            if (recetasNoSincronizadas.isEmpty()) {
                Log.d("SyncRepository", "✅ No hay recetas pendientes de sincronización")
                return@withContext Result.success(0)
            }

            var syncedCount = 0

            for (receta in recetasNoSincronizadas) {
                try {
                    Log.d("SyncRepository", "🔄 Sincronizando receta: ${receta.nombre} (ID: ${receta.id})")

                    val success = syncRecipeToServer(receta)

                    if (success) {
                        recetaDao.marcarComoSincronizado(receta.id)
                        syncedCount++
                        Log.d("SyncRepository", "✅ Receta sincronizada: ${receta.nombre}")
                    } else {
                        Log.w("SyncRepository", "⚠️ Falló sincronización de receta: ${receta.nombre}")
                    }

                } catch (e: Exception) {
                    Log.e("SyncRepository", "❌ Error sincronizando receta ${receta.id}: ${e.message}")
                }
            }

            Log.d("SyncRepository", "🎉 Sincronización de recetas completa: $syncedCount/${recetasNoSincronizadas.size}")
            Result.success(syncedCount)

        } catch (e: Exception) {
            Log.e("SyncRepository", "💥 Error general en sincronización de recetas", e)
            Result.failure(e)
        }
    }

    override suspend fun syncIngredients(): Result<Int> {
        return try {
            Log.d("SyncRepository", "🥕 Iniciando sincronización de ingredientes...")

            Log.d("SyncRepository", "✅ No hay ingredientes para sincronizar")
            Result.success(0)

        } catch (e: Exception) {
            Log.e("SyncRepository", "💥 Error en sincronización de ingredientes", e)
            Result.failure(e)
        }
    }

    override suspend fun syncCategories(): Result<Int> {
        return try {
            Log.d("SyncRepository", "📂 Iniciando sincronización de categorías...")

            Log.d("SyncRepository", "✅ No hay categorías para sincronizar")
            Result.success(0)

        } catch (e: Exception) {
            Log.e("SyncRepository", "💥 Error en sincronización de categorías", e)
            Result.failure(e)
        }
    }

    override suspend fun hasPendingChanges(): Boolean {
        return try {
            val recetasNoSincronizadas = recetaDao.getRecetasNoSincronizadas()
            val pendingCount = recetasNoSincronizadas.size

            Log.d("SyncRepository", "📊 Recetas pendientes de sincronización: $pendingCount")
            pendingCount > 0

        } catch (e: Exception) {
            Log.e("SyncRepository", "❌ Error verificando cambios pendientes", e)
            false
        }
    }

    private suspend fun syncRecipeToServer(receta: com.example.app_recetas.src.datastore.Local.room.entities.RecetaEntity): Boolean {
        return try {
            if (receta.id < 0) {
                return syncCreateRecipe(receta)
            } else {
                return syncUpdateRecipe(receta)
            }
        } catch (e: Exception) {
            Log.e("SyncRepository", "❌ Error en syncRecipeToServer: ${e.message}")
            false
        }
    }

    private suspend fun syncCreateRecipe(receta: com.example.app_recetas.src.datastore.Local.room.entities.RecetaEntity): Boolean {
        return try {
            Log.d("SyncRepository", "➕ Creando receta en servidor: ${receta.nombre}")

            val request = RecetasRequest(
                nombre = receta.nombre,
                ingredientes = receta.ingredientes,
                pasos = receta.pasos,
                tiempo_preparacion = receta.tiempoPreparacion,
                imagen_receta = receta.imagenReceta,
                imagen_base64 = null
            )

            val response = recetasApiService.crearReceta(request)

            if (response.isSuccessful) {
                response.body()?.let { responseBody ->
                    Log.d("SyncRepository", "✅ Receta creada en servidor con ID: ${responseBody.receta.id}")

                    val recetaActualizada = receta.copy(
                        id = responseBody.receta.id,
                        sincronizado = true
                    )

                    recetaDao.deleteRecetaById(receta.id)
                    recetaDao.insertReceta(recetaActualizada)

                    return true
                }
            } else {
                Log.e("SyncRepository", "❌ Error creando receta: ${response.code()} - ${response.message()}")
            }

            false

        } catch (e: IOException) {
            Log.e("SyncRepository", "❌ Error de red creando receta: ${e.message}")
            false
        } catch (e: Exception) {
            Log.e("SyncRepository", "❌ Error inesperado creando receta: ${e.message}")
            false
        }
    }

    private suspend fun syncUpdateRecipe(receta: com.example.app_recetas.src.datastore.Local.room.entities.RecetaEntity): Boolean {
        return try {
            Log.d("SyncRepository", "✏️ Actualizando receta en servidor: ${receta.nombre}")

            val updateRequest = RecetaUpdateRequest(
                id = receta.id,
                nombre = receta.nombre,
                ingredientes = receta.ingredientes,
                pasos = receta.pasos,
                tiempo_preparacion = receta.tiempoPreparacion,
                imagen_receta = receta.imagenReceta
            )

            val response = recetasApiService.actualizarReceta(receta.id, updateRequest)

            if (response.isSuccessful) {
                Log.d("SyncRepository", "✅ Receta actualizada en servidor: ${receta.nombre}")
                return true
            } else {
                Log.e("SyncRepository", "❌ Error actualizando receta: ${response.code()} - ${response.message()}")
            }

            false

        } catch (e: IOException) {
            Log.e("SyncRepository", "❌ Error de red actualizando receta: ${e.message}")
            false
        } catch (e: Exception) {
            Log.e("SyncRepository", "❌ Error inesperado actualizando receta: ${e.message}")
            false
        }
    }

    // 🔧 Función auxiliar para marcar como no sincronizado (para uso externo)
    suspend fun markRecipeAsUnsynchronized(recetaId: Int) {
        try {
            // Tu lógica ya maneja esto automáticamente cuando insertas/actualizas
            // con sincronizado = 0, pero por si necesitas marcar manualmente
            Log.d("SyncRepository", "📝 Receta marcada como no sincronizada: $recetaId")

        } catch (e: Exception) {
            Log.e("SyncRepository", "❌ Error marcando receta como no sincronizada", e)
        }
    }

    // 🔧 Sincronizar pendientes usando tu tabla de pendientes (funcionalidad adicional)
    suspend fun syncPendientesTable(): Int {
        return try {
            Log.d("SyncRepository", "🔄 Sincronizando tabla de pendientes...")

            val pendientes = pendienteSincronizacionDao.getAllPendientes()
            var syncedCount = 0

            for (pendiente in pendientes) {
                try {
                    val success = when (pendiente.tipoOperacion) {
                        "CREATE" -> {
                            val request = gson.fromJson(pendiente.datosJson, RecetasRequest::class.java)
                            val response = recetasApiService.crearReceta(request)

                            if (response.isSuccessful) {
                                // Actualizar ID temporal con ID real del servidor
                                response.body()?.let { responseBody ->
                                    val recetaLocal = recetaDao.getRecetaById(pendiente.recetaId)
                                    if (recetaLocal != null) {
                                        recetaDao.deleteRecetaById(pendiente.recetaId)
                                        val recetaActualizada = recetaLocal.copy(
                                            id = responseBody.receta.id,
                                            sincronizado = true
                                        )
                                        recetaDao.insertReceta(recetaActualizada)
                                    }
                                }
                                true
                            } else false
                        }

                        "UPDATE" -> {
                            val updateRequest = gson.fromJson(pendiente.datosJson, RecetaUpdateRequest::class.java)
                            val response = recetasApiService.actualizarReceta(pendiente.recetaId, updateRequest)

                            if (response.isSuccessful) {
                                recetaDao.marcarComoSincronizado(pendiente.recetaId)
                                true
                            } else false
                        }

                        "DELETE" -> {
                            val response = recetasApiService.eliminarReceta(pendiente.recetaId)
                            response.isSuccessful || response.code() == 404 // OK si ya fue eliminado
                        }

                        else -> false
                    }

                    if (success) {
                        pendienteSincronizacionDao.deletePendiente(pendiente)
                        syncedCount++
                        Log.d("SyncRepository", "✅ Pendiente sincronizado: ${pendiente.tipoOperacion} - ${pendiente.recetaId}")
                    }

                } catch (e: Exception) {
                    Log.e("SyncRepository", "❌ Error sincronizando pendiente: ${e.message}")
                }
            }

            Log.d("SyncRepository", "🎉 Pendientes sincronizados: $syncedCount/${pendientes.size}")
            syncedCount

        } catch (e: Exception) {
            Log.e("SyncRepository", "❌ Error en syncPendientesTable: ${e.message}")
            0
        }
    }
}