package com.example.proyecto_final.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.proyecto_final.R
import com.example.proyecto_final.viewmodel.DocumentsViewModel
import com.example.proyecto_final.viewmodel.ItemStatus
import com.example.proyecto_final.viewmodel.UnifiedItem

enum class SheetType { DOCUMENT, MAINTENANCE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentsScreen(
    navController: NavController, 
    viewModel: DocumentsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showSheet by remember { mutableStateOf(false) }
    var sheetType by remember { mutableStateOf<SheetType?>(null) }
    val sheetState = rememberModalBottomSheetState()

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) },
        floatingActionButton = {
            if (uiState.hasMotorcycle) {
                FloatingActionButton(
                    onClick = { showSheet = true },
                    containerColor = Color(0xFF1A237E),
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_button_desc))
                }
            }
        }
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
                        text = stringResource(R.string.documents_mgmt_title),
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.documents_mgmt_subtitle),
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
                EmptyDocumentsState(navController)
            } else {
                UnifiedContent(uiState.items)
            }
        }

        if (showSheet) {
            ModalBottomSheet(
                onDismissRequest = { 
                    showSheet = false
                    sheetType = null
                },
                sheetState = sheetState
            ) {
                if (sheetType == null) {
                    SelectionMenu(onSelect = { sheetType = it })
                } else {
                    when (sheetType) {
                        SheetType.DOCUMENT -> AddDocumentForm(
                            onSave = { type, date, entity ->
                                viewModel.addDocument(type, date, entity)
                                showSheet = false
                                sheetType = null
                            }
                        )
                        SheetType.MAINTENANCE -> AddMaintenanceForm(
                            onSave = { desc, type, value ->
                                viewModel.addMaintenance(desc, type, value)
                                showSheet = false
                                sheetType = null
                            }
                        )
                        null -> { /* No op */ }
                    }
                }
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

@Composable
fun SelectionMenu(onSelect: (SheetType) -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("¿Qué deseas agregar?", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { onSelect(SheetType.DOCUMENT) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A237E))
        ) {
            Icon(Icons.Default.Info, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Nuevo Documento (SOAT, RTM, etc.)")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { onSelect(SheetType.MAINTENANCE) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
        ) {
            Icon(Icons.Default.Build, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Nuevo Mantenimiento")
        }
    }
}

@Composable
fun AddDocumentForm(onSave: (String, String, String) -> Unit) {
    var type by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var entity by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Detalles del Documento", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        OutlinedTextField(value = type, onValueChange = { type = it }, label = { Text("Tipo (ej: SOAT)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Fecha Vencimiento (dd/mm/yyyy)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = entity, onValueChange = { entity = it }, label = { Text("Entidad") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { onSave(type, date, entity) }, 
            modifier = Modifier.fillMaxWidth(),
            enabled = type.isNotBlank() && date.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A237E))
        ) {
            Text("Guardar Documento")
        }
    }
}

@Composable
fun AddMaintenanceForm(onSave: (String, String, String) -> Unit) {
    var desc by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("MILEAGE") }
    var value by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Programar Mantenimiento", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Descripción (ej: Cambio Aceite)") }, modifier = Modifier.fillMaxWidth())
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = type == "MILEAGE", onClick = { type = "MILEAGE" })
            Text("Por Kilometraje", modifier = Modifier.padding(end = 16.dp))
            RadioButton(selected = type == "TIME", onClick = { type = "TIME" })
            Text("Por Fecha")
        }

        OutlinedTextField(
            value = value, 
            onValueChange = { value = it }, 
            label = { Text(if(type == "MILEAGE") "Kilometraje Objetivo" else "Fecha Objetivo (dd/mm/yyyy)") }, 
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { onSave(desc, type, value) }, 
            modifier = Modifier.fillMaxWidth(),
            enabled = desc.isNotBlank() && value.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A237E))
        ) {
            Text("Programar Mantenimiento")
        }
    }
}

@Composable
fun EmptyDocumentsState(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🏍️", fontSize = 64.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text("No hay moto registrada", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A237E))
        Text(
            "Debes registrar una motocicleta para gestionar sus documentos y mantenimientos.",
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
fun UnifiedContent(items: List<UnifiedItem>) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (items.isEmpty()) {
            Text(
                text = stringResource(R.string.empty_documents_list),
                modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                textAlign = TextAlign.Center,
                color = Color.Gray
            )
        } else {
            items.forEach { item ->
                UnifiedItemCard(item)
            }
        }
    }
}

@Composable
fun UnifiedItemCard(item: UnifiedItem) {
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
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(item.status.color.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (item.isMaintenance) Icons.Default.Build else Icons.Default.Info,
                            contentDescription = null,
                            tint = item.status.color
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(item.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(item.subtitle, fontSize = 12.sp, color = Color.Gray)
                    }
                }
                Surface(
                    color = item.status.color,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = item.status.label,
                        color = Color.White,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(
                        text = if (item.isMaintenance) "Meta" else stringResource(R.string.label_expiry),
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                    Text(item.targetValue, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { item.progress },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = item.status.color,
                trackColor = Color(0xFFE0E0E0),
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        }
    }
}
