package com.example.menu

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView

class ScoreActivity : AppCompatActivity() {

    private lateinit var finalScoreTextView: TextView
    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.final_screen)

        // Récupérer le score passé via l'Intent
        val finalScore = intent.getIntExtra("final_score", 0)

        finalScoreTextView = findViewById(R.id.finalScoreTextView)
        finalScoreTextView.text = "Score final : $finalScore"

        // Démarrer la musique de fond
        mediaPlayer = MediaPlayer.create(this, R.raw.congratulations_music)
        mediaPlayer.start()

        // Après 10 secondes, retourner au menu principal
        Handler().postDelayed({
            mediaPlayer.stop() // Arrêter la musique
            val intent = Intent(this, Home::class.java) // Rediriger vers le menu principal
            startActivity(intent)
            finish() // Fermer l'écran de score
        }, 10000) // Délai de 10 secondes
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
        mediaPlayer.release()
    }
}
