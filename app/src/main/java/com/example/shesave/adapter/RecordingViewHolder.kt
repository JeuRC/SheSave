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
    private val name = view.findViewById<TextView>(R.id.txtRecording)
    private val play = view.findViewById<ImageButton>(R.id.imgPlay)
    private val delete = view.findViewById<ImageButton>(R.id.imgDelete)
    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false
    private var recordingModel: Recording? = null
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

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

    fun render(recordingModel: Recording, onClickDelete: (Int) -> Unit) {
        this.recordingModel = recordingModel
        name.text = dateFormat.format(Date(recordingModel.timestamp))
        delete.setOnClickListener { onClickDelete(adapterPosition) }
    }

    private fun startPlaying() {
        recordingModel?.let { model ->
            try {
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(model.track) // Ruta del archivo de audio
                    prepare()
                    start()
                    setOnCompletionListener { stopPlaying() }
                }
                isPlaying = true
                //play.setImageResource(R.drawable.ic_pause) // Cambia el icono del botón a pausa
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(itemView.context, "Error al reproducir", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun stopPlaying() {
        mediaPlayer?.release()
        mediaPlayer = null
        isPlaying = false
        //play.setImageResource(R.drawable.ic_play) // Cambia el icono del botón a reproducir
    }
}