package com.example.proyecto_final.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class GPSViewModel : ViewModel() {
    var isTracking by mutableStateOf(false)
        private set
    var speed by mutableStateOf("0")
        private set
    var distance by mutableStateOf("0")
        private set
    var time by mutableStateOf("00:00:00")
        private set

    fun toggleTracking() {
        isTracking = !isTracking
    }
}
