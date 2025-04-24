package com.example.menu.utils

import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.example.menu.ScoreActivity

fun displayMultiplayerEnd(
    activity: Activity,
    myScore: Int,
    otherScore: Int,
    endLayout: LinearLayout,
    multiplayerScoreText: TextView,
    totalScore: Int,
    selectedGames: List<Class<*>>,
    nextGameIndex: Int
) {
    Log.d("displayMultiplayerEnd", "myScore: $myScore, otherScore: $otherScore")
    multiplayerScoreText.text = "Ton score : $myScore\nScore adversaire : $otherScore"

    val newTotalScore = totalScore + myScore

    var mediaPlayer: MediaPlayer? = null
    if (myScore > otherScore) {
        mediaPlayer = MediaPlayer.create(activity, com.example.menu.R.raw.congratulations_music)
        mediaPlayer?.start()
    }

    endLayout.visibility = View.VISIBLE
    endLayout.bringToFront() // vues se chevauchent
    endLayout.invalidate()   // Redessine
    endLayout.requestLayout() // Force une mise à jour

    Thread.sleep(100)

    multiplayerScoreText.postDelayed({
        if (nextGameIndex < selectedGames.size) {
            val intent = Intent(activity, selectedGames[nextGameIndex])
            intent.putExtra("nextGameIndex", nextGameIndex + 1)
            intent.putExtra("selectedGames", selectedGames.map { it.name }.toTypedArray())
            intent.putExtra("totalScore", newTotalScore)
            intent.putExtra("isMultiplayer", true)
            intent.putExtra("isQuickPlay", false)
            Log.d("displayMultiplayerEnd", "fin du game, lancement du prochain")
            activity.startActivity(intent)
        } else {
            val intent = Intent(activity, ScoreActivity::class.java)
            intent.putExtra("myFinalScore", newTotalScore)
            intent.putExtra("isMultiplayer", true)
            intent.putExtra("isQuickPlay", false)
            Log.d("displayMultiplayerEnd", "la fin de la fin")
            activity.startActivity(intent)
        }
        mediaPlayer?.stop()
        mediaPlayer?.release()
        activity.finish()
    }, 4000)
}