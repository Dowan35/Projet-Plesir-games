package com.example.menu

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import com.example.menu.utils.hasBluetoothPermissions
import com.example.menu.utils.requestBluetoothPermissionsIfNeeded

class BluetoothPairingScreen : ComponentActivity() {
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val _availableDevices = mutableStateListOf<BluetoothDevice>()
    private val _pairedDevices = mutableStateListOf<BluetoothDevice>()

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == BluetoothDevice.ACTION_FOUND) {
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                device?.let {
                    if (!_availableDevices.contains(it)) _availableDevices.add(it)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!hasBluetoothPermissions()) { // fonctions de BluetoothUtils
            if (requestBluetoothPermissionsIfNeeded()) return
        }
        startDiscovery() // demarre la recherche des appareils

        setContent {
            MaterialTheme {
                BluetoothDeviceScreen(
                    pairedDevices = _pairedDevices,
                    availableDevices = _availableDevices,
                    onRefreshClick = { startDiscovery() },
                    onDeviceClick = { device -> pairDevice(device) }
                )
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startDiscovery() {
        _availableDevices.clear()
        _pairedDevices.clear()
        bluetoothAdapter?.bondedDevices?.forEach { _pairedDevices.add(it) }

        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(receiver, filter)
        bluetoothAdapter?.startDiscovery()
    }
    @SuppressLint("MissingPermission")
    private fun pairDevice(device: BluetoothDevice) {
        if (device.bondState == BluetoothDevice.BOND_NONE) {
            device.createBond()
            Toast.makeText(this, "Appairage avec ${device.name}", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "${device.name} déjà appairé", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try { unregisterReceiver(receiver) } catch (_: Exception) {}
    }
}
