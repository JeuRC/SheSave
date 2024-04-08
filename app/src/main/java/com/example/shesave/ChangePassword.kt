package com.example.shesave

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
        val edtEmail = findViewById<EditText>(R.id.edtPassword)
        val btnSend = findViewById<Button>(R.id.btnSend)

        imgBack.setOnClickListener {
            val intent = Intent(this, Setting::class.java)
            startActivity(intent)
        }

        btnSend.setOnClickListener {
            if (edtEmail.text.toString().isNotEmpty()) {
                changePassword(edtEmail.text.toString())
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
        Toast.makeText(this, "Password3 successfully changed", Toast.LENGTH_SHORT).show()
    }
}