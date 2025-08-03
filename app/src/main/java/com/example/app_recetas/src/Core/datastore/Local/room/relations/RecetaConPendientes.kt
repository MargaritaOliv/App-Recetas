package com.example.app_recetas.src.datastore.Local.room.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.example.app_recetas.src.datastore.Local.room.entities.RecetaEntity
import com.example.app_recetas.src.datastore.Local.room.entities.PendienteSincronizacionEntity

data class RecetaConPendientes(
    @Embedded val receta: RecetaEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "recetaId"
    )
    val pendientes: List<PendienteSincronizacionEntity>
)