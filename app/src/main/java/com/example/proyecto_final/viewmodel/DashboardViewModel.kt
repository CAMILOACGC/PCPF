package com.example.proyecto_final.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto_final.MODELS.Document
import com.example.proyecto_final.MODELS.Maintenance
import com.example.proyecto_final.MODELS.Motorcycle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

data class DashboardUiState(
    val hasMotorcycle: Boolean = false,
    val motorcycle: Motorcycle? = null,
    val isLoading: Boolean = true,
    val topItems: List<UnifiedItem> = emptyList()
)

class DashboardViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _uiState.value = _uiState.value.copy(isLoading = false)
            return
        }
        
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
                    val moto = doc.toObject(Motorcycle::class.java)
                    val motoId = doc.id
                    val currentMileage = moto?.currentMileage ?: 0
                    
                    _uiState.value = _uiState.value.copy(
                        hasMotorcycle = true,
                        motorcycle = moto
                    )
                    loadSummaryItems(motoId, currentMileage)
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

    private suspend fun loadSummaryItems(motoId: String, currentMileage: Int) {
        try {
            val docsSnapshot = db.collection("documents").whereEqualTo("motorcycleId", motoId).get().await()
            val docsList = docsSnapshot.toObjects(Document::class.java)

            val maintSnapshot = db.collection("maintenance").whereEqualTo("motorcycleId", motoId).get().await()
            val maintList = maintSnapshot.toObjects(Maintenance::class.java)

            val allItems = mutableListOf<UnifiedItem>()

            docsList.forEach { doc ->
                val statusInfo = calculateStatus(doc.expiryDate, null, currentMileage)
                allItems.add(
                    UnifiedItem(
                        id = doc.id,
                        title = doc.type,
                        subtitle = doc.entity,
                        date = doc.expiryDate,
                        targetValue = doc.expiryDate,
                        status = statusInfo.first,
                        progress = statusInfo.second,
                        isMaintenance = false
                    )
                )
            }

            maintList.forEach { maint ->
                val statusInfo = calculateStatus(maint.targetDate, maint.targetMileage, currentMileage)
                val targetLabel = if (maint.schedulingType == "MILEAGE") "${maint.targetMileage} km" else maint.targetDate ?: ""
                allItems.add(
                    UnifiedItem(
                        id = maint.id,
                        title = maint.description,
                        subtitle = if (maint.schedulingType == "MILEAGE") "Mantenimiento por KM" else "Mantenimiento por Fecha",
                        date = maint.targetDate ?: "",
                        targetValue = targetLabel,
                        status = statusInfo.first,
                        progress = statusInfo.second,
                        isMaintenance = true
                    )
                )
            }

            // Ordenar por prioridad: Crítico > Pendiente > Vigente
            val sortedItems = allItems.sortedWith(compareBy(
                { it.status !is ItemStatus.Critico },
                { it.status !is ItemStatus.Pendiente }
            )).take(4)

            _uiState.value = _uiState.value.copy(
                topItems = sortedItems,
                isLoading = false
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    private fun calculateStatus(targetDateStr: String?, targetMileage: Int?, currentMileage: Int): Pair<ItemStatus, Float> {
        val now = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        
        if (targetMileage != null && targetMileage > 0) {
            val progress = (currentMileage.toFloat() / targetMileage.toFloat()).coerceIn(0f, 1f)
            return when {
                currentMileage > targetMileage -> ItemStatus.Critico to 1.0f
                currentMileage == targetMileage -> ItemStatus.Pendiente to 1.0f
                currentMileage >= (targetMileage * 0.9) -> ItemStatus.Pendiente to progress
                else -> ItemStatus.Vigente to progress
            }
        }

        if (!targetDateStr.isNullOrBlank()) {
            try {
                val targetDate = dateFormat.parse(targetDateStr) ?: return ItemStatus.Vigente to 0f
                val cal = Calendar.getInstance().apply {
                    time = targetDate
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val diffDays = (cal.time.time - now.time) / (1000 * 60 * 60 * 24)

                return when {
                    diffDays < 0 -> ItemStatus.Critico to 1.0f
                    diffDays <= 2 -> ItemStatus.Pendiente to 0.9f
                    else -> ItemStatus.Vigente to (1f - (diffDays.toFloat() / 365f)).coerceIn(0f, 1f)
                }
            } catch (e: Exception) {
                return ItemStatus.Vigente to 0f
            }
        }
        return ItemStatus.Vigente to 0f
    }
}
