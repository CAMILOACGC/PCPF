package com.example.proyecto_final.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.proyecto_final.MODELS.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class LoginViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var isRegisterMode by mutableStateOf(false)

    fun onEmailChange(newValue: String) {
        email = newValue
    }

    fun onPasswordChange(newValue: String) {
        password = newValue
    }

    fun toggleMode() {
        isRegisterMode = !isRegisterMode
        errorMessage = null
    }

    fun login(onSuccess: () -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            errorMessage = "Por favor completa todos los campos"
            return
        }
        
        isLoading = true
        errorMessage = null
        
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    if (user != null) {
                        saveUserToFirestore(user, onSuccess)
                    } else {
                        isLoading = false
                        onSuccess()
                    }
                } else {
                    isLoading = false
                    errorMessage = task.exception?.localizedMessage ?: "Error al iniciar sesión"
                }
            }
    }

    fun register(onSuccess: () -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            errorMessage = "Por favor completa todos los campos"
            return
        }

        if (password.length < 6) {
            errorMessage = "La contraseña debe tener al menos 6 caracteres"
            return
        }

        isLoading = true
        errorMessage = null

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    if (user != null) {
                        saveUserToFirestore(user, onSuccess)
                    } else {
                        isLoading = false
                        onSuccess()
                    }
                } else {
                    isLoading = false
                    errorMessage = task.exception?.localizedMessage ?: "Error al registrarse"
                }
            }
    }

    fun signInWithGoogle(idToken: String, onSuccess: () -> Unit) {
        isLoading = true
        errorMessage = null
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    if (user != null) {
                        saveUserToFirestore(user, onSuccess)
                    } else {
                        isLoading = false
                        onSuccess()
                    }
                } else {
                    isLoading = false
                    errorMessage = task.exception?.localizedMessage ?: "Error con Google Sign-In"
                }
            }
    }

    private fun saveUserToFirestore(firebaseUser: com.google.firebase.auth.FirebaseUser, onSuccess: () -> Unit) {
        val user = User(
            uid = firebaseUser.uid,
            name = firebaseUser.displayName ?: email.substringBefore("@").ifBlank { "Usuario de MotoTrack" },
            email = firebaseUser.email ?: email,
            photoUrl = firebaseUser.photoUrl?.toString()
        )

        db.collection("users").document(firebaseUser.uid)
            .set(user, SetOptions.merge())
            .addOnCompleteListener { task ->
                isLoading = false
                if (!task.isSuccessful) {
                    Log.e("LoginViewModel", "Error al guardar en Firestore", task.exception)
                }
                // Importante: Llamamos a onSuccess incluso si Firestore falla 
                // para que el usuario no se quede bloqueado en la pantalla de login.
                onSuccess()
            }
    }
}
