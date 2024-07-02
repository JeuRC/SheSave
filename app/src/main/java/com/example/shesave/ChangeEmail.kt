package com.example.shesave

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ChangeEmail : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide() // Oculta la barra de accion
        setContentView(R.layout.activity_change_email)

        // Inicializacion de vistas
        val imgBack = findViewById<ImageButton>(R.id.imgBack)
        val edtEmail = findViewById<EditText>(R.id.edtEmail)
        val btnSend = findViewById<Button>(R.id.btnSend)

        // Configuracion del listener para el boton de retroceso
        imgBack.setOnClickListener {
            val intent = Intent(this, Setting::class.java)
            startActivity(intent)
        }

        // Configuracion del listener para el boton de enviar
        btnSend.setOnClickListener {
            // Verifica que el campo de correo no este vacio
            if (edtEmail.text.toString().isNotEmpty()) {Z
                if (isValidEmail(edtEmail.text.toString())) {
                    changeEmail(edtEmail.text.toString())
                } else {
                    Toast.makeText(this, "Ingrese un correo electrónico válido", Toast.LENGTH_LONG)
                        .show()
                }
            } else {
                Toast.makeText(this, "Ingresa un correo", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Metodo para cambiar el correo
    private fun changeEmail(newEmail: String) {
        // Verifica que el nuevo correo sea diferente al actual
        if (!isDifferentEmail(newEmail)) {
            Toast.makeText(this, "El correo electrónico ya está registrado", Toast.LENGTH_SHORT)
                .show()
            return
        }

        val pref = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE)
        val currentEmail = pref.getString("Email", null)

        if (currentEmail == null) {
            Toast.makeText(this, "No hay usuario en sesión", Toast.LENGTH_SHORT).show()
            return
        }

        val gson = Gson()
        val json = pref.getString("USER_LIST", null)
        val type = object : TypeToken<MutableList<User>>() {}.type
        val users =
            if (json != null) gson.fromJson<MutableList<User>>(json, type) else mutableListOf()

        var userFound = false

        // Busca el usuario actual en la lista de usuarios
        for (user in users) {
            if (user.email == currentEmail) {
                user.email = newEmail // Actualiza el correo del usuario
                userFound = true
                break
            }
        }

        // Si el usuario es encontrado, guarda los cambios en SharedPreferences
        if (userFound) {
            val editor = pref.edit()
            editor.putString("Email", newEmail)
            editor.putString("USER_LIST", gson.toJson(users))
            editor.apply()

            val intent = Intent(this, Setting::class.java)
            startActivity(intent)
            Toast.makeText(this, "El correo se cambió correctamente", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "No se encontró el usuario en la lista", Toast.LENGTH_SHORT).show()
        }
    }

    // Metodo para verificar si el nuevo correo es diferente al actual
    private fun isDifferentEmail(edtEmail: String): Boolean {
        val prefs = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE)
        val json = prefs.getString("USER_LIST", null)
        val gson = Gson()
        val type = object : TypeToken<MutableList<User>>() {}.type
        val users =
            if (json != null) gson.fromJson<MutableList<User>>(json, type) else mutableListOf()

        // Verifica si el correo ya existe en la lista de usuarios
        for (user in users) {
            if (user.email == edtEmail) {
                return false
            }
        }
        return true
    }

    // Metodo para verificar si un correo es valido
    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z](.*)([@])(.+)(\\.)(.+)"
        return email.matches(emailRegex.toRegex())
    }
}