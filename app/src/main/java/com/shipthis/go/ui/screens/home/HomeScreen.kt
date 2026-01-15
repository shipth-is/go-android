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
import androidx.navigation.compose.currentBackStackEntryAsState

import android.app.Activity
import com.google.zxing.integration.android.IntentIntegrator

import com.shipthis.go.ui.components.LoggedInScreenLayout
import com.shipthis.go.ui.components.rememberQrScannerLauncher

@Composable
fun HomeScreen(
    navController: androidx.navigation.NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "home"

    LoggedInScreenLayout(
        navController = navController,
        currentRoute = currentRoute
    ) {
        val uiState by viewModel.uiState.collectAsState()
        val context = LocalContext.current
        val activity = context as Activity

        val startQrScan = rememberQrScannerLauncher(activity) { result ->
            if (!result.isNullOrBlank()) {
                viewModel.updateBuildId(result)
                viewModel.submitBuildId(context)
            }
        }

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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = startQrScan,
                    modifier = Modifier.weight(1f),
                    enabled = !uiState.isLoading
                ) {
                    Text("Scan QR")
                }

                Button(
                    onClick = { viewModel.submitBuildId(context) },
                    modifier = Modifier.weight(1f),
                    enabled = !uiState.isLoading
                ) {
                    Text("Submit")
                }
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
}
