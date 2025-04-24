package com.example.menu

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import com.example.menu.BluetoothService.Companion.inputStream
import com.example.menu.BluetoothService.Companion.outputStream
import com.example.menu.utils.displayMultiplayerEnd
import com.example.menu.utils.receiveMessage
import com.example.menu.utils.sendMessage

class ScoreActivity : AppCompatActivity() {

    private lateinit var finalScoreTextView: TextView
    private lateinit var winnerMessage: TextView
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.final_screen)

        supportActionBar?.hide()

        finalScoreTextView = findViewById(R.id.finalScoreTextView)
        winnerMessage = findViewById(R.id.nicePlayBuddy)
        winnerMessage.visibility = View.GONE

        val isMultiplayer = intent.getBooleanExtra("isMultiplayer", false)
        val isQuickPlay = intent.getBooleanExtra("isQuickPlay", false)

        if (isMultiplayer) {// 3 jeux terminés, on affiche le score des deux joueurs

            val myFinalScore = intent.getIntExtra("myFinalScore", 0)

            //echange des scores
            sendMessage("FINALSCORE:$myFinalScore;", outputStream)
            Log.d("ScoreActivity", "score final envoyé $myFinalScore")
            Thread.sleep(100)
            val opponentFinalScore = receiveMessage(inputStream)?.trimEnd(';')
            Log.d("ScoreActivity", "score final reçu : $opponentFinalScore")
            var opponentFinalScoreInt = 0
            if (opponentFinalScore != null && opponentFinalScore.startsWith("FINALSCORE:")) {
                opponentFinalScoreInt = opponentFinalScore.removePrefix("FINALSCORE:").toInt()

            }else{
                opponentFinalScoreInt = 0
            }
            Log.d("ScoreActivity", "Affichage score : moi=$myFinalScore, autre=$opponentFinalScoreInt")

            finalScoreTextView.text = "Ton score final: $myFinalScore\nScore adversaire final: $opponentFinalScoreInt"

            if (myFinalScore > opponentFinalScoreInt) {
                winnerMessage.visibility = View.VISIBLE// affiche le message gg
                mediaPlayer = MediaPlayer.create(this, R.raw.congratulations_music)
                mediaPlayer?.start()
            }
        } else {// 3 jeux terminés, on affiche le score du joueur solo

            val finalScore = intent.getIntExtra("final_score", 0)
            finalScoreTextView.text = "Score final : $finalScore"

            if (isQuickPlay) {
                winnerMessage.visibility = View.VISIBLE
                mediaPlayer = MediaPlayer.create(this, R.raw.congratulations_music)
                mediaPlayer?.start()
            }
        }
        //termine et coupe la musique apres 8 sec
        Handler(Looper.getMainLooper()).postDelayed({
            mediaPlayer?.stop()
            mediaPlayer?.release()
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
            finish()
        }, 8000)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.stop()
        mediaPlayer?.release()
    }
}