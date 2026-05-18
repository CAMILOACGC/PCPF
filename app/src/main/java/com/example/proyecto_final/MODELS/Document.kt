package com.example.proyecto_final.MODELS

import java.util.Date
import java.util.Calendar

data class Document(
    val id: String = "",
    val motorcycleId: String = "",
    val type: String = "", // SOAT, Técnico-mecánica, Licencia, etc.
    val emissionDate: String = "",
    val expiryDate: String = "",
    val entity: String = "",
    val status: String = "Vigente" // Se puede calcular dinámicamente
)
