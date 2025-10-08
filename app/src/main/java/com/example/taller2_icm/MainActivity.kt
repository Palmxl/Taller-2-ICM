package com.example.taller2_icm

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.taller2_icm.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ajuste automático para evitar solapamiento con las barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // --- Configuración de los botones de navegación ---

        // Ir a ContactosActivity
        binding.btnContactos.setOnClickListener {
            startActivity(Intent(this, ContactosActivity::class.java))
        }

        // Ir a ImagenActivity
        binding.btnImagen.setOnClickListener {
            startActivity(Intent(this, ImagenActivity::class.java))
        }

        // Ir a MapaActivity
        binding.btnMapa.setOnClickListener {
            startActivity(Intent(this, MapaActivity::class.java))
        }
    }
}
