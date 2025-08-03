package com.example.app_recetas.src.Features.Recetas.data.model

import com.google.gson.annotations.SerializedName

data class RecetasRequest(
    val nombre: String,
    val ingredientes: List<String>,
    val pasos: List<String>,
    @SerializedName("tiempo_preparacion")
    val tiempo_preparacion: Int,
    @SerializedName("imagen_receta")
    val imagen_receta: String?,
    @SerializedName("imagen_base64")  // ✅ Agregar este campo
    val imagen_base64: String? = null
)