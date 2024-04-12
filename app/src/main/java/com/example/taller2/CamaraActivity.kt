package com.example.taller2

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import android.graphics.Bitmap
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class CamaraActivity : AppCompatActivity() {

    companion object{
        private const val CAMERA_PERMISSION_CODE = 1
        private const val PERMISSION_REQUEST_CAMERA = 2
    }

    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camara)

        val btnGaleria = findViewById<Button>(R.id.btnGaleria)
        val btnCamara = findViewById<Button>(R.id.btnCamara)
        val imagen = findViewById<ImageView>(R.id.imagenCargada)

        activarResultLauncher(imagen)

        btnGaleria.setOnClickListener {

        }

        btnCamara.setOnClickListener {
            permisosCamara()
        }
    }

    private fun permisosCamara(){
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_CODE
            )
        } else {
            abrirCamara()
        }
    }

    private fun abrirCamara(){
        val intentCamara = Intent("android.media.action.IMAGE_CAPTURE")
        activityResultLauncher.launch(intentCamara)
    }

    private fun activarResultLauncher(imagen: ImageView){
        activityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()){ resultado ->
            if(resultado.resultCode == Activity.RESULT_OK){
                val bitMapImagen = resultado.data?.extras?.get("data") as? Bitmap
                if(bitMapImagen != null){
                    imagen.setImageBitmap(bitMapImagen)
                }
            }else{
                Toast.makeText(this, "No se pudo cargar la imagen", Toast.LENGTH_SHORT).show()
            }
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, start the contacts activity
                abrirCamara()
            } else {
                // Permission denied, show a message asking the user to change the setting in the device settings
                Toast.makeText(this, "Permiso no otorgado por el usuario", Toast.LENGTH_SHORT).show()
            }
        }
    }
}