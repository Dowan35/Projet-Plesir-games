package com.example.projet_jeu_mobile

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import android.content.Context
import android.hardware.SensorManager
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.viewinterop.AndroidView
import com.example.projet_jeu_mobile.ui.theme.Projet_jeu_mobileTheme
import android.widget.VideoView
import android.net.Uri
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null

    private var shakeThreshold: Float = 12.0f
    private var currentAcceleration = 0f
    private var lastAcceleration = 0f
    private var acceleration = 0f

    private var shakeProgress by mutableStateOf(0f)
    private var isExploded by mutableStateOf(false)
    private var gameStarted by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)

        setContent {
            Projet_jeu_mobileTheme {
                if (!gameStarted) {
                    StartScreen { startGame() }
                } else {
                    BottleGameScreen(shakeProgress, isExploded) { resetGame() }
                }
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_ACCELEROMETER || !gameStarted) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        acceleration = Math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()
        val delta = acceleration - lastAcceleration
        currentAcceleration = currentAcceleration * 0.9f + delta

        if (currentAcceleration > shakeThreshold && !isExploded) {
            shakeProgress += 15f
            if (shakeProgress >= 600f) {
                triggerExplosion()
            }
        }

        lastAcceleration = acceleration
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun triggerExplosion() {
        isExploded = true
    }

    private fun resetGame() {
        shakeProgress = 0f
        isExploded = false
        gameStarted = false
    }

    private fun startGame() {
        gameStarted = true
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
    }
}

@Composable
fun StartScreen(onStart: () -> Unit) {
    var countdown by remember { mutableStateOf(3) }
    var countdownStarted by remember { mutableStateOf(false) }

    // Déclenchement du compte à rebours une fois que countdownStarted passe à true
    LaunchedEffect(countdownStarted) {
        if (countdownStarted) {
            for (i in 3 downTo 1) {
                delay(1000L)
                countdown = i - 1
            }
            onStart() // Démarrer le jeu après le compte à rebours
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (!countdownStarted) {
            Button(onClick = { countdownStarted = true }) {
                Text("Commencer")
            }
        } else {
            Text("Début dans $countdown...", style = MaterialTheme.typography.headlineMedium)
        }
    }
}

@Composable
fun startCountdown(onStart: () -> Unit, onTick: () -> Unit) {
    LaunchedEffect(Unit) {
        for (i in 3 downTo 1) {
            delay(1000L)
            onTick()
        }
        onStart()
    }
}

@Composable
fun BottleGameScreen(shakeProgress: Float, isExploded: Boolean, onReset: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (!isExploded) {
            val image: Painter = painterResource(id = R.drawable.champagne_bottle)
            Image(
                painter = image,
                contentDescription = "Bouteille de Champagne",
                modifier = Modifier.fillMaxSize().offset(y = shakeProgress.dp)
            )
            Text(text = "Secouez pour faire exploser la bouteille !", style = MaterialTheme.typography.bodyLarge)
        } else {
            AndroidView(
                factory = { context ->
                    VideoView(context).apply {
                        setVideoURI(Uri.parse("android.resource://com.example.projet_jeu_mobile/${R.raw.explosion_video}"))
                        start()
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
            Button(onClick = onReset) {
                Text(text = "Rejouer")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewBottleGame() {
    BottleGameScreen(shakeProgress = 0f, isExploded = false) {}
}
