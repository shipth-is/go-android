package com.shipthis.go.ui.screens.builds

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shipthis.go.data.model.GoBuild
import com.shipthis.go.util.DateUtil

@Composable
fun GoBuildCard(
    goBuild: GoBuild,
    onLaunchClick: (GoBuild) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Top row: Project name (left) + Status badge (right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = goBuild.project.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (goBuild.isFound == true) "Available" else "Not Found",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (goBuild.isFound == true) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                    fontWeight = FontWeight.Bold
                )
            }

            // Version line
            Text(
                text = "v${goBuild.jobDetails.semanticVersion} #${goBuild.jobDetails.buildNumber}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Game engine line
            Text(
                text = "${goBuild.jobDetails.gameEngine} ${goBuild.jobDetails.gameEngineVersion}",
                style = MaterialTheme.typography.bodyMedium
            )

            // Created date line
            Text(
                text = "Created: ${DateUtil.formatDate(goBuild.createdAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )

            // Git info (conditional)
            val gitBranch = goBuild.jobDetails.gitBranch
            val gitCommitHash = goBuild.jobDetails.gitCommitHash
            if (gitBranch != null && gitCommitHash != null) {
                val shortHash = gitCommitHash.take(7)
                Text(
                    text = "Branch: $gitBranch • $shortHash",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Launch button at bottom
            Button(
                onClick = { onLaunchClick(goBuild) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Launch Build")
            }
        }
    }
}

