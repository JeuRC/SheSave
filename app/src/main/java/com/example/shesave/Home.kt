package com.example.shesave

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.telephony.SmsManager
import android.view.MotionEvent
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class Home : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var isSosButtonPressed = false
    private val sosHandler = Handler()
    private val sosRunnable = Runnable {
        if (isSosButtonPressed) {
            sendSosMessage()
        }
    }

    companion object {
        const val REQUEST_CODE_LOCATION = 0
        const val REQUEST_CODE_SMS_PERMISSION = 1
        var SOS_MESSAGE = ""
        var SOS_PHONE_NUMBER = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_home)
        createFragment()

        val btnSetting = findViewById<ImageButton>(R.id.btnSetting)
        val btnSos = findViewById<Button>(R.id.btnSos)
        val imgRecording = findViewById<ImageButton>(R.id.imgRecording)
        val imgContacts = findViewById<ImageButton>(R.id.imgContacts)

        btnSetting.setOnClickListener {
            val intent = Intent(this, Setting::class.java)
            startActivity(intent)
        }

        btnSos.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    isSosButtonPressed = true
                    sosHandler.postDelayed(sosRunnable, 2000) // 2000 ms = 2 segundos
                }
                MotionEvent.ACTION_UP -> {
                    isSosButtonPressed = false
                    sosHandler.removeCallbacks(sosRunnable)
                }
            }
            true
        }

        imgRecording.setOnClickListener {
            val intent = Intent(this, Setting::class.java)
            startActivity(intent)
        }

        imgContacts.setOnClickListener {
            val intent = Intent(this, Contacts::class.java)
            startActivity(intent)
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        getCurrentLocation()
    }

    private fun sendSosMessage() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.SEND_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.SEND_SMS),
                REQUEST_CODE_SMS_PERMISSION
            )
        } else {
            sendSms()
        }
    }

    private fun sendSms() {
        try {
            val smsManager = SmsManager.getDefault()
            val prefs =
                getSharedPreferences(getString(R.string.txtEmergency_text), Context.MODE_PRIVATE)
            val SOS_MESSAGE = prefs.getString("Text", null) + "\n" + "Mi ubiacion actual es esta: " + createMapLink()
            val phoneNumbers = SOS_PHONE_NUMBER.split(",")
            phoneNumbers.forEach { number ->
                smsManager.sendTextMessage(number.trim(), null, SOS_MESSAGE, null, null)
            }
            Toast.makeText(this, "Mensaje de SOS enviado", Toast.LENGTH_SHORT).show()
        } catch (ex: Exception) {
            Toast.makeText(this, "Error al enviar el mensaje de SOS", Toast.LENGTH_SHORT).show()
            ex.printStackTrace()
        }
    }

    private fun createFragment() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        enableLocation()
    }

    private fun isLocationPermissionGranted() = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    private fun enableLocation() {
        if (!::map.isInitialized) return
        if (isLocationPermissionGranted()) {
            map.isMyLocationEnabled = true
        } else {
            requestLocationPermission()
        }
    }

    private fun requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            Toast.makeText(this, "Acepta los permisos", Toast.LENGTH_SHORT).show()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_CODE_LOCATION
            )
        }
    }

    private fun getCurrentLocation() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "No se han otorgado permisos de ubicación", Toast.LENGTH_SHORT).show()
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val coordinates = LatLng(location.latitude, location.longitude)
                    map.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(coordinates, 18f),
                        4000,
                        null
                    )
                    val pref = getSharedPreferences("RealTimeLocation", Context.MODE_PRIVATE)
                    val editor = pref.edit()
                    editor.putString("Latitude", location.latitude.toString())
                    editor.putString("Longitude", location.longitude.toString())
                    editor.apply()
                } else {
                    Toast.makeText(this, "No se pudo obtener la ubicación actual", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun createMapLink(): String {
        val pref = getSharedPreferences("RealTimeLocation", Context.MODE_PRIVATE)
        val latitude = pref.getString("Latitude", null)
        val longitude = pref.getString("Longitude", null)
        return "https://www.google.com/maps/search/?api=1&query=$latitude,$longitude"
    }

    private fun updateSosPhoneNumber() {
        SOS_PHONE_NUMBER = getAllContactNumbers()
    }

    private fun getAllContactNumbers(): String {
        val sharedPreferences = getSharedPreferences("SHARED_PREFS", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("CONTACT_LIST", null)
        val type = object : TypeToken<MutableList<Contact>>() {}.type
        val contacts =
            if (json != null) gson.fromJson<MutableList<Contact>>(json, type) else mutableListOf()

        val phoneNumbers = contacts.map { it.number }
        return phoneNumbers.joinToString(separator = ",")
    }

    override fun onResume() {
        super.onResume()
        updateSosPhoneNumber()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_SMS_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sendSms()
            } else {
                Toast.makeText(
                    this,
                    "Permiso para enviar mensajes de texto denegado",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        when (requestCode) {
            REQUEST_CODE_LOCATION -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                map.isMyLocationEnabled = true
            } else {
                Toast.makeText(this, "Acepta los permisos", Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        if (!::map.isInitialized) return
        if (!isLocationPermissionGranted()) {
            map.isMyLocationEnabled = false
            Toast.makeText(this, "Acepta los permisos", Toast.LENGTH_SHORT).show()
        }
    }
}