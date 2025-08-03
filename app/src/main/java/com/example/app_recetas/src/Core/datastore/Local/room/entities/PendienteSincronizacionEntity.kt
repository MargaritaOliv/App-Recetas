package com.example.app_recetas.src.datastore.Local.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pendientes_sincronizacion")
data class PendienteSincronizacionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val recetaId: Int,
    val tipoOperacion: String,
    val datosJson: String?,
    val fechaCreacion: Long = System.currentTimeMillis()
)