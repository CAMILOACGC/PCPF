package com.example.proyecto_final.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class DashboardUiState(
    val motorcycleName: String = "Honda CB500F 2022",
    val mileage: String = "15,450",
    val oilChangeRemaining: String = "En 550 km",
    val generalCheckRemaining: String = "En 1,550 km",
    val soatStatus: String = "Vigente",
    val rtmStatus: String = "Próximo a vencer",
    val lastServiceDays: String = "Hace 15 días",
    val pendingAlerts: String = "1 pendiente"
)

class DashboardViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState
}
