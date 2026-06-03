package com.example.proyecto_final.viewmodel

import androidx.compose.ui.graphics.Color
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

sealed class ItemStatus(val color: Color, val label: String) {
    object Vigente : ItemStatus(Color(0xFF4CAF50), "Vigente")
    object Pendiente : ItemStatus(Color(0xFFFFA000), "Pendiente")
    object Critico : ItemStatus(Color(0xFFF44336), "Crítico")
}

data class UnifiedItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val date: String,
    val targetValue: String,
    val status: ItemStatus,
    val isMaintenance: Boolean,
    val progress: Float
)

data class DocumentsUiState(
    val hasMotorcycle: Boolean = false,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val items: List<UnifiedItem> = emptyList(),
    val motorcycleId: String? = null,
    val currentMileage: Int = 0
)

class DocumentsViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    private val _uiState = MutableStateFlow(DocumentsUiState())
    val uiState: StateFlow<DocumentsUiState> = _uiState

    init {
        checkMotorcycleAndLoadData()
    }

    fun checkMotorcycleAndLoadData() {
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
                    val moto = doc.toObject(Motorcycle::class.java)
                    val motoId = doc.id
                    val currentMileage = moto?.currentMileage ?: 0
                    
                    _uiState.value = _uiState.value.copy(
                        hasMotorcycle = true,
                        motorcycleId = motoId,
                        currentMileage = currentMileage
                    )
                    loadAllItems(motoId, currentMileage)
                } else {
                    _uiState.value = _uiState.value.copy(hasMotorcycle = false, isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    private suspend fun loadAllItems(motoId: String, currentMileage: Int) {
        try {
            val docsSnapshot = db.collection("documents").whereEqualTo("motorcycleId", motoId).get().await()
            val docsList = docsSnapshot.toObjects(Document::class.java)

            val maintSnapshot = db.collection("maintenance").whereEqualTo("motorcycleId", motoId).get().await()
            val maintList = maintSnapshot.toObjects(Maintenance::class.java)

            val unifiedItems = mutableListOf<UnifiedItem>()

            docsList.forEach { doc ->
                val statusInfo = calculateStatus(doc.expiryDate, null, currentMileage)
                unifiedItems.add(
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
                unifiedItems.add(
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

            _uiState.value = _uiState.value.copy(
                items = unifiedItems.sortedWith(compareBy({ it.status is ItemStatus.Critico }, { it.status is ItemStatus.Pendiente }, { it.status is ItemStatus.Vigente })),
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
        
        // Lógica por Kilometraje
        if (targetMileage != null && targetMileage > 0) {
            val progress = (currentMileage.toFloat() / targetMileage.toFloat()).coerceIn(0f, 1f)
            return when {
                currentMileage > targetMileage -> ItemStatus.Critico to 1.0f
                currentMileage == targetMileage -> ItemStatus.Pendiente to 1.0f
                currentMileage >= (targetMileage * 0.9) -> ItemStatus.Pendiente to progress
                else -> ItemStatus.Vigente to progress
            }
        }

        // Lógica por Fecha
        if (!targetDateStr.isNullOrBlank()) {
            try {
                val targetDate = dateFormat.parse(targetDateStr) ?: return ItemStatus.Vigente to 0f
                val targetCal = Calendar.getInstance().apply {
                    time = targetDate
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                
                val diffMillis = targetCal.time.time - now.time
                val diffDays = diffMillis / (1000 * 60 * 60 * 24)

                return when {
                    diffMillis < 0 -> ItemStatus.Critico to 1.0f
                    diffDays <= 2 -> ItemStatus.Pendiente to 0.9f
                    else -> ItemStatus.Vigente to (1f - (diffDays.toFloat() / 30f)).coerceIn(0f, 1f) // Progress simplified to 30 days window for documents
                }
            } catch (e: Exception) {
                return ItemStatus.Vigente to 0f
            }
        }

        return ItemStatus.Vigente to 0f
    }

    fun addDocument(type: String, expiryDate: String, entity: String) {
        val motoId = _uiState.value.motorcycleId ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            try {
                val docRef = db.collection("documents").document()
                val newDoc = Document(id = docRef.id, motorcycleId = motoId, type = type, expiryDate = expiryDate, entity = entity)
                docRef.set(newDoc).await()
                checkMotorcycleAndLoadData()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSaving = false)
            }
        }
    }

    fun addMaintenance(desc: String, type: String, value: String) {
        val motoId = _uiState.value.motorcycleId ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            try {
                val docRef = db.collection("maintenance").document()
                val maintenance = Maintenance(
                    id = docRef.id,
                    motorcycleId = motoId,
                    description = desc,
                    schedulingType = type,
                    targetMileage = if (type == "MILEAGE") value.toIntOrNull() else null,
                    targetDate = if (type == "TIME") value else null,
                    status = "SCHEDULED"
                )
                docRef.set(maintenance).await()
                checkMotorcycleAndLoadData()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSaving = false)
            }
        }
    }
}
