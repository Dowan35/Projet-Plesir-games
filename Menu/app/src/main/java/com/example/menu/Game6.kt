package com.example.menu

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class Game6 : AppCompatActivity() {

    private lateinit var container: FrameLayout
    private lateinit var scoreText: TextView
    private lateinit var feedbackText: TextView
    private lateinit var reactionButton: Button

    private val handler = Handler(Looper.getMainLooper())
    private var score = 0
    private var round = 0
    private val totalRounds = 10
    private var buttonVisibleTime: Long = 0
    private var gameMode: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.game6)
        supportActionBar?.hide()

        container = findViewById(R.id.reactionContainer)
        scoreText = findViewById(R.id.scoreText)
        feedbackText = findViewById(R.id.feedbackText)
        reactionButton = Button(this).apply {
            text = "TOUCHE !"
            setOnClickListener { checkReaction() }
        }

        gameMode = intent.getStringExtra("gameMode")

        startNextRound()
    }

    private fun startNextRound() {
        feedbackText.text = ""
        handler.postDelayed({
            showButtonRandomly()
        }, Random.nextLong(1000, 2000)) // délai aléatoire pour éviter d’anticiper
    }

    private fun showButtonRandomly() {
        val size = 250
        val layoutParams = FrameLayout.LayoutParams(size, size)
        val maxX = container.width - size
        val maxY = container.height - size

        layoutParams.leftMargin = Random.nextInt(0, if (maxX > 0) maxX else 1)
        layoutParams.topMargin = Random.nextInt(0, if (maxY > 0) maxY else 1)

        container.removeAllViews()
        container.addView(reactionButton, layoutParams)

        buttonVisibleTime = System.currentTimeMillis()

        // Disparaît automatiquement après 1s
        handler.postDelayed({
            if (container.indexOfChild(reactionButton) != -1) {
                container.removeAllViews()
                feedbackText.text = "⏱ Trop lent !"
                proceed(false)
            }
        }, 1000)
    }

    private fun checkReaction() {
        val reactionTime = System.currentTimeMillis() - buttonVisibleTime
        container.removeAllViews()

        if (reactionTime <= 1000) {
            score++
            feedbackText.text = "Bien joué ! (${reactionTime}ms)"
        } else {
            feedbackText.text = "Trop lent ! (${reactionTime}ms)"
        }

        proceed(true)
    }

    private fun proceed(clicked: Boolean) {
        round++
        scoreText.text = "Score : $score / $round"

        if (round < totalRounds) {
            handler.postDelayed({ startNextRound() }, 1000)
        } else {
            endGame()
        }
    }

    private fun endGame() {
        feedbackText.text = "Score final : $score / $totalRounds"
        scoreText.text = ""
        handler.postDelayed({
            when (gameMode) {
                "entrainement" -> {
                    startActivity(Intent(this, Home::class.java))
                    finish()
                }
                else -> {
                    Home.continueQuickPlay(this,score)
                }
            }
        }, 2000)
    }
}
