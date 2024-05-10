package com.example.shesave

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shesave.adapter.ContactsAdapter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

private val contactList = mutableListOf<Contact>()
private lateinit var adapter: ContactsAdapter

class Contacts : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_contacts)
        val imgBack = findViewById<ImageButton>(R.id.imgBack)
        val txtAdd_contact = findViewById<TextView>(R.id.txtAdd_contact)
        val btnAccept = findViewById<Button>(R.id.btnAccept)

        imgBack.setOnClickListener {
            val intent = Intent(this, Setting::class.java)
            startActivity(intent)
        }

        txtAdd_contact.setOnClickListener {
            val intent = Intent(this, AddContact::class.java)
            startActivity(intent)
        }

        btnAccept.setOnClickListener {
            val intent = Intent(this, Setting::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        loadContact()
        initRecyclerView()
    }

    private fun initRecyclerView() {
        adapter = ContactsAdapter(
            list = contactList,
            onClickDelete = { position -> onDeletedItem(position) },
            onClickItem = { contact -> showContactDetails(contact) })
        val manager = LinearLayoutManager(this)
        val decoration = DividerItemDecoration(this, manager.orientation)
        val recyclerView = findViewById<RecyclerView>(R.id.rvwContacts)
        recyclerView.layoutManager = manager
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(decoration)
    }

    private fun onDeletedItem(position: Int) {
        contactList.removeAt(position)
        adapter.notifyItemRemoved(position)
        saveContact()
    }

    private fun showContactDetails(contact: Contact) {
        AlertDialog.Builder(this)
            .setTitle(contact.name)
            .setMessage(contact.number)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun saveContact() {
        val sharedPreferences = getSharedPreferences("SHARED_PREFS", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(contactList)
        editor.putString("CONTACT_LIST", json)
        editor.apply()
    }

    private fun loadContact() {
        val sharedPreferences = getSharedPreferences("SHARED_PREFS", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("CONTACT_LIST", null)
        val type = object : TypeToken<MutableList<Contact>>() {}.type
        contactList.clear()
        if (json != null) {
            contactList.addAll(gson.fromJson(json, type))
        }
    }
}