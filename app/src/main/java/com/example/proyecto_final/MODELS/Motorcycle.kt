package com.example.proyecto_final.MODELS

data class Motorcycle(
    val id: String = "",
    val userId: String = "",
    val brand: String = "",
    val model: String = "",
    val year: Int = 0,
    val licensePlate: String = "",
    val currentMileage: Int = 0,
    val imageUrl: String? = null
)
