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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

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

    fun openBluetoothService() {
        val intent = Intent(context, BluetoothService::class.java)
        intent.putExtra("mode", mode)
        intent.putExtra("userName", pseudo.text)
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
                showDialog = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Host")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                mode = "join"
                showDialog = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Join")
        }

        Spacer(modifier = Modifier.height(16.dp))

//        Button(
//            onClick = {
//                val intent = Intent(context, BluetoothPairingScreen::class.java)
//                context.startActivity(intent)
//            },
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            Text("Pair Device")
//        }
    }

    if (showDialog) {
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
                            openBluetoothService()
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
