package com.example.model

enum class SocType(
    val id: String,
    val label: String,
    val fdl1Addr: String,
    val fdl2Addr: String,
    val noverifyAddr: String,
    val altNoverifyAddr: String,
    val supportedBrands: List<DeviceBrand>
) {
    UMS9230(
        id = "ums9230",
        label = "UMS9230 (T606/T616)",
        fdl1Addr = "0x65000800",
        fdl2Addr = "0x9efffe00",
        noverifyAddr = "0x65015f08",
        altNoverifyAddr = "0x65015f48",
        supportedBrands = listOf(DeviceBrand.INFINIX, DeviceBrand.ITEL, DeviceBrand.REALME, DeviceBrand.TECNO)
    ),
    SC9863A(
        id = "sc9863a",
        label = "SC9863A",
        fdl1Addr = "0x5000",
        fdl2Addr = "0x9efffe00",
        noverifyAddr = "0x4ee8",
        altNoverifyAddr = "0x4f48",
        supportedBrands = listOf(DeviceBrand.ITEL, DeviceBrand.REALME)
    ),
    UMS512(
        id = "ums512",
        label = "UMS512 (T610/T618)",
        fdl1Addr = "0x5500",
        fdl2Addr = "0x9efffe00",
        noverifyAddr = "0x3ee8",
        altNoverifyAddr = "0x3f48",
        supportedBrands = listOf(DeviceBrand.INFINIX, DeviceBrand.REALME)
    )
}

enum class DeviceBrand(val id: String, val displayName: String) {
    INFINIX("infinix", "Infinix"),
    ITEL("itel", "Itel"),
    REALME("realme", "Realme"),
    TECNO("tecno", "Tecno")
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
