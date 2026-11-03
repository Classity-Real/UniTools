package com.example.manager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UsbStatus(
    val isConnected: Boolean = false,
    val deviceName: String = "No USB Device Connected",
    val vendorId: Int = 0,
    val productId: Int = 0,
    val isUnisocBrom: Boolean = false,
    val deviceCount: Int = 0
)

class UsbDetector(private val context: Context) {
    private val usbManager = context.getSystemService(Context.USB_SERVICE) as? UsbManager

    private val _status = MutableStateFlow(UsbStatus())
    val status: StateFlow<UsbStatus> = _status.asStateFlow()

    // Unisoc / Spreadtrum vendor ID is 0x1782 (6018 in decimal)
    companion object {
        const val UNISOC_VENDOR_ID = 0x1782
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                UsbManager.ACTION_USB_DEVICE_ATTACHED,
                UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                    checkConnectedDevices()
                }
            }
        }
    }

    fun startListening() {
        checkConnectedDevices()
        val filter = IntentFilter().apply {
            addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
            addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(receiver, filter)
        }
    }

    fun stopListening() {
        try {
            context.unregisterReceiver(receiver)
        } catch (ignored: Exception) {}
    }

    fun checkConnectedDevices() {
        val manager = usbManager ?: return
        val deviceList: Map<String, UsbDevice> = manager.deviceList

        if (deviceList.isEmpty()) {
            _status.value = UsbStatus(
                isConnected = false,
                deviceName = "No USB Device Connected (OTG)",
                deviceCount = 0
            )
            return
        }

        // Search for Unisoc device or take the first connected device
        val unisocDevice = deviceList.values.find { it.vendorId == UNISOC_VENDOR_ID }
        val activeDevice = unisocDevice ?: deviceList.values.first()

        val isBrom = activeDevice.vendorId == UNISOC_VENDOR_ID
        val nameStr = if (isBrom) {
            "Spreadtrum/Unisoc Device (VID: 0x${Integer.toHexString(activeDevice.vendorId)})"
        } else {
            val prod = activeDevice.productName ?: "USB Device"
            "$prod (VID: 0x${Integer.toHexString(activeDevice.vendorId)}, PID: 0x${Integer.toHexString(activeDevice.productId)})"
        }

        _status.value = UsbStatus(
            isConnected = true,
            deviceName = nameStr,
            vendorId = activeDevice.vendorId,
            productId = activeDevice.productId,
            isUnisocBrom = isBrom,
            deviceCount = deviceList.size
        )
    }
}
