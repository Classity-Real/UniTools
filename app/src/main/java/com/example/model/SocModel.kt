package com.example.model

import java.io.File

data class SocType(
    val id: String,
    val label: String,
    val fdl1Addr: String,
    val fdl2Addr: String,
    val noverifyAddr: String,
    val altNoverifyAddr: String
) {
    companion object {
        val UMS9230 = SocType("ums9230", "UMS9230 (T606/T616)", "0x65000800", "0x9efffe00", "0x65015f08", "0x65015f48")
        val SC9863A = SocType("sc9863a", "SC9863A", "0x5000", "0x9efffe00", "0x4ee8", "0x4f48")
        val UMS512 = SocType("ums512", "UMS512 (T610/T618)", "0x5500", "0x9efffe00", "0x3ee8", "0x3f48")

        val DEFAULT_LIST = listOf(UMS9230, SC9863A, UMS512)

        fun getAvailableSoCs(binariesDir: File): List<SocType> {
            val customDirs = binariesDir.listFiles()?.filter { 
                it.isDirectory && !it.name.startsWith(".") && 
                it.name != "glibc" && it.name != "backup" && it.name != "input" && it.name != "backup_spl" 
            }?.map { dir ->
                DEFAULT_LIST.find { it.id.equals(dir.name, ignoreCase = true) } ?: SocType(
                    id = dir.name,
                    label = dir.name.replaceFirstChar { it.uppercase() },
                    fdl1Addr = "0x65000800",
                    fdl2Addr = "0x9efffe00",
                    noverifyAddr = "0x65015f08",
                    altNoverifyAddr = "0x65015f48"
                )
            } ?: emptyList()

            val map = mutableMapOf<String, SocType>()
            DEFAULT_LIST.forEach { map[it.id.lowercase()] = it }
            customDirs.forEach { map[it.id.lowercase()] = it }
            return map.values.toList()
        }
    }
}

data class DeviceBrand(val id: String, val displayName: String) {
    companion object {
        val INFINIX = DeviceBrand("infinix", "Infinix")
        val ITEL = DeviceBrand("itel", "Itel")
        val REALME = DeviceBrand("realme", "Realme")
        val TECNO = DeviceBrand("tecno", "Tecno")

        val DEFAULT_LIST = listOf(INFINIX, ITEL, REALME, TECNO)

        fun getAvailableBrands(binariesDir: File, socId: String): List<DeviceBrand> {
            val socFolder = File(binariesDir, socId)
            val customDirs = socFolder.listFiles()?.filter { it.isDirectory && !it.name.startsWith(".") }?.map { dir ->
                DEFAULT_LIST.find { it.id.equals(dir.name, ignoreCase = true) } ?: DeviceBrand(
                    id = dir.name,
                    displayName = dir.name.replaceFirstChar { it.uppercase() }
                )
            } ?: emptyList()

            val map = mutableMapOf<String, DeviceBrand>()
            DEFAULT_LIST.forEach { map[it.id.lowercase()] = it }
            customDirs.forEach { map[it.id.lowercase()] = it }
            return map.values.toList()
        }
    }
}

enum class RebootMode(val flag: String, val label: String) {
    SYSTEM("reset", "Normal (System)"),
    RECOVERY("reboot-recovery", "Recovery"),
    FASTBOOTD("reboot-fastboot", "FastbootD"),
    POWEROFF("poweroff", "Power Off")
}

enum class HexMode(val index: Int, val label: String) {
    STANDARD(1, "Standard Hex (0x...)"),
    ALTERNATIVE(2, "Alternative Hex (Alt 0x...)")
}
