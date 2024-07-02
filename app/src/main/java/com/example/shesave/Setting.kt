package com.example.shesave

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat

class Setting : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide() // Oculta la barra de accion
        setContentView(R.layout.activity_setting)

        // Inicializacion de vistas
        val imgBack = findViewById<ImageButton>(R.id.imgBack)
        val imgLogout = findViewById<ImageButton>(R.id.imgLogout)
        val imgEdit_email = findViewById<ImageButton>(R.id.imgEdit_email)
        val imgEdit_characters = findViewById<ImageButton>(R.id.imgEdit_characters)
        val txtAudioRecording = findViewById<SwitchCompat>(R.id.txtAudioRecording)
        val txtVideoRecording = findViewById<SwitchCompat>(R.id.txtVideoRecording)
        // SharedPreferences para guardar configuraciones
        val pref = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE)
        val imgRecording = findViewById<ImageButton>(R.id.imgRecording)
        val imgMessage = findViewById<ImageButton>(R.id.imgMessage)
        val imgEmergency_contacts = findViewById<ImageButton>(R.id.imgEmergency_contacts)

        // Listener para el boton de retroceso
        imgBack.setOnClickListener {
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
        }

        // Listener para el boton de cierre de sesion
        imgLogout.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }

        // Listener para el boton de edicion de correo electronico
        imgEdit_email.setOnClickListener {
            val intent = Intent(this, ChangeEmail::class.java)
            startActivity(intent)
        }

        // Listener para el boton de edicion de caracteres
        imgEdit_characters.setOnClickListener {
            val intent = Intent(this, ChangePassword::class.java)
            startActivity(intent)
        }

        // Listener para el switch de grabacion de audio
        txtAudioRecording.setOnCheckedChangeListener { _, AudioChecked ->
            with(pref.edit()) {
                putBoolean("AudioChecked", AudioChecked)
                apply()
            }
            if (txtAudioRecording.isChecked) {
                txtVideoRecording.isChecked = false
            }
        }

        // Listener para el switch de grabacion de video
        txtVideoRecording.setOnCheckedChangeListener { _, VideoChecked ->
            with(pref.edit()) {
                putBoolean("VideoChecked", VideoChecked)
                apply()
            }
            if (txtVideoRecording.isChecked) {
                txtAudioRecording.isChecked = false
            }
        }

        // Listener para el boton de grabaciones
        imgRecording.setOnClickListener {
            val intent = Intent(this, Recordings::class.java)
            startActivity(intent)
        }

        // Listener para el boton de texto de emergencia
        imgMessage.setOnClickListener {
            val intent = Intent(this, EmergencyText::class.java)
            startActivity(intent)
        }

        // Listener para el boton de contactos de emergencia
        imgEmergency_contacts.setOnClickListener {
            val intent = Intent(this, Contacts::class.java)
            startActivity(intent)
        }

        // Carga las configuraciones guardadas y ajusta los estados de los switches
        val AudioChecked = pref.getBoolean("AudioChecked", false)
        if (AudioChecked) {
            txtAudioRecording.isChecked = true
            txtVideoRecording.isChecked = false
        }
        val VideoChecked = pref.getBoolean("VideoChecked", false)
        if (VideoChecked) {
            txtVideoRecording.isChecked = true
            txtAudioRecording.isChecked = false
        }

        profile()
    }

    // Muestra la informacion del perfil del usuario
    private fun profile() {
        val prefs = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE)
        val email = prefs.getString("Email", null)
        val password = prefs.getString("Password", null)
        val length = prefs.getInt("Length", 0)

        if (email != null && password != null) {
            val txtEmail_address = findViewById<TextView>(R.id.txtEmail_address)
            val txtCharacters = findViewById<TextView>(R.id.txtCharacters)

            txtEmail_address.text = email
            txtCharacters.text = "*".repeat(length)
        }
    }
}