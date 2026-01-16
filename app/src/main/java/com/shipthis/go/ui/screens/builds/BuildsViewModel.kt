package com.shipthis.go.ui.screens.builds

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shipthis.go.data.model.GoBuild
import com.shipthis.go.data.repository.GoBuildRepository
import com.shipthis.go.util.BuildLauncher
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class BuildsViewModel @Inject constructor(
    private val repository: GoBuildRepository,
    private val buildLauncher: BuildLauncher
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    // Observe builds from the database
    val builds: StateFlow<List<GoBuild>> = repository.getAllGoBuilds()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    val error: StateFlow<String?> = _error.asStateFlow()

    fun launchBuild(context: Context, goBuild: GoBuild) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isLoading.value = true
                _error.value = null

                // Launch the build using BuildLauncher service
                buildLauncher.launchBuild(context, goBuild)

                _isLoading.value = false
                _error.value = null
            } catch (e: Exception) {
                _isLoading.value = false
                _error.value = e.message ?: "Failed to launch build"
            }
        }
    }
}

