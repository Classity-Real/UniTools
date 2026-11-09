package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FlashTaskItem
import com.example.model.PartitionCatalog
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.darkColorScheme
import java.io.File

/**
 * Partition flash queue panel styled with Miuix (HyperOS) theme.
 * Renders as its own themed surface below the Material bootloader panel.
 */
@Composable
fun MiuixPartitionFlashPanel(
    flashTasks: List<FlashTaskItem>,
    isExecuting: Boolean,
    flashEnabled: Boolean,
    rebootLabel: String,
    selectedTargetPartition: String,
    onSelectedTargetPartitionChange: (String) -> Unit,
    onPickFile: () -> Unit,
    onClearFlashTasks: () -> Unit,
    onFlashAll: () -> Unit,
    onRemoveFlashTask: (partition: String) -> Unit,
    onUpdateFlashTaskPartition: (oldPartition: String, newPartition: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isPartitionMenuOpen by remember { mutableStateOf(false) }

    MiuixTheme(colors = darkColorScheme()) {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Partition Flashing",
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = MiuixTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Type target partition or pick a suggestion, then select the image file.",
                    fontSize = 12.sp,
                    color = MiuixTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = selectedTargetPartition,
                            onValueChange = onSelectedTargetPartitionChange,
                            label = { androidx.compose.material3.Text("Target Partition") },
                            placeholder = { androidx.compose.material3.Text("boot, recovery, vbmeta…") },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            trailingIcon = {
                                IconButton(onClick = { isPartitionMenuOpen = !isPartitionMenuOpen }) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Common partitions"
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
                                    text = { androidx.compose.material3.Text(part) },
                                    onClick = {
                                        onSelectedTargetPartitionChange(part)
                                        isPartitionMenuOpen = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = onPickFile,
                        enabled = !isExecuting && flashEnabled,
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

                val quickSuggestions = listOf(
                    "boot", "init_boot", "recovery", "vbmeta", "super", "misc", "dtbo", "userdata"
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(quickSuggestions) { part ->
                        Button(
                            onClick = { onSelectedTargetPartitionChange(part) },
                            colors = ButtonDefaults.buttonColors(),
                            minHeight = 32.dp,
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(part, fontSize = 12.sp)
                        }
                    }
                }

                if (flashTasks.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "QUEUE (${flashTasks.size})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MiuixTheme.colorScheme.primary
                        )
                        Text(
                            text = "Clear All",
                            fontSize = 12.sp,
                            color = MiuixTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                            modifier = Modifier.clickable { onClearFlashTasks() }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    flashTasks.forEach { task ->
                        MiuixFlashTaskRow(
                            task = task,
                            onDelete = { onRemoveFlashTask(task.partitionName) },
                            onRename = { onUpdateFlashTaskPartition(task.partitionName, it) }
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = onFlashAll,
                        enabled = !isExecuting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_flash_all")
                    ) {
                        Icon(Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Flash ${flashTasks.size} Partitions ($rebootLabel)")
                    }
                }
            }
        }
    }
}

@Composable
private fun MiuixFlashTaskRow(
    task: FlashTaskItem,
    onDelete: () -> Unit,
    onRename: (String) -> Unit
) {
    var showRename by remember { mutableStateOf(false) }
    var renameInput by remember(task.partitionName) { mutableStateOf(task.partitionName) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = task.partitionName,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${File(task.filePath).name} · ${task.fileSizeFormatted}",
                fontSize = 11.sp,
                color = MiuixTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Row {
            IconButton(onClick = { showRename = true }) {
                Icon(Icons.Default.Edit, contentDescription = "Rename", modifier = Modifier.size(18.dp))
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Remove", modifier = Modifier.size(18.dp))
            }
        }
    }

    if (showRename) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showRename = false },
            title = { androidx.compose.material3.Text("Edit Target Partition") },
            text = {
                OutlinedTextField(
                    value = renameInput,
                    onValueChange = { renameInput = it },
                    label = { androidx.compose.material3.Text("Partition Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        if (renameInput.isNotBlank()) {
                            onRename(renameInput.trim())
                            showRename = false
                        }
                    }
                ) { androidx.compose.material3.Text("Save") }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showRename = false }) {
                    androidx.compose.material3.Text("Cancel")
                }
            }
        )
    }
}
