package com.example.shesave

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class SignIn : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide() // Oculta la barra de accion
        setContentView(R.layout.activity_sign_in)

        // Inicializacion de vistas
        val imgBack = findViewById<ImageButton>(R.id.imgBack)
        val edtEmail = findViewById<EditText>(R.id.edtEmail)
        val edtPassword = findViewById<EditText>(R.id.edtPassword)
        val imgShowPassword = findViewById<ImageButton>(R.id.imgShowPassword)
        val edtRPassword = findViewById<EditText>(R.id.edtRPassword)
        val imgShowRPassword = findViewById<ImageButton>(R.id.imgShowRPassword)
        val btnSignIn = findViewById<Button>(R.id.btnSignIn)
        var passwordVisible = false

        // Listener para el boton de retroceso
        imgBack.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }

        // Listener para mostrar/ocultar contraseña principal
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

        // Listener para mostrar/ocultar contraseña de confirmacion
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

        // Listener para el boton de registro
        btnSignIn.setOnClickListener {
            if (edtEmail.text.toString().isNotEmpty() && edtPassword.text.toString().isNotEmpty() &&
                edtRPassword.text.toString().isNotEmpty()
            ) {
                if (isValidEmail(edtEmail.text.toString())) {
                    if (isDifferentEmail(edtEmail.text.toString())) {
                        if (edtPassword.text.toString() == edtRPassword.text.toString()) {
                            if (isPasswordSecure(edtPassword.text.toString())) {
                                save(edtEmail.text.toString(), edtPassword.text.toString())
                            } else {
                                Toast.makeText(
                                    this,
                                    "La contraseña no es segura",
                                    Toast.LENGTH_LONG
                                )
                                    .show()
                            }
                        } else {
                            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_LONG)
                                .show()
                        }
                    } else {
                        Toast.makeText(this, "El usuario ya esta registrado", Toast.LENGTH_LONG)
                            .show()
                    }
                } else {
                    Toast.makeText(this, "Ingrese un correo electrónico válido", Toast.LENGTH_LONG)
                        .show()
                }
            } else {
                Toast.makeText(this, "Diligencia todos los campos", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Verifica si el correo electronico ya esta registrado
    private fun isDifferentEmail(edtEmail: String): Boolean {
        val prefs = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE)
        val json = prefs.getString("USER_LIST", null)
        val gson = Gson()
        val type = object : TypeToken<MutableList<User>>() {}.type
        val users = if (json != null) gson.fromJson<MutableList<User>>(
            json,
            type
        ) else mutableListOf()

        for (user in users) {
            if (user.email == edtEmail) {
                return false
            }
        }
        return true
    }

    // Guarda un nuevo usuario registrado
    private fun save(edtEmail: String, edtPassword: String) {
        val pref = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE)
        val newUser = User(edtEmail, hashPassword(edtPassword))
        val gson = Gson()
        val json = pref.getString("USER_LIST", null)
        val type = object : TypeToken<MutableList<User>>() {}.type
        val users =
            if (json != null) gson.fromJson<MutableList<User>>(json, type) else mutableListOf()
        users.add(newUser)
        val editor = pref.edit()
        editor.putString("USER_LIST", gson.toJson(users))
        editor.apply()
        val intent = Intent(this, Login::class.java)
        startActivity(intent)
        Toast.makeText(this, "Te has registrado exitosamente", Toast.LENGTH_SHORT).show()
    }

    // Verifica si el correo electronico tiene un formato valido
    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z](.*)([@])(.+)(\\.)(.+)"
        return email.matches(emailRegex.toRegex())
    }

    // Genera un hash SHA-256 de la contraseña
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

    // Verifica si la contraseña es segura
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