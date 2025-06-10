package com.example.app_recetas.src.Features.Recetas.data.model

data class RecetasResponse(
    val message: String,
    val receta: RecetaData
)

data class RecetaData(
    val id: Int,
    val nombre: String,
    val ingredientes: List<String>,
    val pasos: List<String>,
    val tiempo_preparacion: Int
)