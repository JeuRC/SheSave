package com.example.shesave

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.InputType
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.shesave.alarm.ServiceAlert
import com.example.shesave.alarm.PulseCountReceiver
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class Login : AppCompatActivity() {
    private lateinit var receiver: PulseCountReceiver
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
            /*val intent = Intent(this, ChangePassword::class.java)
            startActivity(intent)*/
            Toast.makeText(this, "Funcionalidad no implementada", Toast.LENGTH_SHORT).show()
        }

        val intent = Intent(this, ServiceAlert::class.java)
        startForegroundService(intent)

        receiver = PulseCountReceiver()
    }

    private fun session() {
        val prefs = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE)
        val json = prefs.getString("USER_LIST", null)
        val gson = Gson()
        val type = object : TypeToken<MutableList<User>>() {}.type
        val users = if (json != null) gson.fromJson<MutableList<User>>(
            json,
            type
        ) else mutableListOf<User>()

        val edtEmail = findViewById<EditText>(R.id.edtEmail)
        val edtPassword = findViewById<EditText>(R.id.edtPassword)
        val enteredEmail = edtEmail.text.toString()
        val enteredPasswordHash = hashPassword(edtPassword.text.toString())

        var userFound = false

        for (user in users) {
            if (user.email == enteredEmail && user.password == enteredPasswordHash) {
                userFound = true
                val pref = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE)
                val editor = pref.edit()
                editor.putString("Email", enteredEmail)
                editor.putString("Password", hashPassword(enteredPasswordHash))
                editor.putInt("Length", edtPassword.text.toString().length)
                editor.apply()
                break
            }
        }

        if (userFound) {
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

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter(Intent.ACTION_SCREEN_ON)
        filter.addAction(Intent.ACTION_SCREEN_OFF)
        registerReceiver(receiver, filter)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(receiver)
    }
}