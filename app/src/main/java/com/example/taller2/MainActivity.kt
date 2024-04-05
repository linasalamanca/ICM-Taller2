package com.example.taller2

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    companion object {
        private const val CONTACTS_PERMISSION_CODE = 1
        private const val PERMISSION_REQUEST_CONTACTS = 2
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val BTNContactos = findViewById<ImageButton>(R.id.btnContactos)
        val BTNCamara = findViewById<ImageButton>(R.id.btnCamara)
        val BTNMapa = findViewById<ImageButton>(R.id.btnMapa)

        BTNContactos.setOnClickListener {solicitarPermisos()}

    }
    fun solicitarPermisos(){
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_CONTACTS),
                CONTACTS_PERMISSION_CODE
            )
        } else {
            // Permission already granted, proceed with the contacts activity
            val intent = Intent(this, ContactosActivity::class.java)
            startActivity(intent)
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CONTACTS_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, start the contacts activity
                val intent = Intent(this, ContactosActivity::class.java)
                startActivity(intent)
            } else {
                // Permission denied, show a message asking the user to change the setting in the device settings
                showPermissionDeniedDialog()
            }
        }
    }

    // Function to show a dialog when permission is denied
    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permission Denied")
            .setMessage("Without contacts permission, the app cannot function properly. Do you want to change this in settings?")
            .setPositiveButton("Settings") { _, _ ->
                // Open app settings
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri: Uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

}