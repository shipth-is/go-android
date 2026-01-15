package com.shipthis.go.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.shipthis.go.data.model.GDPRRequest
import com.shipthis.go.data.model.GDPRRequestType
import com.shipthis.go.ui.components.LoggedInScreenLayout
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "settings"

    LoggedInScreenLayout(
        navController = navController,
        currentRoute = currentRoute
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(24.dp))

            val user by viewModel.currentUser.collectAsState()
            
            user?.let { currentUser ->
                Text(
                    text = "Email: ${currentUser.email}",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Account Type: ${currentUser.accountType.name}",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                if (currentUser.isBetaUser) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "You have a beta account",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            } ?: run {
                Text(
                    text = "No user information available",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Your Data section
            YourDataSection(viewModel = viewModel)
        }
    }
}

@Composable
fun YourDataSection(viewModel: SettingsViewModel) {
    val pendingRequests by viewModel.pendingRequests.collectAsState()
    val hasPendingExport by viewModel.hasPendingExport.collectAsState()
    val hasPendingDelete by viewModel.hasPendingDelete.collectAsState()
    val showDeleteConfirmation by viewModel.showDeleteConfirmation.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // Delete confirmation dialog
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { viewModel.hideDeleteConfirmation() },
            title = {
                Text(text = "Delete Account")
            },
            text = {
                Text(
                    text = "Are you sure you want to delete your account?\n" +
                            "This will permanently delete your account and all associated data. " +
                            "This action cannot be undone.\n\n" +
                            "We process deletion requests manually - which may take up to 30 days.\n\n" +
                            "If you're sure, click Delete to proceed. Otherwise, click Cancel to keep your account."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.requestAccountDeletion() }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.hideDeleteConfirmation() }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    Column {
        Text(
            text = "Your Data",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "We take your privacy seriously. You can request a copy of your data or " +
                    "delete your account at any time.\n\n" +
                    "All requests are processed manually to ensure security and compliance " +
                    "with our Privacy Policy. This may take up to 30 days, but we'll do our best " +
                    "to handle it as quickly as possible.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Pending requests list
        if (pendingRequests.isNotEmpty() || isLoading) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Pending Requests",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (pendingRequests.isNotEmpty()) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 200.dp)
                ) {
                    items(pendingRequests) { request ->
                        PendingRequestCard(request = request)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Error message
        error?.let {
            Text(
                text = "Error: $it",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Action buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = { viewModel.requestDataExport() },
                enabled = !hasPendingExport && !isLoading,
                modifier = Modifier.weight(1f)
            ) {
                Text("Export Data")
            }

            Button(
                onClick = { viewModel.showDeleteConfirmation() },
                enabled = !hasPendingDelete && !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text("Delete Account")
            }
        }
    }
}

@Composable
fun PendingRequestCard(request: GDPRRequest) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (request.type) {
                        GDPRRequestType.EXPORT -> "Data Export"
                        GDPRRequestType.DELETE -> "Account Deletion"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "PENDING",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Created: ${formatDate(request.createdAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

fun formatDate(iso8601String: String): String {
    return try {
        val instant = Instant.parse(iso8601String)
        val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
        formatter.format(instant.atZone(java.time.ZoneId.systemDefault()))
    } catch (e: Exception) {
        iso8601String // Fallback to original string if parsing fails
    }
}

