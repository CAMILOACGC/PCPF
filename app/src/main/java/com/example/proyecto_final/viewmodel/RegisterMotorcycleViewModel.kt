package com.example.proyecto_final.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class RegisterMotorcycleViewModel : ViewModel() {
    var brand by mutableStateOf("")
        private set
    var model by mutableStateOf("")
        private set
    var year by mutableStateOf("")
        private set
    var mileage by mutableStateOf("")
        private set

    fun onBrandChange(value: String) { brand = value }
    fun onModelChange(value: String) { model = value }
    fun onYearChange(value: String) { year = value }
    fun onMileageChange(value: String) { mileage = value }

    fun saveMotorcycle() {
        // Lógica para guardar
    }
}
