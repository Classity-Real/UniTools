package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
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
        modifier = modifier.padding(top = 4.dp, bottom = 2.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
    ) {
        val rootGranted = uiState.isRootGranted == true
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (rootGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = if (rootGranted) "Root granted" else "Root required",
                tint = if (rootGranted) StatusGreen else StatusAmber,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${uiState.selectedSoc.label}  ·  ${uiState.selectedBrand.displayName}  ·  ${uiState.hexMode.label}  ·  ${uiState.rebootMode.label}",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            IconButton(
                onClick = { showConfigSheet = true },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Target configuration",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
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
