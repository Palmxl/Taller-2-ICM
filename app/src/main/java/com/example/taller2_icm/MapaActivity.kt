package com.example.taller2_icm

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.*

class MapaActivity : AppCompatActivity(), OnMapReadyCallback {

    // Variables principales del mapa y ubicación
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    // Sensor de luz
    private lateinit var sensorManager: SensorManager
    private var lightSensor: Sensor? = null
    private lateinit var lightListener: SensorEventListener

    // Cuadro de búsqueda
    private lateinit var searchBox: EditText

    // Código del permiso
    private val LOCATION_PERMISSION_REQUEST = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapa)

        // Cargar el mapa
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Inicializar ubicación y cuadro de texto
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        searchBox = findViewById(R.id.texto)

        // Activar sensor de luz
        configurarSensorDeLuz()
    }

    // Cuando el mapa está listo
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isZoomGesturesEnabled = true

        iniciarActualizacionUbicacion()
        configurarEventosDelMapa()
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(lightListener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(lightListener)
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    // Configura la petición de ubicación
    private fun crearPeticionUbicacion(): LocationRequest =
        LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
            .setMinUpdateIntervalMillis(5000)
            .build()

    private fun iniciarActualizacionUbicacion() {
        // Revisar permisos
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST
            )
            return
        }

        // Configurar actualizaciones
        locationRequest = crearPeticionUbicacion()
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation ?: return
                val latLng = LatLng(location.latitude, location.longitude)

                // Marcar la ubicación actual
                mMap.clear()
                mMap.addMarker(MarkerOptions().position(latLng).title("Ubicación actual"))
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))

                guardarUbicacion(location)
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        } catch (e: SecurityException) {
            Toast.makeText(this, "Error: sin permisos de ubicación", Toast.LENGTH_LONG).show()
        }
    }

    // Respuesta cuando el usuario da o niega permisos
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                iniciarActualizacionUbicacion()
            else
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_LONG).show()
        }
    }

    // Guarda coordenadas en un archivo JSON
    private fun guardarUbicacion(location: Location) {
        val datos = JSONObject()
        datos.put("latitud", location.latitude)
        datos.put("longitud", location.longitude)
        datos.put("fecha", Date().toString())

        val archivo = File(getExternalFilesDir(null), "ubicaciones.json")
        val jsonArray = if (archivo.exists()) JSONArray(archivo.readText()) else JSONArray()
        jsonArray.put(datos)
        archivo.writeText(jsonArray.toString())
    }

    // Configura el sensor de luz para cambiar el tema del mapa
    private fun configurarSensorDeLuz() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

        if (lightSensor == null) {
            Toast.makeText(this, "Este dispositivo no tiene sensor de luz", Toast.LENGTH_SHORT).show()
            return
        }

        lightListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (!::mMap.isInitialized) return

                val valorLuz = event.values[0]
                val estilo = if (valorLuz < 5000) R.raw.style_dark else R.raw.style_light

                try {
                    mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this@MapaActivity, estilo))
                } catch (_: Exception) {
                    Toast.makeText(this@MapaActivity, "Error al cambiar el estilo del mapa", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
    }

    // Configura eventos del mapa (buscar y clic largo)
    private fun configurarEventosDelMapa() {
        // Buscar dirección con el teclado
        searchBox.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                val texto = searchBox.text.toString()
                val geocoder = Geocoder(this)
                val direcciones = geocoder.getFromLocationName(texto, 1)

                if (!direcciones.isNullOrEmpty()) {
                    val resultado = direcciones[0]
                    val latLng = LatLng(resultado.latitude, resultado.longitude)
                    mMap.addMarker(MarkerOptions().position(latLng).title(texto))
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                }
                true
            } else false
        }

        // Agregar marcador con clic largo
        mMap.setOnMapLongClickListener { latLng ->
            val geocoder = Geocoder(this)
            val direccion = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            val titulo = direccion?.get(0)?.getAddressLine(0) ?: "Ubicación sin nombre"
            mMap.addMarker(MarkerOptions().position(latLng).title(titulo))
            calcularDistancia(latLng)
        }
    }

    // Calcula la distancia entre tu ubicación actual y el punto tocado
    private fun calcularDistancia(destino: LatLng) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this, "Permiso de ubicación requerido", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            fusedLocationClient.lastLocation.addOnSuccessListener {
                if (it != null) {
                    val resultados = FloatArray(1)
                    Location.distanceBetween(it.latitude, it.longitude, destino.latitude, destino.longitude, resultados)
                    Toast.makeText(this, "Distancia: ${resultados[0].toInt()} metros", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: SecurityException) {
            Toast.makeText(this, "Error al obtener la ubicación", Toast.LENGTH_SHORT).show()
        }
    }
}
