package com.shipthis.go.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shipthis.go.data.repository.SampleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: SampleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val data = repository.getSampleData()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    data = data,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    fun updateBuildId(buildId: String) {
        _uiState.value = _uiState.value.copy(buildId = buildId)
    }

    fun submitBuildId() {
        val buildId = _uiState.value.buildId
        if (buildId.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Build ID cannot be empty")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                // TODO: Replace with actual API call
                // val result = repository.submitBuildId(buildId)
                
                // Simulate API call delay
                kotlinx.coroutines.delay(2000)
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    data = "Build ID '$buildId' submitted successfully!",
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to submit Build ID"
                )
            }
        }
    }
}

data class HomeUiState(
    val isLoading: Boolean = false,
    val data: String? = null,
    val error: String? = null,
    val buildId: String = ""
)
