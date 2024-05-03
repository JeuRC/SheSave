package com.example.shesave

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast

class ChangePassword : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_change_password)

        val imgBack = findViewById<ImageButton>(R.id.imgBack)
        val edtPassword = findViewById<EditText>(R.id.edtPassword)
        val imgShowPassword = findViewById<ImageButton>(R.id.imgShowPassword)
        val btnSend = findViewById<Button>(R.id.btnSend)
        var passwordVisible = false

        imgBack.setOnClickListener {
            val intent = Intent(this, Setting::class.java)
            startActivity(intent)
        }

        imgShowPassword.setOnClickListener {
            passwordVisible = !passwordVisible
            if (passwordVisible) {
                edtPassword.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                imgShowPassword.setImageResource(R.drawable.icon_password_on)
            } else {
                edtPassword.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                imgShowPassword.setImageResource(R.drawable.icon_password_off)
            }
            edtPassword.setSelection(edtPassword.text.length)
        }

        btnSend.setOnClickListener {
            if (edtPassword.text.toString().isNotEmpty()) {
                changePassword(edtPassword.text.toString())
            }
        }
    }

    private fun changePassword(edtPassword: String) {
        val pref = getSharedPreferences(getString(R.string.txtSignIn), Context.MODE_PRIVATE)
        val editor = pref.edit()
        editor.putString("Password", edtPassword)
        editor.apply()
        val intent = Intent(this, Setting::class.java)
        startActivity(intent)
        Toast.makeText(this, "Contraseña cambiada correctamente", Toast.LENGTH_SHORT).show()
    }
}