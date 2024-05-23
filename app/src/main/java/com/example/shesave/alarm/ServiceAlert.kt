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

    private val receiver = PulseCountReceiver()
    private val notificationChannelId = "alert_notification"

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        registerReceiver(receiver, IntentFilter(Intent.ACTION_SCREEN_ON).apply {
            priority = IntentFilter.SYSTEM_HIGH_PRIORITY - 1
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        val notification = NotificationCompat.Builder(this, notificationChannelId)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle(getString(R.string.app_name))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        startForeground(1, notification)
        return START_STICKY
    }

    private fun createNotificationChannel() {
        getSystemService(NotificationManager::class.java)?.createNotificationChannel(
            NotificationChannel(
                notificationChannelId,
                "Alert Notification",
                NotificationManager.IMPORTANCE_HIGH
            )
        )
    }
}