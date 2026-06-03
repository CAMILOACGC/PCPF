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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.proyecto_final.R
import com.example.proyecto_final.ui.theme.PROYECTO_FINALTheme
import com.example.proyecto_final.viewmodel.DashboardViewModel

@Composable
fun DashboardScreen(navController: NavController, viewModel: DashboardViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

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
                EmptyDashboardState(navController)
            } else {
                DashboardContent(navController, uiState)
            }
        }
    }
}

@Composable
fun EmptyDashboardState(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(120.dp),
            color = Color(0xFF1A237E).copy(alpha = 0.1f),
            shape = RoundedCornerShape(60.dp)
        ) {

        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.empty_dashboard_title),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A237E),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.empty_dashboard_subtitle),
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { navController.navigate("register_motorcycle") },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A237E))
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.register_motorcycle_title))
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
            MaintenanceCard(uiState.oilChangeRemaining, uiState.generalCheckRemaining)
            DocumentsCard(uiState.soatStatus, uiState.rtmStatus)
            
            Button(
                onClick = { navController.navigate("gps") },
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
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SmallStatusCard(
                    title = stringResource(R.string.label_last_service),
                    value = uiState.lastServiceDays,
                    icon = Icons.Default.Build,
                    modifier = Modifier.weight(1f)
                )
                SmallStatusCard(
                    title = stringResource(R.string.label_alerts),
                    value = uiState.pendingAlerts,
                    icon = Icons.Default.Notifications,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun HeaderSection(title: String, subtitle: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color(0xFF1A237E),
                shape = RoundedCornerShape(bottomStart = 0.dp, bottomEnd = 0.dp)
            )
            .padding(24.dp)
    ) {
        Column {
            Text(
                text = title,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
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
                Text(
                    text = mileage,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A237E)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = stringResource(R.string.unit_km), fontSize = 18.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 6.dp))
            }
        }
    }
}

@Composable
fun MaintenanceCard(oil: String, check: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Build, contentDescription = null, tint = Color(0xFF2196F3))
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.next_maintenance_title), color = Color.Gray, fontWeight = FontWeight.Medium)
            }
            Spacer(modifier = Modifier.height(16.dp))
            MaintenanceRow(stringResource(R.string.maintenance_oil_change), oil, Color(0xFF2196F3))
            MaintenanceRow(stringResource(R.string.maintenance_general_check), check, Color(0xFF2196F3))
        }
    }
}

@Composable
fun MaintenanceRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray)
        Text(value, color = valueColor, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun DocumentsCard(soat: String, rtm: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFF4CAF50))
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.document_status_title), color = Color.Gray, fontWeight = FontWeight.Medium)
            }
            Spacer(modifier = Modifier.height(16.dp))
            DocumentRow("SOAT", soat, if (soat == "Vigente" || soat == "Valid") Color(0xFF4CAF50) else Color(0xFFFFA000))
            DocumentRow(stringResource(R.string.label_tecno_expiry), rtm, if (rtm == "Vigente" || rtm == "Valid") Color(0xFF4CAF50) else Color(0xFFFFA000))
        }
    }
}

@Composable
fun DocumentRow(label: String, status: String, statusColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(8.dp).background(statusColor, RoundedCornerShape(4.dp)))
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, color = Color.Gray)
        }
        Text(status, color = statusColor, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SmallStatusCard(title: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF1A237E))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, fontSize = 10.sp, color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val currentRoute = navController.currentBackStackEntry?.destination?.route

    NavigationBar(containerColor = Color.White) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_home)) },
            selected = currentRoute == "dashboard",
            onClick = { navController.navigate("dashboard") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_register)) },
            selected = currentRoute == "register_motorcycle",
            onClick = { navController.navigate("register_motorcycle") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_maintenance)) },
            selected = currentRoute == "maintenance",
            onClick = { navController.navigate("maintenance") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.List, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_documents)) },
            selected = currentRoute == "documents",
            onClick = { navController.navigate("documents") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_gps)) },
            selected = currentRoute == "gps",
            onClick = { navController.navigate("gps") }
        )
    }
}
