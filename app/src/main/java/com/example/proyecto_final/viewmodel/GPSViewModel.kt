package com.example.proyecto_final.viewmodel

import android.annotation.SuppressLint
import android.location.Location
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto_final.MODELS.Motorcycle
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Locale
import java.util.concurrent.TimeUnit

class GPSViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    var isTracking by mutableStateOf(false)
        private set
    var speed by mutableStateOf("0.0")
        private set
    var maxSpeed by mutableStateOf("0.0")
        private set
    private var maxSpeedValue = 0.0

    var distance by mutableStateOf("0.0")
        private set
    var time by mutableStateOf("00:00:00")
        private set
    
    val routePoints = mutableStateListOf<LatLng>()

    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null
    
    private var startTime = 0L
    private var timerJob: Job? = null
    private var totalDistance = 0.0

    fun startTracking(client: FusedLocationProviderClient) {
        if (isTracking) return
        
        fusedLocationClient = client
        isTracking = true
        startTime = System.currentTimeMillis()
        totalDistance = 0.0
        maxSpeedValue = 0.0
        maxSpeed = "0.0"
        routePoints.clear()
        
        startTimer()
        startLocationUpdates()
    }

    fun stopTracking() {
        if (!isTracking) return
        
        isTracking = false
        timerJob?.cancel()
        locationCallback?.let { callback ->
            fusedLocationClient?.removeLocationUpdates(callback)
        }
        locationCallback = null
        
        updateMotorcycleMileage()
    }

    private fun updateMotorcycleMileage() {
        val userId = auth.currentUser?.uid ?: return
        val distanceTraveledKm = totalDistance / 1000.0
        
        viewModelScope.launch {
            try {
                val snapshot = db.collection("motorcycles")
                    .whereEqualTo("userId", userId)
                    .limit(1)
                    .get()
                    .await()

                if (!snapshot.isEmpty) {
                    val doc = snapshot.documents[0]
                    val moto = doc.toObject(Motorcycle::class.java)
                    moto?.let {
                        val newMileage = it.currentMileage + distanceTraveledKm.toInt()
                        db.collection("motorcycles").document(doc.id)
                            .update("currentMileage", newMileage)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun toggleTracking(client: FusedLocationProviderClient) {
        if (isTracking) stopTracking() else startTracking(client)
    }

    private fun startTimer() {
        timerJob = viewModelScope.launch {
            while (isTracking) {
                val elapsed = System.currentTimeMillis() - startTime
                time = formatTime(elapsed)
                delay(1000)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setMinUpdateIntervalMillis(2000)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    updateMetrics(location)
                }
            }
        }
        locationCallback = callback

        fusedLocationClient?.requestLocationUpdates(
            locationRequest,
            callback,
            Looper.getMainLooper()
        )
    }

    private fun updateMetrics(location: Location) {
        val newPoint = LatLng(location.latitude, location.longitude)
        
        // Speed in km/h
        val speedKmH = location.speed * 3.6
        speed = String.format(Locale.getDefault(), "%.1f", speedKmH)
        
        if (speedKmH > maxSpeedValue) {
            maxSpeedValue = speedKmH
            maxSpeed = String.format(Locale.getDefault(), "%.1f", maxSpeedValue)
        }

        if (routePoints.isNotEmpty()) {
            val lastPoint = routePoints.last()
            val results = FloatArray(1)
            Location.distanceBetween(
                lastPoint.latitude, lastPoint.longitude,
                newPoint.latitude, newPoint.longitude,
                results
            )
            totalDistance += results[0]
            distance = String.format(Locale.getDefault(), "%.2f", totalDistance / 1000) // Convert to km
        }
        
        routePoints.add(newPoint)
    }

    private fun formatTime(millis: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(millis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
    }

    override fun onCleared() {
        super.onCleared()
        stopTracking()
    }
}
