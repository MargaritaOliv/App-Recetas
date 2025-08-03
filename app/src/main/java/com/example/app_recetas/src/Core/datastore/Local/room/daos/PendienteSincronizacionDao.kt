package com.example.app_recetas.src.datastore.Local.room.daos

import androidx.room.*
import com.example.app_recetas.src.datastore.Local.room.entities.PendienteSincronizacionEntity

@Dao
interface PendienteSincronizacionDao {

    @Query("SELECT * FROM pendientes_sincronizacion ORDER BY fechaCreacion ASC")
    suspend fun getAllPendientes(): List<PendienteSincronizacionEntity>

    @Insert
    suspend fun insertPendiente(pendiente: PendienteSincronizacionEntity)

    @Delete
    suspend fun deletePendiente(pendiente: PendienteSincronizacionEntity)

    @Query("DELETE FROM pendientes_sincronizacion WHERE recetaId = :recetaId")
    suspend fun deletePendientesByRecetaId(recetaId: Int)

    @Query("DELETE FROM pendientes_sincronizacion")
    suspend fun deleteAllPendientes()
}