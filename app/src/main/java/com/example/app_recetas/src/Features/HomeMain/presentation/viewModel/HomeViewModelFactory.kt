package com.example.app.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.app.domain.usecase.HomeUseCase

class HomeViewModelFactory(
    private val homeUseCase: HomeUseCase
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(homeUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}