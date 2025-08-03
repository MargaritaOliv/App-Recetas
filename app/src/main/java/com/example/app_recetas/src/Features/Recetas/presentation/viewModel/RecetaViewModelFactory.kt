package com.example.app_recetas.src.Features.Recetas.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.app_recetas.src.Features.Recetas.di.AppNetwork

class RecetaViewModelFactory : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(RecetasViewModel::class.java) -> {
                // ✅ USAR LAS FUNCIONES PROVIDE EXISTENTES
                AppNetwork.provideRecetasViewModel() as T
            }
            modelClass.isAssignableFrom(RecetasListViewModel::class.java) -> {
                RecetasListViewModel(
                    getAllRecetasUseCase = AppNetwork.getAllRecetasUseCase,
                    deleteRecetaUseCase = AppNetwork.deleteRecetaUseCase,
                    connectivityRepository = AppNetwork.provideConnectivityRepository()
                ) as T
            }
            modelClass.isAssignableFrom(RecetaDetailViewModel::class.java) -> {
                RecetaDetailViewModel(
                    getRecetaUseCase = AppNetwork.getRecetaUseCase,
                    deleteRecetaUseCase = AppNetwork.deleteRecetaUseCase,
                    connectivityRepository = AppNetwork.provideConnectivityRepository()
                ) as T
            }
            modelClass.isAssignableFrom(RecetaEditViewModel::class.java) -> {
                RecetaEditViewModel(
                    getRecetaUseCase = AppNetwork.getRecetaUseCase,
                    updateRecetaUseCase = AppNetwork.updateRecetaUseCase,
                    connectivityRepository = AppNetwork.provideConnectivityRepository()
                ) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}