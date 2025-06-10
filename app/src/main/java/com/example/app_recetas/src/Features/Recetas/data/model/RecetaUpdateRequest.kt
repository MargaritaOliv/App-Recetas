package com.example.app_recetas.src.Features.Recetas.data.model

data class RecetaUpdateRequest(
    val id: Int,
    val nombre: String,
    val ingredientes: List<String>,
    val pasos: List<String>,
    val tiempo_preparacion: Int
)