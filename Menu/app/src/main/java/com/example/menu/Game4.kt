package com.example.menu

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class Game4 : AppCompatActivity(), SensorEventListener {

    private lateinit var ballView: BallView
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null

    private lateinit var timerText: TextView
    private lateinit var scoreText: TextView  // Pour afficher le score calculé
    private val handler = Handler(Looper.getMainLooper())
    private var secondsElapsed = 0
    private var isGameFinished = false
    private var mode: String? = null  // ← Pour savoir si on est en rapide ou entraînement

    private val timerRunnable = object : Runnable {
        override fun run() {
            if (!isGameFinished) {
                secondsElapsed++
                timerText.text = "Temps : $secondsElapsed sec"
                handler.postDelayed(this, 1000)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.game4)

        // On récupère le mode depuis l’intent
        mode = intent.getStringExtra("mode")

        val container = findViewById<FrameLayout>(R.id.mazeContainer)
        timerText = findViewById(R.id.timerText)
        scoreText = findViewById(R.id.scoreText)  // Assurez-vous d'avoir un TextView dans votre layout pour afficher le score.

        // Crée et ajoute la vue personnalisée
        ballView = BallView(this) { onWin() }
        container.addView(ballView)

        // Lance le timer
        handler.postDelayed(timerRunnable, 1000)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    private fun onWin() {
        isGameFinished = true
        timerText.text = "Gagné en $secondsElapsed secondes !"

        // Calcul du score : 30 - le temps écoulé
        val score = 30 - secondsElapsed
        scoreText.text = "Score : $score"

        // Si le score devient négatif, le mettre à 0 (ou une valeur minimum)
        val finalScore = if (score < 0) 0 else score

        // Affichage du score à la fin
        scoreText.text = "Score final : $finalScore"

        handler.postDelayed({
            if (mode == "entrainement") {
                startActivity(Intent(this, Home::class.java))
            } else {
                Home.continueQuickPlay(this,score)
            }
            finish()
        }, 2000)
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null && !isGameFinished) {
            val x = -event.values[0]
            val y = event.values[1]
            ballView.updatePosition(x, y)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
