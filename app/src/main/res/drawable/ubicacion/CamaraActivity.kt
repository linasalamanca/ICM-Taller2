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
import androidx.activity.result.ActivityResultLauncher
import android.graphics.Bitmap
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide

class CamaraActivity : AppCompatActivity() {

    companion object{
        private const val CAMERA_PERMISSION_CODE = 1
        private const val GALERY_PERMISSION_CODE = 2
    }

    private lateinit var activityResultLauncherCamara: ActivityResultLauncher<Intent>
    private lateinit var activityResultLauncherGaleria: ActivityResultLauncher<Intent>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camara)

        val btnGaleria = findViewById<Button>(R.id.btnGaleria)
        val btnCamara = findViewById<Button>(R.id.btnCamara)
        val imagen = findViewById<ImageView>(R.id.imagenCargada)

        activarResultLauncherCamara(imagen)
        activarResultLauncherGaleria(imagen)

        btnGaleria.setOnClickListener {
            permisosGaleria()
        }

        btnCamara.setOnClickListener {
            permisosCamara()
        }
    }

    //Código relacionado a los permisos

    private fun permisosGaleria(){
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                GALERY_PERMISSION_CODE
            )
        } else {
            abrirGaleria()
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            CAMERA_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, start the contacts activity
                    abrirCamara()
                } else {
                    // Permission denied, show a message asking the user to change the setting in the device settings
                    Toast.makeText(this, "Permiso no otorgado por el usuario", Toast.LENGTH_SHORT).show()
                }
            }
            GALERY_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, start the contacts activity
                    abrirGaleria()
                } else {
                    // Permission denied, show a message asking the user to change the setting in the device settings
                    Toast.makeText(this, "Permiso no otorgado por el usuario", Toast.LENGTH_SHORT).show()
                }
            }

        }
    }

    //Código relacionado a la cámara

    private fun abrirCamara(){
        val intentCamara = Intent("android.media.action.IMAGE_CAPTURE")
        activityResultLauncherCamara.launch(intentCamara)
    }

    private fun activarResultLauncherCamara(imagen: ImageView){
        activityResultLauncherCamara = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()){ resultado ->
            if(resultado.resultCode == Activity.RESULT_OK){
                val bitMapImagen = resultado.data?.extras?.get("data") as? Bitmap
                if(bitMapImagen != null){
                    //imagen.setImageBitmap(bitMapImagen)
                    Glide.with(this).load(bitMapImagen).into(imagen)
                }
            }else{
                Toast.makeText(this, "No se pudo cargar la imagen", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //Código relacionado a la galería
    private fun abrirGaleria(){
        val intentGaleria = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        activityResultLauncherGaleria.launch(intentGaleria)
    }

    private fun activarResultLauncherGaleria(imagen: ImageView){
        activityResultLauncherGaleria = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()){ resultado ->
            if(resultado.resultCode == Activity.RESULT_OK){
                val uri = resultado.data?.data
                //imagen.setImageURI(uri)
                Glide.with(this).load(uri).into(imagen)
            }else{
                Toast.makeText(this, "No se pudo cargar la imagen", Toast.LENGTH_SHORT).show()
            }
        }
    }
}