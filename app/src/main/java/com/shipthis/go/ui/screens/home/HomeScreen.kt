package com.shipthis.go.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome to ShipThis Go!",
            style = MaterialTheme.typography.headlineMedium
        )

        // Build ID Input
        OutlinedTextField(
            value = uiState.buildId,
            onValueChange = { newValue ->
                if (newValue.length <= 8) {
                    viewModel.updateBuildId(newValue)
                }
            },
            label = { Text("Build ID") },
            placeholder = { Text("Enter Build ID (8 chars)") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            enabled = !uiState.isLoading,
            singleLine = true
        )

        // Submit Build ID Button → triggers download/unzip/launch
        Button(
            onClick = { viewModel.submitBuildId(context) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            enabled = !uiState.isLoading
        ) {
            Text("Submit Build ID")
        }

        // Status or Error
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            }
            uiState.error != null -> {
                Text(
                    text = "Error: ${uiState.error}",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }
            else -> {
                Text(
                    text = uiState.data ?: "No data yet",
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

       
    }
}
