package com.example.shesave

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*

class EmergencyText : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide() // Oculta la barra de accion
        setContentView(R.layout.activity_emergency_text)

        // Inicializacion de vistas
        val imgBack = findViewById<ImageButton>(R.id.imgBack)
        val txtText = findViewById<TextView>(R.id.txtText)
        val edtText = findViewById<EditText>(R.id.edtText)
        val btnAccept = findViewById<Button>(R.id.btnAccept)

        // Configura el boton de retroceso
        imgBack.setOnClickListener {
            val intent = Intent(this, Setting::class.java)
            startActivity(intent)
        }

        // Configura el boton de aceptar
        btnAccept.setOnClickListener {
            // Guardar el texto solo si el campo no está vacío
            if (edtText.text.toString().isNotEmpty()) {
                saveText(edtText.text.toString())
            }
        }

        // Carga y muestra el texto guardado previamente
        val pref = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE)
        val savedText = pref.getString("Text", "")
        txtText.text = savedText
    }

    // Guarda el texto en SharedPreferences
    private fun saveText(edtText: String) {
        val pref = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE)
        val editor = pref.edit()
        editor.putString("Text", edtText)
        editor.apply()
        val intent = Intent(this, Setting::class.java)
        startActivity(intent)
        Toast.makeText(this, "El texto de emergencia se ha guardo exitosamete", Toast.LENGTH_SHORT)
            .show()
    }
}