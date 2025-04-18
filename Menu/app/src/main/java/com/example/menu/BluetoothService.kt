package com.example.menu

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.menu.utils.*
import java.io.InputStream
import java.io.OutputStream
import java.util.*

class BluetoothService : ComponentActivity() {

    private val bluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private val _availableDevices = mutableStateListOf<BluetoothDevice>()
    private val _pairedDevices = mutableStateListOf<BluetoothDevice>()
    private val uuid: UUID = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66")

    companion object {
        var hostName: String = ""
        var clientName: String = ""
        var socket: BluetoothSocket? = null
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null
    }

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!hasBluetoothPermissions()) {
            if (requestBluetoothPermissionsIfNeeded()) return
        }

        val mode = intent.getStringExtra("mode")
        val userName = intent.getStringExtra("userName") ?: "Joueur"

        if (mode == "host") {
            hostName = userName
            // Demande à l'utilisateur de rendre l'appareil visible
            val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120) // 2 minutes
            startActivity(discoverableIntent)
            startHost()
        } else {
            if (!bluetoothAdapter.isEnabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, 1) // 1 est un code arbitraire
                return // On attend le résultat avant de continuer
            }

            clientName = userName
            setContent{ startClient() }

        }
    }

    @SuppressLint("MissingPermission")
    private fun startHost() {
        val originalName = bluetoothAdapter.name
        bluetoothAdapter.name = hostName // Remplace temporairement le nom Bluetooth

        setContent {
            setContent {
                CommonBluetoothLayout(
                    topContent = {
                        Text("Serveur démarré : $hostName", style = MaterialTheme.typography.headlineMedium)
                        Text("En attente du deuxième joueur...")
                    },
                    bottomButtonText = "Fermer la connexion",
                    onBottomButtonClick = { stopHost(originalName) }
                )
            }

        }

        Thread {
            try {
                val serverSocket: BluetoothServerSocket =
                    bluetoothAdapter.listenUsingRfcommWithServiceRecord("BTService", uuid)
                socket = serverSocket.accept()
                inputStream = socket?.inputStream
                outputStream = socket?.outputStream

                // Envoyer le nom hôte au client dès qu’il se connecte
                outputStream?.write(hostName.toByteArray())

                // Lire le nom du client reçu
                val buffer = ByteArray(1024)
                val bytesRead = inputStream?.read(buffer) ?: 0
                clientName = buffer.decodeToString(0, bytesRead)

                runOnUiThread {
                    setContent {
                        setContent {
                            CommonBluetoothLayout(
                                topContent = {
                                    Text("Serveur : $hostName", style = MaterialTheme.typography.headlineMedium)
                                    Text("Joueur $clientName connecté !")
                                },
                                bottomButtonText = "Fermer la connexion",
                                onBottomButtonClick = { stopHost(originalName) }
                            )
                        }

                    }
                }

                serverSocket.close()
                bluetoothAdapter.name = originalName // Restaure le nom initial

            } catch (e: Exception) {
                e.printStackTrace()
                bluetoothAdapter.name = originalName
            }
        }.start()
    }

    @Composable
    @SuppressLint("MissingPermission")
    private fun startClient() {
            startDiscovery() // Lance la découverte ici

            LaunchedEffect(Unit) {
                val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
                registerReceiver(receiver, filter)
            }

        setContent {
            CommonBluetoothLayout(
                topContent = {
                    Text("Sélectionnez un serveur à rejoindre", style = MaterialTheme.typography.headlineMedium)
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(_availableDevices) { device ->
                            Button(
                                onClick = {
                                    if (device.bondState != BluetoothDevice.BOND_BONDED) {
                                        device.createBond()
                                        Toast.makeText(this@BluetoothService, "Appairage avec ${device.name}...", Toast.LENGTH_SHORT).show()
                                    }
                                    connectToDevice(device)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Text("Rejoindre ${device.name}")
                            }
                        }
                    }
                },
                bottomButtonText = "Fermer la connexion",
                onBottomButtonClick = { stopClient() }
            )
        }
    }
    //permet d'activer le bluetooth si il n'est pas activé
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1) { // même code que plus haut
            if (resultCode == RESULT_OK) {
                // Bluetooth activé, on peut lancer le client
                setContent { startClient() }
            } else {
                Toast.makeText(this, "Bluetooth requis pour se connecter", Toast.LENGTH_SHORT).show()
                finish() // Optionnel : fermer si refusé
            }
        }
    }

    @SuppressLint("MissingPermission")
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == BluetoothDevice.ACTION_FOUND) {
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                device?.let {
                    if (!_availableDevices.contains(it) && it.name != null) _availableDevices.add(it)
                }
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
    private fun connectToDevice(device: BluetoothDevice) {
        Thread {
            try {
                socket = device.createRfcommSocketToServiceRecord(uuid)
                bluetoothAdapter.cancelDiscovery()
                socket?.connect()
                inputStream = socket?.inputStream
                outputStream = socket?.outputStream

                // Lecture du nom hôte envoyé par le serveur
                val buffer = ByteArray(1024)
                val bytesRead = inputStream?.read(buffer) ?: 0
                val serverName = buffer.decodeToString(0, bytesRead)

                // Envoyer le nom du client au serveur
                outputStream?.write(clientName.toByteArray())

                runOnUiThread {
                    Toast.makeText(this, "Connecté à $serverName", Toast.LENGTH_SHORT).show()

                    setContent {
                        CommonBluetoothLayout(
                            topContent = {
                                Text(
                                    "Connecté au serveur de $serverName",
                                    style = MaterialTheme.typography.headlineMedium
                                )
                                Text("Votre pseudo : $clientName")
                            },
                            bottomButtonText = "Fermer la connexion",
                            onBottomButtonClick = { stopClient() }
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this, "Échec de connexion", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    @SuppressLint("MissingPermission")
    private fun stopHost(originalName: String) {
        try {
            socket?.close()
            inputStream?.close()
            outputStream?.close()

            bluetoothAdapter.name = originalName // Restaure le nom initial
            socket = null
            inputStream = null
            outputStream = null

            // Afficher un toast et remettre à zéro l'interface
            runOnUiThread {
                Toast.makeText(this, "Connexion fermée", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, Home::class.java))// Redémarrer l'interface d'accueil
                //setContent { startHost() }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopClient() {
        try {
            socket?.close()
            inputStream?.close()
            outputStream?.close()

            socket = null
            inputStream = null
            outputStream = null

            // Afficher un toast et réinitialiser l'interface
            runOnUiThread {
                Toast.makeText(this, "Déconnexion réussie", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, Home::class.java))// Redémarrer l'interface d'accueil
                //setContent { startClient() }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
