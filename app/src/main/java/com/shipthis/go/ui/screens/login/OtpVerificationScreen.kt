package com.shipthis.go.ui.screens.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun OtpVerificationScreen(
    email: String,
    viewModel: LoginViewModel = hiltViewModel(),
    onVerificationSuccess: () -> Unit,
    onBackToEmail: () -> Unit
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
            text = "Enter the code sent to",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = email,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = uiState.otpCode,
            onValueChange = viewModel::updateOtpCode,
            label = { Text("OTP Code") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = KeyboardType.NumberPassword,
                imeAction = ImeAction.Done
            ),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = {
                    viewModel.goBackToEmail()
                    onBackToEmail()
                },
                modifier = Modifier.weight(1f),
                enabled = !uiState.isLoading
            ) {
                Text("Back")
            }

            Button(
                onClick = {
                    viewModel.verifyOtp(email, onVerificationSuccess)
                },
                modifier = Modifier.weight(1f),
                enabled = !uiState.isLoading && uiState.otpCode.length == 6
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Text("Verify")
                }
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

