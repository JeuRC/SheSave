package com.example.shesave

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder

class ButtonPressService : Service() {
    private var volumeUpCount = 0
    private var volumeDownCount = 0
    private val handler = Handler()
    private val resetRunnable = Runnable {
        volumeUpCount = 0
        volumeDownCount = 0
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopDetection()
    }

    private fun startDetection() {
        // Aquí configuras la detección de pulsaciones de botones de volumen
        // Se puede hacer de la misma manera que en el método onKeyDown de tu actividad
        // Pero aquí se ejecuta en un bucle continuo mientras el servicio esté activo
    }

    private fun stopDetection() {
        // Detener la detección de pulsaciones de botones de volumen
        // Por ejemplo, puedes detener los callbacks del handler
        handler.removeCallbacks(resetRunnable)
    }
}