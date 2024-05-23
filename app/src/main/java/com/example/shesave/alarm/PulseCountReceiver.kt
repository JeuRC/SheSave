package com.example.shesave.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import com.example.shesave.Home
import com.example.shesave.R
import kotlinx.coroutines.*

class PulseCountReceiver : BroadcastReceiver() {

    private var pulseCount = 3
    private var numClicks = 0
    private var lastClickTime: Long = 0L
    private val message = AlertMessage()
    private val recording = Home()

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_SCREEN_ON || intent?.action == Intent.ACTION_SCREEN_OFF) {
            CoroutineScope(Dispatchers.Default).launch {
                updatePulseCount(context)
                //processClicks(context)
                timePulse(context)
            }
        }
    }

    private fun updatePulseCount(context: Context) {
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)
        pulseCount = sharedPreferences.getInt("key_pulse", 2)
    }

    private fun timePulse(context: Context) {
        val clickTime = System.currentTimeMillis()

        if ((clickTime - lastClickTime) < 5000) {
            if (numClicks < pulseCount) {
                numClicks++
            }

            if (numClicks == pulseCount) {
                message.sendSosMessage(context)
                recording.startRecording()
                lastClickTime = System.currentTimeMillis()
                numClicks = 0
                lastClickTime = 0L
            }
        } else {
            numClicks = 1
        }
        lastClickTime = clickTime
    }

    /*private fun processClicks(context: Context) {
        val currentClickTime = System.currentTimeMillis()
        if (isClickValid(currentClickTime)) {
            numClicks++
            if (numClicks == pulseCount) {
                message.sendSosMessage(context)
                recording.startRecording()
                resetClickTracking()
            }
        } else {
            resetClickTracking()
            numClicks++
        }
        lastClickTime = currentClickTime
    }

    private fun isClickValid(currentClickTime: Long) = (currentClickTime - lastClickTime) < 5000

    private fun resetClickTracking() {
        numClicks = 0
        lastClickTime = 0L
    }*/
}