package com.example.shesave

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class Setting : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_setting)

        val imgBack = findViewById<ImageButton>(R.id.imgBack)
        val imgLogout = findViewById<ImageButton>(R.id.imgLogout)
        val imgEdit_email = findViewById<ImageButton>(R.id.imgEdit_email)
        val imgEdit_characters = findViewById<ImageButton>(R.id.imgEdit_characters)
        val imgRecording = findViewById<ImageButton>(R.id.imgRecording)
        val imgEmergency_contacts = findViewById<ImageButton>(R.id.imgEmergency_contacts)

        imgBack.setOnClickListener {
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
        }

        imgLogout.setOnClickListener {
            val prefs = getSharedPreferences(getString(R.string.txtSignIn), Context.MODE_PRIVATE)
            val editor = prefs.edit()
            editor.clear()
            editor.apply()
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }

        imgEdit_email.setOnClickListener {
            val intent = Intent(this, ChangeEmail::class.java)
            startActivity(intent)
        }

        imgEdit_characters.setOnClickListener {
            val intent = Intent(this, ChangePassword::class.java)
            startActivity(intent)
        }

        imgRecording.setOnClickListener {

        }

        imgEmergency_contacts.setOnClickListener {
            val intent = Intent(this, Contacts::class.java)
            startActivity(intent)
        }

        profile()
    }

    private fun profile() {
        val prefs = getSharedPreferences(getString(R.string.txtSignIn), Context.MODE_PRIVATE)
        val email = prefs.getString("Email", null)
        val password = prefs.getString("Password", null)

        if (email != null && password != null) {
            val txtEmail_address = findViewById<TextView>(R.id.txtEmail_address)
            val txtCharacters = findViewById<TextView>(R.id.txtCharacters)

            txtEmail_address.text = email
            txtCharacters.text = password
        }
    }
}