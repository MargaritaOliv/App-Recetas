package com.example.app_recetas.src.datastore.Local.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usuario")
data class UsuarioEntity(
    @PrimaryKey
    val id: Int = 1,
    val correo: String,
    val token: String,
    val fechaLogin: Long = System.currentTimeMillis()
)
