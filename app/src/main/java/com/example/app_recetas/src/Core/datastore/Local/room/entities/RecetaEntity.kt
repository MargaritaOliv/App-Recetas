package com.example.app_recetas.src.datastore.Local.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recetas")
data class RecetaEntity(
    @PrimaryKey
    val id: Int,
    val nombre: String,
    val ingredientes: List<String>,
    val pasos: List<String>,
    val tiempoPreparacion: Int,
    val imagenReceta: String?,
    val sincronizado: Boolean = false,
    val fechaCreacion: Long = System.currentTimeMillis(),
    val fechaActualizacion: Long = System.currentTimeMillis()
)