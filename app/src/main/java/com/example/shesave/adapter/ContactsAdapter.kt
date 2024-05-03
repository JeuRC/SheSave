package com.example.shesave.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.shesave.Contact
import com.example.shesave.R

class ContactsAdapter(
    private val list: List<Contact>,
    private val onClickDelete: (Int) -> Unit,
    private val onClickItem: (Contact) -> Unit
) :
    RecyclerView.Adapter<ContactsViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactsViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ContactsViewHolder(layoutInflater.inflate(R.layout.item_contact, parent, false))
    }

    override fun onBindViewHolder(holder: ContactsViewHolder, position: Int) {
        val item = list[position]
        holder.render(item, onClickDelete) {
            onClickItem(item)
        }
    }

    override fun getItemCount(): Int = list.size
}