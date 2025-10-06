package com.shipthis.go.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

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
            singleLine = true,
            maxLines = 1
        )
        
        // Submit Build ID Button
        Button(
            onClick = { viewModel.submitBuildId() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            enabled = !uiState.isLoading
        ) {
            Text("Submit Build ID")
        }
        
        // Status Display
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
                    text = uiState.data ?: "No data available",
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        
        // Demo Load Data Button (keeping for now)
        Button(
            onClick = { viewModel.loadData() },
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Load Demo Data")
        }
    }
}
