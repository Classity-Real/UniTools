package com.example

import com.example.model.HexMode
import com.example.model.PartitionCatalog
import com.example.model.SocType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun `verify Unisoc SoC configurations`() {
        assertEquals("0x65000800", SocType.UMS9230.fdl1Addr)
        assertEquals("0x9efffe00", SocType.UMS9230.fdl2Addr)
        assertEquals("0x65015f08", SocType.UMS9230.noverifyAddr)
        assertEquals("0x65015f48", SocType.UMS9230.altNoverifyAddr)

        assertEquals("0x5000", SocType.SC9863A.fdl1Addr)
        assertEquals("0x5500", SocType.UMS512.fdl1Addr)
    }

    @Test
    fun `verify IMEI partition list`() {
        val imeiParts = PartitionCatalog.IMEI_PARTITION_NAMES
        assertTrue(imeiParts.contains("miscdata"))
        assertTrue(imeiParts.contains("prodnv"))
        assertTrue(imeiParts.contains("l_fixnv1"))
        assertTrue(imeiParts.contains("l_fixnv2"))
        assertTrue(imeiParts.contains("l_runtimenv1"))
        assertTrue(imeiParts.contains("l_runtimenv2"))
    }

    @Test
    fun `verify command format matches menu_sh`() {
        val soc = SocType.UMS9230
        val hexMode = HexMode.STANDARD
        val noverify = if (hexMode == HexMode.STANDARD) soc.noverifyAddr else soc.altNoverifyAddr
        val fdl1 = soc.fdl1Addr
        val fdl2 = soc.fdl2Addr
        val command = "./spd_dump exec_addr $noverify fdl fdl1-dl.bin $fdl1 fdl fdl2-dl.bin $fdl2 exec"

        assertEquals("./spd_dump exec_addr 0x65015f08 fdl fdl1-dl.bin 0x65000800 fdl fdl2-dl.bin 0x9efffe00 exec", command)
    }
}
