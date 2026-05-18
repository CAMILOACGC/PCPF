package com.example.proyecto_final.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
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
import com.example.proyecto_final.viewmodel.DocumentsViewModel
import com.example.proyecto_final.viewmodel.MaintenanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentsScreen(
    navController: NavController, 
    viewModel: DocumentsViewModel = viewModel(),
    maintViewModel: MaintenanceViewModel = viewModel()
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
                    Icon(Icons.Default.Add, contentDescription = "Agregar")
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
                        text = "Gestión Central",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Documentos y servicios",
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
                DocumentsContent(uiState)
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
                                maintViewModel.addMaintenance(desc, type, value)
                                showSheet = false
                                sheetType = null
                            }
                        )
                        else -> {}
                    }
                }
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

enum class SheetType { DOCUMENT, MAINTENANCE }

@Composable
fun SelectionMenu(onSelect: (SheetType) -> Unit) {
    Column(modifier = Modifier.padding(24.dp)) {
        Text("¿Qué deseas agregar?", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))
        
        OutlinedButton(
            onClick = { onSelect(SheetType.DOCUMENT) },
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Description, contentDescription = null)
            Spacer(modifier = Modifier.width(12.dp))
            Text("Nuevo Documento (SOAT, RTM...)")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedButton(
            onClick = { onSelect(SheetType.MAINTENANCE) },
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Build, contentDescription = null)
            Spacer(modifier = Modifier.width(12.dp))
            Text("Nuevo Mantenimiento")
        }
    }
}

@Composable
fun AddDocumentForm(onSave: (String, String, String) -> Unit) {
    var type by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var entity by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(24.dp)) {
        Text("Agregar Documento", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(value = type, onValueChange = { type = it }, label = { Text("Tipo (ej: SOAT)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Vencimiento (DD/MM/AAAA)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = entity, onValueChange = { entity = it }, label = { Text("Entidad emisora") }, modifier = Modifier.fillMaxWidth())
        
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { onSave(type, date, entity) },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = type.isNotBlank() && date.isNotBlank()
        ) {
            Text("Guardar Documento")
        }
    }
}

@Composable
fun AddMaintenanceForm(onSave: (String, String, String) -> Unit) {
    var desc by remember { mutableStateOf("") }
    var value by remember { mutableStateOf("") }
    var isMileage by remember { mutableStateOf(true) }

    Column(modifier = Modifier.padding(24.dp)) {
        Text("Programar Mantenimiento", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Descripción (ej: Aceite)") }, modifier = Modifier.fillMaxWidth())
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = isMileage, onClick = { isMileage = true })
            Text("Por Kilometraje")
            RadioButton(selected = !isMileage, onClick = { isMileage = false })
            Text("Por Fecha")
        }

        OutlinedTextField(
            value = value, 
            onValueChange = { value = it }, 
            label = { Text(if (isMileage) "Kilometraje objetivo" else "Fecha (DD/MM/AAAA)") }, 
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { onSave(desc, if (isMileage) "MILEAGE" else "TIME", value) },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = desc.isNotBlank() && value.isNotBlank()
        ) {
            Text("Programar Servicio")
        }
    }
}

@Composable
fun EmptyDocumentsState(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("📄", fontSize = 64.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Sin motocicleta",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A237E)
        )
        Text(
            "Debes registrar una moto para ver el estado de sus documentos.",
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
fun DocumentsContent(uiState: com.example.proyecto_final.viewmodel.DocumentsUiState) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (uiState.documents.isEmpty()) {
            Text(
                "No hay documentos registrados para esta moto.",
                modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                textAlign = TextAlign.Center,
                color = Color.Gray
            )
        } else {
            uiState.documents.forEach { doc ->
                DetailedDocumentCard(
                    title = doc.type,
                    subtitle = if (doc.type == "SOAT") "Seguro Obligatorio" else "Certificado RTM",
                    status = doc.status,
                    statusColor = if (doc.status == "Vigente") Color(0xFF4CAF50) else Color(0xFFFFA000),
                    emissionDate = doc.emissionDate.ifBlank { "--" },
                    expiryDate = doc.expiryDate,
                    remainingDays = "Verificar fecha",
                    progress = 0.5f,
                    entity = doc.entity.ifBlank { "No especificada" }
                )
            }
        }
    }
}

@Composable
private fun DetailedDocumentCard(
    title: String,
    subtitle: String,
    status: String,
    statusColor: Color,
    emissionDate: String,
    expiryDate: String,
    remainingDays: String,
    progress: Float,
    entity: String
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
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(statusColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = statusColor)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(subtitle, fontSize = 12.sp, color = Color.Gray)
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

            Spacer(modifier = Modifier.height(20.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Emisión", fontSize = 10.sp, color = Color.Gray)
                    Text(emissionDate, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Vencimiento", fontSize = 10.sp, color = Color.Gray)
                    Text(expiryDate, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Entidad", fontSize = 10.sp, color = Color.Gray)
                    Text(entity, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = Color(0xFF1A237E),
                    trackColor = Color(0xFFE0E0E0),
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            }
        }
    }
}
