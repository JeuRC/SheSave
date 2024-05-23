package com.example.shesave

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast

class ChangeEmail : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_change_email)

        val imgBack = findViewById<ImageButton>(R.id.imgBack)
        val edtEmail = findViewById<EditText>(R.id.edtEmail)
        val btnSend = findViewById<Button>(R.id.btnSend)

        imgBack.setOnClickListener {
            val intent = Intent(this, Setting::class.java)
            startActivity(intent)
        }

        btnSend.setOnClickListener {
            if (edtEmail.text.toString().isNotEmpty()) {
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

    private fun changeEmail(edtEmail: String) {
        val pref = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE)
        val editor = pref.edit()
        editor.putString("Email", edtEmail)
        editor.apply()
        val intent = Intent(this, Setting::class.java)
        startActivity(intent)
        Toast.makeText(this, "El correo se cambió correctamente", Toast.LENGTH_SHORT).show()
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z](.*)([@])(.+)(\\.)(.+)"
        return email.matches(emailRegex.toRegex())
    }
}