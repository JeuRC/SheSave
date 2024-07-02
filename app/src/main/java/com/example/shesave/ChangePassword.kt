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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class ChangePassword : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide() // Oculta la barra de accion
        setContentView(R.layout.activity_change_password)

        // Inicializacion de vistas
        val imgBack = findViewById<ImageButton>(R.id.imgBack)
        val edtPassword = findViewById<EditText>(R.id.edtPassword)
        val imgShowPassword = findViewById<ImageButton>(R.id.imgShowPassword)
        val edtRPassword = findViewById<EditText>(R.id.edtRPassword)
        val imgShowRPassword = findViewById<ImageButton>(R.id.imgShowRPassword)
        val btnSend = findViewById<Button>(R.id.btnSend)
        var passwordVisible = false

        // Configuracion del listener para el boton de retroceso
        imgBack.setOnClickListener {
            val intent = Intent(this, Setting::class.java)
            startActivity(intent)
        }

        // Configuracion del listener para mostrar/ocultar la contraseña
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

        // Configuracion del listener para mostrar/ocultar la confirmacion de contraseña
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

        // Configuracion del listener para el boton de enviar
        btnSend.setOnClickListener {
            // Verifica que ambos campos de contraseña no esten vacios
            if (edtPassword.text.toString().isNotEmpty() && edtRPassword.text.toString()
                    .isNotEmpty()
            ) {
                // Verifica que ambas contraseñas sean iguales
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

    // Metodo para cambiar la contraseña
    private fun changePassword(newPassword: String) {
        val pref = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE)
        val currentEmail = pref.getString("Email", null)
        if (currentEmail == null) {
            Toast.makeText(this, "No hay usuario en sesión", Toast.LENGTH_SHORT).show()
            return
        }
        val gson = Gson()
        val json = pref.getString("USER_LIST", null)
        val type = object : TypeToken<MutableList<User>>() {}.type
        val users = if (json != null) gson.fromJson<MutableList<User>>(json, type) else mutableListOf()
        var userFound = false
        val hashedPassword = hashPassword(newPassword)

        // Busca el usuario actual en la lista de usuarios y actualiza su contraseña
        for (user in users) {
            if (user.email == currentEmail) {
                user.password = hashedPassword
                userFound = true
                break
            }
        }

        // Si el usuario es encontrado, guarda los cambios en SharedPreferences
        if (userFound) {
            val editor = pref.edit()
            editor.putString("Password", hashedPassword)
            editor.putString("USER_LIST", gson.toJson(users))
            editor.apply()
            val intent = Intent(this, Setting::class.java)
            startActivity(intent)
            Toast.makeText(this, "Contraseña cambiada correctamente", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "No se encontró el usuario en la lista", Toast.LENGTH_SHORT).show()
        }
    }

    // Metodo para encriptar la contraseña usando SHA-256
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

    // Metodo para verificar si la contraseña es segura
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