package com.example.shesave

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast

class AddContact : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_add_contact)

        val imgBack = findViewById<ImageButton>(R.id.imgBack)
        val imgContacts = findViewById<ImageButton>(R.id.imgContacts)
        val edtName = findViewById<EditText>(R.id.edtName)
        val edtNumber = findViewById<EditText>(R.id.edtNumber)
        val btnSave = findViewById<Button>(R.id.btnSave)

        imgBack.setOnClickListener {
            val intent = Intent(this, Setting::class.java)
            startActivity(intent)
        }

        imgContacts.setOnClickListener {

        }

        btnSave.setOnClickListener {
            if (edtName.text.toString().isNotEmpty() && edtNumber.text.toString().isNotEmpty()) {
                save(edtName.text.toString(), edtNumber.text.toString())
            } else {
                Toast.makeText(this, "You must fill out all the fields", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun save(edtName: String, edtNumber: String) {
        val pref = getSharedPreferences(getString(R.string.txtContacts), Context.MODE_PRIVATE)
        val editor = pref.edit()
        editor.putString("Name", edtName)
        editor.putString("Number", edtNumber)
        editor.apply()
        val intent = Intent(this, Contacts::class.java)
        startActivity(intent)
        Toast.makeText(this, "You have successfully registered", Toast.LENGTH_SHORT).show()
    }
}