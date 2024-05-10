package com.example.shesave

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.media.MediaRecorder
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.telephony.SmsManager
import android.view.KeyEvent
import android.view.MotionEvent
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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class Home : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var volumeUpCount = 0
    private var volumeDownCount = 0
    private var recordingName: String? = null
    private var recordingTrack: String? = null
    private var mediaRecorder: MediaRecorder? = null
    private var recordingTimer: CountDownTimer? = null
    private var isSosButtonPressed = false
    private val sosHandler = Handler()
    private val sosRunnable = Runnable {
        if (isSosButtonPressed) {
            sendSosMessage()
            val recording = startRecording()
        }
    }
    private var isButtonPressed = false
    private val handler = Handler()
    private val resetRunnable = Runnable {
        volumeUpCount = 0
        volumeDownCount = 0
        if (isButtonPressed) {
            sendSosMessage()
            val recording = startRecording()
        }
    }

    companion object {
        const val REQUEST_CODE_LOCATION = 0
        const val REQUEST_CODE_SMS_PERMISSION = 1
        const val REQUEST_CODE_VIDEO_RECORDING = 0
        const val REQUEST_CODE_AUDIO_RECORDING = 0
        var SOS_MESSAGE = ""
        var SOS_PHONE_NUMBER = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_home)
        createFragment()

        val btnSetting = findViewById<ImageButton>(R.id.btnSetting)
        val btnSos = findViewById<ImageButton>(R.id.btnSos)
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
            val intent = Intent(this, Recordings::class.java)
            startActivity(intent)
        }

        imgContacts.setOnClickListener {
            val intent = Intent(this, Contacts::class.java)
            startActivity(intent)
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        getCurrentLocation()

        val serviceIntent = Intent(this, ButtonPressService::class.java)
        startService(serviceIntent)

        startService(Intent(this, ServiceAlert::class.java))
    }

    private fun startRecording() {
        val prefs = getSharedPreferences("IS_AUDIO_ENABLED", Context.MODE_PRIVATE)
        val AudioChecked = prefs.getBoolean("AudioChecked", false)
        val VideoChecked = prefs.getBoolean("VideoChecked", false)
        if (AudioChecked && !VideoChecked) {
            startAudioRecording()
        }
        if (VideoChecked && !AudioChecked) {
            startVideoRecording()
        }
    }

    private fun startAudioRecording() {
        if (isAudioRecordingPermissionGranted()) {
            val fileName = "REC_${System.currentTimeMillis()}.3gp"
            val outputFile =
                File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "MyRecording/$fileName")
            val currentTimeMillis = System.currentTimeMillis()

            mediaRecorder = MediaRecorder()
            mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
            mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            mediaRecorder?.setOutputFile(outputFile.absolutePath)
            try {
                mediaRecorder?.prepare()
                mediaRecorder?.start()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Error al iniciar la grabación", Toast.LENGTH_SHORT).show()
            }
            recordingName = currentTimeMillis.toString()
            recordingTrack = outputFile.absolutePath
            val recording = Recording(
                recordingName.toString(),
                recordingTrack.toString(),
                currentTimeMillis
            )
            recordingFile(recording)

            startTimer()
        } else {
            requestAudioRecordingPermission()
        }
    }

    private fun startVideoRecording() {
        if (isVideoRecordingPermissionGranted()) {
            val REQUEST_VIDEO_CAPTURE = 1
            Intent(MediaStore.ACTION_VIDEO_CAPTURE).also { video ->
                video.resolveActivity(packageManager)?.also {
                    startActivityForResult(video, REQUEST_VIDEO_CAPTURE)
                }
            }
        } else {
            requestVideoRecordingPermission()
        }
    }

    private fun isAudioRecordingPermissionGranted() = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.RECORD_AUDIO
    ) == PackageManager.PERMISSION_GRANTED

    private fun isVideoRecordingPermissionGranted() = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    private fun requestAudioRecordingPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) && ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.RECORD_AUDIO
            )
        ) {
            Toast.makeText(
                this,
                "Acepta los permisos de almacenamiento y audio",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.RECORD_AUDIO
                ),
                REQUEST_CODE_AUDIO_RECORDING
            )
        }
    }

    private fun requestVideoRecordingPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) && ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.CAMERA
            )
        ) {
            Toast.makeText(
                this,
                "Acepta los permisos de almacenamiento y camara",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA),
                REQUEST_CODE_VIDEO_RECORDING
            )
        }
    }

    private fun startTimer() {
        recordingTimer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // Actualizar UI con el tiempo restante si es necesario
            }

            override fun onFinish() {
                stopRecording()
            }
        }.start()
    }

    private fun recordingFile(recording: Recording) {
        val prefs = getSharedPreferences("Recordings", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = prefs.getString("RECORDING_LIST", null)
        val type = object : TypeToken<MutableList<Recording>>() {}.type
        val recordings =
            if (json != null) gson.fromJson<MutableList<Recording>>(json, type) else mutableListOf()
        recordings.add(recording)
        val edit = prefs.edit()
        edit.putString("RECORDING_LIST", gson.toJson(recordings))
        edit.apply()
    }

    private fun stopRecording() {
        try {
            mediaRecorder?.stop()
            mediaRecorder?.release()
            mediaRecorder = null
            recordingTimer?.cancel()
            recordingTimer = null
            Toast.makeText(this, "Grabación finalizada", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error al detener la grabación", Toast.LENGTH_SHORT).show()
        }
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
            updateSosPhoneNumber()
            if (SOS_PHONE_NUMBER.isBlank()) {
                Toast.makeText(this, "No hay contactos de emergencia configurados", Toast.LENGTH_LONG).show()
            } else {
                sendSms()
            }
        }
    }

    private fun sendSms() {
        try {
            val smsManager = SmsManager.getDefault()
            val prefs =
                getSharedPreferences(getString(R.string.txtEmergency_text), Context.MODE_PRIVATE)
            val SOS_MESSAGE = prefs.getString(
                "Text",
                "!!!Necesito Ayuda!!!"
            ) + "\n" + "Mi ubicacion actual es esta: " + createMapLink()
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
            Toast.makeText(this, "Acepta los permisos de localizacion", Toast.LENGTH_SHORT).show()
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
            Toast.makeText(this, "No se han otorgado permisos de ubicación", Toast.LENGTH_SHORT)
                .show()
            return
        }

        if (::map.isInitialized && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
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
                        Toast.makeText(
                            this,
                            "No se pudo obtener la ubicación actual",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
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

    override fun onDestroy() {
        super.onDestroy()
        val serviceIntent = Intent(this, ButtonPressService::class.java)
        stopService(serviceIntent)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_POWER -> {
                handler.removeCallbacks(resetRunnable)
                volumeUpCount++
                if (volumeUpCount == 1) {
                    isButtonPressed = true
                    volumeUpCount = 0
                }
                handler.postDelayed(
                    resetRunnable,
                    2000
                )
                return true
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                handler.removeCallbacks(resetRunnable)
                volumeDownCount++
                if (volumeDownCount == 4) {
                    isButtonPressed = true
                    volumeDownCount = 0
                }
                handler.postDelayed(
                    resetRunnable,
                    2000
                )
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        enableLocation()
        getCurrentLocation()
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
                    "Acepta los permisos para enviar mensajes de texto",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        when (requestCode) {
            REQUEST_CODE_LOCATION -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                map.isMyLocationEnabled = true
            } else {
                Toast.makeText(this, "Acepta los permisos de localizacion", Toast.LENGTH_SHORT)
                    .show()
            }
            else -> {}
        }
        when (requestCode) {
            REQUEST_CODE_AUDIO_RECORDING -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {
                Toast.makeText(this, "Acepta los permisos de alamacenamiento y audio", Toast.LENGTH_SHORT)
                    .show()
            }
            else -> {}
        }
        when (requestCode) {
            REQUEST_CODE_VIDEO_RECORDING -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {
                Toast.makeText(this, "Acepta los permisos de alamacenamiento y camara", Toast.LENGTH_SHORT)
                    .show()
            }
            else -> {}
        }
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        if (!::map.isInitialized) return
        if (!isLocationPermissionGranted()) {
            map.isMyLocationEnabled = false

            requestLocationPermission()
            Toast.makeText(this, "Acepta los permisos de localizacion", Toast.LENGTH_SHORT).show()
        }
    }
}