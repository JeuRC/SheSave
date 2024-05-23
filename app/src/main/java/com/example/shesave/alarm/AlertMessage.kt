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

    fun sendSosMessage(context: Context) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.SEND_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(Manifest.permission.SEND_SMS),
                REQUEST_CODE_SMS_PERMISSION
            )
        } else {
            updateSosPhoneNumber(context)
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

    fun sendSms(context: Context) {
        try {
            val smsManager = SmsManager.getDefault()
            val prefs =
                context.getSharedPreferences(
                    context.getString(R.string.app_name),
                    Context.MODE_PRIVATE
                )
            SOS_MESSAGE = prefs.getString(
                "Text",
                "!!!Necesito Ayuda!!!"
            ) + "\n" + "Mi ubicacion actual es esta: " + createMapLink(context)
            val phoneNumbers = SOS_PHONE_NUMBER.split(",")
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

    private fun createMapLink(context: Context): String {
        val pref =
            context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)
        val latitude = pref.getString("Latitude", null)
        val longitude = pref.getString("Longitude", null)
        return "https://www.google.com/maps/search/?api=1&query=$latitude,$longitude"
    }

    fun updateSosPhoneNumber(context: Context) {
        SOS_PHONE_NUMBER = getAllContactNumbers(context)
    }

    private fun getAllContactNumbers(context: Context): String {
        val sharedPreferences =
            context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)
        val gson = Gson()

        val currentUserEmail = sharedPreferences.getString("Email", null)
        if (currentUserEmail == null) {
            Toast.makeText(context, "No se encontró el usuario actual", Toast.LENGTH_SHORT).show()
            return ""
        }

        val json = sharedPreferences.getString("USER_LIST", null)
        val type = object : TypeToken<MutableList<User>>() {}.type
        val users = if (json != null) gson.fromJson<MutableList<User>>(json, type) else mutableListOf()

        val currentUser = users.find { it.email == currentUserEmail }
        if (currentUser != null) {
            val phoneNumbers = currentUser.contacts?.map { it.number } ?: listOf()
            return phoneNumbers.joinToString(separator = ",")
        } else {
            Toast.makeText(context, "No se encontró el usuario actual", Toast.LENGTH_SHORT).show()
            return ""
        }
    }
}