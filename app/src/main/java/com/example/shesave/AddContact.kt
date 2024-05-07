package com.example.shesave

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class AddContact : AppCompatActivity() {

    companion object {
        private const val CONTACT_PICK_REQUEST = 1
        private const val CONTACTS_PERMISSION_REQUEST_CODE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_add_contact)

        val imgBack = findViewById<ImageButton>(R.id.imgBack)
        val imgContacts = findViewById<ImageButton>(R.id.imgContacts)
        val edtName = findViewById<EditText>(R.id.edtName)
        val edtNumber = findViewById<EditText>(R.id.edtNumber)
        val btnSave = findViewById<Button>(R.id.btnSave)

        imgBack.setOnClickListener {
            val intent = Intent(this, Contacts::class.java)
            startActivity(intent)
        }

        imgContacts.setOnClickListener {
            checkContactsPermission()
        }

        btnSave.setOnClickListener {
            if (edtName.text.toString().isNotEmpty() && edtNumber.text.toString().isNotEmpty()) {
                if (edtNumber.text.toString().length == 10) {
                    if (edtNumber.text.toString().startsWith("3")) {
                        save(edtName.text.toString(), edtNumber.text.toString())
                    } else {
                        Toast.makeText(
                            this,
                            "El numero debe comenzar con el digito 3",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    Toast.makeText(this, "El numero debe tener 10 digitos", Toast.LENGTH_LONG)
                        .show()
                }
            } else {
                Toast.makeText(this, "Debes llenar todos los campos", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun openContacts() {
        val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
        startActivityForResult(intent, CONTACT_PICK_REQUEST)
    }

    private fun save(edtName: String, edtNumber: String) {
        val newContact = Contact(edtName, edtNumber)
        val sharedPreferences = getSharedPreferences("SHARED_PREFS", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("CONTACT_LIST", null)
        val type = object : TypeToken<MutableList<Contact>>() {}.type
        val contacts =
            if (json != null) gson.fromJson<MutableList<Contact>>(json, type) else mutableListOf()
        contacts.add(newContact)
        val editor = sharedPreferences.edit()
        editor.putString("CONTACT_LIST", gson.toJson(contacts))
        editor.apply()
        Toast.makeText(this, "El contacto se ha guardado exitosamente", Toast.LENGTH_SHORT).show()
        onBackPressed()
    }

    private fun checkContactsPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_CONTACTS),
                CONTACTS_PERMISSION_REQUEST_CODE
            )
        } else {
            openContacts()
        }
    }

    private fun getContactPhoneNumber(cursor: android.database.Cursor): String {
        val contactId =
            cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
        val phoneCursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
            arrayOf(contactId),
            null
        )
        var phoneNumber = ""
        if (phoneCursor?.moveToFirst() == true) {
            phoneNumber =
                phoneCursor.getString(phoneCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
        }
        phoneCursor?.close()
        return phoneNumber
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CONTACTS_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openContacts()
            } else {
                Toast.makeText(
                    this,
                    "Permiso denegado, no se puede acceder a los contactos",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CONTACT_PICK_REQUEST && resultCode == RESULT_OK) {
            val contactData = data?.data
            val cursor = contentResolver.query(contactData!!, null, null, null, null)
            if (cursor?.moveToFirst() == true) {
                val name =
                    cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME))
                val number = getContactPhoneNumber(cursor)
                cursor.close()
                save(name, number)
            }
        }
    }
}