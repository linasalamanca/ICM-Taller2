package com.example.taller2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.ListView

class ContactosActivity : AppCompatActivity() {
    private lateinit var listView: ListView
    private lateinit var contactosAdapter: ContactosAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contactos)

        listView = findViewById(R.id.listView)

        // Query the contacts database and get a cursor
        val cursor = contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            arrayOf(
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME
            ),
            null,
            null,
            null
        )

        // Initialize the adapter with the cursor
        contactosAdapter = ContactosAdapter(this, cursor, 0)

        // Set the adapter to the ListView
        listView.adapter = contactosAdapter
    }
}