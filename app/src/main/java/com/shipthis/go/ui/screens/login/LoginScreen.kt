package com.shipthis.go.ui.screens.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onOtpRequested: (String) -> Unit
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
            text = "Enter your email",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = uiState.email,
            onValueChange = viewModel::updateEmail,
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                viewModel.requestOtp {
                    onOtpRequested(uiState.email)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading && uiState.email.isNotBlank()
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
            } else {
                Text("Send OTP")
            }
        }
        
        uiState.error?.let { error ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

