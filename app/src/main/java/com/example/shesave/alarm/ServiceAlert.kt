package com.example.shesave.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.shesave.R

class ServiceAlert : Service() {

    private val receiver = PulseCountReceiver() // Receptor para gestionar eventos de pulsacion
    private val notificationChannelId = "alert_notification" // ID del canal de notificacion

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        // Registra el receptor para recibir eventos de encendido de pantalla con alta prioridad
        registerReceiver(receiver, IntentFilter(Intent.ACTION_SCREEN_ON).apply {
            priority = IntentFilter.SYSTEM_HIGH_PRIORITY - 1
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver) // Desregistra el receptor al destruir el servicio
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        // Construye la notificación para el servicio en primer plano
        val notification = NotificationCompat.Builder(this, notificationChannelId)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle(getString(R.string.app_name)) // Titulo de la notificacion
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Prioridad alta para la notificacion
            .build()

        startForeground(1, notification) // Inicia el servicio en primer plano con la notificacion
        return START_STICKY // Indica que el servicio debe ser reiniciado si se detiene
    }

    // Metodo para crear el canal de notificacion si no existe
    private fun createNotificationChannel() {
        getSystemService(NotificationManager::class.java)?.createNotificationChannel(
            NotificationChannel(
                notificationChannelId,
                "Alert Notification", // Nombre del canal de notificacion
                NotificationManager.IMPORTANCE_HIGH // Importancia alta para las notificaciones del canal
            )
        )
    }
}