package com.example.shesave.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.shesave.Contact
import com.example.shesave.R

class ContactsAdapter(
    private val list: List<Contact>, // Lista de contactos a mostrar en el RecyclerView
    private val onClickDelete: (Int) -> Unit, // Callback para manejar la accion de eliminar contacto
    private val onClickItem: (Contact) -> Unit // Callback para manejar la accion de hacer clic en un contacto
) :
    RecyclerView.Adapter<ContactsViewHolder>() {
    // Crea y retorna un nuevo ViewHolder para cada item de la lista
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactsViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ContactsViewHolder(layoutInflater.inflate(R.layout.item_contact, parent, false))
    }

    // Vincula datos a un ViewHolder especifico segun su posicion en la lista
    override fun onBindViewHolder(holder: ContactsViewHolder, position: Int) {
        val item = list[position]
        holder.render(item, onClickDelete) {
            onClickItem(item)
        }
    }

    // Retorna la cantidad total de items en la lista
    override fun getItemCount(): Int = list.size
}