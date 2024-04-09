package com.example.taller2
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapaActivity : AppCompatActivity(), OnMapReadyCallback {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapa)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        // Personaliza el mapa aquí, por ejemplo, estableciendo una ubicación inicial
        val location = LatLng(-34.0, 151.0) // Cambia esto a tu ubicación deseada
        googleMap.addMarker(MarkerOptions().position(location).title("Marker Title"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(location))
    }
}
