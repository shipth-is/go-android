package com.shipthis.go.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shipthis.go.data.model.GDPRRequest
import com.shipthis.go.data.model.GDPRRequestStatus
import com.shipthis.go.data.model.GDPRRequestType
import com.shipthis.go.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    val currentUser = authRepository.currentUser

    private val _gdprRequests = MutableStateFlow<List<GDPRRequest>>(emptyList())
    val gdprRequests: StateFlow<List<GDPRRequest>> = _gdprRequests.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _showDeleteConfirmation = MutableStateFlow(false)
    val showDeleteConfirmation: StateFlow<Boolean> = _showDeleteConfirmation.asStateFlow()

    val pendingRequests: StateFlow<List<GDPRRequest>> = _gdprRequests
        .map { requests ->
            requests.filter { it.status == GDPRRequestStatus.PENDING }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val hasPendingExport: StateFlow<Boolean> = pendingRequests
        .map { requests ->
            requests.any { it.type == GDPRRequestType.EXPORT }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val hasPendingDelete: StateFlow<Boolean> = pendingRequests
        .map { requests ->
            requests.any { it.type == GDPRRequestType.DELETE }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    init {
        loadGdprRequests()
    }

    fun loadGdprRequests() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                _gdprRequests.value = authRepository.fetchGdprStatus()
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load GDPR requests"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun requestDataExport() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                authRepository.requestExport()
                // Refresh the list after successful request
                loadGdprRequests()
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to request data export"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun requestAccountDeletion() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                authRepository.requestDelete()
                _showDeleteConfirmation.value = false
                // Refresh the list after successful request
                loadGdprRequests()
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to request account deletion"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun showDeleteConfirmation() {
        _showDeleteConfirmation.value = true
    }

    fun hideDeleteConfirmation() {
        _showDeleteConfirmation.value = false
    }
}

