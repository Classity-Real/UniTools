package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.ui.FilesNavMode
import com.example.ui.UniToolsUiState
import com.example.ui.components.Android15CardGroup
import com.example.ui.components.Android15GroupDivider
import com.example.ui.components.Android15GroupRow
import com.example.ui.components.ActionTile
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FilesScreen(
    uiState: UniToolsUiState,
    binariesDir: File,
    onOpenDirectory: (File) -> Unit,
    onNavigateBack: () -> Unit,
    onResetNav: () -> Unit,
    onImportFiles: (List<Uri>, File) -> Unit,
    onCreateFolder: (File, String) -> Unit,
    onRenameFile: (File, String) -> Unit,
    onDeleteFile: (File) -> Unit,
    onToggleExecutable: (File) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val currentTargetDir = remember(uiState.currentFileDir, binariesDir, uiState.fileRefreshTrigger) {
        uiState.currentFileDir ?: binariesDir
    }

    val currentFileList = remember(currentTargetDir, uiState.fileRefreshTrigger) {
        currentTargetDir.listFiles()?.sortedWith(compareBy({ !it.isDirectory }, { it.name })) ?: emptyList()
    }

    val importFilesLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            onImportFiles(uris, currentTargetDir)
        }
    }

    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }

    var fileToRename by remember { mutableStateOf<File?>(null) }
    var fileToDelete by remember { mutableStateOf<File?>(null) }
    var fileDetails by remember { mutableStateOf<File?>(null) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 0.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Group 1: File Actions (Import, Add Files, New Folder, Reset)
        item {
            Android15CardGroup(title = "Files") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ActionTile(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.FileDownload,
                            label = "Import",
                            onClick = { importFilesLauncher.launch("*/*") },
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            iconTint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        ActionTile(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.CreateNewFolder,
                            label = "New folder",
                            onClick = {
                                newFolderName = ""
                                showCreateFolderDialog = true
                            },
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            iconTint = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
        }

        // Breadcrumb Navigation Row (if inside a subdirectory)
        if (currentTargetDir != binariesDir) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                    }
                    BreadcrumbsRow(
                        modifier = Modifier.weight(1f),
                        currentDir = currentTargetDir,
                        binariesDir = binariesDir,
                        onResetNav = onResetNav,
                        onOpenDirectory = onOpenDirectory
                    )
                    TextButton(onClick = onResetNav) {
                        Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset")
                    }
                }
            }
        }

        // Group 2: Content Browser
        item {
            if (currentFileList.isEmpty()) {
                Android15CardGroup {
                    Text(
                        text = "Folder '${currentTargetDir.name}' is empty. Use 'New Folder', 'Import' or 'Add Files' to add content.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            } else {
                Android15CardGroup {
                    currentFileList.forEachIndexed { index, file ->
                        FileItemRow(
                            file = file,
                            onClick = { if (file.isDirectory) onOpenDirectory(file) else fileDetails = file },
                            onRename = { fileToRename = file },
                            onDelete = { fileToDelete = file },
                            onDetails = { fileDetails = file },
                            onToggleExec = { onToggleExecutable(file) },
                            onShare = { fileSharing(context, file) }
                        )
                        if (index < currentFileList.size - 1) {
                            Android15GroupDivider()
                        }
                    }
                }
            }
        }
    }

    // Dialog: Create New Folder
    if (showCreateFolderDialog) {
        AlertDialog(
            onDismissRequest = { showCreateFolderDialog = false },
            icon = {
                Icon(Icons.Default.CreateNewFolder, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            },
            title = { Text("Add Folder") },
            text = {
                Column {
                    Text(
                        text = "Create new folder in '${currentTargetDir.name}':",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = newFolderName,
                        onValueChange = { newFolderName = it },
                        label = { Text("Folder Name") },
                        placeholder = { Text("e.g. mdl, loaders, backups") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_new_folder_name")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newFolderName.isNotBlank()) {
                            onCreateFolder(currentTargetDir, newFolderName.trim())
                            showCreateFolderDialog = false
                        }
                    },
                    enabled = newFolderName.isNotBlank(),
                    modifier = Modifier.testTag("btn_confirm_create_folder")
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateFolderDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Dialog: Rename File / Folder
    fileToRename?.let { file ->
        var renameInput by remember { mutableStateOf(file.name) }
        AlertDialog(
            onDismissRequest = { fileToRename = null },
            icon = { Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text(if (file.isDirectory) "Rename Folder" else "Rename File") },
            text = {
                OutlinedTextField(
                    value = renameInput,
                    onValueChange = { renameInput = it },
                    label = { Text("New Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_rename_file")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (renameInput.isNotBlank()) {
                            onRenameFile(file, renameInput.trim())
                            fileToRename = null
                        }
                    },
                    enabled = renameInput.isNotBlank()
                ) {
                    Text("Rename")
                }
            },
            dismissButton = {
                TextButton(onClick = { fileToRename = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Dialog: Delete File / Folder
    fileToDelete?.let { file ->
        AlertDialog(
            onDismissRequest = { fileToDelete = null },
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text(if (file.isDirectory) "Delete Folder?" else "Delete File?") },
            text = {
                Text("Are you sure you want to delete '${file.name}'? This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteFile(file)
                        fileToDelete = null
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { fileToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Dialog: File / Folder Details
    fileDetails?.let { file ->
        AlertDialog(
            onDismissRequest = { fileDetails = null },
            icon = { Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Item Details") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    DetailLine("Name", file.name)
                    DetailLine("Type", if (file.isDirectory) "Directory / Folder" else "File")
                    DetailLine("Size", if (file.isDirectory) "${file.listFiles()?.size ?: 0} items" else "${file.length()} bytes (${file.length() / 1024} KB)")
                    DetailLine("Path", file.absolutePath)
                    DetailLine("Executable", if (file.canExecute()) "Yes (chmod 755)" else "No (chmod 644)")
                    DetailLine("Modified", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(file.lastModified())))
                }
            },
            confirmButton = {
                TextButton(onClick = { fileDetails = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun BreadcrumbsRow(
    currentDir: File,
    binariesDir: File,
    onResetNav: () -> Unit,
    onOpenDirectory: (File) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(vertical = 4.dp)
    ) {
        Text(
            text = "Files",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable { onResetNav() }
        )

        val relativePath = currentDir.absolutePath.removePrefix(binariesDir.absolutePath).trim('/')
        if (relativePath.isNotEmpty()) {
            val segments = relativePath.split('/')
            var cumulative = binariesDir
            segments.forEach { segment ->
                cumulative = File(cumulative, segment)
                val target = cumulative
                Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(16.dp))
                Text(
                    text = segment,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.clickable { onOpenDirectory(target) }
                )
            }
        }
    }
}

@Composable
fun DetailLine(label: String, value: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        Text(text = value, style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
    }
}

@Composable
fun FileItemRow(
    file: File,
    onClick: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onDetails: () -> Unit,
    onToggleExec: () -> Unit,
    onShare: () -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(14.dp),
                color = if (file.isDirectory) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (file.isDirectory) Icons.Default.Folder else Icons.Default.InsertDriveFile,
                        contentDescription = null,
                        tint = if (file.isDirectory) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val sizeLabel = if (file.isDirectory) "${file.listFiles()?.size ?: 0} items" else "${file.length() / 1024} KB"
                val permLabel = if (file.canExecute()) "chmod 755" else "chmod 644"
                Text(
                    text = "$sizeLabel • $permLabel",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Box {
            IconButton(onClick = { menuOpen = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Menu")
            }

            DropdownMenu(
                expanded = menuOpen,
                onDismissRequest = { menuOpen = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Details") },
                    leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
                    onClick = { menuOpen = false; onDetails() }
                )
                DropdownMenuItem(
                    text = { Text("Rename") },
                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                    onClick = { menuOpen = false; onRename() }
                )
                DropdownMenuItem(
                    text = { Text(if (file.canExecute()) "Revoke Executable (-x)" else "Make Executable (+x)") },
                    leadingIcon = { Icon(Icons.Default.Security, contentDescription = null) },
                    onClick = { menuOpen = false; onToggleExec() }
                )
                DropdownMenuItem(
                    text = { Text("Export") },
                    leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                    onClick = { menuOpen = false; onShare() }
                )
                DropdownMenuItem(
                    text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                    onClick = { menuOpen = false; onDelete() }
                )
            }
        }
    }
}

private fun fileSharing(context: Context, file: File) {
    try {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "*/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share ${file.name}"))
    } catch (_: Exception) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "File: ${file.name} (${file.length()} bytes)")
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share ${file.name}"))
    }
}
