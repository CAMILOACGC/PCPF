package com.example.proyecto_final.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto_final.MODELS.Motorcycle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class DashboardUiState(
    val hasMotorcycle: Boolean = false,
    val motorcycle: Motorcycle? = null,
    val isLoading: Boolean = true,
    val oilChangeRemaining: String = "--",
    val generalCheckRemaining: String = "--",
    val soatStatus: String = "--",
    val rtmStatus: String = "--",
    val lastServiceDays: String = "--",
    val pendingAlerts: String = "--"
)

class DashboardViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        val userId = auth.currentUser?.uid ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val snapshot = db.collection("motorcycles")
                    .whereEqualTo("userId", userId)
                    .limit(1)
                    .get()
                    .await()

                if (!snapshot.isEmpty) {
                    val moto = snapshot.documents[0].toObject(Motorcycle::class.java)
                    _uiState.value = _uiState.value.copy(
                        hasMotorcycle = true,
                        motorcycle = moto,
                        isLoading = false
                    )
                    // Aquí se podrían cargar documentos y mantenimientos para completar el estado
                } else {
                    _uiState.value = _uiState.value.copy(
                        hasMotorcycle = false,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
}
