package com.example.app_recetas.src.Features.Register.presentation.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app_recetas.src.Features.Register.di.AppModule
import com.example.app_recetas.src.Features.Register.presentation.viewModel.RegisterViewModel
import com.example.app_recetas.src.Features.Register.presentation.viewModel.RegisterViewModelFactory

// Colores pastel lila y celeste
private val PastelLilac = Color(0xFFB39DDB)
private val PastelBlue = Color(0xFF90CAF9)
private val SoftPurple = Color(0xFFCE93D8)
private val FieldBorderPastel = Color(0xFFE1BEE7)
private val ErrorRed = Color(0xFFD32F2F)
private val TextPrimary = Color.Black

@Composable
fun RegisterScreen(
    onNavigateBack: () -> Unit = {},
    onRegisterSuccess: () -> Unit = {}, // Ya no es @Composable
    registerViewModel: RegisterViewModel = viewModel(
        factory = RegisterViewModelFactory(AppModule.provideRegisterUseCase())
    )
) {
    val uiState by registerViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onRegisterSuccess()
            registerViewModel.resetSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        // Imagen predeterminada desde URL
        AsyncImage(
            model = "https://riberasalud.com/cardiosalus/wp-content/uploads/2024/09/buddha-bowl-dish-with-vegetables-legumes-top-view-1.jpg",
            contentDescription = "Buddha bowl con vegetales",
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "¡Registro!",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = PastelLilac
        )

        Spacer(modifier = Modifier.height(48.dp))

        CustomTextField(
            value = uiState.correo,
            onValueChange = registerViewModel::updateCorreo,
            placeholder = "Correo",
            icon = Icons.Default.Email,
            enabled = !uiState.isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        CustomTextField(
            value = uiState.nombreUsuario,
            onValueChange = registerViewModel::updateNombreUsuario,
            placeholder = "Nombre de usuario",
            icon = Icons.Default.Person,
            enabled = !uiState.isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        CustomTextField(
            value = uiState.contrasena,
            onValueChange = registerViewModel::updateContrasena,
            placeholder = "Contraseña",
            icon = Icons.Default.Lock,
            isPassword = true,
            enabled = !uiState.isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        uiState.errorMessage?.let { error ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFCDD2)
                )
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(16.dp),
                    color = ErrorRed,
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Button(
            onClick = registerViewModel::register,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (uiState.isLoading) FieldBorderPastel else PastelLilac
            ),
            shape = RoundedCornerShape(25.dp),
            enabled = !uiState.isLoading &&
                    uiState.correo.isNotBlank() &&
                    uiState.contrasena.isNotBlank() &&
                    uiState.nombreUsuario.isNotBlank()
        ) {
            if (uiState.isLoading) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Registrando...",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                Text(
                    text = "Crear cuenta",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onNavigateBack) {
            Text(
                text = "¿Ya tienes cuenta? Inicia sesión",
                color = PastelLilac
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: ImageVector,
    isPassword: Boolean = false,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = placeholder,
                color = FieldBorderPastel
            )
        },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) FieldBorderPastel else Color.LightGray
            )
        },
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(25.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PastelLilac,
            unfocusedBorderColor = FieldBorderPastel,
            disabledBorderColor = FieldBorderPastel,
            focusedLabelColor = PastelLilac,
            cursorColor = PastelLilac,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            disabledTextColor = Color.Gray
        ),
        singleLine = true,
        enabled = enabled
    )
}