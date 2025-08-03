package com.example.app_recetas.src.Features.Recetas.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Base64
import com.example.app_recetas.src.Features.Recetas.data.datasource.remote.RecetasApiService
import com.example.app_recetas.src.Features.Recetas.data.model.RecetaUpdateRequest
import com.example.app_recetas.src.Features.Recetas.data.model.RecetasRequest
import com.example.app_recetas.src.Features.Recetas.data.model.RecetasResponse
import com.example.app_recetas.src.Features.Recetas.domain.model.Recetas
import com.example.app_recetas.src.Features.Recetas.domain.model.RecetasError
import com.example.app_recetas.src.Features.Recetas.domain.model.RecetasResult
import com.example.app_recetas.src.Features.Recetas.domain.repository.RecetasRepository
import com.example.app_recetas.src.Core.di.DatabaseModule
import com.example.app_recetas.src.Core.appcontext.AppContextHolder
import com.example.app_recetas.src.datastore.Local.room.entities.PendienteSincronizacionEntity
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.io.File
import java.io.IOException

class RecetasRepositoryImpl(
    private val apiService: RecetasApiService
) : RecetasRepository {


    private val recetaDao = DatabaseModule.recetaDao
    private val pendienteDao = DatabaseModule.pendienteSincronizacionDao
    private val usuarioDao = DatabaseModule.usuarioDao


    private val context = AppContextHolder.get()
    private val gson = Gson()


    private fun hasInternetConnection(): Boolean {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork

            println("🔍 [CONNECTIVITY] Network activo: $network")

            if (network == null) {
                println("❌ [CONNECTIVITY] No hay red activa")
                return false
            }

            val capabilities = connectivityManager.getNetworkCapabilities(network)
            println("🔍 [CONNECTIVITY] Capabilities: $capabilities")

            if (capabilities == null) {
                println("❌ [CONNECTIVITY] No hay capabilities de red")
                return false
            }

            val hasWifi = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
            val hasCellular = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
            val hasEthernet = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
            val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            val isValidated = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)

            println("🔍 [CONNECTIVITY] WiFi: $hasWifi, Cellular: $hasCellular, Ethernet: $hasEthernet")
            println("🔍 [CONNECTIVITY] Internet capability: $hasInternet, Validated: $isValidated")

            val result = (hasWifi || hasCellular || hasEthernet) && hasInternet
            println("🔍 [CONNECTIVITY] Resultado final: $result")

            return result
        } catch (e: Exception) {
            println("❌ [CONNECTIVITY] Error verificando conectividad: ${e.message}")
            return false
        }
    }


    private suspend fun hasRealInternetConnection(): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            println("🌐 [REAL_CONNECTIVITY] Verificando conectividad real...")


            if (!hasInternetConnection()) {
                println("❌ [REAL_CONNECTIVITY] Sin conectividad básica")
                return@withContext false
            }


            val runtime = Runtime.getRuntime()
            val process = runtime.exec("/system/bin/ping -c 1 8.8.8.8")
            val result = process.waitFor()

            println("🌐 [REAL_CONNECTIVITY] Ping result: $result")

            if (result == 0) {
                println("✅ [REAL_CONNECTIVITY] Conectividad real confirmada")
                return@withContext true
            } else {
                println("❌ [REAL_CONNECTIVITY] Ping falló")
                return@withContext false
            }
        } catch (e: Exception) {
            println("❌ [REAL_CONNECTIVITY] Error en verificación real: ${e.message}")
            return@withContext false
        }
    }


    private fun Recetas.toEntity(sincronizado: Boolean = false): com.example.app_recetas.src.datastore.Local.room.entities.RecetaEntity {
        return com.example.app_recetas.src.datastore.Local.room.entities.RecetaEntity(
            id = this.id,
            nombre = this.nombre,
            ingredientes = this.ingredientes,
            pasos = this.pasos,
            tiempoPreparacion = this.tiempoPreparacion,
            imagenReceta = this.imagenReceta,
            sincronizado = sincronizado,
            fechaActualizacion = System.currentTimeMillis()
        )
    }

    private fun com.example.app_recetas.src.datastore.Local.room.entities.RecetaEntity.toDomain(): Recetas {
        return Recetas(
            id = this.id,
            nombre = this.nombre,
            ingredientes = this.ingredientes,
            pasos = this.pasos,
            tiempoPreparacion = this.tiempoPreparacion,
            imagenReceta = this.imagenReceta
        )
    }


    override suspend fun crearReceta(receta: Recetas): RecetasResult<Recetas> = withContext(Dispatchers.IO) {
        try {
            if (hasInternetConnection()) {

                println("🌐 [DEBUG] CON INTERNET - Enviando al servidor")

                val result = crearRecetaEnServidor(receta)
                if (result is RecetasResult.Success) {
                    recetaDao.insertReceta(result.data.toEntity(sincronizado = true))
                    println("✅ [DEBUG] Receta guardada en Room como sincronizada")
                }

                return@withContext result

            } else {

                println("📱 [DEBUG] SIN INTERNET - Guardando solo en Room")


                val tempId = -(System.currentTimeMillis().toInt())
                val recetaConTempId = receta.copy(id = tempId)


                recetaDao.insertReceta(recetaConTempId.toEntity(sincronizado = false))


                val recetaJson = gson.toJson(recetaConTempId.toRequest())
                pendienteDao.insertPendiente(
                    PendienteSincronizacionEntity(
                        recetaId = tempId,
                        tipoOperacion = "CREATE",
                        datosJson = recetaJson
                    )
                )

                println("✅ [DEBUG] Receta guardada localmente, pendiente de sincronización")
                return@withContext RecetasResult.Success(recetaConTempId)
            }
        } catch (e: Exception) {
            println("❌ [DEBUG] Error en crearReceta: ${e.message}")
            return@withContext RecetasResult.Error(RecetasError.UnknownError(e.message ?: "Error desconocido"))
        }
    }

    private suspend fun crearRecetaEnServidor(receta: Recetas): RecetasResult<Recetas> {
        return try {
            val response: Response<RecetasResponse> = if (receta.imagenReceta != null && File(receta.imagenReceta!!).exists()) {
                crearRecetaConBase64(receta)
            } else {
                crearRecetaSinImagen(receta)
            }

            if (response.isSuccessful) {
                response.body()?.let { responseBody ->
                    val nuevaReceta = Recetas(
                        id = responseBody.receta.id,
                        nombre = responseBody.receta.nombre,
                        ingredientes = responseBody.receta.ingredientes,
                        pasos = responseBody.receta.pasos,
                        tiempoPreparacion = responseBody.receta.tiempo_preparacion,
                        imagenReceta = responseBody.receta.imagen_receta
                    )
                    RecetasResult.Success(nuevaReceta)
                } ?: RecetasResult.Error(RecetasError.UnknownError("Respuesta vacía del servidor"))
            } else {
                handleErrorResponse(response.code())
            }
        } catch (e: IOException) {
            RecetasResult.Error(RecetasError.NetworkError)
        } catch (e: Exception) {
            RecetasResult.Error(RecetasError.UnknownError(e.message ?: "Error desconocido"))
        }
    }
