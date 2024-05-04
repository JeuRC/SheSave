package com.example.shesave.adapter

import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.shesave.Contact
import com.example.shesave.R

class ContactsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val name = view.findViewById<TextView>(R.id.txtContact)
    private val click = view.findViewById<Button>(R.id.txtContact)
    private val delete = view.findViewById<ImageButton>(R.id.imgDelete)

    fun render(contactModel: Contact, onClickDelete: (Int) -> Unit, onClickItem: () -> Unit) {
        name.text = contactModel.name
        click.setOnClickListener { onClickItem() }
        delete.setOnClickListener { onClickDelete(adapterPosition) }
    }
}