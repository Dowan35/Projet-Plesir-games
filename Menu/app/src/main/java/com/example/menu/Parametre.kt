package com.example.menu

import android.content.*
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

class Parametre : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                ParametreScreen()
            }
        }
    }
}

@Composable
fun ParametreScreen() {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    var mode by remember { mutableStateOf("") } // "host" ou "join"
    var pseudo by remember { mutableStateOf(TextFieldValue("")) }

    // permet de griser un bouton si on clique sur un autre
    val lifecycleOwner = LocalLifecycleOwner.current
    var hostNameState by remember { mutableStateOf("") }
    var clientNameState by remember { mutableStateOf("") }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // Met à jour les noms à chaque retour sur l'écran
                hostNameState = BluetoothService.hostName
                clientNameState = BluetoothService.clientName
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    fun openBluetoothService() {
        val intent = Intent(context, BluetoothService::class.java)

        intent.putExtra("mode", mode)

        val finalUserName = when (mode) {
            "host" -> hostNameState.ifBlank { pseudo.text }
            "join" -> clientNameState.ifBlank { pseudo.text }
            else -> pseudo.text
        }

        intent.putExtra("userName", finalUserName)
        context.startActivity(intent)
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = {
                mode = "host"
                if (BluetoothService.hostName.isNotBlank()) {
                    openBluetoothService()
                } else {
                    showDialog = true
                }
            },
            enabled = clientNameState.isBlank(), // Désactivé si client actif
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Host")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                mode = "join"
                if (BluetoothService.clientName.isNotBlank()) {
                    openBluetoothService()
                } else {
                    showDialog = true
                }
            },
            enabled = hostNameState.isBlank(), // Désactivé si host actif
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Join")
        }

        Spacer(modifier = Modifier.height(16.dp))

    }

    if (showDialog) { // box de dialogue pour mettre son nom.
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Entrez votre pseudo :") },
            text = {
                OutlinedTextField(
                    value = pseudo,
                    onValueChange = { pseudo = it },
                    singleLine = true,
                    label = { Text("Pseudo") }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (pseudo.text.isNotBlank()) {
                            showDialog = false
                            openBluetoothService() // ferme la boite et lance la fonction pour la redirection
                        }
                    }
                ) {
                    Text("Valider")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}
