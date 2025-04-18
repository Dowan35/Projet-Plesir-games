package com.example.menu

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@SuppressLint("MissingPermission")
@Composable
fun BluetoothDeviceScreen(
    pairedDevices: List<BluetoothDevice>,
    availableDevices: List<BluetoothDevice>,
    onRefreshClick: () -> Unit,
    onDeviceClick: (BluetoothDevice) -> Unit
) {
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Appareils déjà appairés", style = MaterialTheme.typography.headlineSmall)
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(pairedDevices) { device ->
                DeviceItem(device, onDeviceClick)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Appareils disponibles", style = MaterialTheme.typography.headlineSmall)
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(availableDevices) { device ->
                DeviceItem(device, onDeviceClick)
            }
        }

        Button(
            onClick = onRefreshClick,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Text("Rafraîchir la liste")
        }

    }
}

@SuppressLint("MissingPermission")
@Composable
fun DeviceItem(device: BluetoothDevice, onClick: (BluetoothDevice) -> Unit) {
    val name = device.name ?: "Inconnu"
    val address = device.address

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(device) }
            .padding(8.dp)
    ) {
        Text("Nom : $name")
        Text("Adresse : $address", style = MaterialTheme.typography.bodySmall)
        Divider()
    }
}
