package com.example.app_recetas.src.Features.Recetas.presentation.view

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.app_recetas.src.Core.di.HardwareModule
import kotlinx.coroutines.launch
import java.io.File


private val DarkBackground = Color(0xFF1A1A1A)
private val PrimaryColor = Color(0xFFB39DDB)

@Composable
fun Camara(
    onPhotoTaken: (String) -> Unit,
    onDismiss: () -> Unit,
    enableCompression: Boolean = true,
    compressionQuality: Int = 80
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()


    val cameraManager = remember {
        HardwareModule.cameraManager(lifecycleOwner)
    }


    var hasPermission by remember { mutableStateOf(false) }
    var isCapturing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var previewView by remember { mutableStateOf<PreviewView?>(null) }


    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        if (!isGranted) {
            onDismiss()
        }
    }


    LaunchedEffect(Unit) {
        hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }


    fun takePhotoAction() {
        if (isCapturing) return

        scope.launch {
            isCapturing = true
            errorMessage = null

            try {

                val outputDir = File(context.filesDir, "recipe_photos")
                if (!outputDir.exists()) {
                    outputDir.mkdirs()
                }

                val result = if (enableCompression) {
                    cameraManager.takePhotoCompressed(outputDir, compressionQuality)
                } else {
                    cameraManager.takePhoto(outputDir)
                }

                result.fold(
                    onSuccess = { file ->
                        onPhotoTaken(file.absolutePath)
                    },
                    onFailure = { exception ->
                        errorMessage = exception.message ?: "Error al tomar la foto"
                    }
                )
            } catch (e: Exception) {
                errorMessage = e.message ?: "Error inesperado al capturar la foto"
            } finally {
                isCapturing = false
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DarkBackground
    ) {
        if (!hasPermission) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "📷",
                    fontSize = 80.sp,
                    color = PrimaryColor
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Permiso de Cámara Requerido",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Para tomar fotos de tus recetas, necesitamos acceso a tu cámara",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        )
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryColor
                        )
                    ) {
                        Text("Conceder Permiso")
                    }
                }
            }
        } else if (!cameraManager.isCameraAvailable()) {
            // Pantalla de cámara no disponible
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "📷",
                    fontSize = 80.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Cámara no disponible",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "No se detectó una cámara en este dispositivo",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryColor
                    )
                ) {
                    Text("Cerrar")
                }
            }
        } else {
            // Interfaz principal de la cámara
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                // Preview de la cámara
                AndroidView(
                    factory = { ctx ->
                        PreviewView(ctx).apply {
                            previewView = this
                            // Inicializar cámara cuando se crea el preview
                            post {
                                if (cameraManager is com.example.app_recetas.src.Core.Hardware.Camara.data.CameraManager) {
                                    cameraManager.initializeCamera(this)
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Botón cerrar (esquina superior derecha)
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.5f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar cámara",
                            tint = Color.White,
                            modifier = Modifier
                                .size(48.dp)
                                .padding(12.dp)
                        )
                    }
                }

                // Controles de captura (parte inferior)
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    // Indicador de compresión
                    if (enableCompression) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color.Black.copy(alpha = 0.7f)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = "Calidad: ${compressionQuality}%",
                                color = Color.White,
                                modifier = Modifier.padding(12.dp, 8.dp),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    // Texto de ayuda
                    Text(
                        text = "Toca el botón para capturar la foto de tu receta",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Botón de captura
                    Button(
                        onClick = { takePhotoAction() },
                        enabled = !isCapturing,
                        modifier = Modifier.size(80.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            disabledContainerColor = Color.White.copy(alpha = 0.5f)
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        if (isCapturing) {
                            CircularProgressIndicator(
                                color = DarkBackground,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(32.dp)
                            )
                        } else {
                            Text(
                                text = "📷",
                                fontSize = 32.sp,
                                color = DarkBackground
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isCapturing) "Capturando..." else "Capturar",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }

                // Mensaje de error
                errorMessage?.let { message ->
                    LaunchedEffect(message) {
                        kotlinx.coroutines.delay(3000)
                        errorMessage = null
                    }

                    Card(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Red.copy(alpha = 0.9f)
                        )
                    ) {
                        Text(
                            text = message,
                            color = Color.White,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}