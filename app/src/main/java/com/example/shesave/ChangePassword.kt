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
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class ChangePassword : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_change_password)

        val imgBack = findViewById<ImageButton>(R.id.imgBack)
        val edtPassword = findViewById<EditText>(R.id.edtPassword)
        val imgShowPassword = findViewById<ImageButton>(R.id.imgShowPassword)
        val edtRPassword = findViewById<EditText>(R.id.edtRPassword)
        val imgShowRPassword = findViewById<ImageButton>(R.id.imgShowRPassword)
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

        imgShowRPassword.setOnClickListener {
            passwordVisible = !passwordVisible
            if (passwordVisible) {
                edtRPassword.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                imgShowRPassword.setImageResource(R.drawable.icon_password_on)
            } else {
                edtRPassword.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                imgShowRPassword.setImageResource(R.drawable.icon_password_off)
            }
            edtRPassword.setSelection(edtRPassword.text.length)
        }

        btnSend.setOnClickListener {
            if (edtPassword.text.toString().isNotEmpty() && edtRPassword.text.toString()
                    .isNotEmpty()
            ) {
                if (edtPassword.text.toString() == edtRPassword.text.toString()) {
                    if (isPasswordSecure(edtPassword.text.toString())) {
                        changePassword(edtPassword.text.toString())
                    } else {
                        Toast.makeText(this, "La contraseña no es segura", Toast.LENGTH_LONG)
                            .show()
                    }
                } else {
                    Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(this, "Diligencia todos los campos", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun changePassword(edtPassword: String) {
        val pref = getSharedPreferences(getString(R.string.txtSignIn), Context.MODE_PRIVATE)
        val editor = pref.edit()
        editor.putString("Password", hashPassword(edtPassword))
        editor.apply()
        val intent = Intent(this, Setting::class.java)
        startActivity(intent)
        Toast.makeText(this, "Contraseña cambiada correctamente", Toast.LENGTH_SHORT).show()
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

    private fun isPasswordSecure(password: String): Boolean {
        val minLength = 8

        if (password.length < minLength) {
            Toast.makeText(
                this,
                "La contraseña debe tener al menos 8 caracteres",
                Toast.LENGTH_LONG
            ).show()
            return false
        }

        val containsNumber = password.any { it.isDigit() }
        if (!containsNumber) {
            Toast.makeText(
                this,
                "La contraseña debe contener al menos un número",
                Toast.LENGTH_LONG
            ).show()
            return false
        }

        val containsUpperCase = password.any { it.isUpperCase() }
        if (!containsUpperCase) {
            Toast.makeText(
                this,
                "La contraseña debe contener al menos una letra mayúscula",
                Toast.LENGTH_LONG
            ).show()
            return false
        }

        val containsSpecialChar = password.any { it.isLetterOrDigit().not() }
        if (!containsSpecialChar) {
            Toast.makeText(
                this,
                "La contraseña debe contener al menos un carácter especial",
                Toast.LENGTH_LONG
            ).show()
            return false
        }
        return true
    }
}