package com.example.app.presentation.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app.domain.model.HomeUser
import com.example.app.presentation.viewModel.HomeViewModel
import com.example.app.presentation.viewModel.HomeViewModelFactory

// Colores pastel lila y celeste
private val PastelLilac = Color(0xFFB39DDB)
private val PastelBlue = Color(0xFF90CAF9)
private val SoftPurple = Color(0xFFCE93D8)
private val FieldBorderPastel = Color(0xFFE1BEE7)
private val LightLavender = Color(0xFFF3E5F5)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModelFactory: HomeViewModelFactory,
    userToken: String? = null,
    onNavigateToCreate: (String) -> Unit,
    onNavigateToDetail: (Int) -> Unit = {},
    viewModel: HomeViewModel = viewModel(factory = viewModelFactory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val token = userToken ?: "tu_token_aqui"

    LaunchedEffect(token) {
        if (token != "tu_token_aqui") {
            viewModel.getRecetas(token)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mis Recetas",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { /* Acción del menú */ }) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menú",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PastelLilac
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (token != "tu_token_aqui") {
                        onNavigateToCreate(token)
                    }
                },
                containerColor = SoftPurple,
                contentColor = Color.White,
                modifier = Modifier.shadow(8.dp, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Agregar receta",
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        containerColor = LightLavender
    ) { paddingValues ->
        when (val currentState = uiState) {
            is HomeUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            color = PastelLilac,
                            strokeWidth = 4.dp,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Cargando recetas deliciosas...",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Black,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            is HomeUiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(currentState.recetas) { receta ->
                        ModernRecipeCard(
                            receta = receta,
                            onNavigateToDetail = onNavigateToDetail
                        )
                    }
                }
            }

            is HomeUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFCDD2)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Error al cargar recetas",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFFD32F2F)
                            )
                            Text(
                                text = currentState.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFD32F2F),
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            Button(
                                onClick = { viewModel.getRecetas(token) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PastelLilac
                                )
                            ) {
                                Text("Reintentar", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ModernRecipeCard(
    receta: HomeUser,
    onNavigateToDetail: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                PastelLilac.copy(alpha = 0.1f),
                                PastelBlue.copy(alpha = 0.2f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = receta.nombre ?: "Receta sin nombre",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (receta.tiempoPreparacion != null && receta.tiempoPreparacion > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${receta.tiempoPreparacion} min",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }

                Column {
                    if (!receta.ingredientes.isNullOrEmpty()) {
                        Text(
                            text = "Ingredientes: ${receta.ingredientes.size}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Black
                        )
                    }

                    if (!receta.pasos.isNullOrEmpty()) {
                        Text(
                            text = "Pasos: ${receta.pasos.size}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = {
                                receta.id?.let { id ->
                                    onNavigateToDetail(id)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PastelLilac
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(
                                text = "Ver receta",
                                fontSize = 12.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}