package com.example.shesave

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Handler
import android.os.IBinder
import android.view.KeyEvent

class ServiceAlert : Service() {

    //private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate() {

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //mediaPlayer = MediaPlayer.create(this, R.raw.audio)
        //mediaPlayer.start()
        return START_STICKY
    }

    override fun onDestroy() {
        //mediaPlayer.stop()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}