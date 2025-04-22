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
import kotlin.math.pow

class GameView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private var onGameOver: (() -> Unit)? = null

    private val caddiePaint = Paint().apply { color = Color.BLUE }
    private val obstaclePaint = Paint().apply { color = Color.RED }

    private var caddieX = 550f
    private var caddieY = 950f
    private val caddieRadius = 50f

    private val obstacles = mutableListOf<Pair<Float, Float>>()
    private val obstacleRadii = mutableListOf<Float>()

    private var score = 0
    private var gameOver = false

    private var scoreTextView: TextView? = null

    private val screenWidth = 990f
    private var initialX = 0f

    private val handler = Handler(Looper.getMainLooper())

    private val obstacleRunnable = object : Runnable {
        override fun run() {
            if (!gameOver) {
                val newX = Random.nextFloat() * width
                val newY = 0f
                val newRadius = Random.nextFloat() * 40 + 30
                obstacles.add(Pair(newX, newY))
                obstacleRadii.add(newRadius)
                handler.postDelayed(this, 3000)
            }
        }
    }

    private val scoreRunnable = object : Runnable {
        override fun run() {
            if (!gameOver) {
                score++
                scoreTextView?.text = "Score: $score"
                handler.postDelayed(this, 1000)
            }
        }
    }

    fun startGame() {
        score = 0
        gameOver = false
        obstacles.clear()
        obstacleRadii.clear()
        caddieX = 550f

        scoreTextView?.text = "Score: $score"
        handler.post(obstacleRunnable)
        handler.post(scoreRunnable)

        invalidate()
    }

    fun getCurrentScore(): Int = score

    fun setScoreTextView(textView: TextView) {
        this.scoreTextView = textView
    }

    fun setOnGameOverCallback(callback: () -> Unit) {
        this.onGameOver = callback
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (!gameOver) {
            canvas.drawCircle(caddieX, caddieY, caddieRadius, caddiePaint)

            for (i in obstacles.indices) {
                val (obstacleX, obstacleY) = obstacles[i]
                val obstacleRadius = obstacleRadii[i]

                canvas.drawCircle(obstacleX, obstacleY, obstacleRadius, obstaclePaint)
                obstacles[i] = Pair(obstacleX, obstacleY + 10f)

                if (obstacleY > height) {
                    obstacles[i] = Pair(Random.nextFloat() * width, 0f)
                    obstacleRadii[i] = Random.nextFloat() * 50 + 30
                }

                val distance = sqrt((caddieX - obstacleX).pow(2) + (caddieY - obstacleY).pow(2))
                if (distance < caddieRadius + obstacleRadius) {
                    gameOver = true
                    scoreTextView?.text = "Game Over! Score: $score"
                    onGameOver?.invoke()
                }
            }

            postInvalidateDelayed(30)
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
            }
        }
        return true
    }

    private fun Float.pow(power: Int): Float = this.toDouble().pow(power).toFloat()
}
