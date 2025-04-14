package com.example.menu

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import kotlin.math.sqrt
import kotlin.random.Random

class GameView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private var onGameOver: (() -> Unit)? = null

    private val caddiePaint = Paint().apply { color = Color.BLUE }
    private val obstaclePaint = Paint().apply { color = Color.RED }

    private var caddieX = 550f   // Position initiale du caddie au centre
    private var caddieY = 950f   // Position fixe en bas de l'écran
    private val caddieRadius = 50f

    private val obstacles = mutableListOf<Pair<Float, Float>>()  // Liste des obstacles
    private val obstacleRadii = mutableListOf<Float>()  // Liste des rayons des obstacles

    private var score = 0
    private var gameOver = false

    private var scoreTextView: TextView? = null

    private val screenWidth = 990f

    private var initialX = 0f

    private val handler = Handler(Looper.getMainLooper())

    // Ajouter des obstacles périodiquement
    private val obstacleRunnable = object : Runnable {
        override fun run() {
            if (!gameOver) {
                // Ajouter un nouvel obstacle avec une taille aléatoire
                val newX = Random.nextFloat() * width
                val newY = 0f
                val newRadius = Random.nextFloat() * 40 + 30
                obstacles.add(Pair(newX, newY))
                obstacleRadii.add(newRadius)

                // Relancer ce runnable après 1 seconde
                handler.postDelayed(this, 3000)
            }
        }
    }

    fun getCurrentScore(): Int {
        return score
    }

    // Runnable pour augmenter le score toutes les secondes
    private val scoreRunnable = object : Runnable {
        override fun run() {
            if (!gameOver) {
                score++  // Incrémenter le score toutes les secondes
                scoreTextView?.text = "Score: $score"
                handler.postDelayed(this, 1000)  // Relancer ce runnable après 1 seconde
            }
        }
    }

    fun setScoreTextView(textView: TextView) {
        this.scoreTextView = textView
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (!gameOver) {
            // Dessiner le caddie
            canvas.drawCircle(caddieX, caddieY, caddieRadius, caddiePaint)

            // Dessiner tous les obstacles
            for (i in obstacles.indices) {
                val (obstacleX, obstacleY) = obstacles[i]
                val obstacleRadius = obstacleRadii[i]
                canvas.drawCircle(obstacleX, obstacleY, obstacleRadius, obstaclePaint)

                // Mettre à jour la position des obstacles
                obstacles[i] = Pair(obstacleX, obstacleY + 10f)

                // Si un obstacle dépasse l'écran, on le réinitialise
                if (obstacleY > height) {
                    obstacles[i] = Pair(Random.nextFloat() * width, 0f)
                    obstacleRadii[i] = Random.nextFloat() * 50 + 30  // Nouvelle taille
                }

                // Vérifier la collision
                val distance = sqrt((caddieX - obstacleX) * (caddieX - obstacleX) +
                        (caddieY - obstacleY) * (caddieY - obstacleY))

                if (distance < caddieRadius + obstacleRadius) {
                    gameOver = true
                    scoreTextView?.text = "Game Over! Score: $score"
                    onGameOver?.invoke()
                }
            }

            postInvalidateDelayed(30)  // Réactualiser la vue tous les 30ms
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            val pointerIndex = it.actionIndex
            when (it.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = it.getX(pointerIndex)
                }
                MotionEvent.ACTION_MOVE -> {
                    val currentX = it.getX(pointerIndex)
                    val deltaX = currentX - initialX

                    if (deltaX > 50) {
                        caddieX = (caddieX + 100).coerceIn(caddieRadius, screenWidth - caddieRadius)
                        initialX = currentX
                    } else if (deltaX < -50) {
                        caddieX = (caddieX - 100).coerceIn(caddieRadius, screenWidth - caddieRadius)
                        initialX = currentX
                    }
                    invalidate()
                }
                else -> { }
            }
        }
        return true
    }

    init {
        // Commencer à ajouter des obstacles au lancement du jeu
        handler.post(obstacleRunnable)

        // Commencer à incrémenter le score chaque seconde
        handler.post(scoreRunnable)
    }

    fun setOnGameOverCallback(callback: () -> Unit) {
        this.onGameOver = callback
    }

}
