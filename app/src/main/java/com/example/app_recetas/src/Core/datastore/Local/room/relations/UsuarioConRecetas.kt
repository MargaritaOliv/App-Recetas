package com.example.app_recetas.src.datastore.Local.room.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.example.app_recetas.src.datastore.Local.room.entities.UsuarioEntity
import com.example.app_recetas.src.datastore.Local.room.entities.RecetaEntity

data class UsuarioConRecetas(
    @Embedded val usuario: UsuarioEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id" // Asumiendo que las recetas están asociadas al usuario por algún campo
    )
    val recetas: List<RecetaEntity>
)
