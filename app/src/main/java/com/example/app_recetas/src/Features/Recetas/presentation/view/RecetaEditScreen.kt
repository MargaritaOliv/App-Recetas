package com.example.app_recetas.src.Features.Recetas.presentation.view

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app_recetas.src.Features.Recetas.presentation.viewModel.RecetaEditViewModel
import com.example.app_recetas.src.Features.Recetas.presentation.viewModel.RecetaViewModelFactory

// Colores pastel lila y celeste
private val PastelLilac = Color(0xFFB39DDB)
private val SoftPurple = Color(0xFFCE93D8)
private val LightLavender = Color(0xFFF3E5F5)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecetaEditScreen(
    recetaId: Int,
    onNavigateBack: () -> Unit = {},
    onRecetaUpdated: () -> Unit = {},
    viewModel: RecetaEditViewModel = viewModel(factory = RecetaViewModelFactory())
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var nombre by remember { mutableStateOf("") }
    var ingredientes by remember { mutableStateOf("") }
    var pasos by remember { mutableStateOf("") }
    var tiempoPreparacion by remember { mutableStateOf("") }

    // Cargar receta al iniciar
    LaunchedEffect(recetaId) {
        viewModel.cargarReceta(recetaId)
    }

    // Llenar campos cuando se carga la receta
    LaunchedEffect(uiState.receta) {
        uiState.receta?.let { receta ->
            nombre = receta.nombre
            ingredientes = receta.ingredientes.joinToString("\n")
            pasos = receta.pasos.joinToString("\n")
            tiempoPreparacion = receta.tiempoPreparacion.toString()
        }
    }

    // Observar actualización exitosa
    LaunchedEffect(uiState.isUpdated) {
        if (uiState.isUpdated) {
            Toast.makeText(
                context,
                "Receta actualizada exitosamente",
                Toast.LENGTH_LONG
            ).show()
            kotlinx.coroutines.delay(1000)
            onRecetaUpdated()
            viewModel.resetState()
        }
    }

    // Observar errores
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.updateError) {
        uiState.updateError?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            viewModel.clearUpdateError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Editar Receta",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PastelLilac
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(LightLavender)
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    // Estado de carga inicial
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(
                                color = PastelLilac,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "Cargando receta...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Black
                            )
                        }
                    }
                }

                uiState.receta != null -> {
                    // Formulario de edición
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Header
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    PastelLilac,
                                    RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                                )
                                .padding(24.dp)
                        ) {
                            // Header vacío para mantener el espacio
                        }

                        // Form Content
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            // Nombre de la receta
                            EditCustomTextField(
                                value = nombre,
                                onValueChange = { nombre = it },
                                label = "Nombre de la receta",
                                icon = Icons.Default.Edit,
                                placeholder = "Ej: Pasta con champiñones"
                            )

                            // Ingredientes
                            EditCustomTextField(
                                value = ingredientes,
                                onValueChange = { ingredientes = it },
                                label = "Ingredientes",
                                icon = Icons.Default.List,
                                placeholder = "Escribe cada ingrediente en una línea nueva...",
                                isMultiline = true,
                                minLines = 4
                            )

                            // Pasos
                            EditCustomTextField(
                                value = pasos,
                                onValueChange = { pasos = it },
                                label = "Pasos de preparación",
                                icon = Icons.Default.List,
                                placeholder = "Describe cada paso en una línea nueva...",
                                isMultiline = true,
                                minLines = 4
                            )

                            // Tiempo de preparación
                            EditCustomTextField(
                                value = tiempoPreparacion,
                                onValueChange = { tiempoPreparacion = it },
                                label = "Tiempo de preparación",
                                icon = Icons.Default.Add,
                                placeholder = "30",
                                keyboardType = KeyboardType.Number,
                                suffix = "minutos"
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Botón actualizar
                            Button(
                                onClick = {
                                    // Validación básica
                                    if (nombre.isBlank()) {
                                        Toast.makeText(context, "El nombre es obligatorio", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }

                                    if (ingredientes.isBlank()) {
                                        Toast.makeText(context, "Los ingredientes son obligatorios", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }

                                    if (pasos.isBlank()) {
                                        Toast.makeText(context, "Los pasos son obligatorios", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }

                                    val ingredientesList = ingredientes.split("\n").filter { it.isNotBlank() }
                                    val pasosList = pasos.split("\n").filter { it.isNotBlank() }
                                    val tiempo = tiempoPreparacion.toIntOrNull() ?: 0

                                    viewModel.actualizarReceta(
                                        recetaId = recetaId,
                                        nombre = nombre,
                                        ingredientes = ingredientesList,
                                        pasos = pasosList,
                                        tiempoPreparacion = tiempo
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SoftPurple
                                ),
                                enabled = !uiState.isUpdating
                            ) {
                                if (uiState.isUpdating) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Actualizar Receta",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }

                else -> {
                    // Estado de error
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "No se pudo cargar la receta",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Black
                            )
                            Button(
                                onClick = {
                                    viewModel.cargarReceta(recetaId)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PastelLilac
                                )
                            ) {
                                Text("Reintentar")
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditCustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    placeholder: String = "",
    isMultiline: Boolean = false,
    minLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text,
    suffix: String? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = PastelLilac,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = PastelLilac,
                    fontWeight = FontWeight.SemiBold
                )
            }

            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = {
                    Text(
                        text = placeholder,
                        color = Color.Gray.copy(alpha = 0.6f)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                minLines = minLines,
                maxLines = if (isMultiline) 6 else 1,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PastelLilac,
                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                    cursorColor = PastelLilac,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                ),
                trailingIcon = if (suffix != null) {
                    {
                        Text(
                            text = suffix,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray.copy(alpha = 0.6f),
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                } else null
            )
        }
    }
}