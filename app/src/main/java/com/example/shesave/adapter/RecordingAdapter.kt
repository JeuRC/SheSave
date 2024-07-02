package com.example.shesave.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.shesave.R
import com.example.shesave.Recording

class RecordingAdapter(
    private val list: List<Recording>, // Lista de grabaciones a mostrar en el RecyclerView
    private val onClickDelete: (Int) -> Unit // Callback para manejar la accion de eliminar grabacion
) :
    RecyclerView.Adapter<RecordingViewHolder>() {
    // Crea y retorna un nuevo ViewHolder para cada item de la lista
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordingViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return RecordingViewHolder(layoutInflater.inflate(R.layout.item_recording, parent, false))
    }

    // Vincula datos a un ViewHolder especifico segun su posicion en la lista
    override fun onBindViewHolder(holder: RecordingViewHolder, position: Int) {
        val item = list[position]
        holder.render(item, onClickDelete)
    }

    // Retorna la cantidad total de items en la lista
    override fun getItemCount(): Int = list.size
}