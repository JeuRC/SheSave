package com.example.shesave.alarm

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.SmsManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.shesave.R
import com.example.shesave.User
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlertMessage {

    companion object {
        const val REQUEST_CODE_SMS_PERMISSION = 1
        var SOS_MESSAGE = ""
        var SOS_PHONE_NUMBER = ""
    }

    // Funcion para enviar un mensaje de SOS
    fun sendSosMessage(context: Context) {
        // Verifica si tiene permisos para enviar SMS
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.SEND_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Solicita permisos si no estan concedidos
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(Manifest.permission.SEND_SMS),
                REQUEST_CODE_SMS_PERMISSION
            )
        } else {
            updateSosPhoneNumber(context)
            // Notifica si no hay contactos de emergencia configurados
            if (SOS_PHONE_NUMBER.isBlank()) {
                Toast.makeText(
                    context,
                    "No hay contactos de emergencia configurados",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                sendSms(context)
            }
        }
    }

    // Funcion para enviar SMS
    fun sendSms(context: Context) {
        try {
            val smsManager = SmsManager.getDefault()
            val prefs =
                context.getSharedPreferences(
                    context.getString(R.string.app_name),
                    Context.MODE_PRIVATE
                )
            // Construye el mensaje de SOS con la ubicacion actual
            SOS_MESSAGE = prefs.getString(
                "Text",
                "!!!Necesito Ayuda!!!"
            ) + "\n" + "Mi ubicacion actual es esta: " + createMapLink(context)
            // Obtiene y divide los numeros de telefono de emergencia configurados
            val phoneNumbers = SOS_PHONE_NUMBER.split(",")
            // Envia el mensaje a cada numero de telefono
            phoneNumbers.forEach { number ->
                smsManager.sendTextMessage(number.trim(), null, SOS_MESSAGE, null, null)
            }
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(context, "Mensaje de SOS enviado", Toast.LENGTH_SHORT).show()
            }
        } catch (ex: Exception) {
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(context, "Error al enviar el mensaje de SOS", Toast.LENGTH_SHORT)
                    .show()
            }
            ex.printStackTrace()
        }
    }

    // Funcion para crear el enlace del mapa con la ubicacion actual
    private fun createMapLink(context: Context): String {
        val pref =
            context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)
        val latitude = pref.getString("Latitude", null)
        val longitude = pref.getString("Longitude", null)
        return "https://www.google.com/maps/search/?api=1&query=$latitude,$longitude"
    }

    // Funcion para actualizar el numero de telefono de SOS
    fun updateSosPhoneNumber(context: Context) {
        SOS_PHONE_NUMBER = getAllContactNumbers(context)
    }

    // Funcion para obtener todos los numeros de contacto configurados
    private fun getAllContactNumbers(context: Context): String {
        val sharedPreferences =
            context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)
        val gson = Gson()

        val currentUserEmail = sharedPreferences.getString("Email", null)
        // Notifica si no se encuentra el usuario actual
        if (currentUserEmail == null) {
            Toast.makeText(context, "No se encontró el usuario actual", Toast.LENGTH_SHORT).show()
            return ""
        }

        val json = sharedPreferences.getString("USER_LIST", null)
        val type = object : TypeToken<MutableList<User>>() {}.type
        val users =
            if (json != null) gson.fromJson<MutableList<User>>(json, type) else mutableListOf()

        val currentUser = users.find { it.email == currentUserEmail }
        // Obtiene los numeros de telefono de los contactos del usuario actual
        return if (currentUser != null) {
            val phoneNumbers = currentUser.contacts.map { it.number } ?: listOf()
            phoneNumbers.joinToString(separator = ",")
        } else {
            Toast.makeText(context, "No se encontró el usuario actual", Toast.LENGTH_SHORT).show()
            ""
        }
    }
}