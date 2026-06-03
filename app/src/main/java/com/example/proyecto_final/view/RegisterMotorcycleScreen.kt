package com.example.proyecto_final.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.proyecto_final.R
import com.example.proyecto_final.ui.theme.PROYECTO_FINALTheme
import com.example.proyecto_final.viewmodel.RegisterMotorcycleViewModel

@Composable
fun RegisterMotorcycleScreen(
    navController: NavController,
    viewModel: RegisterMotorcycleViewModel = viewModel()
) {
    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
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
                        text = stringResource(R.string.register_motorcycle_title),
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.register_motorcycle_subtitle),
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
            }

            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = stringResource(R.string.vehicle_info_section),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        InputField(
                            label = stringResource(R.string.label_brand), 
                            placeholder = stringResource(R.string.placeholder_brand),
                            value = viewModel.brand,
                            onValueChange = { viewModel.brand = it }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        InputField(
                            label = stringResource(R.string.label_model), 
                            placeholder = stringResource(R.string.placeholder_model),
                            value = viewModel.model,
                            onValueChange = { viewModel.model = it }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        InputField(
                            label = stringResource(R.string.label_year), 
                            placeholder = stringResource(R.string.placeholder_year),
                            value = viewModel.year,
                            onValueChange = { viewModel.year = it }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        InputField(
                            label = stringResource(R.string.label_plate), 
                            placeholder = stringResource(R.string.placeholder_plate),
                            value = viewModel.plate,
                            onValueChange = { viewModel.plate = it }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        InputField(
                            label = stringResource(R.string.label_mileage), 
                            placeholder = stringResource(R.string.placeholder_mileage), 
                            suffix = "km",
                            value = viewModel.mileage,
                            onValueChange = { viewModel.mileage = it }
                        )

                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = stringResource(R.string.mandatory_docs_section),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        InputField(
                            label = stringResource(R.string.label_soat_expiry), 
                            placeholder = stringResource(R.string.placeholder_date),
                            value = viewModel.soatExpiry,
                            onValueChange = { viewModel.soatExpiry = it }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        InputField(
                            label = stringResource(R.string.label_tecno_expiry), 
                            placeholder = stringResource(R.string.placeholder_date),
                            value = viewModel.tecnoExpiry,
                            onValueChange = { viewModel.tecnoExpiry = it }
                        )
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        if (viewModel.errorMessage != null) {
                            Text(
                                text = viewModel.errorMessage!!,
                                color = Color.Red,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        Button(
                            onClick = { 
                                viewModel.registerMotorcycle {
                                    navController.navigate("dashboard") { // Corregido el nombre de la ruta si es necesario
                                        popUpTo("register_motorcycle") { inclusive = true }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A237E)),
                            enabled = !viewModel.isLoading
                        ) {
                            if (viewModel.isLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.button_save_motorcycle))
                            }
                        }
                    }
                }
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF2196F3), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.tip_mileage),
                            fontSize = 12.sp,
                            color = Color(0xFF1976D2)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InputField(
    label: String, 
    placeholder: String, 
    value: String,
    onValueChange: (String) -> Unit,
    suffix: String? = null
) {
    Column {
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, fontSize = 14.sp) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            suffix = suffix?.let { { Text(it) } },
            singleLine = true
        )
    }
}
