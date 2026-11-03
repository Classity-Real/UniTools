package com.example.model

data class PartitionItem(
    val name: String,
    val isCritical: Boolean = false,
    val description: String = ""
)

enum class BackupPreset(val label: String, val description: String) {
    IMEI_NVRAM("IMEI / NVRAM", "miscdata, prodnv, fixnv, runtimenv"),
    ALL_LITE("All Lite", "All partitions except userdata, cache, blackbox"),
    ALL_FULL("Full Partitions", "All available system and vendor partitions"),
    CUSTOM("Custom Select", "Select custom partitions from list")
}

object PartitionCatalog {
    val ALL_PARTITION_NAMES = listOf(
        "boot",
        "vendor_boot",
        "init_boot",
        "dtb",
        "dtbo",
        "super",
        "vbmeta",
        "vbmeta_system",
        "vbmeta_vendor",
        "vbmeta_system_ext",
        "vbmeta_product",
        "vbmeta_odm",
        "metadata",
        "cache",
        "userdata",
        "misc",
        "miscdata",
        "prodnv",
        "persist",
        "uboot",
        "uboot_log",
        "sml",
        "trustos",
        "teecfg",
        "hypervsior",
        "logo",
        "fbootlogo",
        "l_fixnv1",
        "l_fixnv2",
        "l_runtimenv1",
        "l_runtimenv2",
        "l_modem",
        "l_deltanv",
        "l_gdsp",
        "l_ldsp",
        "pm_sys",
        "blackbox",
        "sysdumpdb",
        "avbmeta_rs",
        "common_rs1",
        "tranfs",
        "calinv"
    )

    val IMEI_PARTITION_NAMES = listOf(
        "miscdata",
        "prodnv",
        "l_fixnv1",
        "l_fixnv2",
        "l_runtimenv1",
        "l_runtimenv2"
    )

    val EXCLUDED_FROM_LITE = setOf("userdata", "cache", "blackbox")

    val ALL_LITE_PARTITION_NAMES = ALL_PARTITION_NAMES.filter { it !in EXCLUDED_FROM_LITE }
}

data class FlashTaskItem(
    val partitionName: String,
    val filePath: String,
    val fileSizeFormatted: String
)
