package com.example.shesave

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.shesave.alarm.AlertMessage
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException

class Home : AppCompatActivity(), OnMapReadyCallback {

    // Variables para manejar el mapa y la ubicacion
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Variables para manejar la grabacion de audio
    private var recordingName: String? = null
    private var recordingTrack: String? = null
    private var recordingTimer: CountDownTimer? = null
    private var isSosButtonPressed = false
    private var alert = AlertMessage()
    private var fileName: String = ""
    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null
    private val sosHandler = Handler()
    private val sosRunnable = Runnable {
        if (isSosButtonPressed) {
            alert.sendSosMessage(this)
            startRecording()
        }
    }

    // Constantes para manejar permisos
    companion object {
        const val REQUEST_CODE_LOCATION = 0
        const val REQUEST_CODE_SMS_PERMISSION = 1
        const val REQUEST_CODE_VIDEO_RECORDING = 0
        const val REQUEST_CODE_AUDIO_RECORDING = 0
        const val LOG_TAG = "AudioRecordTest"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide() // Oculta la barra de accion
        setContentView(R.layout.activity_home)
        createFragment()

        // Inicializacion de vistas
        val btnSetting = findViewById<ImageButton>(R.id.btnSetting)
        val btnSos = findViewById<ImageButton>(R.id.btnSos)
        val imgRecording = findViewById<ImageButton>(R.id.imgRecording)
        val imgContacts = findViewById<ImageButton>(R.id.imgContacts)

        // Configura el boton de retroceso
        btnSetting.setOnClickListener {
            val intent = Intent(this, Setting::class.java)
            startActivity(intent)
        }

        // Configura el tiempo del boton sos
        btnSos.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    isSosButtonPressed = true
                    sosHandler.postDelayed(sosRunnable, 2000) //2 segundos
                }
                MotionEvent.ACTION_UP -> {
                    isSosButtonPressed = false
                    sosHandler.removeCallbacks(sosRunnable)
                }
            }
            true
        }

        // Configura el boton de grabaciones
        imgRecording.setOnClickListener {
            val intent = Intent(this, Recordings::class.java)
            startActivity(intent)
        }

        // Configura el boton de contactos
        imgContacts.setOnClickListener {
            val intent = Intent(this, Contacts::class.java)
            startActivity(intent)
        }

        // Inicializa el cliente de ubicacion
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        getCurrentLocation()
    }

    // Inicia la grabacion de audio o video basado en las preferencias del usuario
    fun startRecording() {
        val prefs = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE)
        val AudioChecked = prefs.getBoolean("AudioChecked", false)
        val VideoChecked = prefs.getBoolean("VideoChecked", false)
        if (AudioChecked && !VideoChecked) {
            startAudioRecording()
        }
        if (VideoChecked && !AudioChecked) {
            startVideoRecording()
        }
    }

    // Inicia la grabacion de audio
    private fun startAudioRecording() {
        if (isAudioRecordingPermissionGranted()) {
            val currentTimeMillis = System.currentTimeMillis()

            fileName = generateFileName()

            try {
                recorder = MediaRecorder().apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                    setOutputFile(fileName)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                    try {
                        prepare()
                    } catch (e: IOException) {
                        Log.e(LOG_TAG, "prepare() failed")
                    }
                    start()
                }
                Toast.makeText(this, "Grabacion iniciada", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Error al iniciar la grabación", Toast.LENGTH_SHORT).show()
            }

            recordingName = currentTimeMillis.toString()
            recordingTrack = fileName
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

    // Genera un nombre de archivo basado en el timestamp actual
    private fun generateFileName(): String {
        val timestamp = System.currentTimeMillis()
        return "${externalCacheDir?.absolutePath}/audio_$timestamp.3gp"
    }

    // Inicia la grabacion de video
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

    // Verifica si los permisos de grabacion de audio han sido concedidos
    private fun isAudioRecordingPermissionGranted() = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.RECORD_AUDIO
    ) == PackageManager.PERMISSION_GRANTED

    // Verifica si los permisos de grabacion de video han sido concedidos
    private fun isVideoRecordingPermissionGranted() = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    // Solicita permisos para grabacion de audio
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

    // Solicita permisos para grabacion de video
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

    // Inicia un temporizador para detener la grabacion despues de un minuto
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

    // Guarda la grabacion en las preferencias
    private fun recordingFile(recording: Recording) {
        val prefs = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE)
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

    // Detiene la grabacion de audio
    private fun stopRecording() {
        try {
            recorder?.apply {
                stop()
                release()
            }
            recorder = null
            Toast.makeText(this, "Grabación finalizada", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error al detener la grabación", Toast.LENGTH_SHORT).show()
        }
    }

    // Crea el fragmento del mapa
    private fun createFragment() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    // Verifica si los permisos de ubicacion estan concedidos
    private fun isLocationPermissionGranted() = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    // Habilita la ubicacion si los permisos estan concedidos
    private fun enableLocation() {
        if (!::map.isInitialized) return
        if (isLocationPermissionGranted()) {
            map.isMyLocationEnabled = true
        } else {
            requestLocationPermission()
        }
    }

    // Solicita permisos de ubicacion
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

    // Obtiene la ubicacion actual del dispositivo
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
                        val pref =
                            getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE)
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

    // Actualiza el numero de telefono SOS cuando la actividad se reanuda
    override fun onResume() {
        super.onResume()
        alert.updateSosPhoneNumber(this)
    }

    // Libera los recursos al detener la actividad
    override fun onStop() {
        super.onStop()
        recorder?.release()
        recorder = null
        player?.release()
        player = null
    }

    // Configura el mapa cuando este listo para usar
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        enableLocation()
        getCurrentLocation()
    }

    // Maneja los resultados de solicitudes de permisos
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_SMS_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                alert.sendSms(this)
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
                Toast.makeText(
                    this,
                    "Acepta los permisos de alamacenamiento y audio",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
            else -> {}
        }
        when (requestCode) {
            REQUEST_CODE_VIDEO_RECORDING -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {
                Toast.makeText(
                    this,
                    "Acepta los permisos de alamacenamiento y camara",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
            else -> {}
        }
    }

    // Revisa y solicita permisos de ubicacion al reanudar los fragmentos
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