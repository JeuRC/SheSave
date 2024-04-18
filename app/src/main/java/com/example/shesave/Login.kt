package com.example.shesave

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class Login : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_login)

        val edtEmail = findViewById<EditText>(R.id.edtEmail)
        val edtPassword = findViewById<EditText>(R.id.edtPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val txtLogin2 = findViewById<TextView>(R.id.txtLogin2)
        val txtLogin3 = findViewById<TextView>(R.id.txtLogin3)

        btnLogin.setOnClickListener {
            if (edtEmail.text.toString().isNotEmpty() && edtPassword.text.toString().isNotEmpty()) {
                session()
            } else {
                Toast.makeText(this, "Diligencia todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        txtLogin2.setOnClickListener {
            val intent = Intent(this, SignIn::class.java)
            startActivity(intent)
        }

        txtLogin3.setOnClickListener {
            val intent = Intent(this, ResetPassword::class.java)
            startActivity(intent)
        }
    }

    private fun session() {
        val prefs = getSharedPreferences(getString(R.string.txtSignIn), Context.MODE_PRIVATE)
        val email = prefs.getString("Email", null)
        val password = prefs.getString("Password", null)

        showHome(email.toString(), password.toString())
    }

    private fun showHome(email: String, password: String) {
        val edtEmail = findViewById<EditText>(R.id.edtEmail)
        val edtPassword = findViewById<EditText>(R.id.edtPassword)

        if (email == edtEmail.text.toString() && password == edtPassword.text.toString()) {
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
            Toast.makeText(this, "Has ingresado exitosamente", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Los datos ingresados no estan registrados", Toast.LENGTH_SHORT)
                .show()
        }
    }
}