package com.example.app_recetas.src.Features.Recetas.domain.model

data class Recetas(
    val id: Int = 0,
    val nombre: String,
    val ingredientes: List<String>,
    val pasos: List<String>,
    val tiempoPreparacion: Int
)
