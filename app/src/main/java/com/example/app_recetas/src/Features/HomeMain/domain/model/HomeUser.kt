package com.example.app.domain.model

data class HomeUser(
    val id: Int,
    val nombre: String,
    val ingredientes: List<String>,
    val pasos: List<String>,
    val tiempoPreparacion: Int
)