package com.example.menu

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import android.os.Handler
import android.os.Looper
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

data class AnimalQuestion(
    val zoomedImageResId: Int,
    val fullImageResId: Int,
    val options: List<String>,
    val correctAnswer: String
)

class Game5 : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var scoreText: TextView
    private lateinit var feedbackText: TextView
    private lateinit var buttons: List<Button>

    private var mode: String? = null  // ← Pour savoir le mode
    private var currentQuestionIndex = 0
    private var score = 0
    private val handler = Handler(Looper.getMainLooper())

    private val questions = listOf(
        AnimalQuestion(R.drawable.ours1, R.drawable.ours, listOf("Ours", "Chien", "Elan", "Sanglier"), "Ours"),
        AnimalQuestion(R.drawable.tigre1, R.drawable.tigre, listOf("Zebre", "Tigre", "Okapi", "Lémurien"), "Tigre"),
        AnimalQuestion(R.drawable.beluga1, R.drawable.beluga, listOf("Beluga", "Dauphin", "Chauve-souris", "Huitre"), "Beluga"),
        AnimalQuestion(R.drawable.fourmilier1, R.drawable.fourmilier, listOf("Elephant", "Serpent", "Bousier", "Fourmilier"), "Fourmilier"),
        AnimalQuestion(R.drawable.nasique1, R.drawable.nasique, listOf("Orang-outan", "Gélada", "Nasique", "Babouin"), "Nasique")
    ).shuffled()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.game5)

        mode = intent.getStringExtra("mode") // ← On récupère le mode

        imageView = findViewById(R.id.animalImage)
        scoreText = findViewById(R.id.scoreText)
        feedbackText = findViewById(R.id.feedbackText)

        buttons = listOf(
            findViewById(R.id.answerButton1),
            findViewById(R.id.answerButton2),
            findViewById(R.id.answerButton3),
            findViewById(R.id.answerButton4)
        )

        loadQuestion()
    }

    private fun loadQuestion() {
        val question = questions[currentQuestionIndex]
        imageView.setImageResource(question.zoomedImageResId)
        feedbackText.text = ""
        buttons.forEachIndexed { index, button ->
            button.text = question.options[index]
            button.isEnabled = true
            button.setOnClickListener { checkAnswer(button.text.toString()) }
        }
    }

    private fun checkAnswer(selected: String) {
        val question = questions[currentQuestionIndex]
        val isCorrect = selected.equals(question.correctAnswer, ignoreCase = true)

        if (isCorrect) {
            score++
            feedbackText.text = "Bonne réponse !"
        } else {
            feedbackText.text = "Raté ! C'était : ${question.correctAnswer}"
        }

        scoreText.text = "Score : $score"

        imageView.setImageResource(question.fullImageResId)
        buttons.forEach { it.isEnabled = false }

        handler.postDelayed({
            currentQuestionIndex++
            if (currentQuestionIndex < questions.size) {
                loadQuestion()
            } else {
                feedbackText.text = "Quizz terminé ! Score final : $score"
                goNext()
            }
        }, 2000)
    }

    private fun goNext() {
        handler.postDelayed({
            if (mode == "entrainement") {
                startActivity(Intent(this, Home::class.java))
            } else {
                Home.continueQuickPlay(this,score)
            }
            finish()
        }, 2000)
    }
}