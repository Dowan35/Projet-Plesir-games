package com.example.tp4_bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.tp4_bluetooth.ui.theme.TP4_bluetoothTheme

class MainActivity : ComponentActivity() {
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val _availableDevices = mutableStateListOf<BluetoothDevice>()
    private val availableDevices: List<BluetoothDevice> get() = _availableDevices

    // Lancer pour demander l'activation de Bluetooth
    private val enableBluetoothLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                Log.d("DEBUG", "Bluetooth activé par l'utilisateur.")
                startDiscovery() // Démarrer la découverte après activation
            } else {
                Log.d("DEBUG", "L'utilisateur a refusé d'activer Bluetooth.")
                Toast.makeText(this, "Bluetooth doit être activé pour continuer", Toast.LENGTH_LONG).show()
            }
        }

    // Lancer pour demander les permissions Bluetooth
    private val requestBluetoothPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.all { it.value }) {
                Log.d("DEBUG", "Toutes les permissions Bluetooth ont été accordées.")
                startDiscovery()
            } else {
                Log.e("DEBUG", "Certaines permissions Bluetooth ont été refusées.")
                Toast.makeText(this, "Permissions Bluetooth requises pour continuer", Toast.LENGTH_LONG).show()
            }
        }

    private fun checkAndRequestBluetoothPermissions() {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth non pris en charge sur cet appareil", Toast.LENGTH_LONG).show()
            return
        }

        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            enableBluetoothLauncher.launch(enableBtIntent)
            return
        }

        val requiredPermissions = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION)

        if (Build.VERSION.SDK_INT >= 30) {
            requiredPermissions.add(Manifest.permission.BLUETOOTH_CONNECT)
            requiredPermissions.add(Manifest.permission.BLUETOOTH_SCAN)
        }

        val missingPermissions = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            requestBluetoothPermissions.launch(missingPermissions.toTypedArray())
        } else {
            startDiscovery()
        }
    }

    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == BluetoothDevice.ACTION_FOUND) {
                val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                device?.let {
                    if (ContextCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        _availableDevices.add(it)
                        Log.d("DEBUG", "Appareil découvert : ${it.name ?: "Inconnu"} - ${it.address}")
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkAndRequestBluetoothPermissions()

        setContent {
            TP4_bluetoothTheme {
                BluetoothDeviceScreen(
                    pairedDevices = getPairedDevices(),
                    availableDevices = availableDevices,
                    context = this
                )
            }
        }
    }

    private fun getPairedDevices(): List<BluetoothDevice> {
        return if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            bluetoothAdapter?.bondedDevices?.toList() ?: emptyList()
        } else {
            emptyList()
        }
    }

    private fun startDiscovery() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
            registerReceiver(bluetoothReceiver, filter)
            bluetoothAdapter?.startDiscovery()?.let {
                Log.d("DEBUG", "Démarrage de la découverte : $it")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(bluetoothReceiver)
    }
}

@Composable
fun BluetoothDeviceScreen(pairedDevices: List<BluetoothDevice>, availableDevices: List<BluetoothDevice>, context: Context) {
    val hasLocationPermission = remember {
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }
    // Utilisation d'un Set pour éviter les doublons
    val availableDevicesSet = remember { mutableSetOf<BluetoothDevice>() }
    availableDevicesSet.clear() // Réinitialise le Set avant d'ajouter de nouveaux appareils

    // Ajout des appareils disponibles dans le Set
    availableDevices.forEach {
        if (hasLocationPermission) {
            availableDevicesSet.add(it)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Appareils appairés", style = MaterialTheme.typography.headlineMedium)
        LazyColumn(modifier = Modifier.weight(1f).padding(bottom = 16.dp)) {
            items(pairedDevices) { device ->
                val deviceName = if (hasLocationPermission) {device.name ?: "Inconnu"} else {"Permission manquante"}
                Text(text = "$deviceName - ${device.address}")
            }
        }

        Text(text = "Appareils disponibles", style = MaterialTheme.typography.headlineMedium)
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(availableDevicesSet.toList()) { device ->
                val deviceName = if (hasLocationPermission) {device.name ?: "Inconnu"} else {"Permission manquante"}
                Text(text = "$deviceName - ${device.address}")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewBluetoothScreen() {
    TP4_bluetoothTheme {
        BluetoothDeviceScreen(emptyList(), emptyList(), LocalContext.current)
    }
}
