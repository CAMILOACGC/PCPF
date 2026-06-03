package com.example.proyecto_final.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto_final.MODELS.Maintenance
import com.example.proyecto_final.MODELS.Motorcycle
import com.example.proyecto_final.utils.NotificationHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class MaintenanceUiState(
    val hasMotorcycle: Boolean = false,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val maintenanceList: List<Maintenance> = emptyList(),
    val motorcycleId: String? = null,
    val currentMileage: Int = 0
)

class MaintenanceViewModel(application: Application) : AndroidViewModel(application) {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val notificationHelper = NotificationHelper(application)

    var selectedTab by mutableIntStateOf(0)
        private set

    private val _uiState = MutableStateFlow(MaintenanceUiState())
    val uiState: StateFlow<MaintenanceUiState> = _uiState

    init {
        checkMotorcycleAndLoadData()
    }

    fun onTabSelected(index: Int) {
        selectedTab = index
    }

    private fun checkMotorcycleAndLoadData() {
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
                    val doc = snapshot.documents[0]
                    val motoId = doc.id
                    val moto = doc.toObject(Motorcycle::class.java)
                    val currentMileage = moto?.currentMileage ?: 0
                    
                    _uiState.value = _uiState.value.copy(
                        hasMotorcycle = true,
                        motorcycleId = motoId,
                        currentMileage = currentMileage
                    )
                    loadMaintenance(motoId, currentMileage)
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

    private fun loadMaintenance(motoId: String, currentMileage: Int) {
        viewModelScope.launch {
            try {
                val snapshot = db.collection("maintenance")
                    .whereEqualTo("motorcycleId", motoId)
                    .get()
                    .await()

                val list = snapshot.toObjects(Maintenance::class.java)
                _uiState.value = _uiState.value.copy(
                    maintenanceList = list,
                    isLoading = false
                )
                
                checkMaintenanceAlerts(list, currentMileage)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    private fun checkMaintenanceAlerts(list: List<Maintenance>, currentMileage: Int) {
        list.forEach { maintenance ->
            if (maintenance.status == "SCHEDULED" && maintenance.schedulingType == "MILEAGE") {
                val target = maintenance.targetMileage ?: 0
                if (currentMileage >= target) {
                    notificationHelper.showNotification(
                        NotificationHelper.CHANNEL_MAINTENANCE_ID,
                        "Mantenimiento Vencido",
                        "Es hora de: ${maintenance.description}. Kilometraje alcanzado.",
                        maintenance.id.hashCode()
                    )
                } else if (target - currentMileage <= 100) {
                    notificationHelper.showNotification(
                        NotificationHelper.CHANNEL_MAINTENANCE_ID,
                        "Próximo Mantenimiento",
                        "Faltan pocos km para: ${maintenance.description}",
                        maintenance.id.hashCode()
                    )
                }
            }
        }
    }

    fun addMaintenance(description: String, type: String, value: String, isCompleted: Boolean = false) {
        val motoId = _uiState.value.motorcycleId ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            try {
                val docRef = db.collection("maintenance").document()
                val maintenance = Maintenance(
                    id = docRef.id,
                    motorcycleId = motoId,
                    description = description,
                    status = if (isCompleted) "COMPLETED" else "SCHEDULED",
                    schedulingType = type,
                    targetMileage = if (type == "MILEAGE") value.toIntOrNull() else null,
                    targetDate = if (type == "TIME") value else null,
                    completionDate = if (isCompleted) value else null,
                    completionMileage = if (isCompleted) value.toIntOrNull() else null
                )
                
                docRef.set(maintenance).await()
                checkMotorcycleAndLoadData() // Recargar todo
                _uiState.value = _uiState.value.copy(isSaving = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSaving = false)
            }
        }
    }
}
