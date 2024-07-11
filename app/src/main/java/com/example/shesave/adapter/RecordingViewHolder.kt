package com.example.shesave.adapter

import android.media.MediaPlayer
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.shesave.R
import com.example.shesave.Recording
import java.text.SimpleDateFormat
import java.util.*

class RecordingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val name = view.findViewById<TextView>(R.id.txtRecording) // TextView para mostrar el nombre de la grabacion
    private val play = view.findViewById<ImageButton>(R.id.imgPlay) // ImageButton para reproducir o pausar la grabacion
    private val delete = view.findViewById<ImageButton>(R.id.imgDelete) // ImageButton para eliminar la grabacion
    private var mediaPlayer: MediaPlayer? = null // Objeto MediaPlayer para reproducir la grabacion
    private var isPlaying = false // Estado de reproduccion actual
    private var recordingModel: Recording? = null // Modelo de grabacion asociado a este ViewHolder
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) // Formato de fecha para mostrar el timestamp

    init {
        play.setOnClickListener {
            recordingModel?.let {
                if (!isPlaying) {
                    startPlaying()
                } else {
                    stopPlaying()
                }
            }
        }
    }

    // Metodo para renderizar los datos de la grabacion en el ViewHolder
    fun render(recordingModel: Recording, onClickDelete: (Int) -> Unit) {
        this.recordingModel = recordingModel
        name.text = dateFormat.format(Date(recordingModel.timestamp)) // Establece la fecha formateada en el TextView
        delete.setOnClickListener { onClickDelete(adapterPosition) } // Define el listener para el boton de eliminar la grabacion
    }

    // Metodo para iniciar la reproduccion de la grabacion
    private fun startPlaying() {
        recordingModel?.let { model ->
            try {
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(model.track)
                    prepare()
                    start()
                    setOnCompletionListener { stopPlaying() }
                }
                isPlaying = true
                play.setImageResource(R.drawable.icon_pause)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(itemView.context, "Error al reproducir", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Metodo para detener la reproduccion de la grabacion
    private fun stopPlaying() {
        mediaPlayer?.release()
        mediaPlayer = null
        isPlaying = false
        Toast.makeText(itemView.context, "Reproduccion finalizada", Toast.LENGTH_SHORT).show()
        play.setImageResource(R.drawable.icon_play)
    }
}