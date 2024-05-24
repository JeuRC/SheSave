package com.example.shesave.alarm

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.CountDownTimer
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.shesave.Home
import com.example.shesave.R
import com.example.shesave.Recording
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlertRecording {
    private var recordingName: String? = null
    private var recordingTrack: String? = null
    private var recordingTimer: CountDownTimer? = null
    private var fileName: String = ""
    private var recorder: MediaRecorder? = null

    fun startRecording(context: Context) {
        val prefs = context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)
        val AudioChecked = prefs.getBoolean("AudioChecked", false)
        val VideoChecked = prefs.getBoolean("VideoChecked", false)
        if (AudioChecked && !VideoChecked) {
            startAudioRecording(context)
        }
        if (VideoChecked && !AudioChecked) {
            startVideoRecording(context)
        }
    }

    private fun startAudioRecording(context: Context) {
        if (isAudioRecordingPermissionGranted(context)) {
            val currentTimeMillis = System.currentTimeMillis()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    recorder = MediaRecorder().apply {
                        setAudioSource(MediaRecorder.AudioSource.MIC)
                        setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                        setOutputFile(fileName)
                        setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                        try {
                            prepare()
                        } catch (e: IOException) {
                            Log.e(Home.LOG_TAG, "prepare() failed")
                        }
                        start()
                    }
                }catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            recordingName = currentTimeMillis.toString()
            recordingTrack = fileName
            val recording = Recording(
                recordingName.toString(),
                recordingTrack.toString(),
                currentTimeMillis
            )
            recordingFile(recording, context)

            startTimer(context)
        } else {
            requestAudioRecordingPermission(context)
        }
    }

    private fun startVideoRecording(context: Context) {
        if (isVideoRecordingPermissionGranted(context)) {
            val REQUEST_VIDEO_CAPTURE = 1
            val videoIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
            if (videoIntent.resolveActivity(context.packageManager) != null) {
                if (context is Activity) {
                    context.startActivityForResult(videoIntent, REQUEST_VIDEO_CAPTURE)
                }
            }
        } else {
            requestVideoRecordingPermission(context)
        }
    }

    private fun isAudioRecordingPermissionGranted(context: Context) = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.RECORD_AUDIO
    ) == PackageManager.PERMISSION_GRANTED

    private fun isVideoRecordingPermissionGranted(context: Context) = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    private fun requestAudioRecordingPermission(context: Context) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                context as Activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) && ActivityCompat.shouldShowRequestPermissionRationale(
                context,
                Manifest.permission.RECORD_AUDIO
            )
        ) {
        } else {
            ActivityCompat.requestPermissions(
                context,
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.RECORD_AUDIO
                ),
                Home.REQUEST_CODE_AUDIO_RECORDING
            )
        }
    }

    private fun requestVideoRecordingPermission(context: Context) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                context as Activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) && ActivityCompat.shouldShowRequestPermissionRationale(
                context,
                Manifest.permission.CAMERA
            )
        ) {

        } else {
            ActivityCompat.requestPermissions(
                context,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA),
                Home.REQUEST_CODE_VIDEO_RECORDING
            )
        }
    }

    private fun startTimer(context: Context) {
        recordingTimer = object : CountDownTimer(6000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // Actualizar UI con el tiempo restante si es necesario
            }

            override fun onFinish() {
                stopRecording(context)
            }
        }.start()
    }

    private fun recordingFile(recording: Recording, context: Context) {
        val prefs = context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)
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

    private fun stopRecording(context: Context) {
        try {
            recorder?.apply {
                stop()
                release()
            }
            recorder = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}