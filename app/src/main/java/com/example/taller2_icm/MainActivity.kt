package com.example.taller2_icm

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // --- Configuración de los botones de navegación ---
        val btnContactos = findViewById<ImageButton>(R.id.btnContactos)
        val btnImagen = findViewById<ImageButton>(R.id.btnImagen)
        val btnMapa = findViewById<ImageButton>(R.id.btnMapa)

        // Ir a ContactosActivity
        btnContactos.setOnClickListener {
            startActivity(Intent(this, ContactosActivity::class.java))
        }

        // Ir a ImagenActivity
        btnImagen.setOnClickListener {
            startActivity(Intent(this, ImagenActivity::class.java))
        }

        // Ir a MapaActivity
        btnMapa.setOnClickListener {
            startActivity(Intent(this, MapaActivity::class.java))
        }
    }
}
