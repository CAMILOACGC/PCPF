package com.example.proyecto_final.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.proyecto_final.R
import com.example.proyecto_final.viewmodel.DashboardViewModel
import com.example.proyecto_final.viewmodel.UnifiedItem

@Composable
fun DashboardScreen(navController: NavController, viewModel: DashboardViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    // REDIRECCIÓN AUTOMÁTICA PARA NUEVOS USUARIOS
    LaunchedEffect(uiState.isLoading, uiState.hasMotorcycle) {
        if (!uiState.isLoading && !uiState.hasMotorcycle) {
            navController.navigate(Screen.RegisterMotorcycle.route) {
                popUpTo(Screen.Dashboard.route) { inclusive = false }
            }
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF5F5F5))
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (!uiState.hasMotorcycle) {
                // Mientras redirige, mostramos un estado de espera limpio
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.empty_dashboard_title), color = Color.Gray)
                }
            } else {
                DashboardContent(navController, uiState)
            }
        }
    }
}

@Composable
fun DashboardContent(navController: NavController, uiState: com.example.proyecto_final.viewmodel.DashboardUiState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        HeaderSection(uiState.motorcycle?.brand ?: stringResource(R.string.default_moto_title), uiState.motorcycle?.model ?: "")
        
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MileageCard(uiState.motorcycle?.currentMileage?.toString() ?: "0")
            
            SummaryStatusCard(uiState.topItems)
            
            Button(
                onClick = { navController.navigate(Screen.GPS.route) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.button_start_gps))
            }
        }
    }
}

@Composable
fun HeaderSection(title: String, subtitle: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1A237E))
            .padding(24.dp)
    ) {
        Column {
            Text(text = title, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(text = subtitle, color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
        }
    }
}

@Composable
fun MileageCard(mileage: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.Gray)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.label_mileage), color = Color.Gray, fontWeight = FontWeight.Medium)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(text = mileage, fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A237E))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = stringResource(R.string.unit_km), fontSize = 18.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 6.dp))
            }
        }
    }
}

@Composable
fun SummaryStatusCard(items: List<UnifiedItem>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Notifications, contentDescription = null, tint = Color(0xFFFFA000))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Estado de Documentos y Alertas", color = Color.Gray, fontWeight = FontWeight.Medium)
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (items.isEmpty()) {
                Text("Todo al día", color = Color.Gray, modifier = Modifier.fillMaxWidth())
            } else {
                items.forEach { item ->
                    DashboardItemRow(item)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun DashboardItemRow(item: UnifiedItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(modifier = Modifier.size(8.dp).background(item.status.color, RoundedCornerShape(4.dp)))
            Spacer(modifier = Modifier.width(8.dp))
            Text(item.title, color = Color.Gray, maxLines = 1)
        }
        Text(item.status.label, color = item.status.color, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val currentRoute = navController.currentBackStackEntry?.destination?.route
    NavigationBar(containerColor = Color.White) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, null) },
            label = { Text(stringResource(R.string.nav_home)) },
            selected = currentRoute == Screen.Dashboard.route,
            onClick = { navController.navigate(Screen.Dashboard.route) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.List, null) },
            label = { Text(stringResource(R.string.nav_documents)) },
            selected = currentRoute == Screen.Documents.route,
            onClick = { navController.navigate(Screen.Documents.route) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.LocationOn, null) },
            label = { Text(stringResource(R.string.nav_gps)) },
            selected = currentRoute == Screen.GPS.route,
            onClick = { navController.navigate(Screen.GPS.route) }
        )
    }
}
