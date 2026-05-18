package com.example.proyecto_final.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.proyecto_final.ui.theme.PROYECTO_FINALTheme
import com.example.proyecto_final.viewmodel.MaintenanceViewModel

@Composable
fun MaintenanceScreen(navController: NavController, viewModel: MaintenanceViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF5F5F5))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1A237E))
                    .padding(24.dp)
            ) {
                Column {
                    Text(
                        text = "Mantenimientos",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Programa y registra servicios",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
            }

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (!uiState.hasMotorcycle) {
                EmptyMaintenanceState(navController)
            } else {
                MaintenanceContent(uiState, viewModel)
            }
        }
    }
}

@Composable
fun EmptyMaintenanceState(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🏍️", fontSize = 64.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "No hay moto registrada",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A237E)
        )
        Text(
            "Debes registrar una motocicleta para gestionar sus mantenimientos.",
            textAlign = TextAlign.Center,
            color = Color.Gray,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Button(
            onClick = { navController.navigate(Screen.RegisterMotorcycle.route) },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A237E))
        ) {
            Text("Registrar Moto")
        }
    }
}

@Composable
fun MaintenanceContent(uiState: com.example.proyecto_final.viewmodel.MaintenanceUiState, viewModel: MaintenanceViewModel) {
    Column {
        val selectedTab = viewModel.selectedTab
        val tabs = listOf("Programados", "Historial")

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = Color(0xFF1A237E),
            indicator = { tabPositions ->
                if (selectedTab < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Color(0xFF1A237E)
                    )
                }
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { viewModel.onTabSelected(index) },
                    text = { Text(title) }
                )
            }
        }

        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (uiState.maintenanceList.isEmpty()) {
                Text(
                    "No hay registros aún.",
                    modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                    textAlign = TextAlign.Center,
                    color = Color.Gray
                )
            } else {
                uiState.maintenanceList.forEach { maintenance ->
                    val isCompleted = maintenance.status == "COMPLETED"
                    MaintenanceItemCard(
                        title = maintenance.description,
                        frequency = if (isCompleted) "Servicio realizado" else "Servicio pendiente",
                        nextService = if (isCompleted) "${maintenance.completionMileage ?: 0} km" else "${maintenance.targetMileage ?: 0} km",
                        remaining = if (isCompleted) maintenance.completionDate ?: "--" else maintenance.targetDate ?: "--",
                        status = if (isCompleted) "Completado" else "Pendiente",
                        statusColor = if (isCompleted) Color(0xFF4CAF50) else Color(0xFFFFA000)
                    )
                }
            }
        }
    }
}

@Composable
private fun MaintenanceItemCard(
    title: String,
    frequency: String,
    nextService: String,
    remaining: String,
    status: String,
    statusColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (status == "OK" || status == "Completado") Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(frequency, fontSize = 12.sp, color = Color.Gray)
                    }
                }
                Surface(
                    color = statusColor,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        status,
                        color = Color.White,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Kilometraje:", fontSize = 12.sp, color = Color.Gray)
                Text(nextService, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Fecha:", fontSize = 12.sp, color = Color.Gray)
                Text(remaining, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
