package com.example.proyecto_final.MODELS

data class Maintenance(
    val id: String = "",
    val motorcycleId: String = "",
    val description: String = "",
    val status: String = "SCHEDULED", // SCHEDULED, COMPLETED
    val schedulingType: String = "MILEAGE", // MILEAGE, TIME
    
    // For MILEAGE type
    val targetMileage: Int? = null,
    
    // For TIME type
    val targetDate: String? = null,
    
    // Completion data
    val completionDate: String? = null,
    val completionMileage: Int? = null,
    val cost: Double = 0.0
)
