package com.example.model

data class UniToolsConfig(
    // Section: Flashing
    val flashingUnlockBootloader: Boolean = false, // Config: [Unlock Bootloader] - X
    val flashingAddPartition: Boolean = false,     // Config: [Add Partition] - X
    val flashingFlashQueue: Boolean = true,        // Config: [Flash Queue] - O
    val flashingPartitionFlashing: Boolean = true, // Config: [Partition Flashing] - O

    // Section: Backup
    val backupPresets: Boolean = true,            // Config: [Backup Presets] - O
    val backupSelectPartitions: Boolean = true,   // Config: [Select Partitions] - O
    val backupRestoreBackup: Boolean = true,      // Config: [Restore Backup] - O

    // Section: Tools
    val toolsWipeUserdata: Boolean = true,        // Config: Keep Wipe userdata
    val toolsDisableVerity: Boolean = true,       // Config: Disable Verity
    val toolsResetFrp: Boolean = true,            // Config: Reset FRP
    val toolsSlotSwitcher: Boolean = true,        // Config: [The Rest] - O
    val toolsHardwareReboots: Boolean = true,     // Config: [The Rest] - O
    val toolsRepartitionByXml: Boolean = true,    // Config: Repartition by XML

    // Section: Files
    val filesEnabled: Boolean = true              // Config: [The Rest] - O
) {
    companion object {
        fun parseFromText(content: String): UniToolsConfig {
            var unlockBootloader = false
            var addPartition = false
            var flashQueue = true
            var partitionFlashing = true
            var backupPresets = true
            var backupPartitions = true
            var restoreBackup = true
            var wipeUserdata = true
            var disableVerity = true
            var resetFrp = true
            var slotSwitcher = true
            var hardwareReboots = true
            var files = true

            content.lines().forEach { line ->
                val trimmed = line.trim()
                when {
                    trimmed.contains("[Unlock Bootloader]") -> unlockBootloader = !trimmed.contains("- X")
                    trimmed.contains("[Add Partition]") -> addPartition = !trimmed.contains("- X")
                    trimmed.contains("[Flash Queue]") -> flashQueue = !trimmed.contains("- X")
                    trimmed.contains("[Partition Flashing]") -> partitionFlashing = !trimmed.contains("- X")
                    trimmed.contains("[Backup Presets]") -> backupPresets = !trimmed.contains("- X")
                    trimmed.contains("[Select Partitions]") -> backupPartitions = !trimmed.contains("- X")
                    trimmed.contains("[Restore Backup]") -> restoreBackup = !trimmed.contains("- X")
                    trimmed.contains("[Device Maintenance]") -> {
                        wipeUserdata = trimmed.contains("Wipe userdata", ignoreCase = true)
                        disableVerity = trimmed.contains("Disable Verity", ignoreCase = true)
                        resetFrp = trimmed.contains("Reset FRP", ignoreCase = true)
                    }
                }
            }

            return UniToolsConfig(
                flashingUnlockBootloader = unlockBootloader,
                flashingAddPartition = addPartition,
                flashingFlashQueue = flashQueue,
                flashingPartitionFlashing = partitionFlashing,
                backupPresets = backupPresets,
                backupSelectPartitions = backupPartitions,
                backupRestoreBackup = restoreBackup,
                toolsWipeUserdata = wipeUserdata,
                toolsDisableVerity = disableVerity,
                toolsResetFrp = resetFrp,
                toolsSlotSwitcher = slotSwitcher,
                toolsHardwareReboots = hardwareReboots,
                filesEnabled = files
            )
        }
    }
}
