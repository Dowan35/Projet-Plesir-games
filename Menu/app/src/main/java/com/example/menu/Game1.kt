package com.example.menu

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class Game1 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val nextGameIndex = intent.getIntExtra("nextGameIndex", -1)
        val isQuickPlay = nextGameIndex != -1

        supportActionBar?.hide()

        // Affichage d'un log pour savoir que l'on est dans la méthode onCreate
        Log.d("Game1", "onCreate() appelé")

        setContentView(R.layout.game1) // Charge le XML
        Log.d("Game1", "setContentView() terminé, layout chargé")

        // Récupérer le TextView pour afficher le score
        val scoreTextView: TextView = findViewById(R.id.scoreTextView)
        Log.d("Game1", "scoreTextView récupéré : $scoreTextView")

        // Récupérer la GameView et lui envoyer le TextView pour mettre à jour le score
        val gameView: GameView = findViewById(R.id.gameView)
        Log.d("Game1", "gameView récupéré : $gameView")

        gameView.setOnGameOverCallback {
            // Laisse un petit délai pour laisser voir le score
            gameView.postDelayed({
                val score = gameView.getCurrentScore()
                if (isQuickPlay) {

                    Home.continueQuickPlay(this,score)
                } else {
                    finish()
                }
            }, 2000)
        }

        gameView.setScoreTextView(scoreTextView)  // Méthode à ajouter dans GameView.kt
        Log.d("Game1", "scoreTextView envoyé à GameView pour mise à jour du score")
    }
}
