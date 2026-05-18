package com.example.proyecto_final.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto_final.MODELS.Document
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class DocumentsUiState(
    val hasMotorcycle: Boolean = false,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val documents: List<Document> = emptyList(),
    val motorcycleId: String? = null
)

class DocumentsViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(DocumentsUiState())
    val uiState: StateFlow<DocumentsUiState> = _uiState

    init {
        checkMotorcycleAndLoadDocuments()
    }

    fun checkMotorcycleAndLoadDocuments() {
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
                    val motoId = snapshot.documents[0].id
                    _uiState.value = _uiState.value.copy(
                        hasMotorcycle = true,
                        motorcycleId = motoId
                    )
                    loadDocuments(motoId)
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

    private fun loadDocuments(motoId: String) {
        viewModelScope.launch {
            try {
                val snapshot = db.collection("documents")
                    .whereEqualTo("motorcycleId", motoId)
                    .get()
                    .await()

                val list = snapshot.toObjects(Document::class.java)
                _uiState.value = _uiState.value.copy(
                    documents = list,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun addDocument(type: String, expiryDate: String, entity: String) {
        val motoId = _uiState.value.motorcycleId ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            try {
                val docRef = db.collection("documents").document()
                val newDoc = Document(
                    id = docRef.id,
                    motorcycleId = motoId,
                    type = type,
                    expiryDate = expiryDate,
                    entity = entity,
                    status = "Vigente"
                )
                
                docRef.set(newDoc).await()
                loadDocuments(motoId)
                _uiState.value = _uiState.value.copy(isSaving = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSaving = false)
            }
        }
    }
}
