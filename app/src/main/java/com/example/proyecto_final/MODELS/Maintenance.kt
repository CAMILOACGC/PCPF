package com.example.proyecto_final.MODELS

data class Maintenance(
    val id: String = "",
    val motorcycleId: String = "",
    val description: String = "",
    val status: String = "SCHEDULED",
    val schedulingType: String = "MILEAGE",
    

    val targetMileage: Int? = null,
    

    val targetDate: String? = null,
    

    val completionDate: String? = null,
    val completionMileage: Int? = null,
    val cost: Double = 0.0
)
