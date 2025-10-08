package com.example.taller2_icm

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.taller2_icm.databinding.ActivityImagenBinding
import java.io.File

class ImagenActivity : AppCompatActivity() {

    private lateinit var enlace: ActivityImagenBinding
    private lateinit var archivoFoto: File

    // Códigos de solicitud para cámara y galería
    private val CODIGO_SOLICITUD_CAMARA = 100
    private val CODIGO_PERMISO_CAMARA = 200
    private val CODIGO_SOLICITUD_GALERIA = 300
    private val CODIGO_PERMISO_GALERIA = 400

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enlace = ActivityImagenBinding.inflate(layoutInflater)
        setContentView(enlace.root)

        // Botón para abrir la cámara
        enlace.camera.setOnClickListener {
            // Verifica si el permiso de cámara está concedido
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED
            ) {
                // Solicita el permiso si no está concedido
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CAMERA),
                    CODIGO_PERMISO_CAMARA
                )
            } else {
                // Si ya tiene permiso, abre la cámara
                abrirCamara()
            }
        }

        // Botón para abrir la galería
        enlace.gallery.setOnClickListener {
            val permiso = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU)
                Manifest.permission.READ_MEDIA_IMAGES
            else
                Manifest.permission.READ_EXTERNAL_STORAGE

            if (ContextCompat.checkSelfPermission(this, permiso)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(this, arrayOf(permiso), CODIGO_PERMISO_GALERIA)
            } else {
                abrirGaleria()
            }
        }

    }

    // Abre la cámara del sistema
    private fun abrirCamara() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // Crea un archivo temporal para guardar la foto
        archivoFoto = File.createTempFile("foto_", ".jpg", externalCacheDir)
        val uri: Uri = FileProvider.getUriForFile(this, "${packageName}.provider", archivoFoto)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
        startActivityForResult(intent, CODIGO_SOLICITUD_CAMARA)
    }

    // Abre la galería del sistema
    private fun abrirGaleria() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, CODIGO_SOLICITUD_GALERIA)
    }

    // Se ejecuta cuando el usuario responde a la solicitud de permisos
    override fun onRequestPermissionsResult(
        codigoSolicitud: Int,
        permisos: Array<String>,
        resultadosPermiso: IntArray
    ) {
        super.onRequestPermissionsResult(codigoSolicitud, permisos, resultadosPermiso)

        when (codigoSolicitud) {
            CODIGO_PERMISO_CAMARA -> {
                if (resultadosPermiso.isNotEmpty() && resultadosPermiso[0] == PackageManager.PERMISSION_GRANTED) {
                    abrirCamara()
                } else {
                    Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
                }
            }

            CODIGO_PERMISO_GALERIA -> {
                if (resultadosPermiso.isNotEmpty() && resultadosPermiso[0] == PackageManager.PERMISSION_GRANTED) {
                    abrirGaleria()
                } else {
                    Toast.makeText(this, "Permiso de galería denegado", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Recibe los resultados de la cámara o galería
    override fun onActivityResult(codigoSolicitud: Int, codigoResultado: Int, datos: Intent?) {
        super.onActivityResult(codigoSolicitud, codigoResultado, datos)

        if (codigoResultado == Activity.RESULT_OK) {
            when (codigoSolicitud) {
                // Muestra la foto tomada
                CODIGO_SOLICITUD_CAMARA -> {
                    val bitmap = BitmapFactory.decodeFile(archivoFoto.absolutePath)
                    enlace.imageView.setImageBitmap(bitmap)
                }
                // Muestra la imagen seleccionada
                CODIGO_SOLICITUD_GALERIA -> {
                    val uriImagen = datos?.data
                    enlace.imageView.setImageURI(uriImagen)
                }
            }
        }
    }
}
