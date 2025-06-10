package com.example.app.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.domain.usecase.HomeUseCase
import com.example.app.presentation.view.HomeUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val homeUseCase: HomeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun getRecetas(token: String) {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading

            homeUseCase.getRecetas(token)
                .onSuccess { recetas ->
                    _uiState.value = HomeUiState.Success(recetas)
                }
                .onFailure { exception ->
                    _uiState.value = HomeUiState.Error(
                        exception.message ?: "Error desconocido"
                    )
                }
        }
    }
}