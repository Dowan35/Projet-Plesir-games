package com.example.menu

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.viewinterop.AndroidView
import com.example.menu.ui.theme.MenuTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MenuTheme {
                MenuScreen()
            }
        }
    }
}

@Composable
// classe permettant d'afficher le menu d'entrainnement
fun MenuScreen() {
    // Récupère la configuration actuelle de l'appareil
    val configuration = LocalConfiguration.current

    // Affiche en fonction de l'orientation
    AndroidView(
        factory = { context ->
            LayoutInflater.from(context).inflate(R.layout.menu, null).apply {
                // Récupérer chaque bouton par son ID
                val button1: Button = findViewById(R.id.button_option1)
                val button2: Button = findViewById(R.id.button_option2)
                val button3: Button = findViewById(R.id.button_option3)
                val button4: Button = findViewById(R.id.button_option4)
                val button5: Button = findViewById(R.id.button_option5)
                val button6: Button = findViewById(R.id.button_option6)

                // Définir les actions pour chaque bouton
                button1.setOnClickListener {
                    val intent = Intent(context, Game1::class.java)
                    context.startActivity(intent)
                }
                button2.setOnClickListener {
                    val intent = Intent(context, Game2::class.java)
                    context.startActivity(intent)
                }
                button3.setOnClickListener {
                    val intent = Intent(context, Game3::class.java)
                    context.startActivity(intent)
                }
                button4.setOnClickListener {
                    val intent = Intent(context, Game4::class.java)
                    context.startActivity(intent)
                }
                button5.setOnClickListener {
                    val intent = Intent(context, Game5::class.java)
                    context.startActivity(intent)
                }
                button6.setOnClickListener {
                    val intent = Intent(context, Game6::class.java)
                    context.startActivity(intent)
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}
