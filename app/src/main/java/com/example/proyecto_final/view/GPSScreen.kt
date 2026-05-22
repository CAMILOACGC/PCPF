package com.example.proyecto_final.view

import android.Manifest
import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.proyecto_final.ui.theme.PROYECTO_FINALTheme
import com.example.proyecto_final.viewmodel.GPSViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.RoundCap
import com.google.maps.android.compose.*

@SuppressLint("MissingPermission")
@Composable
fun GPSScreen(navController: NavController, viewModel: GPSViewModel = viewModel()) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(4.6097, -74.0817), 15f)
    }

    // Permission handling
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            viewModel.startTracking(fusedLocationClient)
        }
    }

    // Update camera when route changes
    LaunchedEffect(viewModel.routePoints.size) {
        if (viewModel.routePoints.isNotEmpty()) {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(viewModel.routePoints.last(), 17f)
        }
    }

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1A237E))
                    .padding(24.dp)
            ) {
                Column {
                    Text(
                        text = "Recorrido GPS",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Rastrea tu viaje en tiempo real",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
            }

            // Map Area
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(isMyLocationEnabled = viewModel.isTracking),
                    uiSettings = MapUiSettings(myLocationButtonEnabled = true)
                ) {
                    if (viewModel.routePoints.isNotEmpty()) {
                        Polyline(
                            points = viewModel.routePoints.toList(),
                            color = Color(0xFF2196F3),
                            width = 12f,
                            jointType = JointType.ROUND,
                            startCap = RoundCap(),
                            endCap = RoundCap()
                        )
                        
                        Marker(
                            state = rememberMarkerState(position = viewModel.routePoints.last()),
                            title = "Mi Ubicación",
                            snippet = "V. Actual: ${viewModel.speed} km/h"
                        )
                    }
                }

                Surface(
                    modifier = Modifier.padding(16.dp).align(Alignment.TopStart),
                    color = Color.Black.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = if (viewModel.isTracking) "● En curso" else "● Detenido",
                        color = if (viewModel.isTracking) Color.Green else Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 12.sp
                    )
                }
            }

            // Metrics and Controls
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    GPSMetricCard(viewModel.maxSpeed, "km/h Máx", Modifier.weight(1f))
                    GPSMetricCard(viewModel.distance, "km Total", Modifier.weight(1f))
                    GPSMetricCard(viewModel.time, "Tiempo", Modifier.weight(1f))
                }

                Button(
                    onClick = {
                        if (viewModel.isTracking) {
                            viewModel.stopTracking()
                        } else {
                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (viewModel.isTracking) Color.Red else Color(0xFF2196F3)
                    )
                ) {
                    Icon(
                        if (viewModel.isTracking) Icons.Default.Close else Icons.Default.PlayArrow,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (viewModel.isTracking) "Detener Recorrido" else "Iniciar Recorrido")
                }
            }
        }
    }
}

@Composable
private fun GPSMetricCard(value: String, unit: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1A237E))
            Text(unit, fontSize = 10.sp, color = Color.Gray)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GPSScreenPreview() {
    PROYECTO_FINALTheme {
        GPSScreen(rememberNavController())
    }
}
