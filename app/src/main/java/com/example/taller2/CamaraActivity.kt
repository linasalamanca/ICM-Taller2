package com.example.taller2

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class CamaraActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camara)

        val btnGaleria = findViewById<Button>(R.id.btnGaleria)
        val btnCamara = findViewById<Button>(R.id.btnCamara)
        val imagen = findViewById<ImageView>(R.id.imagenCargada)
    }
}