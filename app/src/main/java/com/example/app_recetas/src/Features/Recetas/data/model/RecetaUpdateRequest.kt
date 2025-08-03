package com.example.app_recetas.src.Features.Recetas.data.model

import com.google.gson.annotations.SerializedName

data class RecetaUpdateRequest(
    val id: Int,
    val nombre: String,
    val ingredientes: List<String>,
    val pasos: List<String>,
    @SerializedName("tiempo_preparacion")
    val tiempo_preparacion: Int,
    @SerializedName("imagen_receta")
    val imagen_receta: String?
)