package com.example.menu

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible

class Game3 : AppCompatActivity() {

    data class Question(
        val text: String,
        val answers: List<String>,
        val correctAnswerIndex: Int
    )

    private val questions = listOf(
        Question("Un train électrique va de nord à sud. Où va la fumée ?", listOf("Nord", "Sud", "Est", "Aucun"), 3),
        Question("Quelle est la couleur du cheval blanc d'Henri IV ?", listOf("Noir", "Gris", "Blanc", "Marron"), 2),
        Question("Combien de mois ont 28 jours ?", listOf("1", "2", "12", "0"), 2),
        Question("Le père de Paul a 4 fils : Jean, Jacques, Jules… et ?", listOf("Jean", "Jacques", "Jules", "Paul"), 3),
        Question("Combien d’animaux de chaque espèce Moïse a-t-il pris dans son arche ?", listOf("2", "4", "Aucun", "1"), 2),
        Question("Tu participes à une course et tu doubles le 2ᵉ. À quelle place es-tu ?", listOf("1er", "2ᵉ", "3ᵉ", "Dernier"), 1)
    ).shuffled()


    private var currentQuestionIndex = 0
    private var score = 0
    private lateinit var timer: CountDownTimer

    private lateinit var questionText: TextView
    private lateinit var feedbackText: TextView
    private lateinit var scoreText: TextView
    private lateinit var timerText: TextView
    private lateinit var buttons: List<Button>

    private lateinit var mode: String  // Pour savoir si on est en mode "quick_play" ou "training"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.game3)

        // Récupérer l'extra "mode" pour savoir si c'est une partie rapide ou un entraînement
        mode = intent.getStringExtra("mode") ?: "training"  // Valeur par défaut "training" si pas spécifié

        questionText = findViewById(R.id.questionText)
        feedbackText = findViewById(R.id.feedbackText)
        scoreText = findViewById(R.id.scoreText)
        timerText = findViewById(R.id.timerText)

        buttons = listOf(
            findViewById(R.id.answerButton1),
            findViewById(R.id.answerButton2),
            findViewById(R.id.answerButton3),
            findViewById(R.id.answerButton4)
        )

        loadQuestion()

        buttons.forEachIndexed { index, button ->
            button.setOnClickListener {
                timer.cancel()
                checkAnswer(index)
            }
        }
    }

    private fun loadQuestion() {
        feedbackText.text = ""
        val question = questions[currentQuestionIndex]
        questionText.text = question.text
        scoreText.text = "Score : $score"
        buttons.forEachIndexed { index, button ->
            button.text = question.answers[index]
            button.isEnabled = true
        }

        startTimer()
    }

    private fun startTimer() {
        timer = object : CountDownTimer(7000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timerText.text = "Temps restant : ${millisUntilFinished / 1000}s"
            }

            override fun onFinish() {
                feedbackText.text = "Temps écoulé"
                proceedToNext()
            }
        }
        timer.start()
    }

    private fun checkAnswer(selectedIndex: Int) {
        val correct = questions[currentQuestionIndex].correctAnswerIndex
        if (selectedIndex == correct) {
            feedbackText.text = "Bonne réponse !"
            score++
        } else {
            feedbackText.text = "Mauvaise réponse"
        }
        proceedToNext()
    }

    private fun proceedToNext() {
        buttons.forEach { it.isEnabled = false }

        feedbackText.postDelayed({
            currentQuestionIndex++
            if (currentQuestionIndex < questions.size) {
                loadQuestion()
            } else {
                showFinalScore()
            }
        },2000)
    }

    private fun showFinalScore() {
        questionText.text = "Quiz terminé !"
        feedbackText.text = "Score final : $score/${questions.size}"
        timerText.text = ""
        scoreText.text = ""
        buttons.forEach { it.isVisible = false }

        feedbackText.postDelayed({
            if (mode == "entrainement") {
                startActivity(Intent(this, Home::class.java))
            } else {
                Home.continueQuickPlay(this,score)
            }
            finish()
        }, 2000)
    }
}
