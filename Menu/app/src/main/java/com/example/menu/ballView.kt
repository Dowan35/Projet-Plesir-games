package com.example.menu

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View

class BallView(context: Context, val onWinCallback: () -> Unit) : View(context) {

    private val paint = Paint()
    private val cellSize = 100f
    private val ballRadius = 25f

    // Labyrinthe (0 = libre, 1 = mur)
    private val maze = arrayOf(
        intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
        intArrayOf(1, 0, 0, 0, 1, 0, 0, 0, 0, 1),
        intArrayOf(1, 1, 1, 0, 1, 0, 1, 1, 0, 1),
        intArrayOf(1, 0, 1, 0, 1, 0, 1, 0, 0, 1),
        intArrayOf(1, 0, 0, 0, 1, 0, 1, 0, 1, 1),
        intArrayOf(1, 0, 1, 1, 1, 1, 1, 0, 1, 1),
        intArrayOf(1, 0, 0, 0, 0, 0, 1, 0, 1, 1),
        intArrayOf(1, 1, 1, 0, 1, 1, 1, 0, 1, 1),
        intArrayOf(1, 0, 0, 0, 0, 0, 0, 0, 0, 1),
        intArrayOf(1, 0, 0, 1, 1, 1, 0, 1, 1, 1),
        intArrayOf(1, 0, 1, 1, 1, 0, 0, 1, 1, 1),
        intArrayOf(1, 0, 0, 0, 0, 0, 0, 0, 0, 1),
        intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
    )

    private var ballX = 1 * cellSize + cellSize / 2
    private var ballY = 1 * cellSize + cellSize / 2

    // Vitesse de la balle
    private var ballSpeedX = 5f
    private var ballSpeedY = 5f

    private val exitPositions = listOf(
        Pair(4, 5),
        Pair(11, 8)
    )

    // Choix aléatoire de la position de sortie
    private var exitRow = 0
    private var exitCol = 0

    init {
        // Choisir une position de sortie au lancement
        val (row, col) = exitPositions.random()
        exitRow = row
        exitCol = col
    }

    // Modifier cette méthode dans BallView
    fun updatePosition(dx: Float, dy: Float) {
        val newX = ballX + dx * 2
        val newY = ballY + dy * 2

        // Bords de la balle
        val left = newX - ballRadius
        val right = newX + ballRadius
        val top = newY - ballRadius
        val bottom = newY + ballRadius

        // Indices des cellules touchées
        val leftCol = (left / cellSize).toInt()
        val rightCol = (right / cellSize).toInt()
        val topRow = (top / cellSize).toInt()
        val bottomRow = (bottom / cellSize).toInt()

        val inBounds =
            topRow >= 0 && bottomRow < maze.size &&
                    leftCol >= 0 && rightCol < maze[0].size

        // Vérifier si la balle touche un mur
        if (inBounds &&
            (maze[topRow][leftCol] == 1 || maze[topRow][rightCol] == 1 ||
                    maze[bottomRow][leftCol] == 1 || maze[bottomRow][rightCol] == 1)) {
            // Si la balle touche un mur, inverser sa direction
            if (maze[topRow][leftCol] == 1 || maze[bottomRow][leftCol] == 1) {
                ballSpeedX = -ballSpeedX // Rebond sur le mur gauche/droit
            }
            if (maze[topRow][rightCol] == 1 || maze[bottomRow][rightCol] == 1) {
                ballSpeedY = -ballSpeedY // Rebond sur le mur haut/bas
            }
        } else {
            // Si aucun mur, mettre à jour la position
            ballX = newX
            ballY = newY
        }

        invalidate()
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.WHITE)

        // Dessine le labyrinthe
        for (row in maze.indices) {
            for (col in maze[row].indices) {
                val left = col * cellSize
                val top = row * cellSize
                val right = left + cellSize
                val bottom = top + cellSize

                if (maze[row][col] == 1) {
                    paint.color = Color.DKGRAY
                    canvas.drawRect(left, top, right, bottom, paint)
                } else if (row == exitRow && col == exitCol) {
                    paint.color = Color.GREEN
                    canvas.drawRect(left, top, right, bottom, paint)
                }
            }
        }

        // Balle
        paint.color = Color.RED
        canvas.drawCircle(ballX, ballY, ballRadius, paint)

        // Gagné ?
        val currentRow = (ballY / cellSize).toInt()
        val currentCol = (ballX / cellSize).toInt()
        if (currentRow == exitRow && currentCol == exitCol) {
            onWinCallback()
        }
    }
}
