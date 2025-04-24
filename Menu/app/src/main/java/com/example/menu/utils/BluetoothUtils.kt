package com.example.menu.utils

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.compose.material3.*
import com.example.menu.BluetoothService.Companion.inputStream
import com.example.menu.BluetoothService.Companion.isHosting
import com.example.menu.BluetoothService.Companion.isJoining
import com.example.menu.BluetoothService.Companion.outputStream
import java.io.InputStream
import java.io.OutputStream

/**
 * Vérifie si toutes les permissions Bluetooth nécessaires sont accordées.
 */
fun Context.hasBluetoothPermissions(): Boolean {
    val permissions = getBluetoothPermissions()
    return permissions.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }
}

/**
 * Retourne la liste des permissions Bluetooth selon la version Android.
 */
fun getBluetoothPermissions(): List<String> {
    val permissions = mutableListOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        permissions.add(Manifest.permission.BLUETOOTH_SCAN)
    }

    return permissions
}

/**
 * Demande les permissions Bluetooth manquantes (à appeler dans une Activity).
 */
fun Activity.requestBluetoothPermissionsIfNeeded(requestCode: Int = 101): Boolean {
    val missingPermissions = getBluetoothPermissions().filter {
        ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
    }

    return if (missingPermissions.isNotEmpty()) {
        ActivityCompat.requestPermissions(this, missingPermissions.toTypedArray(), requestCode)
        true // Permissions en cours de demande
    } else {
        false // Toutes les permissions déjà accordées
    }
}

@Composable
fun CommonBluetoothLayout(
    topContent: @Composable ColumnScope.() -> Unit,
    bottomButtonText: String,
    onBottomButtonClick: () -> Unit
) {
    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.align(Alignment.TopCenter),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                topContent()
            }

            Button(
                onClick = onBottomButtonClick,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            ) {
                Text(bottomButtonText)
            }
        }
    }
}

fun isSocketConnected(socket : BluetoothSocket?): Boolean {
    //socket = BluetoothService.socket
    if (socket == null || !socket.isConnected) return false

    return try {
        socket.outputStream.write(0) // tentative d'écriture d’un byte inutile
        true
    } catch (e: Exception) {
        false
    }
}
// Fonction utilitaire pour ecrire un message texte
fun sendMessage(msg: String, outputStream: OutputStream?) {
    try {
        outputStream?.write(msg.toByteArray())
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

object MessageBuffer {
    var buffer = ""
}

// Fonction utilitaire pour lire un message texte (bloquante)
fun receiveMessage(inputStream: InputStream?): String? {
    return try {
        val buffer = ByteArray(1024)//init d'un buffer juste pour un message
        val builder = StringBuilder(MessageBuffer.buffer)// memoire tampon

        while (!builder.contains(";")) {//recupere le contenu
            val bytes = inputStream?.read(buffer) ?: break
            val raw = buffer.decodeToString(0, bytes)
                .filter { it.code in 32..126 || it == ';' }

            builder.append(raw)
        }

        val fullMessage = builder.toString()
        val split = fullMessage.split(";", limit = 2)

        val message = split.getOrNull(0)//?.plus(";")
        MessageBuffer.buffer = split.getOrNull(1) ?: "" // stocker ce qu’il reste pour plus tard

        return message
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun waitForBothReady(message : String): Boolean {
    var receivedReady = false
    var sentReady = false

    if (isJoining) {
        sendMessage(message, outputStream)
        Log.d("Home, client", "Envoi de $message")
        sentReady = true
        val msg = receiveMessage(inputStream)
        Log.d("Home, client", "reception de $msg")
        receivedReady = msg?.trimEnd(';') == message.trimEnd(';')
    } else if (isHosting){
        val msg = receiveMessage(inputStream)
        Log.d("Home, serveur", "reception de $msg")
        receivedReady = msg?.trimEnd(';') == message.trimEnd(';')
        sendMessage(message, outputStream)
        Log.d("Home, serveur", "Envoi de $message")
        sentReady = true
    }

    return sentReady && receivedReady
}





