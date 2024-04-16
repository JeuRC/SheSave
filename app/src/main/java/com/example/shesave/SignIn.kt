package com.example.shesave

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout

class SignIn : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_sign_in)

        val imgBack = findViewById<ImageButton>(R.id.imgBack)
        val edtEmail = findViewById<EditText>(R.id.edtEmail)
        val edtPassword = findViewById<EditText>(R.id.edtPassword)
        val edtRPassword = findViewById<EditText>(R.id.edtRPassword)
        val btnSignIn = findViewById<Button>(R.id.btnSignIn)

        imgBack.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }

        btnSignIn.setOnClickListener {
            if (edtEmail.text.toString().isNotEmpty() && edtPassword.text.toString().isNotEmpty() &&
                edtRPassword.text.toString().isNotEmpty()
            ) {
                if (edtPassword.text.toString() == edtRPassword.text.toString()) {
                    save(edtEmail.text.toString(), edtPassword.text.toString())
                } else {
                    Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(this, "Diligencia todos los campos", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun save(edtEmail: String, edtPassword: String) {
        val pref = getSharedPreferences(getString(R.string.txtSignIn), Context.MODE_PRIVATE)
        val editor = pref.edit()
        editor.putString("Email", edtEmail)
        editor.putString("Password", edtPassword)
        editor.apply()
        val intent = Intent(this, Login::class.java)
        startActivity(intent)
        Toast.makeText(this, "Te has registrado exitosamente", Toast.LENGTH_SHORT).show()
    }
}