package com.shipthis.go.ui.screens.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shipthis.go.data.repository.GoBuildRepository
import com.shipthis.go.util.BuildLauncher
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: GoBuildRepository,
    private val buildLauncher: BuildLauncher
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun updateBuildId(buildId: String) {
        _uiState.value = _uiState.value.copy(buildId = buildId)
    }

    fun submitBuildId(context: Context) {
        val buildId = _uiState.value.buildId
        if (buildId.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Build ID cannot be empty")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null, data = null)

                updateStatus("Fetching build details…")

                // Fetch the GoBuild from the API
                val goBuild = repository.getGoBuild(buildId)

                updateStatus("Build found: ${goBuild.project.name}")

                // Save the build to the local database
                repository.saveGoBuild(goBuild)

                // Launch the build using BuildLauncher service
                buildLauncher.launchBuild(context, goBuild) { status ->
                    updateStatus(status)
                }

                _uiState.value =
                        _uiState.value.copy(
                                isLoading = false,
                                data =
                                        "Build '${goBuild.project.name}' (${goBuild.id}) launched successfully!",
                                error = null
                        )
            } catch (e: Exception) {
                _uiState.value =
                        _uiState.value.copy(
                                isLoading = false,
                                error = e.message ?: "Failed: unknown error"
                        )
            }
        }
    }

    private fun updateStatus(msg: String) {
        _uiState.value = _uiState.value.copy(data = msg)
    }
}

data class HomeUiState(
        val isLoading: Boolean = false,
        val data: String? = null,
        val error: String? = null,
        val buildId: String = ""
)
