package com.example.proyecto_final.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.proyecto_final.MODELS.Document
import com.example.proyecto_final.MODELS.Motorcycle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterMotorcycleViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    var brand by mutableStateOf("")
    var model by mutableStateOf("")
    var year by mutableStateOf("")
    var mileage by mutableStateOf("")
    var plate by mutableStateOf("")
    
    // Campos para documentos
    var soatExpiry by mutableStateOf("")
    var tecnoExpiry by mutableStateOf("")

    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    fun registerMotorcycle(onSuccess: () -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        if (brand.isBlank() || model.isBlank() || plate.isBlank()) {
            errorMessage = "Marca, modelo y placa son obligatorios"
            return
        }

        isLoading = true
        val motoId = db.collection("motorcycles").document().id
        
        val motorcycle = Motorcycle(
            id = motoId,
            userId = userId,
            brand = brand,
            model = model,
            year = year.toIntOrNull() ?: 0,
            licensePlate = plate,
            currentMileage = mileage.toIntOrNull() ?: 0
        )

        db.collection("motorcycles").document(motoId).set(motorcycle)
            .addOnSuccessListener {
                saveInitialDocuments(motoId, onSuccess)
            }
            .addOnFailureListener {
                isLoading = false
                errorMessage = "Error al guardar la moto"
            }
    }

    private fun saveInitialDocuments(motoId: String, onSuccess: () -> Unit) {
        val documents = listOf(
            Document(motorcycleId = motoId, type = "SOAT", expiryDate = soatExpiry),
            Document(motorcycleId = motoId, type = "Técnico-mecánica", expiryDate = tecnoExpiry)
        )

        val batch = db.batch()
        documents.forEach { doc ->
            val docRef = db.collection("documents").document()
            batch.set(docRef, doc.copy(id = docRef.id))
        }

        batch.commit().addOnCompleteListener {
            isLoading = false
            onSuccess()
        }
    }
}
