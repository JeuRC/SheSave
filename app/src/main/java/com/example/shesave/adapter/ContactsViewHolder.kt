package com.example.shesave.adapter

import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.shesave.Contact
import com.example.shesave.R

class ContactsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val name = view.findViewById<TextView>(R.id.txtContact)
    private val delete = view.findViewById<ImageButton>(R.id.imgDelete)

    fun render(contactModel: Contact, onClickDelete: (Int) -> Unit) {
        name.text = contactModel.name
        delete.setOnClickListener { onClickDelete(adapterPosition) }
    }
}