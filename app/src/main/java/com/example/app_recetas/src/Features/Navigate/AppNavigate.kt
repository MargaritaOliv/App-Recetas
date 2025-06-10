// AppNavigate.kt - CORREGIDO PARA TOKENS
package com.example.app_recetas.src.Features.Navigate

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.app_recetas.src.Features.Register.presentation.view.RegisterScreen
import com.example.app_recetas.src.Features.Login.presentation.view.LoginScreen
import com.example.app_recetas.src.Features.Recetas.presentation.view.RecetaCreateScreen
import com.example.app_recetas.src.Features.Recetas.presentation.view.RecetaDetailScreen
import com.example.app_recetas.src.Features.Recetas.presentation.view.RecetaEditScreen
import com.example.app.presentation.view.HomeScreen
import com.example.app.di.AppNetwork

// Sealed class para definir las rutas de navegación
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object CreateReceta : Screen("create_receta")
    object DetailReceta : Screen("detail_receta/{recetaId}") {
        fun createRoute(recetaId: Int) = "detail_receta/$recetaId"
    }
    object EditReceta : Screen("edit_receta/{recetaId}") {
        fun createRoute(recetaId: Int) = "edit_receta/$recetaId"
    }
}

@Composable
fun AppNavigate(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Login.route
) {
    val snackbarHostState = remember { SnackbarHostState() }

    // Estados para manejar mensajes de éxito
    var showLoginSuccessMessage by remember { mutableStateOf(false) }
    var showRegisterSuccessMessage by remember { mutableStateOf(false) }
    var showRecetaCreatedMessage by remember { mutableStateOf(false) }
    var showRecetaUpdatedMessage by remember { mutableStateOf(false) }
    var showRecetaDeletedMessage by remember { mutableStateOf(false) }

    // Estado para guardar el token del usuario
    var userToken by remember { mutableStateOf<String?>(null) }

    // Crear el ViewModelFactory una sola vez
    val homeViewModelFactory = remember { AppNetwork.provideHomeViewModelFactory() }

    // Efecto para mostrar mensaje de login exitoso
    LaunchedEffect(showLoginSuccessMessage) {
        if (showLoginSuccessMessage) {
            snackbarHostState.showSnackbar(
                message = "¡Inicio de sesión exitoso! Bienvenido",
                duration = SnackbarDuration.Short
            )
            showLoginSuccessMessage = false
        }
    }

    // Efecto para mostrar mensaje de registro exitoso
    LaunchedEffect(showRegisterSuccessMessage) {
        if (showRegisterSuccessMessage) {
            snackbarHostState.showSnackbar(
                message = "¡Registro exitoso! Ahora puedes iniciar sesión",
                duration = SnackbarDuration.Long
            )
            showRegisterSuccessMessage = false
        }
    }

    // Efecto para mostrar mensaje de receta creada
    LaunchedEffect(showRecetaCreatedMessage) {
        if (showRecetaCreatedMessage) {
            snackbarHostState.showSnackbar(
                message = "¡Receta guardada exitosamente!",
                duration = SnackbarDuration.Short
            )
            showRecetaCreatedMessage = false
        }
    }

    // Efecto para mostrar mensaje de receta actualizada
    LaunchedEffect(showRecetaUpdatedMessage) {
        if (showRecetaUpdatedMessage) {
            snackbarHostState.showSnackbar(
                message = "¡Receta actualizada exitosamente!",
                duration = SnackbarDuration.Short
            )
            showRecetaUpdatedMessage = false
        }
    }

    // Efecto para mostrar mensaje de receta eliminada
    LaunchedEffect(showRecetaDeletedMessage) {
        if (showRecetaDeletedMessage) {
            snackbarHostState.showSnackbar(
                message = "¡Receta eliminada exitosamente!",
                duration = SnackbarDuration.Short
            )
            showRecetaDeletedMessage = false
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(paddingValues)
        ) {
            // Pantalla de Login
            composable(Screen.Login.route) {
                LoginScreen(
                    onNavigateBack = {
                        // Acción personalizada si la necesitas
                    },
                    onNavigateToRegister = {
                        navController.navigate(Screen.Register.route)
                    },
                    onLoginSuccess = { token ->
                        userToken = token
                        showLoginSuccessMessage = true
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                        println("Login exitoso! Token: $token")
                    }
                )
            }

            // Pantalla de Register
            composable(Screen.Register.route) {
                RegisterScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onRegisterSuccess = {
                        showRegisterSuccessMessage = true
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    }
                )
            }

            // Pantalla principal (Home)
            composable(Screen.Home.route) {
                // VALIDACIÓN: Solo mostrar Home si hay token
                if (userToken != null) {
                    HomeScreen(
                        viewModelFactory = homeViewModelFactory,
                        userToken = userToken!!, // Ya validamos que no es null
                        onNavigateToCreate = {
                            navController.navigate(Screen.CreateReceta.route)
                        },
                        onNavigateToDetail = { recetaId ->
                            navController.navigate(Screen.DetailReceta.createRoute(recetaId))
                        }
                    )
                } else {
                    // Si no hay token, redirigir al login
                    LaunchedEffect(Unit) {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                }
            }

            // Pantalla de crear receta
            composable(Screen.CreateReceta.route) {
                // VALIDACIÓN: Solo mostrar CreateReceta si hay token
                if (userToken != null) {
                    RecetaCreateScreen(
                        userToken = userToken!!, // Ya validamos que no es null
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onRecetaCreated = {
                            showRecetaCreatedMessage = true
                            navController.popBackStack() // Regresa al Home
                        }
                    )
                } else {
                    // Si no hay token, redirigir al login
                    LaunchedEffect(Unit) {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.CreateReceta.route) { inclusive = true }
                        }
                    }
                }
            }

            // Pantalla de detalle de receta
            composable(
                route = Screen.DetailReceta.route,
                arguments = listOf(navArgument("recetaId") { type = NavType.IntType })
            ) { backStackEntry ->
                val recetaId = backStackEntry.arguments?.getInt("recetaId") ?: 0

                // VALIDACIÓN: Solo mostrar DetailReceta si hay token
                if (userToken != null) {
                    RecetaDetailScreen(
                        recetaId = recetaId,
                        userToken = userToken!!, // Ya validamos que no es null
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onNavigateToEdit = { id ->
                            navController.navigate(Screen.EditReceta.createRoute(id))
                        },
                        onRecetaDeleted = {
                            showRecetaDeletedMessage = true
                            navController.popBackStack() // Regresa al Home
                        }
                    )
                } else {
                    // Si no hay token, redirigir al login
                    LaunchedEffect(Unit) {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.DetailReceta.route) { inclusive = true }
                        }
                    }
                }
            }

            // Pantalla de editar receta
            composable(
                route = Screen.EditReceta.route,
                arguments = listOf(navArgument("recetaId") { type = NavType.IntType })
            ) { backStackEntry ->
                val recetaId = backStackEntry.arguments?.getInt("recetaId") ?: 0

                // VALIDACIÓN: Solo mostrar EditReceta si hay token
                if (userToken != null) {
                    RecetaEditScreen(
                        recetaId = recetaId,
                        userToken = userToken!!, // Ya validamos que no es null
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onRecetaUpdated = {
                            showRecetaUpdatedMessage = true
                            navController.popBackStack() // Regresa al detalle
                        }
                    )
                } else {
                    LaunchedEffect(Unit) {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.EditReceta.route) { inclusive = true }
                        }
                    }
                }
            }
        }
    }
}