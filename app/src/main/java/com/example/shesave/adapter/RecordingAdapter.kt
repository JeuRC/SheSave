package com.example.shesave.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.shesave.R
import com.example.shesave.Recording

class RecordingAdapter(
    private val list: List<Recording>,
    private val onClickDelete: (Int) -> Unit
) :
    RecyclerView.Adapter<RecordingViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordingViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return RecordingViewHolder(layoutInflater.inflate(R.layout.item_recording, parent, false))
    }

    override fun onBindViewHolder(holder: RecordingViewHolder, position: Int) {
        val item = list[position]
        holder.render(item, onClickDelete)
    }

    override fun getItemCount(): Int = list.size
}