package com.example.shesave.adapter

import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.shesave.Contact
import com.example.shesave.R

class ContactsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val name = view.findViewById<TextView>(R.id.txtContact) // TextView para mostrar el nombre del contacto
    private val click = view.findViewById<Button>(R.id.txtContact) // Boton asociado al contacto para acciones adicionales
    private val delete = view.findViewById<ImageButton>(R.id.imgDelete) // ImageButton para eliminar el contacto

    // Metodo para renderizar los datos del contacto en el ViewHolder
    fun render(contactModel: Contact, onClickDelete: (Int) -> Unit, onClickItem: () -> Unit) {
        name.text = contactModel.name // Establece el nombre del contacto en el TextView
        click.setOnClickListener { onClickItem() } // Define el listener para el boton de accion del contacto
        delete.setOnClickListener { onClickDelete(adapterPosition) } // Define el listener para eliminar el contacto
    }
}