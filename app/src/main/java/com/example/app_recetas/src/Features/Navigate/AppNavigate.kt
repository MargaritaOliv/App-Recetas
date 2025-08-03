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
import com.example.app_recetas.src.Features.Recetas.di.AppNetwork as RecetasAppNetwork

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

    // ✅ CREAR EL VIEWMODEL UNA SOLA VEZ PARA TODA LA NAVEGACIÓN
    val homeViewModel = remember { RecetasAppNetwork.provideRecetasListViewModel() }

    // Estados para mensajes
    var showLoginSuccessMessage by remember { mutableStateOf(false) }
    var showRegisterSuccessMessage by remember { mutableStateOf(false) }
    var showRecetaCreatedMessage by remember { mutableStateOf(false) }
    var showRecetaUpdatedMessage by remember { mutableStateOf(false) }
    var showRecetaDeletedMessage by remember { mutableStateOf(false) }

    // ✅ ELIMINADO: shouldRefreshHome - Ya no es necesario con Flow reactivo

    // LaunchedEffects para mostrar mensajes
    LaunchedEffect(showLoginSuccessMessage) {
        if (showLoginSuccessMessage) {
            snackbarHostState.showSnackbar(
                message = "¡Inicio de sesión exitoso! Bienvenido",
                duration = SnackbarDuration.Short
            )
            showLoginSuccessMessage = false
        }
    }

    LaunchedEffect(showRegisterSuccessMessage) {
        if (showRegisterSuccessMessage) {
            snackbarHostState.showSnackbar(
                message = "¡Registro exitoso! Ahora puedes iniciar sesión",
                duration = SnackbarDuration.Long
            )
            showRegisterSuccessMessage = false
        }
    }

    LaunchedEffect(showRecetaCreatedMessage) {
        if (showRecetaCreatedMessage) {
            snackbarHostState.showSnackbar(
                message = "¡Receta guardada exitosamente!",
                duration = SnackbarDuration.Short
            )
            showRecetaCreatedMessage = false
        }
    }

    LaunchedEffect(showRecetaUpdatedMessage) {
        if (showRecetaUpdatedMessage) {
            snackbarHostState.showSnackbar(
                message = "¡Receta actualizada exitosamente!",
                duration = SnackbarDuration.Short
            )
            showRecetaUpdatedMessage = false
        }
    }

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
            composable(Screen.Login.route) {
                LoginScreen(
                    onNavigateBack = {
                    },
                    onNavigateToRegister = {
                        navController.navigate(Screen.Register.route)
                    },
                    onLoginSuccess = { token ->
                        showLoginSuccessMessage = true
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                        println("Login exitoso! Token guardado en DataStore")
                    }
                )
            }

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

            // ✅ PANTALLA HOME CON VIEWMODEL REACTIVO
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = homeViewModel,              // ✅ Pasar viewModel en lugar de factory
                    onNavigateToCreate = {
                        navController.navigate(Screen.CreateReceta.route)
                    },
                    onNavigateToDetail = { recetaId ->
                        navController.navigate(Screen.DetailReceta.createRoute(recetaId))
                    }
                    // ✅ ELIMINADO: shouldRefresh y onRefreshHandled - No son necesarios con Flow
                )
            }

            // ✅ PANTALLA CREAR RECETA
            composable(Screen.CreateReceta.route) {
                val viewModel = remember { RecetasAppNetwork.provideRecetasViewModel() }
                RecetaCreateScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onRecetaCreated = {
                        showRecetaCreatedMessage = true
                        // ✅ ELIMINADO: shouldRefreshHome - El Flow se actualiza automáticamente
                        navController.popBackStack()
                    },
                    viewModel = viewModel
                )
            }

            // ✅ PANTALLA DETALLE
            composable(
                route = Screen.DetailReceta.route,
                arguments = listOf(navArgument("recetaId") { type = NavType.IntType })
            ) { backStackEntry ->
                val recetaId = backStackEntry.arguments?.getInt("recetaId") ?: 0
                val viewModel = remember { RecetasAppNetwork.provideRecetaDetailViewModel() }

                RecetaDetailScreen(
                    recetaId = recetaId,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToEdit = { id ->
                        navController.navigate(Screen.EditReceta.createRoute(id))
                    },
                    onRecetaDeleted = {
                        showRecetaDeletedMessage = true
                        // ✅ ELIMINADO: shouldRefreshHome - El Flow se actualiza automáticamente
                        navController.popBackStack(Screen.Home.route, false)
                    },
                    viewModel = viewModel
                )
            }

            // ✅ PANTALLA EDITAR
            composable(
                route = Screen.EditReceta.route,
                arguments = listOf(navArgument("recetaId") { type = NavType.IntType })
            ) { backStackEntry ->
                val recetaId = backStackEntry.arguments?.getInt("recetaId") ?: 0
                val viewModel = remember { RecetasAppNetwork.provideRecetaEditViewModel() }

                RecetaEditScreen(
                    recetaId = recetaId,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onRecetaUpdated = {
                        showRecetaUpdatedMessage = true
                        // ✅ ELIMINADO: shouldRefreshHome - El Flow se actualiza automáticamente
                        navController.popBackStack()
                    },
                    viewModel = viewModel
                )
            }
        }
    }
}