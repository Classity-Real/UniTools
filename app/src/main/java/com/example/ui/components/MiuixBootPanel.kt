package com.example.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.darkColorScheme
import top.yukonga.miuix.kmp.theme.lightColorScheme

/**
 * Separate HyperOS / MIUIX-themed island placed under the Bootloader Unlock panel.
 * Uses MiuixTheme locally so the rest of the app can stay on Material3.
 */
@Composable
fun MiuixBootPanel(
    enabled: Boolean,
    isExecuting: Boolean,
    onUnlockBootloader: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dark = isSystemInDarkTheme()
    val colors = remember(dark) { if (dark) darkColorScheme() else lightColorScheme() }

    var backupLoader by remember { mutableStateOf(true) }
    var verifyMiscdata by remember { mutableStateOf(true) }
    var restoreAfter by remember { mutableStateOf(true) }

    MiuixTheme(colors = colors) {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SmallTitle(
                text = "MIUIX · Boot / Unlock",
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                insideMargin = PaddingValues(0.dp)
            ) {
                BasicComponent(
                    title = "Automated SPL Unlock",
                    summary = "HyperOS-style unlock flow for Unisoc SPL / uboot",
                    onClick = null
                )
                SwitchPreference(
                    title = "Backup splloader & uboot",
                    summary = "Safe copy before writing unlock token",
                    checked = backupLoader,
                    onCheckedChange = { backupLoader = it },
                    enabled = enabled && !isExecuting
                )
                SwitchPreference(
                    title = "Verify miscdata status",
                    summary = "Confirm unlock flag after BROM payload",
                    checked = verifyMiscdata,
                    onCheckedChange = { verifyMiscdata = it },
                    enabled = enabled && !isExecuting
                )
                SwitchPreference(
                    title = "Restore original splloader",
                    summary = "Write back stock loader when unlock finishes",
                    checked = restoreAfter,
                    onCheckedChange = { restoreAfter = it },
                    enabled = enabled && !isExecuting
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                insideMargin = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Steps stay the same as Material panel: backup → token → flash uboot → BROM → verify → restore.",
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onUnlockBootloader,
                        enabled = enabled && !isExecuting,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_miuix_unlock_bootloader"),
                        colors = ButtonDefaults.buttonColorsPrimary()
                    ) {
                        Text(
                            text = if (enabled) "Start Unlock" else "Unlock Disabled",
                            color = MiuixTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}
