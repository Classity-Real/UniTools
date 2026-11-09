package com.example.ui.screens

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FlashTaskItem
import com.example.model.PartitionCatalog
import com.example.ui.UniToolsUiState
import com.example.ui.components.Android15CardGroup
import com.example.ui.components.Android15GroupDivider
import com.example.ui.components.CircularActionButton
import java.io.File
import java.io.FileOutputStream

@Composable
fun FlashingScreen(
    uiState: UniToolsUiState,
    onUnlockBootloader: () -> Unit,
    onAddFlashTask: (partition: String, filePath: String, sizeStr: String) -> Unit,
    onRemoveFlashTask: (partition: String) -> Unit,
    onClearFlashTasks: () -> Unit,
    onFlashAll: () -> Unit,
    onUpdateFlashTaskPartition: (oldPartition: String, newPartition: String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTargetPartition by remember { mutableStateOf("boot") }
    var isPartitionMenuOpen by remember { mutableStateOf(false) }

    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            var fileName = "partition.img"
            var fileSize = 0L

            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    if (nameIndex != -1) fileName = cursor.getString(nameIndex)
                    if (sizeIndex != -1) fileSize = cursor.getLong(sizeIndex)
                }
            }

            val inputDir = File(context.filesDir, "input")
            inputDir.mkdirs()
            val destFile = File(inputDir, fileName)
            try {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(destFile).use { output ->
                        input.copyTo(output)
                    }
                }
            } catch (ignored: Exception) {}

            val baseName = fileName.substringBeforeLast(".").lowercase()
            val detectedPartition = if (selectedTargetPartition.isNotBlank()) {
                selectedTargetPartition.trim()
            } else if (PartitionCatalog.ALL_PARTITION_NAMES.contains(baseName)) {
                baseName
            } else {
                "boot"
            }

            val sizeStr = if (fileSize > 1024 * 1024) {
                "${fileSize / (1024 * 1024)} MB"
            } else {
                "${fileSize / 1024} KB"
            }

            onAddFlashTask(detectedPartition, destFile.absolutePath, sizeStr)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Android 15 Group 1: Flashing Quick Actions
        item {
            Android15CardGroup(title = "Flashing Quick Actions") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    CircularActionButton(
                        icon = Icons.Default.LockOpen,
                        label = "Unlock Bootloader",
                        onClick = onUnlockBootloader,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
                        enabled = !uiState.isExecuting && uiState.config.flashingUnlockBootloader
                    )

                    CircularActionButton(
                        icon = Icons.Default.FolderOpen,
                        label = "Add Partition",
                        onClick = { filePickerLauncher.launch("*/*") },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
                        enabled = !uiState.isExecuting && uiState.config.flashingAddPartition
                    )

                    CircularActionButton(
                        icon = Icons.Default.FlashOn,
                        label = "Flash Queue",
                        onClick = onFlashAll,
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        iconTint = MaterialTheme.colorScheme.onTertiaryContainer,
                        enabled = !uiState.isExecuting && uiState.config.flashingFlashQueue && uiState.flashTasks.isNotEmpty()
                    )
                }
            }
        }

        // Android 15 Group 2: Bootloader Unlock Wizard
        item {
            Android15CardGroup(title = "Bootloader Unlock") {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(42.dp),
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Automated SPL Unlock",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Backs up loader, writes token & verifies miscdata",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Android15GroupDivider()

                Text(
                    text = "1. Safe backup of splloader & uboot\n2. Generate spl-unlock binary token\n3. Flash modified uboot\n4. Execute unlock payload via BROM\n5. Verify status from miscdata\n6. Safely restore original splloader",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onUnlockBootloader,
                    enabled = !uiState.isExecuting && uiState.config.flashingUnlockBootloader,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_unlock_bootloader")
                ) {
                    Icon(imageVector = Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (uiState.config.flashingUnlockBootloader) "Start Bootloader Unlock" else "Bootloader Unlock (Disabled in Config)")
                }
            }
        }

        // Android 15 Group 3: Flash Partitions & Queue
        item {
            Android15CardGroup(title = "Partition Flashing") {
                Text(
                    text = "Type target partition name or pick from suggestions, then select image file",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Editable Partition Input and Pick File button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = selectedTargetPartition,
                            onValueChange = { selectedTargetPartition = it },
                            label = { Text("Target Partition") },
                            placeholder = { Text("e.g. boot, recovery, vbmeta...") },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            trailingIcon = {
                                IconButton(onClick = { isPartitionMenuOpen = !isPartitionMenuOpen }) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Select from common partitions"
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_target_partition")
                        )

                        DropdownMenu(
                            expanded = isPartitionMenuOpen,
                            onDismissRequest = { isPartitionMenuOpen = false }
                        ) {
                            PartitionCatalog.ALL_PARTITION_NAMES.forEach { part ->
                                DropdownMenuItem(
                                    text = { Text(part) },
                                    onClick = {
                                        selectedTargetPartition = part
                                        isPartitionMenuOpen = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = { filePickerLauncher.launch("*/*") },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .height(56.dp)
                            .testTag("btn_pick_flash_file")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Pick File")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Quick partition suggestion chips
                val quickSuggestions = listOf("boot", "init_boot", "recovery", "vbmeta", "super", "misc", "dtbo", "userdata")
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(quickSuggestions) { part ->
                        val isSelected = selectedTargetPartition.equals(part, ignoreCase = true)
                        AssistChip(
                            onClick = { selectedTargetPartition = part },
                            label = { Text(part) },
                            colors = if (isSelected) {
                                AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            } else {
                                AssistChipDefaults.assistChipColors()
                            },
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }

                if (uiState.flashTasks.isNotEmpty()) {
                    Android15GroupDivider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "QUEUE (${uiState.flashTasks.size} FILES)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Clear All",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.clickable { onClearFlashTasks() }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    uiState.flashTasks.forEach { task ->
                        FlashTaskRow(
                            task = task,
                            onDelete = { onRemoveFlashTask(task.partitionName) },
                            onRename = { newPartition -> onUpdateFlashTaskPartition(task.partitionName, newPartition) }
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = onFlashAll,
                        enabled = !uiState.isExecuting,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_flash_all")
                    ) {
                        Icon(imageVector = Icons.Default.FlashOn, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Flash ${uiState.flashTasks.size} Partitions (${uiState.rebootMode.label})")
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

@Composable
fun FlashTaskRow(
    task: FlashTaskItem,
    onDelete: () -> Unit,
    onRename: (newName: String) -> Unit = {}
) {
    var showRenameDialog by remember { mutableStateOf(false) }
    var renameInput by remember(task.partitionName) { mutableStateOf(task.partitionName) }

    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Edit Target Partition") },
            text = {
                Column {
                    Text(
                        text = "Specify the partition to flash ${File(task.filePath).name} into:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = renameInput,
                        onValueChange = { renameInput = it },
                        label = { Text("Partition Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clean = renameInput.trim()
                        if (clean.isNotBlank()) {
                            onRename(clean)
                        }
                        showRenameDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Partition: ${task.partitionName}",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${File(task.filePath).name} • ${task.fileSizeFormatted}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {
                    renameInput = task.partitionName
                    showRenameDialog = true
                }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Partition",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
