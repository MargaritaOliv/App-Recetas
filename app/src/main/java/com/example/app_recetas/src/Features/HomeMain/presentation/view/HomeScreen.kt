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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.app_recetas.src.Features.Recetas.domain.model.Recetas
import com.example.app_recetas.src.Features.Recetas.presentation.viewModel.RecetasListViewModel
import com.example.app_recetas.src.Features.Recetas.presentation.viewModel.RecetasListUiState


private val PastelLilac = Color(0xFFB39DDB)
private val PastelBlue = Color(0xFF90CAF9)
private val SoftPurple = Color(0xFFCE93D8)
private val FieldBorderPastel = Color(0xFFE1BEE7)
private val LightLavender = Color(0xFFF3E5F5)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: RecetasListViewModel,
    onNavigateToCreate: () -> Unit,
    onNavigateToDetail: (Int) -> Unit = {},
    shouldRefresh: Boolean = false,
    onRefreshHandled: () -> Unit = {}
) {

    val uiState by viewModel.uiState.collectAsState()


    LaunchedEffect(shouldRefresh) {
        if (shouldRefresh) {
            println("🔄 [UI] Refresh solicitado externamente")
            viewModel.refreshRecetas()
            onRefreshHandled()
        }
    }


    LaunchedEffect(uiState) {
        println("🔄 [UI] Estado actualizado: ${uiState.recetas.size} recetas, loading: ${uiState.isLoading}")
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
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menú",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    // ✅ BOTÓN DE REFRESH
                    IconButton(
                        onClick = {
                            println("🔄 [UI] Refresh manual solicitado")
                            viewModel.refreshRecetas()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Actualizar",
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
                onClick = onNavigateToCreate,
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

        // ✅ MANEJAR ESTADOS DE LA UI
        when {
            uiState.isLoading && uiState.recetas.isEmpty() -> {
                // Estado de carga inicial
                LoadingContent(paddingValues)
            }

            uiState.error != null && uiState.recetas.isEmpty() -> {
                // Estado de error sin datos
                val errorMessage = uiState.error ?: "Error desconocido"
                ErrorContent(
                    error = errorMessage,
                    paddingValues = paddingValues,
                    onRetry = { viewModel.refreshRecetas() }
                )
            }

            uiState.isEmpty && !uiState.isLoading -> {
                // Estado vacío
                EmptyContent(paddingValues)
            }

            else -> {
                // Estado con datos (usar filteredRecetas para búsqueda)
                RecetasContent(
                    recetas = if (uiState.searchQuery.isNotBlank()) uiState.filteredRecetas else uiState.recetas,
                    isRefreshing = uiState.isRefreshing,
                    paddingValues = paddingValues,
                    onNavigateToDetail = onNavigateToDetail,
                    onRefresh = { viewModel.refreshRecetas() }
                )
            }
        }

        // ✅ MOSTRAR ERROR COMO SNACKBAR SI HAY DATOS
        val currentError = uiState.error
        if (currentError != null && uiState.recetas.isNotEmpty()) {
            LaunchedEffect(currentError) {
                // Mostrar como snackbar o toast
                println("⚠️ [UI] Error con datos existentes: $currentError")
            }
        }
    }
}

@Composable
private fun LoadingContent(paddingValues: PaddingValues) {
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

@Composable
private fun ErrorContent(
    error: String,
    paddingValues: PaddingValues,
    onRetry: () -> Unit
) {
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
                    text = error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFD32F2F),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Button(
                    onClick = onRetry,
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

@Composable
private fun EmptyContent(paddingValues: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = PastelLilac.copy(alpha = 0.6f),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No tienes recetas aún",
                style = MaterialTheme.typography.titleLarge,
                color = Color.Black,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "¡Crea tu primera receta deliciosa!",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun RecetasContent(
    recetas: List<Recetas>,
    isRefreshing: Boolean,
    paddingValues: PaddingValues,
    onNavigateToDetail: (Int) -> Unit,
    onRefresh: () -> Unit
) {
    // ✅ SIMPLE REFRESH SIN PullToRefresh (que no existe en todas las versiones)
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ✅ INDICADOR DE REFRESH EN LA PARTE SUPERIOR
        if (isRefreshing) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        color = PastelLilac,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Actualizando...",
                        style = MaterialTheme.typography.bodySmall,
                        color = PastelLilac
                    )
                }
            }
        }

        items(
            items = recetas,
            key = { it.id }
        ) { receta ->
            ModernRecipeCard(
                receta = receta,
                onNavigateToDetail = onNavigateToDetail
            )
        }
    }
}

@Composable
fun ModernRecipeCard(
    receta: Recetas,
    onNavigateToDetail: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            // Imagen de la receta
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .fillMaxHeight()
            ) {
                if (!receta.imagenReceta.isNullOrEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(receta.imagenReceta)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Imagen de ${receta.nombre}",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Placeholder cuando no hay imagen
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                PastelLilac.copy(alpha = 0.3f),
                                RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Sin imagen",
                            tint = PastelLilac,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }

            // Contenido de la receta
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                PastelLilac.copy(alpha = 0.1f),
                                PastelBlue.copy(alpha = 0.2f)
                            )
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Información superior
                    Column {
                        Text(
                            text = receta.nombre,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (receta.tiempoPreparacion != null && receta.tiempoPreparacion > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${receta.tiempoPreparacion} min",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Black
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Información de ingredientes y pasos
                        if (receta.ingredientes.isNotEmpty()) {
                            Text(
                                text = "Ingredientes: ${receta.ingredientes.size}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Black
                            )
                        }

                        if (receta.pasos.isNotEmpty()) {
                            Text(
                                text = "Pasos: ${receta.pasos.size}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Black
                            )
                        }
                    }


                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = { onNavigateToDetail(receta.id) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PastelLilac
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(
                                text = "Ver receta",
                                fontSize = 11.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}