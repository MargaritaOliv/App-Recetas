package com.example.app_recetas.src.Features.Recetas.presentation.view

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.app_recetas.src.Features.Recetas.presentation.viewModel.RecetasViewModel
import com.example.app_recetas.src.Features.Recetas.presentation.viewModel.RecetaViewModelFactory
import java.io.File


private val PastelLilac = Color(0xFFB39DDB)
private val PastelBlue = Color(0xFF90CAF9)
private val SoftPurple = Color(0xFFCE93D8)
private val FieldBorderPastel = Color(0xFFE1BEE7)
private val LightLavender = Color(0xFFF3E5F5)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecetaCreateScreen(
    onNavigateBack: () -> Unit = {},
    onRecetaCreated: () -> Unit = {},
    viewModel: RecetasViewModel = viewModel(factory = RecetaViewModelFactory())
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()


    var nombre by remember { mutableStateOf("") }
    var ingredientes by remember { mutableStateOf("") }
    var pasos by remember { mutableStateOf("") }
    var tiempoPreparacion by remember { mutableStateOf("") }


    var imagenPath by remember { mutableStateOf<String?>(null) }
    var showCamera by remember { mutableStateOf(false) }


    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (isGranted) {
            showCamera = true
        } else {
            Toast.makeText(
                context,
                "Permiso de cámara necesario para tomar fotos de las recetas",
                Toast.LENGTH_LONG
            ).show()
        }
    }


    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            Toast.makeText(
                context,
                "Receta '${uiState.receta?.nombre}' guardada exitosamente",
                Toast.LENGTH_LONG
            ).show()
            kotlinx.coroutines.delay(1000)
            onRecetaCreated()
            viewModel.resetState()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Nueva Receta",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LightLavender)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        PastelLilac,
                        RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                    )
                    .padding(24.dp)
            ) {

            }


            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {


                ImageSection(
                    imagenPath = imagenPath,
                    onTakePhoto = {
                        if (hasCameraPermission) {
                            showCamera = true
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    onRemovePhoto = { imagenPath = null }
                )


                CustomTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = "Nombre de la receta",
                    icon = Icons.Default.Edit,
                    placeholder = "Ej: Pasta con champiñones"
                )


                CustomTextField(
                    value = ingredientes,
                    onValueChange = { ingredientes = it },
                    label = "Ingredientes",
                    icon = Icons.Default.List,
                    placeholder = "Escribe cada ingrediente en una línea nueva...",
                    isMultiline = true,
                    minLines = 4
                )


                CustomTextField(
                    value = pasos,
                    onValueChange = { pasos = it },
                    label = "Pasos de preparación",
                    icon = Icons.Default.List,
                    placeholder = "Describe cada paso en una línea nueva...",
                    isMultiline = true,
                    minLines = 4
                )


                CustomTextField(
                    value = tiempoPreparacion,
                    onValueChange = { tiempoPreparacion = it },
                    label = "Tiempo de preparación",
                    icon = Icons.Default.Add,
                    placeholder = "30",
                    keyboardType = KeyboardType.Number,
                    suffix = "minutos"
                )

                Spacer(modifier = Modifier.height(8.dp))


                Button(
                    onClick = {

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


                        viewModel.crearReceta(
                            nombre = nombre,
                            ingredientes = ingredientesList,
                            pasos = pasosList,
                            tiempoPreparacion = tiempo,
                            imagenPath = imagenPath
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SoftPurple
                    ),
                    enabled = !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
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
                            text = "Guardar Receta",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }


    if (showCamera) {
        Dialog(
            onDismissRequest = { showCamera = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = false
            )
        ) {
            Camara(
                onPhotoTaken = { path ->
                    imagenPath = path
                    showCamera = false
                    Toast.makeText(context, "Foto capturada exitosamente", Toast.LENGTH_SHORT).show()
                },
                onDismiss = { showCamera = false },
                enableCompression = true,
                compressionQuality = 80
            )
        }
    }
}

@Composable
private fun ImageSection(
    imagenPath: String?,
    onTakePhoto: () -> Unit,
    onRemovePhoto: () -> Unit
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
                Text(
                    text = "📷",
                    fontSize = 20.sp,
                    color = PastelLilac
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Imagen de la receta",
                    style = MaterialTheme.typography.labelLarge,
                    color = PastelLilac,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (imagenPath != null) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(File(imagenPath))
                            .crossfade(true)
                            .build(),
                        contentDescription = "Imagen de la receta",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )


                    IconButton(
                        onClick = onRemovePhoto,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar imagen",
                            tint = Color.White,
                            modifier = Modifier
                                .background(
                                    Color.Red.copy(alpha = 0.7f),
                                    CircleShape
                                )
                                .padding(8.dp)
                                .size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))



                OutlinedButton(
                    onClick = onTakePhoto,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = PastelLilac
                    )
                ) {
                    Text("➕", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("📷 Tomar nueva foto")
                }
            } else {

                OutlinedButton(
                    onClick = onTakePhoto,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = PastelLilac
                    )
                ) {
                    Text("➕", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("📷 Tomar foto de la receta")
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTextField(
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
                    focusedBorderColor = Color(0xFF2E7D32),
                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f)
                ),
                trailingIcon = suffix?.let {
                    {
                        Text(
                            text = suffix,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray.copy(alpha = 0.6f),
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                }
            )
        }
    }
}