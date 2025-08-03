package com.example.app.data.model

import com.google.gson.annotations.SerializedName

data class HomeResponse(
    @SerializedName("id")
    val id: Int,
    @SerializedName("nombre")
    val nombre: String,
    @SerializedName("ingredientes")
    val ingredientes: List<String>,
    @SerializedName("pasos")
    val pasos: List<String>,
    @SerializedName("tiempo_preparacion")
    val tiempoPreparacion: Int,
    @SerializedName("imagen_receta")
    val imagenReceta: String?
)