//
    override suspend fun obtenerRecetas(): RecetasResult<List<Recetas>> = withContext(Dispatchers.IO) {
        try {
            if (hasInternetConnection()) {
                println("🌐 [DEBUG] CON INTERNET - Obteniendo del servidor")
                val result = obtenerRecetasDelServidor()

                if (result is RecetasResult.Success) {
                    val entities = result.data.map { it.toEntity(sincronizado = true) }
                    recetaDao.insertRecetas(entities)
                    println("✅ [DEBUG] Room actualizado con datos del servidor")
                    sincronizarPendientes()
                }

                result
            } else {
                println("📱 [DEBUG] SIN INTERNET - Obteniendo de Room")


                val entities = recetaDao.getAllRecetas().first()
                val recetas = entities.map { it.toDomain() }
                RecetasResult.Success(recetas)
            }
        } catch (e: Exception) {
            println("❌ [DEBUG] Error en obtenerRecetas: ${e.message}")
            RecetasResult.Error(RecetasError.UnknownError(e.message ?: "Error desconocido"))
        }
    }


   override fun obtenerRecetasFlow(): Flow<List<Recetas>> {
        println("🔄 [FLOW] Configurando Flow de recetas desde Room")
        return recetaDao.getAllRecetas().map { entities ->
            println("🔄 [FLOW] Flow emitió ${entities.size} recetas")
            entities.map { it.toDomain() }
        }
    }


    suspend fun forzarSincronizacion(): RecetasResult<List<Recetas>> = withContext(Dispatchers.IO) {
        return@withContext try {
            println("🔄 [FORCE_SYNC] ========== FORZANDO SINCRONIZACIÓN ==========")

            if (!hasInternetConnection()) {
                println("❌ [FORCE_SYNC] Sin conectividad, no se puede sincronizar")
                return@withContext RecetasResult.Error(RecetasError.NetworkError)
            }


            println("🔄 [FORCE_SYNC] Sincronizando pendientes...")
            sincronizarPendientes()


            println("🔄 [FORCE_SYNC] Obteniendo datos del servidor...")
            val result = obtenerRecetasDelServidor()

            if (result is RecetasResult.Success) {

                val entities = result.data.map { it.toEntity(sincronizado = true) }
                recetaDao.insertRecetas(entities)
                println("✅ [FORCE_SYNC] Sincronización completada exitosamente")
            }

            result
        } catch (e: Exception) {
            println("❌ [FORCE_SYNC] Error en sincronización forzada: ${e.message}")
            RecetasResult.Error(RecetasError.UnknownError("Error en sincronización: ${e.message}"))
        }
    }

    private suspend fun obtenerRecetasDelServidor(): RecetasResult<List<Recetas>> {
        return try {
            println("🌐 [SERVER] ========== LLAMADA AL SERVIDOR ==========")
            println("🌐 [SERVER] Iniciando llamada a apiService.obtenerRecetas()")

            val startTime = System.currentTimeMillis()
            val response = apiService.obtenerRecetas()
            val endTime = System.currentTimeMillis()

            println("🌐 [SERVER] Tiempo de respuesta: ${endTime - startTime}ms")
            println("🌐 [SERVER] Response code: ${response.code()}")
            println("🌐 [SERVER] Response successful: ${response.isSuccessful}")
            println("🌐 [SERVER] Response message: ${response.message()}")

            if (!response.isSuccessful) {
                try {
                    val errorBody = response.errorBody()?.string()
                    println("❌ [SERVER] Error body: $errorBody")
                } catch (e: Exception) {
                    println("❌ [SERVER] Error leyendo error body: ${e.message}")
                }
            }

            if (response.isSuccessful) {
                response.body()?.let { responseList ->
                    println("✅ [SERVER] Response body recibido con ${responseList.size} recetas")

                    val recetas = responseList.mapIndexed { index, recetaResponse ->
                        println("📝 [SERVER] Procesando receta $index: ${recetaResponse.nombre}")
                        Recetas(
                            id = recetaResponse.id,
                            nombre = recetaResponse.nombre,
                            ingredientes = recetaResponse.ingredientes,
                            pasos = recetaResponse.pasos,
                            tiempoPreparacion = recetaResponse.tiempo_preparacion,
                            imagenReceta = recetaResponse.imagen_receta
                        )
                    }

                    println("✅ [SERVER] ${recetas.size} recetas procesadas correctamente")
                    RecetasResult.Success(recetas)
                } ?: run {
                    println("❌ [SERVER] Response body es null")
                    RecetasResult.Error(RecetasError.UnknownError("Respuesta vacía del servidor"))
                }
            } else {
                println("❌ [SERVER] Response no exitoso")
                handleErrorResponse(response.code())
            }
        } catch (e: IOException) {
            println("❌ [SERVER] IOException: ${e.message}")
            println("❌ [SERVER] IOException cause: ${e.cause}")
            RecetasResult.Error(RecetasError.NetworkError)
        } catch (e: Exception) {
            println("❌ [SERVER] Exception general: ${e.message}")
            println("❌ [SERVER] Exception type: ${e.javaClass.simpleName}")
            println("❌ [SERVER] Stack trace: ${e.stackTrace.take(5).joinToString("\n")}")
            RecetasResult.Error(RecetasError.UnknownError(e.message ?: "Error desconocido del servidor"))
        }
    }


    override suspend fun obtenerReceta(recetaId: Int): RecetasResult<Recetas> = withContext(Dispatchers.IO) {
        try {
            if (hasInternetConnection()) {

                println("🌐 [DEBUG] CON INTERNET - Obteniendo receta $recetaId del servidor")
                val result = obtenerRecetaDelServidor(recetaId)

                if (result is RecetasResult.Success) {

                    recetaDao.insertReceta(result.data.toEntity(sincronizado = true))
                }

                return@withContext result
            } else {

                println("📱 [DEBUG] SIN INTERNET - Obteniendo receta $recetaId de Room")
                val entity = recetaDao.getRecetaById(recetaId)

                return@withContext if (entity != null) {
                    RecetasResult.Success(entity.toDomain())
                } else {
                    RecetasResult.Error(RecetasError.ValidationError("Receta no encontrada"))
                }
            }
        } catch (e: Exception) {
            println("❌ [DEBUG] Error en obtenerReceta: ${e.message}")
            return@withContext RecetasResult.Error(RecetasError.UnknownError(e.message ?: "Error desconocido"))
        }
    }

    private suspend fun obtenerRecetaDelServidor(recetaId: Int): RecetasResult<Recetas> {
        return try {
            val response = apiService.obtenerReceta(recetaId)

            if (response.isSuccessful) {
                response.body()?.let { responseBody ->
                    val receta = Recetas(
                        id = responseBody.id,
                        nombre = responseBody.nombre,
                        ingredientes = responseBody.ingredientes,
                        pasos = responseBody.pasos,
                        tiempoPreparacion = responseBody.tiempo_preparacion,
                        imagenReceta = responseBody.imagen_receta
                    )
                    RecetasResult.Success(receta)
                } ?: RecetasResult.Error(RecetasError.UnknownError("Respuesta vacía del servidor"))
            } else if (response.code() == 404) {
                RecetasResult.Error(RecetasError.ValidationError("Receta no encontrada"))
            } else {
                handleErrorResponse(response.code())
            }
        } catch (e: IOException) {
            RecetasResult.Error(RecetasError.NetworkError)
        } catch (e: Exception) {
            RecetasResult.Error(RecetasError.UnknownError(e.message ?: "Error desconocido"))
        }
    }


    override suspend fun actualizarReceta(receta: Recetas): RecetasResult<Recetas> = withContext(Dispatchers.IO) {
        try {
            if (hasInternetConnection()) {

                println("🌐 [DEBUG] CON INTERNET - Actualizando receta ${receta.id} en servidor")
                val result = actualizarRecetaEnServidor(receta)

                if (result is RecetasResult.Success) {

                    recetaDao.updateReceta(result.data.toEntity(sincronizado = true))
                }

                return@withContext result
            } else {

                println("📱 [DEBUG] SIN INTERNET - Actualizando receta ${receta.id} solo en Room")


                recetaDao.updateReceta(receta.toEntity(sincronizado = false))


                val recetaJson = gson.toJson(receta.toUpdateRequest())
                pendienteDao.insertPendiente(
                    PendienteSincronizacionEntity(
                        recetaId = receta.id,
                        tipoOperacion = "UPDATE",
                        datosJson = recetaJson
                    )
                )

                println("✅ [DEBUG] Receta actualizada localmente, pendiente de sincronización")
                return@withContext RecetasResult.Success(receta)
            }
        } catch (e: Exception) {
            println("❌ [DEBUG] Error en actualizarReceta: ${e.message}")
            return@withContext RecetasResult.Error(RecetasError.UnknownError(e.message ?: "Error desconocido"))
        }
    }

    private suspend fun actualizarRecetaEnServidor(receta: Recetas): RecetasResult<Recetas> {
        return try {
            val request = RecetaUpdateRequest(
                id = receta.id,
                nombre = receta.nombre,
                ingredientes = receta.ingredientes,
                pasos = receta.pasos,
                tiempo_preparacion = receta.tiempoPreparacion,
                imagen_receta = receta.imagenReceta
            )

            val response = apiService.actualizarReceta(receta.id, request)

            if (response.isSuccessful) {
                try {
                    val getResponse = apiService.obtenerReceta(receta.id)
                    if (getResponse.isSuccessful) {
                        getResponse.body()?.let { recetaActualizada ->
                            val recetaUpdated = Recetas(
                                id = recetaActualizada.id,
                                nombre = recetaActualizada.nombre,
                                ingredientes = recetaActualizada.ingredientes,
                                pasos = recetaActualizada.pasos,
                                tiempoPreparacion = recetaActualizada.tiempo_preparacion,
                                imagenReceta = recetaActualizada.imagen_receta
                            )
                            RecetasResult.Success(recetaUpdated)
                        } ?: RecetasResult.Success(receta)
                    } else {
                        RecetasResult.Success(receta)
                    }
                } catch (e: Exception) {
                    RecetasResult.Success(receta)
                }
            } else if (response.code() == 404) {
                RecetasResult.Error(RecetasError.ValidationError("Receta no encontrada"))
            } else {
                handleErrorResponse(response.code())
            }
        } catch (e: IOException) {
            RecetasResult.Error(RecetasError.NetworkError)
        } catch (e: Exception) {
            RecetasResult.Error(RecetasError.UnknownError(e.message ?: "Error desconocido"))
        }
    }


    override suspend fun eliminarReceta(recetaId: Int): RecetasResult<Unit> = withContext(Dispatchers.IO) {
        try {
            if (hasInternetConnection()) {

                println("🌐 [DEBUG] CON INTERNET - Eliminando receta $recetaId del servidor")
                val result = eliminarRecetaDelServidor(recetaId)

                if (result is RecetasResult.Success) {

                    recetaDao.deleteRecetaById(recetaId)
                }

                return@withContext result
            } else {

                println("📱 [DEBUG] SIN INTERNET - Marcando receta $recetaId para eliminación")


                pendienteDao.insertPendiente(
                    PendienteSincronizacionEntity(
                        recetaId = recetaId,
                        tipoOperacion = "DELETE",
                        datosJson = null
                    )
                )


                recetaDao.deleteRecetaById(recetaId)

                println("✅ [DEBUG] Receta marcada para eliminación, pendiente de sincronización")
                return@withContext RecetasResult.Success(Unit)
            }
        } catch (e: Exception) {
            println("❌ [DEBUG] Error en eliminarReceta: ${e.message}")
            return@withContext RecetasResult.Error(RecetasError.UnknownError(e.message ?: "Error desconocido"))
        }
    }

    private suspend fun eliminarRecetaDelServidor(recetaId: Int): RecetasResult<Unit> {
        return try {
            val response = apiService.eliminarReceta(recetaId)

            if (response.isSuccessful) {
                RecetasResult.Success(Unit)
            } else if (response.code() == 404) {
                RecetasResult.Error(RecetasError.ValidationError("Receta no encontrada"))
            } else {
                handleErrorResponse(response.code())
            }
        } catch (e: IOException) {
            RecetasResult.Error(RecetasError.NetworkError)
        } catch (e: Exception) {
            RecetasResult.Error(RecetasError.UnknownError(e.message ?: "Error desconocido"))
        }
    }


    suspend fun sincronizarPendientes() = withContext(Dispatchers.IO) {
        try {
            if (!hasInternetConnection()) {
                println("📱 [DEBUG] Sin internet, no se puede sincronizar")
                return@withContext
            }

            println("🔄 [DEBUG] Iniciando sincronización de pendientes")
            val pendientes = pendienteDao.getAllPendientes()

            for (pendiente in pendientes) {
                try {
                    when (pendiente.tipoOperacion) {
                        "CREATE" -> {
                            println("🔄 [DEBUG] Sincronizando CREATE para receta ${pendiente.recetaId}")
                            val recetaRequest = gson.fromJson(pendiente.datosJson, RecetasRequest::class.java)
                            val response = apiService.crearReceta(recetaRequest)

                            if (response.isSuccessful) {
                                response.body()?.let { responseBody ->

                                    val recetaLocal = recetaDao.getRecetaById(pendiente.recetaId)
                                    if (recetaLocal != null) {

                                        recetaDao.deleteRecetaById(pendiente.recetaId)

                                        val recetaSincronizada = recetaLocal.copy(
                                            id = responseBody.receta.id,
                                            sincronizado = true
                                        )
                                        recetaDao.insertReceta(recetaSincronizada)
                                    }
                                }
                                pendienteDao.deletePendiente(pendiente)
                                println("✅ [DEBUG] CREATE sincronizado para receta ${pendiente.recetaId}")
                            }
                        }

                        "UPDATE" -> {
                            println("🔄 [DEBUG] Sincronizando UPDATE para receta ${pendiente.recetaId}")
                            val updateRequest = gson.fromJson(pendiente.datosJson, RecetaUpdateRequest::class.java)
                            val response = apiService.actualizarReceta(pendiente.recetaId, updateRequest)

                            if (response.isSuccessful) {
                                recetaDao.marcarComoSincronizado(pendiente.recetaId)
                                pendienteDao.deletePendiente(pendiente)
                                println("✅ [DEBUG] UPDATE sincronizado para receta ${pendiente.recetaId}")
                            }
                        }

                        "DELETE" -> {
                            println("🔄 [DEBUG] Sincronizando DELETE para receta ${pendiente.recetaId}")
                            val response = apiService.eliminarReceta(pendiente.recetaId)

                            if (response.isSuccessful || response.code() == 404) {
                                // Si fue exitoso o la receta ya no existe, eliminar el pendiente
                                pendienteDao.deletePendiente(pendiente)
                                println("✅ [DEBUG] DELETE sincronizado para receta ${pendiente.recetaId}")
                            }
                        }
                    }
                } catch (e: Exception) {
                    println("❌ [DEBUG] Error sincronizando ${pendiente.tipoOperacion} para receta ${pendiente.recetaId}: ${e.message}")
                    // Continuar con el siguiente pendiente
                }
            }

            println("✅ [DEBUG] Sincronización completada")
        } catch (e: Exception) {
            println("❌ [DEBUG] Error en sincronizarPendientes: ${e.message}")
        }
    }


    private fun Recetas.toRequest(): RecetasRequest {
        return RecetasRequest(
            nombre = this.nombre,
            ingredientes = this.ingredientes,
            pasos = this.pasos,
            tiempo_preparacion = this.tiempoPreparacion,
            imagen_receta = this.imagenReceta,
            imagen_base64 = null
        )
    }

    private fun Recetas.toUpdateRequest(): RecetaUpdateRequest {
        return RecetaUpdateRequest(
            id = this.id,
            nombre = this.nombre,
            ingredientes = this.ingredientes,
            pasos = this.pasos,
            tiempo_preparacion = this.tiempoPreparacion,
            imagen_receta = this.imagenReceta
        )
    }

    private suspend fun crearRecetaConBase64(receta: Recetas): Response<RecetasResponse> {
        try {
            val file = File(receta.imagenReceta!!)
            val bytes = file.readBytes()
            val base64String = Base64.encodeToString(bytes, Base64.NO_WRAP)

            val mimeType = when (file.extension.lowercase()) {
                "png" -> "image/png"
                "gif" -> "image/gif"
                "webp" -> "image/webp"
                else -> "image/jpeg"
            }

            val base64WithPrefix = "data:$mimeType;base64,$base64String"

            val request = RecetasRequest(
                nombre = receta.nombre,
                ingredientes = receta.ingredientes,
                pasos = receta.pasos,
                tiempo_preparacion = receta.tiempoPreparacion,
                imagen_receta = null,
                imagen_base64 = base64WithPrefix
            )

            return apiService.crearReceta(request)

        } catch (e: Exception) {
            throw e
        }
    }

    private suspend fun crearRecetaSinImagen(receta: Recetas): Response<RecetasResponse> {
        val request = RecetasRequest(
            nombre = receta.nombre,
            ingredientes = receta.ingredientes,
            pasos = receta.pasos,
            tiempo_preparacion = receta.tiempoPreparacion,
            imagen_receta = null,
            imagen_base64 = null
        )

        return apiService.crearReceta(request)
    }

    private fun handleErrorResponse(code: Int): RecetasResult.Error {
        println("❌ [ERROR_HANDLER] ========== MANEJO DE ERROR HTTP ==========")
        println("❌ [ERROR_HANDLER] Código HTTP: $code")

        return when (code) {
            401 -> {
                println("❌ [ERROR_HANDLER] Error 401: Token no válido o expirado")
                RecetasResult.Error(RecetasError.UnauthorizedError)
            }
            403 -> {
                println("❌ [ERROR_HANDLER] Error 403: Acceso prohibido")
                RecetasResult.Error(RecetasError.UnauthorizedError)
            }
            404 -> {
                println("❌ [ERROR_HANDLER] Error 404: Recurso no encontrado")
                RecetasResult.Error(RecetasError.ValidationError("Recurso no encontrado"))
            }
            408 -> {
                println("❌ [ERROR_HANDLER] Error 408: Timeout de conexión")
                RecetasResult.Error(RecetasError.NetworkError)
            }
            in 400..499 -> {
                println("❌ [ERROR_HANDLER] Error 400-499: Error del cliente - $code")
                RecetasResult.Error(RecetasError.ValidationError("Error de validación - código: $code"))
            }
            500 -> {
                println("❌ [ERROR_HANDLER] Error 500: Error interno del servidor")
                RecetasResult.Error(RecetasError.ServerError)
            }
            502 -> {
                println("❌ [ERROR_HANDLER] Error 502: Bad Gateway")
                RecetasResult.Error(RecetasError.NetworkError)
            }
            503 -> {
                println("❌ [ERROR_HANDLER] Error 503: Servicio no disponible")
                RecetasResult.Error(RecetasError.ServerError)
            }
            504 -> {
                println("❌ [ERROR_HANDLER] Error 504: Gateway Timeout")
                RecetasResult.Error(RecetasError.NetworkError)
            }
            in 500..599 -> {
                println("❌ [ERROR_HANDLER] Error 500-599: Error del servidor - $code")
                RecetasResult.Error(RecetasError.ServerError)
            }
            else -> {
                println("❌ [ERROR_HANDLER] Error desconocido: $code")
                RecetasResult.Error(RecetasError.UnknownError("Error HTTP desconocido: $code"))
            }
        }
    }
}