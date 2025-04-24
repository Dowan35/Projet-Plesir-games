package com.example.menu

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.menu.utils.isSocketConnected
import com.example.menu.BluetoothService.Companion.inputStream
import com.example.menu.BluetoothService.Companion.outputStream
import com.example.menu.utils.receiveMessage
import com.example.menu.utils.sendMessage
import com.example.menu.utils.waitForBothReady
import java.util.concurrent.Executors

class Home : AppCompatActivity() {

    private val games = listOf(
        Game1::class.java,
        Game2::class.java,
        Game3::class.java,
        Game4::class.java,
        Game5::class.java,
        Game6::class.java
    )

    private val executor = Executors.newSingleThreadExecutor() // permet de lancer les 3 jeux en meme temps

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.menu_home)

        supportActionBar?.hide()

        val quickPlayButton = findViewById<Button>(R.id.quickPlayButton)
        val trainingButton = findViewById<Button>(R.id.trainingButton)
        val multiPlayerButton = findViewById<Button>(R.id.multiplayerButton)
        val buttonParametre = findViewById<Button>(R.id.buttonParametre)

        quickPlayButton.setOnClickListener {
            launchRandomGames()
        }
        trainingButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        multiPlayerButton.setOnClickListener {
            Log.d("starting", "multijoueur cliqué")
            if (isSocketConnected(BluetoothService.socket)) {
                Log.d("starting", "socket bien connecté")
                if (BluetoothService.isHosting) {//serveur
                    Log.d("starting", "mode serveur")
                    waitForOpponentAndLaunch()
                } else if (BluetoothService.isJoining) {//client
                    Log.d("starting", "mode client")
                    listenForGamesFromHost()
                }

            } else {
                Toast.makeText(this, "Veuillez d'abord vous connecter dans les paramètres", Toast.LENGTH_SHORT).show()
            }
        }

        buttonParametre.setOnClickListener {
            startActivity(Intent(this, Parametre::class.java))
        }
    }

    private fun waitForOpponentAndLaunch() {
        // attend l'autre joueur et lance 3 jeux random
        executor.execute {
            try {

                //if (response == "WAITING") {
                val result = waitForBothReady("WAITING;")
                if (result) {
                    //val selectedGames = games.shuffled().take(3)
                    val selectedGames = listOf(games[0], games[0], games[0]) // test lancer 3 fois le meme jeu

                    val gameNames = selectedGames.map { it.name }
                    val gameMessage = "GAMES:${gameNames.joinToString(",")};"
                    sendMessage(gameMessage, outputStream)//envoie les jeux au client
                    Log.d("Home, serveur", "jeux envoyes : $gameMessage")
                    Thread.sleep(100)
                    runOnUiThread {
                        Log.d("Home, serveur", "demarrage des jeux")
                        startGameList(0, selectedGames, isMultiplayer = true)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    // attends l'autre joueur et recoit la liste des jeux, fonction pour le client
    private fun listenForGamesFromHost() {
        executor.execute {
            try {
                val result = waitForBothReady("WAITING;")
                if (result) {
                    val gamesMessage = receiveMessage(inputStream)?.trimEnd(';')//attends la liste des jeux
                    Log.d("Home, client", "jeux reçu : $gamesMessage")
                    if (gamesMessage?.startsWith("GAMES:") == true) {
                        val gameClassNames = gamesMessage.removePrefix("GAMES:").split(",")
                        val selectedGames = gameClassNames.mapNotNull { className ->
                            try {
                                Class.forName(className)
                            } catch (e: Exception) { null }
                        }

                        runOnUiThread {
                            Log.d("Home, client", "demarrage des jeux")
                            startGameList(0, selectedGames, isMultiplayer = true)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private fun launchRandomGames() {// prepare la liste de 3 jeux et lance: quickplay
        val selectedGames = games.shuffled().take(3)
        startGameList(0, selectedGames, false)
    }

    //lance une activity de x jeux avec x la longueur de la liste, et index le numero du jeu
    private fun startGameList(index: Int, selectedGames: List<Class<*>>, isMultiplayer: Boolean) {//index de jeux, la liste et si cest multiplayer
        if (index >= selectedGames.size) return

        val intent = Intent(this, selectedGames[index])//intent de demarrer le jeu avec l'index fourni
        intent.putExtra("nextGameIndex", index + 1)//incrementation
        intent.putExtra("selectedGames", selectedGames.map { it.name }.toTypedArray())
        intent.putExtra("isMultiplayer",  isMultiplayer)
        intent.putExtra("isQuickPlay",  !isMultiplayer)// si on lance pas en multi, alors cest une quickplay
        intent.putExtra("totalScore", 0) // score total additioné
        startActivity(intent)
    }

    companion object {
        //continue de lancer les jeux suivants apres avoir lancé startGameList, en quickplay solo
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
                intent.putExtra("isQuickPlay", true)
                intent.putExtra("isMultiplayer", false)
                activity.startActivity(intent)
                activity.finish()
            } else {// fin de la liste, ecran de fin
                val intent = Intent(activity, ScoreActivity::class.java)
                intent.putExtra("isQuickPlay", true)
                intent.putExtra("final_score", newTotalScore)
                activity.startActivity(intent)
                activity.finish()
            }
        }

    }
}
