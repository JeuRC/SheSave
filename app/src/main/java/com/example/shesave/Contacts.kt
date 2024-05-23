package com.example.shesave

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
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
        val sharedPreferences = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE)
        val gson = Gson()

        val currentUserEmail = sharedPreferences.getString("Email", null)
        if (currentUserEmail == null) {
            Toast.makeText(this, "No se encontró el usuario actual", Toast.LENGTH_SHORT).show()
            return
        }

        val json = sharedPreferences.getString("USER_LIST", null)
        val type = object : TypeToken<MutableList<User>>() {}.type
        val users = if (json != null) gson.fromJson<MutableList<User>>(json, type) else mutableListOf()

        val currentUser = users.find { it.email == currentUserEmail }
        if (currentUser != null) {
            currentUser.contacts?.removeAt(position)
            val editor = sharedPreferences.edit()
            val updatedJson = gson.toJson(users)
            editor.putString("USER_LIST", updatedJson)
            editor.apply()
            contactList.removeAt(position)
            adapter.notifyItemRemoved(position)
        } else {
            Toast.makeText(this, "No se encontró el usuario actual", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showContactDetails(contact: Contact) {
        AlertDialog.Builder(this)
            .setTitle(contact.name)
            .setMessage(contact.number)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    /*private fun saveContact() {
        val sharedPreferences = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(contactList)
        editor.putString("CONTACT_LIST", json)
        editor.apply()
    }*/

    private fun loadContact() {
        val sharedPreferences = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE)
        val gson = Gson()

        // Obtener el email del usuario actualmente registrado
        val currentUserEmail = sharedPreferences.getString("Email", null)
        if (currentUserEmail == null) {
            Toast.makeText(this, "No se encontró el usuario actual", Toast.LENGTH_SHORT).show()
            return
        }

        val json = sharedPreferences.getString("USER_LIST", null)
        val type = object : TypeToken<MutableList<User>>() {}.type
        val users = if (json != null) gson.fromJson<MutableList<User>>(json, type) else mutableListOf()

        val currentUser = users.find { it.email == currentUserEmail }
        if (currentUser != null) {
            contactList.clear()

            currentUser.contacts?.let {
                contactList.addAll(it)
            }
        } else {
            Toast.makeText(this, "No se encontró el usuario actual", Toast.LENGTH_SHORT).show()
        }
    }
}