package com.example.menu

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.concurrent.Executors
import com.example.menu.BluetoothService.Companion.inputStream
import com.example.menu.BluetoothService.Companion.outputStream
import com.example.menu.utils.*
import com.example.menu.utils.displayMultiplayerEnd

class Game1 : AppCompatActivity() {

    private val executor = Executors.newSingleThreadExecutor()
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // infos fournies par home.startGameList, puis par multiplayerTools.displayMultiplayerEnd
        val isQuickPlay =  intent.getBooleanExtra("isQuickPlay", false)
        val isMultiplayer = intent.getBooleanExtra("isMultiplayer", false)
        val totalScore = intent.getIntExtra("totalScore", 0)
        val nextIndex = intent.getIntExtra("nextGameIndex", 0)
        val selectedGames = intent.getStringArrayExtra("selectedGames")?.map { Class.forName(it) } ?: emptyList()
        Log.d("Game1", "Jeu lancé avec index = = $nextIndex / total = ${selectedGames.size}")
// (tag:Game1 | tag:"Home, client" | tag:"game1" | tag:"displayMultiplayerEnd" | tag:"package:mine" | tag:"Home, serveur" | tag:"ScoreActivity") & (-tag~:"ViewRoot*")
        supportActionBar?.hide()

        Log.d("Game1", "onCreate() appelé")

        setContentView(R.layout.game1)
        Log.d("Game1", "setContentView() terminé, layout chargé")

        val introLayout = findViewById<LinearLayout>(R.id.introLayout)
        val gameLayout = findViewById<LinearLayout>(R.id.gameLayout)
        val startButton = findViewById<Button>(R.id.startButton)
        val endLayout = findViewById<LinearLayout>(R.id.endLayout)

        val scoreTextView: TextView = findViewById(R.id.scoreTextView)
        val gameView: GameView = findViewById(R.id.gameView)
        val multiplayerScoreText: TextView = findViewById(R.id.multiplayerScoreText)//ecran de fin de jeu

        gameLayout.visibility = View.GONE
        endLayout.visibility = View.GONE
        gameView.isEnabled = false
        scoreTextView.isEnabled = false

        gameView.setScoreTextView(scoreTextView)

        startButton.setOnClickListener {
            if (isMultiplayer) {
                executor.execute {
                    try {
                        val result = waitForBothReady("READY;")//attends que les deux cliquent sur le bouton pour lancer
                        if (result) {
                            runOnUiThread {
                                startGame(introLayout, gameLayout, gameView, scoreTextView)
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            } else {
                startGame(introLayout, gameLayout, gameView, scoreTextView)
            }
        }

        gameView.setOnGameOverCallback {
            gameView.postDelayed({
                val myScore = gameView.getCurrentScore()

                if (isMultiplayer) {
                    executor.execute {
                        try {
                            sendMessage("SCORE:$myScore;",outputStream)
                            Log.d("game1", "score envoyé")
                            Thread.sleep(100)
                            val message = receiveMessage(inputStream)?.trimEnd(';')
                            Log.d("game1", "score reçu : $message")

                            if (message != null && message.startsWith("SCORE:")) {
                                val otherScore = message.removePrefix("SCORE:").toInt()
                                runOnUiThread {
                                    // affiche mon score et le sien, puis lance le prochain jeu
                                    displayMultiplayerEnd(
                                        activity = this,
                                        myScore = myScore,
                                        otherScore = otherScore,
                                        endLayout = endLayout,
                                        multiplayerScoreText = multiplayerScoreText,
                                        totalScore = totalScore,
                                        selectedGames = selectedGames,
                                        nextGameIndex = nextIndex
                                    )
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                } else {
                    if (isQuickPlay) {
                        Home.continueQuickPlay(this, myScore)
                    } else {
                        finish()
                    }
                }
            }, 3000)
        }
    }

    private fun startGame(
        introLayout: LinearLayout,
        gameLayout: LinearLayout,
        gameView: GameView,
        scoreTextView: TextView
    ) {
        introLayout.visibility = View.GONE
        gameLayout.visibility = View.VISIBLE
        gameView.isEnabled = true
        scoreTextView.isEnabled = true
        gameView.startGame()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
    }
}
