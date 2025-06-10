package com.example.app_recetas.src.Features.Recetas.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.app_recetas.src.Features.Recetas.di.AppNetwork

class RecetaViewModelFactory : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(RecetasViewModel::class.java) -> {
                AppNetwork.provideRecetasViewModel() as T
            }
            modelClass.isAssignableFrom(RecetasListViewModel::class.java) -> {
                AppNetwork.provideRecetasListViewModel() as T
            }
            modelClass.isAssignableFrom(RecetaDetailViewModel::class.java) -> {
                AppNetwork.provideRecetaDetailViewModel() as T
            }
            modelClass.isAssignableFrom(RecetaEditViewModel::class.java) -> {
                AppNetwork.provideRecetaEditViewModel() as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}