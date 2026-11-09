package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DeviceBrand
import com.example.model.HexMode
import com.example.model.RebootMode
import com.example.model.SocType
import com.example.ui.UniToolsUiState
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.StatusGreen
import java.io.File

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HeaderConfigCard(
    uiState: UniToolsUiState,
    binariesDir: File,
    onSelectSoc: (SocType) -> Unit,
    onSelectBrand: (DeviceBrand) -> Unit,
    onSelectHexMode: (HexMode) -> Unit,
    onToggleHexMode: () -> Unit,
    onSelectRebootMode: (RebootMode) -> Unit,
    modifier: Modifier = Modifier
) {
    var showConfigSheet by remember { mutableStateOf(false) }

    Android15CardGroup(
        title = "Target Configuration",
        modifier = modifier.padding(vertical = 4.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) {
        // Root Status Row with Android 15 Squircle badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val rootGranted = uiState.isRootGranted == true
                Surface(
                    modifier = Modifier.size(28.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = if (rootGranted) StatusGreen.copy(alpha = 0.2f) else StatusAmber.copy(alpha = 0.2f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (rootGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = "Root status",
                            tint = if (rootGranted) StatusGreen else StatusAmber,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (rootGranted) "Root Access Granted" else "Root Privileges Required (su)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (rootGranted) StatusGreen else StatusAmber
                    )
                    Text(
                        text = "Native spd_dump engine loaded",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(
                onClick = { showConfigSheet = true },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Configure Chipset & Options",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Android15GroupDivider()

        // Hex Mode Selection Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Hex Mode:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(HexMode.STANDARD, HexMode.MODE_1, HexMode.MODE_2).forEach { mode ->
                    val isSelected = uiState.hexMode == mode
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                        border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onSelectHexMode(mode) }
                    ) {
                        Text(
                            text = mode.label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        Android15GroupDivider()

        // Chips Row: SoC, Brand, Hex Mode, Post Reboot
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            AssistChip(
                onClick = { showConfigSheet = true },
                label = { Text(uiState.selectedSoc.label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Memory,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )

            AssistChip(
                onClick = { showConfigSheet = true },
                label = { Text(uiState.selectedBrand.displayName, fontSize = 12.sp) },
                shape = RoundedCornerShape(12.dp),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.PhoneAndroid,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            )

            AssistChip(
                onClick = onToggleHexMode,
                label = {
                    val addr = when (uiState.hexMode) {
                        HexMode.STANDARD -> "Standard"
                        HexMode.MODE_1 -> uiState.selectedSoc.noverifyAddr
                        HexMode.MODE_2 -> uiState.selectedSoc.altNoverifyAddr
                    }
                    Text("Hex: $addr (${uiState.hexMode.label})", fontSize = 11.sp)
                },
                shape = RoundedCornerShape(12.dp),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Tag,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                }
            )

            AssistChip(
                onClick = { showConfigSheet = true },
                label = { Text("Post: ${uiState.rebootMode.label}", fontSize = 11.sp) },
                shape = RoundedCornerShape(12.dp),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.RestartAlt,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                }
            )
        }
    }

    if (showConfigSheet) {
        ConfigBottomSheet(
            uiState = uiState,
            binariesDir = binariesDir,
            onSelectSoc = { onSelectSoc(it) },
            onSelectBrand = { onSelectBrand(it) },
            onSelectHexMode = onSelectHexMode,
            onSelectRebootMode = onSelectRebootMode,
            onDismiss = { showConfigSheet = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ConfigBottomSheet(
    uiState: UniToolsUiState,
    binariesDir: File,
    onSelectSoc: (SocType) -> Unit,
    onSelectBrand: (DeviceBrand) -> Unit,
    onSelectHexMode: (HexMode) -> Unit,
    onSelectRebootMode: (RebootMode) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val availableSoCs = remember(binariesDir, uiState.fileRefreshTrigger) {
        SocType.getAvailableSoCs(binariesDir)
    }

    val availableBrands = remember(binariesDir, uiState.selectedSoc, uiState.fileRefreshTrigger) {
        DeviceBrand.getAvailableBrands(binariesDir, uiState.selectedSoc.id)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Target Configuration",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Select your Unisoc chipset, bypass mode, and target device OEM",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "CHIPSET / SOC PLATFORM",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                availableSoCs.forEach { soc ->
                    FilterChip(
                        selected = uiState.selectedSoc.id.equals(soc.id, ignoreCase = true),
                        onClick = { onSelectSoc(soc) },
                        label = { Text(soc.label) },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "HEX / EXECUTION BYPASS MODE",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(HexMode.STANDARD, HexMode.MODE_1, HexMode.MODE_2).forEach { mode ->
                    val isSelected = uiState.hexMode == mode
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectHexMode(mode) },
                        label = {
                            val detail = when (mode) {
                                HexMode.STANDARD -> "Standard (Default)"
                                HexMode.MODE_1 -> "Mode 1 (${uiState.selectedSoc.noverifyAddr})"
                                HexMode.MODE_2 -> "Mode 2 (${uiState.selectedSoc.altNoverifyAddr})"
                            }
                            Text(detail)
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "DEVICE BRAND FDL PROFILE",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                availableBrands.forEach { brand ->
                    FilterChip(
                        selected = uiState.selectedBrand.id.equals(brand.id, ignoreCase = true),
                        onClick = { onSelectBrand(brand) },
                        label = { Text(brand.displayName) },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "REBOOT MODE AFTER EXECUTION",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                RebootMode.values().forEach { mode ->
                    FilterChip(
                        selected = uiState.rebootMode == mode,
                        onClick = { onSelectRebootMode(mode) },
                        label = { Text(mode.label) },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Done")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
