package com.example.menu

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.menu.utils.isSocketConnected

class Home : AppCompatActivity() {

    private val games = listOf(
        Game1::class.java,
        Game2::class.java,
        Game3::class.java,
        Game4::class.java,
        Game5::class.java,
        Game6::class.java
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.menu_home)

        supportActionBar?.hide()

        val quickPlayButton = findViewById<Button>(R.id.quickPlayButton)
        val trainingButton = findViewById<Button>(R.id.trainingButton)
        val multiPlayerButton = findViewById<Button>(R.id.multiplayerButton)
        val buttonParametre = findViewById<Button>(R.id.buttonParametre)

        quickPlayButton.setOnClickListener {
            launchRandomGames(false)
        }
        trainingButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        multiPlayerButton.setOnClickListener {
            if (isSocketConnected(BluetoothService.socket)) {
                launchRandomGames(true)
            } else {
                Toast.makeText(this, "Veuillez d'abord vous connecter dans les paramètres", Toast.LENGTH_SHORT).show()
            }
        }

        buttonParametre.setOnClickListener {
            startActivity(Intent(this, Parametre::class.java))
        }
    }

    private fun launchRandomGames(isMultiplayer: Boolean) {
        val selectedGames = games.shuffled().take(3)
        startNextGame(0, selectedGames, isMultiplayer)
    }

    private fun startNextGame(index: Int, selectedGames: List<Class<*>>, isMultiplayer: Boolean) {
        if (index >= selectedGames.size) return

        val intent = Intent(this, selectedGames[index])
        intent.putExtra("nextGameIndex", index + 1)
        intent.putExtra("selectedGames", selectedGames.map { it.name }.toTypedArray())
        intent.putExtra("isMultiplayer",  isMultiplayer)
        startActivity(intent)
    }

    companion object {
        fun continueQuickPlay(activity: AppCompatActivity, score: Int) {
            val currentIntent = activity.intent
            val selectedGames = (currentIntent.getStringArrayExtra("selectedGames") ?: return)
                .mapNotNull { className ->
                    try {
                        Class.forName(className)
                    } catch (e: Exception) {
                        null
                    }
                }
            val nextIndex = currentIntent.getIntExtra("nextGameIndex", 0)
            val currentTotalScore = currentIntent.getIntExtra("totalScore", 0)
            val newTotalScore = currentTotalScore + score

            if (nextIndex < selectedGames.size) {
                val intent = Intent(activity, selectedGames[nextIndex])
                intent.putExtra("nextGameIndex", nextIndex + 1)
                intent.putExtra("selectedGames", selectedGames.map { it.name }.toTypedArray())
                intent.putExtra("totalScore", newTotalScore)
                activity.startActivity(intent)
                activity.finish()
            } else {
                val intent = Intent(activity, ScoreActivity::class.java)
                intent.putExtra("final_score", newTotalScore)
                activity.startActivity(intent)
                activity.finish()
            }
        }
    }
}
