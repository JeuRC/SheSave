package com.example.shesave.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.shesave.Home
import kotlinx.coroutines.*

class PulseCountReceiver : BroadcastReceiver() {

    private var pulseCount = 3 // Numero de clics necesarios para activar la alerta
    private var numClicks = 0 // Contador de clics
    private var lastClickTime: Long = 0L // Tiempo del ultimo clic
    private val message = AlertMessage() // Instancia para enviar mensajes de alerta
    private val recording = Home() // Instancia para manejar la grabación de audio

    override fun onReceive(context: Context, intent: Intent?) {
        // Verifica si la accion recibida es encendido o apagado de pantalla
        if (intent?.action == Intent.ACTION_SCREEN_ON || intent?.action == Intent.ACTION_SCREEN_OFF) {
            // Procesa los clics en un nuevo hilo de trabajo usando Coroutines
            CoroutineScope(Dispatchers.Default).launch {
                processClicks(context)
            }
        }
    }

    // Metodo para procesar los clics y activar la alerta si se cumplen las condiciones
    private fun processClicks(context: Context) {
        val currentClickTime = System.currentTimeMillis()
        if (isClickValid(currentClickTime)) {
            numClicks++
            if (numClicks == pulseCount) {
                message.sendSosMessage(context) // Envía el mensaje de alerta SOS
                recording.startRecording() // Inicia la grabacion de audio
                resetClickTracking()
            }
        } else {
            resetClickTracking()
            numClicks++
        }
        lastClickTime = currentClickTime // Actualiza el tiempo del ultimo clic
    }

    // Metodo para verificar si un clic es valido basado en el tiempo transcurrido
    private fun isClickValid(currentClickTime: Long) = (currentClickTime - lastClickTime) < 5000

    // Metodo para reiniciar el seguimiento de clics
    private fun resetClickTracking() {
        numClicks = 0
        lastClickTime = 0L
    }
}