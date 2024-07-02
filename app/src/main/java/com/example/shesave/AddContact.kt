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

    // Definicion de constantes dentro del companion object
    companion object {
        private const val CONTACT_PICK_REQUEST = 1 // Codigo de solicitud para seleccionar contacto
        private const val CONTACTS_PERMISSION_REQUEST_CODE = 101 // Codigo de solicitud para permiso de contactos
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide() // Oculta la barra de accion
        setContentView(R.layout.activity_add_contact)

        // Inicializacion de vistas
        val imgBack = findViewById<ImageButton>(R.id.imgBack)
        val imgContacts = findViewById<ImageButton>(R.id.imgContacts)
        val edtName = findViewById<EditText>(R.id.edtName)
        val edtNumber = findViewById<EditText>(R.id.edtNumber)
        val btnSave = findViewById<Button>(R.id.btnSave)

        // Configuracion del listener para el boton de retroceso
        imgBack.setOnClickListener {
            val intent = Intent(this, Contacts::class.java)
            startActivity(intent)
        }

        // Configuracion del listener para el boton de contactos
        imgContacts.setOnClickListener {
            checkContactsPermission()
        }

        // Configuracion del listener para el boton de guardar
        btnSave.setOnClickListener {
            // Verifica que los campos de nombre y numero no esten vacios
            if (edtName.text.toString().isNotEmpty() && edtNumber.text.toString().isNotEmpty()) {
                // Verifica que el numero tenga 10 digitos
                if (edtNumber.text.toString().length == 10) {
                    // Verifica que el numero comience con el digito 3
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

    // Metodo para abrir la lista de contactos
    private fun openContacts() {
        val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
        startActivityForResult(intent, CONTACT_PICK_REQUEST)
    }

    // Metodo para guardar el contacto en SharedPreferences
    private fun save(edtName: String, edtNumber: String) {
        val sharedPreferences =
            getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE)
        val gson = Gson()

        val currentUserEmail = sharedPreferences.getString("Email", null)
        if (currentUserEmail == null) {
            Toast.makeText(this, "No se encontró el usuario actual", Toast.LENGTH_SHORT).show()
            return
        }

        // Obtiene la lista de usuarios de SharedPreferences
        val json = sharedPreferences.getString("USER_LIST", null)
        val type = object : TypeToken<MutableList<User>>() {}.type
        val users =
            if (json != null) gson.fromJson<MutableList<User>>(json, type) else mutableListOf()

        // Encuentra el usuario actual en la lista
        val currentUser = users.find { it.email == currentUserEmail }
        if (currentUser != null) {
            // Añade el nuevo contacto al usuario actual
            val newContact = Contact(edtName, edtNumber)
            currentUser.contacts.add(newContact)
            val editor = sharedPreferences.edit()
            val updatedJson = gson.toJson(users)
            editor.putString("USER_LIST", updatedJson)
            editor.apply() // Guarda los cambios en SharedPreferences
            val intent = Intent(this, Contacts::class.java)
            startActivity(intent)
            Toast.makeText(this, "Contacto guardado", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "No se encontró el usuario actual", Toast.LENGTH_SHORT).show()
        }
    }

    // Metodo para verificar el permiso de contactos
    private fun checkContactsPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Solicita el permiso si no esta concedido
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_CONTACTS),
                CONTACTS_PERMISSION_REQUEST_CODE
            )
        } else {
            openContacts()
        }
    }

    // Metodo para obtener el numero de telefono de un contacto
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

    // Metodo que maneja el resultado de la solicitud de permisos
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

    // Metodo que maneja el resultado de la seleccion de un contacto
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