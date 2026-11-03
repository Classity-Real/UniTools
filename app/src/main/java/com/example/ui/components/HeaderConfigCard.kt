package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.platform.testTag
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

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HeaderConfigCard(
    uiState: UniToolsUiState,
    onSelectSoc: (SocType) -> Unit,
    onSelectBrand: (DeviceBrand) -> Unit,
    onToggleHexMode: () -> Unit,
    onSelectRebootMode: (RebootMode) -> Unit,
    modifier: Modifier = Modifier
) {
    var showConfigSheet by remember { mutableStateOf(false) }

    Android15CardGroup(
        title = "Target Configuration",
        modifier = modifier.padding(horizontal = 16.dp, vertical = 4.dp),
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

        // Chips Row: SoC, Brand, Hex Mode, Post Reboot
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // SoC Chip
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

            // Brand Chip
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

            // Hex Mode Chip
            AssistChip(
                onClick = onToggleHexMode,
                label = {
                    val addr = if (uiState.hexMode == HexMode.STANDARD) uiState.selectedSoc.noverifyAddr else uiState.selectedSoc.altNoverifyAddr
                    Text("Hex: $addr", fontSize = 11.sp)
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

            // Reboot Mode Chip
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
            onSelectSoc = { onSelectSoc(it) },
            onSelectBrand = { onSelectBrand(it) },
            onSelectRebootMode = onSelectRebootMode,
            onDismiss = { showConfigSheet = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ConfigBottomSheet(
    uiState: UniToolsUiState,
    onSelectSoc: (SocType) -> Unit,
    onSelectBrand: (DeviceBrand) -> Unit,
    onSelectRebootMode: (RebootMode) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Target Configuration",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Select your Unisoc chipset and target device OEM",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // SoC Section
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
                SocType.values().forEach { soc ->
                    FilterChip(
                        selected = uiState.selectedSoc == soc,
                        onClick = { onSelectSoc(soc) },
                        label = { Text(soc.label) },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Brand Section
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
                uiState.selectedSoc.supportedBrands.forEach { brand ->
                    FilterChip(
                        selected = uiState.selectedBrand == brand,
                        onClick = { onSelectBrand(brand) },
                        label = { Text(brand.displayName) },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Post Execution Reboot Mode
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
