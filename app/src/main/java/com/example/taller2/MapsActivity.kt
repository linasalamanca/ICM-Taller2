package com.example.taller2

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.taller2.databinding.ActivityMapaBinding
import org.json.JSONArray
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.TilesOverlay
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.io.Writer
import java.util.Date
import kotlin.math.roundToInt
import java.util.Locale
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.OverlayWithIW
import android.view.MotionEvent
import org.osmdroid.views.MapView


class MapsActivity : AppCompatActivity() {

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
    private lateinit var bindingMapa: ActivityMapaBinding
    private lateinit var sensorManejador: SensorManager
    private lateinit var sensorLuz: Sensor
    private lateinit var sensorLuzListener: SensorEventListener

    private var latitud: Double = 0.0
    private var longitud: Double = 0.0
    val RADIUS_OF_EARTH_KM = 6371.0
    private val startPoint = org.osmdroid.util.GeoPoint(4.628593, -74.065041)

    override fun onCreate(savedInstanceState: Bundle?) {
        //Sensores
        sensorManejador = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorLuz = sensorManejador.getDefaultSensor(Sensor.TYPE_LIGHT)!!
        sensorLuzListener = createLightSensorListener()
        sensorManejador.registerListener(
            sensorLuzListener,
            sensorLuz,
            SensorManager.SENSOR_DELAY_FASTEST
        )

        //OSM
        val ctx = applicationContext
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))
        Configuration.getInstance().userAgentValue = packageName

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapa)
        bindingMapa = ActivityMapaBinding.inflate(layoutInflater)
        setContentView(bindingMapa.root)

        bindingMapa.osmMap.setTileSource(TileSourceFactory.MAPNIK)
        bindingMapa.osmMap.setMultiTouchControls(true)

        permisos()
        val addressInput = findViewById<EditText>(R.id.addressInput)
        addressInput.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val location = getLocationFromAddress(v.text.toString())
                location?.let {
                    actualizarMarcador(it.latitude, it.longitude)
                    bindingMapa.osmMap.controller.animateTo(GeoPoint(it.latitude, it.longitude))
                    bindingMapa.osmMap.controller.setZoom(18.0)
                }
                true  // Consumir el evento aquí
            } else {
                false  // Dejar que otros manejadores procesen el evento
            }
        }
        bindingMapa.osmMap.overlays.add(object : Overlay() {
            override fun onLongPress(e: MotionEvent?, mapView: MapView?): Boolean {
                e?.let {
                    val projection = mapView?.projection
                    val geoPoint = projection?.fromPixels(e.x.toInt(), e.y.toInt()) as GeoPoint
                    getAddressFromLocation(geoPoint.latitude, geoPoint.longitude, mapView.context)
                    return true
                }
                return false
            }
        })
    }

    //Sensores de luz

    private fun createLightSensorListener(): SensorEventListener {
        return object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (bindingMapa.osmMap != null) {
                    if (event.values[0] < 3000) {
                        Log.i("MAPS", "DARK MAP " + event.values[0])
                        bindingMapa.osmMap.overlayManager.tilesOverlay.setColorFilter(TilesOverlay.INVERT_COLORS)
                    } else {
                        Log.i("MAPS", "LIGHT MAP " + event.values[0])
                        bindingMapa.osmMap.overlayManager.tilesOverlay.setColorFilter(null)
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }
    }

    //Ubicación del emulador

    private fun permisos(){
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            ubicacionActual()
        }
    }

    @SuppressLint("MissingPermission")
    private fun ubicacionActual() {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000L, 10f, locationListener)
    }

    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            Log.d("MapsActivity", "Ubicación actualizada: $location")

            var latitudPasada = latitud
            var longitudPasada = longitud
            latitud = location.latitude
            longitud = location.longitude
            Log.i("LISTENER", "Latitud: $latitud y Longitud: $longitud")
            actualizarMarcador(latitud, longitud)
            val dist = distancia(latitudPasada, longitudPasada, latitud, longitud)
            Log.d("DISTANCIA", "Distancia calculada: $dist")
            if (dist > 0.3)
                escribirJSON(latitud, longitud)
        }
    }

    private fun distancia(lat1: Double, long1: Double, lat2: Double, long2: Double): Double {
        val latDistance = Math.toRadians(lat1 - lat2)
        val lngDistance = Math.toRadians(long1 - long2)
        val a = (Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2))
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        val result = RADIUS_OF_EARTH_KM * c
        return (result * 100.0).roundToInt() / 100.0
    }

    private var marcador: Marker? = null
    private fun actualizarMarcador(latitud: Double, longitud: Double) {
        if (marcador != null){
            marcador?.let { bindingMapa.osmMap.overlays.remove(it)}
        }

        val punto = GeoPoint(latitud, longitud)
        marcador = Marker(bindingMapa.osmMap).apply{
            icon = cambioTamañoIcono(resources.getDrawable(R.drawable.ubicacion))
            position = punto
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        }

        bindingMapa.osmMap.overlays.add(marcador)

        val mapController: IMapController = bindingMapa.osmMap.controller
        mapController.animateTo(punto)
        mapController.setZoom(18.0)
    }

    private fun cambioTamañoIcono(icono: Drawable): Drawable {
        val bitmap = (icono as BitmapDrawable).bitmap
        val bitmapCambiado = Bitmap.createScaledBitmap(bitmap, 50, 50, false)
        return BitmapDrawable(resources, bitmapCambiado)
    }

    private fun escribirJSON(latitud: Double, longitud: Double){
        val localizaciones: JSONArray = JSONArray()
        localizaciones.put(Ubicacion(
            Date(System.currentTimeMillis()), latitud, longitud).toJSON())
        var output: Writer?
        val filename = "locations.json"
        try {
            val file = File(baseContext.getExternalFilesDir(null), filename)
            Log.i("ARCHIVO", "Ubicacion de archivo: $file")
            output = BufferedWriter(FileWriter(file))
            output.write(localizaciones.toString())
            output.close()
            Toast.makeText(applicationContext, "Archivo escrito", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Log.e("ARCHIVO", "Error al escribir en el archivo", e)
            }
     }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            ubicacionActual()
        }else{

        }
    }
    override fun onResume() {
        super.onResume()
        bindingMapa.osmMap.onResume()
        val mapController: IMapController = bindingMapa.osmMap.controller
        mapController.setZoom(18.0)
        mapController.setCenter(this.startPoint)
    }

    override fun onPause() {
        super.onPause()
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        locationManager.removeUpdates(locationListener)
        bindingMapa.osmMap.onPause()
    }

    private fun getLocationFromAddress(strAddress: String): Location? {
        val geocoder = Geocoder(this)
        val addressList = geocoder.getFromLocationName(strAddress, 1)
        if (addressList != null && addressList.isNotEmpty()) {
            val address = addressList[0]
            val location = Location(LocationManager.GPS_PROVIDER)
            location.latitude = address.latitude
            location.longitude = address.longitude
            Log.d("Geocoder", "Dirección encontrada: Lat=${address.latitude}, Long=${address.longitude}")
            return location
        } else {
            Log.d("Geocoder", "No se encontraron direcciones")
            return null
        }
    }

    private fun getAddressFromLocation(latitude: Double, longitude: Double, context: Context) {
        val geocoder = Geocoder(context, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses?.isNotEmpty() == true) {
                val address = addresses[0].getAddressLine(0)
                runOnUiThread {
                    addMarker(latitude, longitude, address)
                }
            } else {
                Log.d("MapsActivity", "No address found.")
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("MapsActivity", "Geocoder failed", e)
        }
    }

    private fun addMarker(latitude: Double, longitude: Double, address: String) {
        val marker = Marker(bindingMapa.osmMap)
        marker.position = GeoPoint(latitude, longitude)
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.title = address
        bindingMapa.osmMap.overlays.add(marker)
        bindingMapa.osmMap.invalidate()
    }

}