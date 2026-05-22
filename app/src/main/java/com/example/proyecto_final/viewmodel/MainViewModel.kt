package com.example.proyecto_final.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto_final.MODELS.Motorcycle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class MainUiState(
    val motorcycle: Motorcycle? = null,
    val hasMotorcycle: Boolean = false,
    val isLoading: Boolean = true,
    val userId: String? = null
)

class MainViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState

    init {
        monitorAuthState()
    }

    private fun monitorAuthState() {
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                _uiState.value = _uiState.value.copy(userId = user.uid)
                startListeningToMotorcycle(user.uid)
            } else {
                _uiState.value = MainUiState(isLoading = false)
            }
        }
    }

    private fun startListeningToMotorcycle(userId: String) {
        db.collection("motorcycles")
            .whereEqualTo("userId", userId)
            .limit(1)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val moto = snapshot.documents[0].toObject(Motorcycle::class.java)
                    _uiState.value = _uiState.value.copy(
                        motorcycle = moto,
                        hasMotorcycle = true,
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        motorcycle = null,
                        hasMotorcycle = false,
                        isLoading = false
                    )
                }
            }
    }
}
