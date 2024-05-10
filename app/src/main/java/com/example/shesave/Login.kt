package com.example.shesave

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class Login : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_login)

        val edtEmail = findViewById<EditText>(R.id.edtEmail)
        val edtPassword = findViewById<EditText>(R.id.edtPassword)
        val imgShowPassword = findViewById<ImageButton>(R.id.imgShowPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val txtLogin2 = findViewById<TextView>(R.id.txtLogin2)
        val txtLogin3 = findViewById<TextView>(R.id.txtLogin3)
        var passwordVisible = false

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
            val intent = Intent(this, ChangePassword::class.java)
            startActivity(intent)
        }
    }

    private fun session() {
        val prefs = getSharedPreferences(getString(R.string.txtSignIn), Context.MODE_PRIVATE)
        val email = prefs.getString("Email", null)
        val savedPasswordHash = prefs.getString("Password", null)

        val edtEmail = findViewById<EditText>(R.id.edtEmail)
        val edtPassword = findViewById<EditText>(R.id.edtPassword)

        val enteredPasswordHash = hashPassword(edtPassword.text.toString())

        if (email != null && savedPasswordHash != null && email == edtEmail.text.toString() && savedPasswordHash == enteredPasswordHash) {
            showHome()
        } else {
            Toast.makeText(this, "Los datos ingresados no están registrados", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun showHome() {
        val intent = Intent(this, Home::class.java)
        startActivity(intent)
        Toast.makeText(this, "Has ingresado exitosamente", Toast.LENGTH_SHORT).show()
    }

    private fun hashPassword(password: String): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val bytes = digest.digest(password.toByteArray())
            bytes.joinToString("") { "%02x".format(it) }
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
            ""
        }
    }
}