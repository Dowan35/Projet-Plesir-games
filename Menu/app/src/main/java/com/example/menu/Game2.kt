package com.example.menu

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.VideoView
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.menu.ui.theme.Projet_jeu_mobileTheme
import kotlinx.coroutines.delay


class Game2 : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null

    private var shakeThreshold: Float = 12.0f
    private var currentAcceleration = 0f
    private var lastAcceleration = 0f
    private var acceleration = 0f

    private var shakeProgress by mutableStateOf(0f)
    private var isExploded by mutableStateOf(false)
    private var gameStarted by mutableStateOf(false)

    private var vibrate by mutableStateOf(false)
    private var startTime by mutableStateOf(0L)
    private var elapsedTime by mutableStateOf(0L)

    private var showFinalScore by mutableStateOf(false)

    private var mode: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()

        // Récupérer le mode (entrainement ou autre)
        mode = intent.getStringExtra("mode")

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)

        setContent {
            Projet_jeu_mobileTheme {
                GameScreen()
            }
        }
    }

    @Composable
    fun GameScreen() {
        // Initialiser l'état `showInstructions` ici, dans un @Composable fonction
        var showInstructions by remember { mutableStateOf(true) }

        // Si on affiche les instructions
        if (showInstructions) {
            InstructionScreen(onContinue = {
                showInstructions = false
                startGame()
            })
        } else if (!gameStarted) {
            InstructionScreen {  }
        } else {
            BottleGameScreen(
                shakeProgress,
                isExploded,
                vibrate,
                showFinalScore,
                elapsedTime,
                onReset = { resetGame() }
            )
        }
    }

    @Composable
    fun InstructionScreen(onContinue: () -> Unit) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .background(Color.Black),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Règles du jeu",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White
            )

            Text(
                text = "Secouez votre téléphone pour augmenter la pression dans la bouteille.".trimIndent(),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            Button(onClick = onContinue) {
                Text("Commencer")
            }
        }
    }

    private fun calculateScore(): Float {
        return elapsedTime / 1000f
    //return (30- shakeProgress.toInt()).coerceAtLeast(0)
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
            shakeProgress += 1.5f
            vibrate = true
            Handler(Looper.getMainLooper()).postDelayed({
                vibrate = false
            }, 450)
            if (shakeProgress >= 125f) {
                triggerExplosion()
            }
        }

        lastAcceleration = acceleration
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun triggerExplosion() {
        isExploded = true
        //elapsedTime = ((System.currentTimeMillis() - startTime) / 1000).toInt()
        showFinalScore = true

        // Vibration
        val v = getSystemService(VIBRATOR_SERVICE) as Vibrator

        // Vibrate for 500 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(800, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            //deprecated in API 26
            v.vibrate(800)
        }

        Handler(Looper.getMainLooper()).postDelayed({
            if (mode == "entrainement") {
                startActivity(Intent(this, Home::class.java))
            } else {
                Home.continueQuickPlay(this, calculateScore().toInt())
            }
            finish()
        }, 9000) // Redirection après 9s

    }

    private fun resetGame() {
        shakeProgress = 0f
        isExploded = false
        gameStarted = false
    }

    private fun startGame() {
        startTime = System.currentTimeMillis()
        startTimer()
        //elapsedTime = 0
        gameStarted = true

    }
    private fun startTimer() {
        val handler = Handler(Looper.getMainLooper())
        handler.post(object : Runnable {
            override fun run() {
                if (!isExploded) {
                    elapsedTime = System.currentTimeMillis() - startTime
                    handler.postDelayed(this, 100)
                }
            }
        })
    }


    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
    }
}

@Composable
fun BottleGameScreen(
    shakeProgress: Float,
    isExploded: Boolean,
    vibrate: Boolean,
    showFinalScore: Boolean,
    elapsedTime: Long,
    onReset: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (!isExploded) {
            val image: Painter = painterResource(id = R.drawable.champagne_bottle)
            var vibeOffset by remember { mutableStateOf(0.dp) }

            LaunchedEffect(vibrate) {
                if (vibrate) {
                    repeat(3) {
                        vibeOffset = (-12).dp
                        delay(50)
                        vibeOffset = 12.dp
                        delay(50)
                    }
                    vibeOffset = 0.dp
                }
            }

            var chrono by remember { mutableStateOf(0) }
            LaunchedEffect(true) {
                while (!isExploded) {
                    delay(1000)
                    chrono++
                }
            }

            var textOffset by remember { mutableStateOf(0.dp) }
            LaunchedEffect(true) {
                while (!isExploded) {
                    textOffset = (-7).dp
                    delay(300)
                    textOffset = 7.dp
                    delay(300)
                }
            }
            // Image bouteille
            Image(
                painter = image,
                contentDescription = "Bouteille de Champagne",
                modifier = Modifier
                    .fillMaxSize()
                    //.align(Alignment.Center)
                    //.height(300.dp)
                    .offset(y = shakeProgress.dp + vibeOffset)
                    .padding(top = 32.dp)
            )

            // Texte animé "Secouez !"
            Text(
                text = "Secouez !",
                color = Color.White,
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 72.dp)
                    .offset(y = textOffset)
            )

            // Chrono en haut à gauche
            Text(
                text = "Temps : ${"%.1f".format(elapsedTime / 1000f)}s",
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
            )

        } else {
            // Explosion en fond
            AndroidView(
                factory = { context ->
                    VideoView(context).apply {
                        val packageName = context.packageName
                        setVideoURI(Uri.parse("android.resource://$packageName/${R.raw.explosion_video}"))
                        //setOnCompletionListener { onFinished() }
                        start()
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 30.dp)
            )

            // Texte "Terminé" superposé (visible seulement si showFinalScore est true)
            if (showFinalScore) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 100.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    Text(
                        text = "Terminé !",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White
                    )
                    Text(
                        text = "Score : ${"%.1f".format(elapsedTime / 1000f)}s",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White
                    )
                }
            }
        }
    }
}



//@Preview(showBackground = true)
//@Composable
//fun PreviewBottleGame() {
//    BottleGameScreen(
//        shakeProgress = 0f,
//        isExploded = false,
//        vibrate = false,
//        showFinalScore,
//        elapsedTime,
//        onReset = { resetGame() }
//    )
//}
