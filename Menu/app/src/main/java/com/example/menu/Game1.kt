package com.example.menu

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class Game1 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val nextGameIndex = intent.getIntExtra("nextGameIndex", -1)
        val isQuickPlay = nextGameIndex != -1
        val isMultiplayer = intent.getBooleanExtra("isMultiplayer", false)

        supportActionBar?.hide()

        Log.d("Game1", "onCreate() appelé")

        setContentView(R.layout.game1)
        Log.d("Game1", "setContentView() terminé, layout chargé")

        // Layouts
        val introLayout = findViewById<LinearLayout>(R.id.introLayout)
        val gameLayout = findViewById<LinearLayout>(R.id.gameLayout)
        val startButton = findViewById<Button>(R.id.startButton)

        val scoreTextView: TextView = findViewById(R.id.scoreTextView)
        val gameView: GameView = findViewById(R.id.gameView)

        // Désactivation au début
        gameLayout.visibility = View.GONE
        gameView.isEnabled = false
        scoreTextView.isEnabled = false

        gameView.setScoreTextView(scoreTextView)

        // Lancement du jeu au clic
        startButton.setOnClickListener {
            introLayout.visibility = View.GONE
            gameLayout.visibility = View.VISIBLE

            gameView.isEnabled = true
            scoreTextView.isEnabled = true

            gameView.startGame() // Cette méthode doit exister dans GameView.kt
        }

        gameView.setOnGameOverCallback {
            gameView.postDelayed({
                val score = gameView.getCurrentScore()
                if (isQuickPlay) {
                    Home.continueQuickPlay(this, score)
                } else {
                    finish()
                }
            }, 2000)
        }

        Log.d("Game1", "Initialisation complète")
    }
}