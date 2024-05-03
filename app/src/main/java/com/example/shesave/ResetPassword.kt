package com.example.shesave

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class ResetPassword : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_reset_password)

        val imgBack = findViewById<ImageButton>(R.id.imgBack)
        val edtEmail = findViewById<EditText>(R.id.edtEmail)
        val btnSend = findViewById<Button>(R.id.btnSend)

        imgBack.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }

        btnSend.setOnClickListener {
            if (isValidEmail(edtEmail.text.toString())) {
                buttonSendEmail(edtEmail.text.toString())
                val intent = Intent(this, Login::class.java)
                startActivity(intent)
                //Toast.makeText(this, "Funcionalidad aun no implementada", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(
                    this,
                    "Por favor, ingrese un correo electrónico válido",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun buttonSendEmail(email: String) {
        try {
            val prefs = getSharedPreferences(getString(R.string.txtSignIn), Context.MODE_PRIVATE)
            val length = prefs.getInt("Length", 0)

            val stringSenderEmail = "jrojas16@uan.edu.co"
            val stringPasswordSenderEmail = "Prueba1."
            val stringHost = "smtp.gmail.com"

            val properties = System.getProperties().apply {
                put("mail.smtp.host", stringHost)
                put("mail.smtp.port", "465")
                put("mail.smtp.ssl.enable", "true")
                put("mail.smtp.auth", "true")
            }

            val session = Session.getInstance(properties, object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(stringSenderEmail, stringPasswordSenderEmail)
                }
            })

            val mimeMessage = MimeMessage(session).apply {
                addRecipient(Message.RecipientType.TO, InternetAddress(email))
                subject = "Recuperacion de contraseña SheSave"
                setText("Tu contraseña es: ${"*".repeat(length)}")
            }

            Thread {
                try {
                    Transport.send(mimeMessage)
                } catch (e: MessagingException) {
                    e.printStackTrace()
                }
            }.start()

            Toast.makeText(
                this,
                "Se ha enviado tu contraseña a el correo $email",
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "No se ha enviado el correo", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z](.*)([@]{1})(.{1,})(\\.)(.{1,})"
        return email.matches(emailRegex.toRegex())
    }
}