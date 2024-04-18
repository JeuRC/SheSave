package com.example.shesave

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*

class EmergencyText : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_emergency_text)

        val imgBack = findViewById<ImageButton>(R.id.imgBack)
        val edtText = findViewById<EditText>(R.id.edtText)
        val btnAccept = findViewById<Button>(R.id.btnAccept)

        imgBack.setOnClickListener {
            val intent = Intent(this, Setting::class.java)
            startActivity(intent)
        }

        btnAccept.setOnClickListener {
            if (edtText.text.toString().isNotEmpty()) {
                saveText(edtText.text.toString())
            }
        }
    }

    private fun saveText(edtText: String) {
        val pref = getSharedPreferences(getString(R.string.txtEmergency_text), Context.MODE_PRIVATE)
        val editor = pref.edit()
        editor.putString("Text", edtText)
        editor.apply()
        val intent = Intent(this, Setting::class.java)
        startActivity(intent)
        Toast.makeText(this, "El texto de emergencia se ha guardo exitosamete", Toast.LENGTH_SHORT)
            .show()
    }
}