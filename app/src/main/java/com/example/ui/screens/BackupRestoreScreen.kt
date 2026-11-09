package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BackupPreset
import com.example.model.PartitionCatalog
import com.example.ui.UniToolsUiState
import com.example.ui.components.Android15CardGroup
import com.example.ui.components.Android15GroupDivider
import com.example.ui.components.CircularActionButton
import java.io.File

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BackupRestoreScreen(
    uiState: UniToolsUiState,
    onSelectPreset: (BackupPreset) -> Unit,
    onTogglePartition: (String) -> Unit,
    onStartBackup: () -> Unit,
    onStartRestore: (File) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    val backupDir = remember { File(context.filesDir, "backup") }
    val existingBackups = remember(uiState.isExecuting) {
        backupDir.listFiles()?.filter { it.isFile && (it.name.endsWith(".img") || it.name.endsWith(".bin")) } ?: emptyList()
    }

    val filteredPartitions = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            PartitionCatalog.ALL_PARTITION_NAMES
        } else {
            PartitionCatalog.ALL_PARTITION_NAMES.filter { it.contains(searchQuery.trim(), ignoreCase = true) }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 0.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Android15CardGroup(title = "Backup Presets") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    CircularActionButton(
                        icon = Icons.Default.SimCard,
                        label = "IMEI & NVRAM",
                        onClick = { onSelectPreset(BackupPreset.IMEI_NVRAM) },
                        containerColor = if (uiState.activeBackupPreset == BackupPreset.IMEI_NVRAM)
                            MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHighest,
                        iconTint = if (uiState.activeBackupPreset == BackupPreset.IMEI_NVRAM)
                            MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    CircularActionButton(
                        icon = Icons.Default.FilterList,
                        label = "All Lite Backup",
                        onClick = { onSelectPreset(BackupPreset.ALL_LITE) },
                        containerColor = if (uiState.activeBackupPreset == BackupPreset.ALL_LITE)
                            MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHighest,
                        iconTint = if (uiState.activeBackupPreset == BackupPreset.ALL_LITE)
                            MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    CircularActionButton(
                        icon = Icons.Default.Backup,
                        label = "Full Partitions",
                        onClick = { onSelectPreset(BackupPreset.ALL_FULL) },
                        containerColor = if (uiState.activeBackupPreset == BackupPreset.ALL_FULL)
                            MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHighest,
                        iconTint = if (uiState.activeBackupPreset == BackupPreset.ALL_FULL)
                            MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Android15CardGroup(title = "Select Partitions") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Selected (${uiState.selectedBackupPartitions.size})",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = uiState.activeBackupPreset.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search partition name...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    filteredPartitions.forEach { partitionName ->
                        val isSelected = uiState.selectedBackupPartitions.contains(partitionName)
                        FilterChip(
                            selected = isSelected,
                            onClick = { onTogglePartition(partitionName) },
                            label = { Text(partitionName, fontSize = 11.sp) },
                            shape = RoundedCornerShape(10.dp),
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            } else null
                        )
                    }
                }

                Android15GroupDivider()

                Button(
                    onClick = onStartBackup,
                    enabled = !uiState.isExecuting && uiState.selectedBackupPartitions.isNotEmpty(),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_start_backup")
                ) {
                    Icon(Icons.Default.Backup, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Backup ${uiState.selectedBackupPartitions.size} Partitions")
                }
            }
        }

        item {
            Android15CardGroup(title = "Restore Backup") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(36.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Restore,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Stored Backups (${existingBackups.size})",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Internal folder: ${backupDir.name}/",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Android15GroupDivider()

                if (existingBackups.isEmpty()) {
                    Text(
                        text = "No backup images created yet. Run a backup above to store files.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                } else {
                    existingBackups.take(6).forEach { file ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Folder,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = file.name,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Text(
                                    text = "${file.length() / 1024} KB",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { onStartRestore(backupDir) },
                        enabled = !uiState.isExecuting,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Restore All Backups to Device")
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(120.dp))
        }
    }
}
