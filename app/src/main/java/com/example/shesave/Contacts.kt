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

// Lista mutable de contactos
private val contactList = mutableListOf<Contact>()
// Adaptador para la lista de contactos
private lateinit var adapter: ContactsAdapter

class Contacts : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide() // Oculta la barra de accion
        setContentView(R.layout.activity_contacts)

        // Inicializacion de vistas
        val imgBack = findViewById<ImageButton>(R.id.imgBack)
        val txtAdd_contact = findViewById<TextView>(R.id.txtAdd_contact)
        val btnAccept = findViewById<Button>(R.id.btnAccept)

        // Configuracion del listener para el boton de retroceso
        imgBack.setOnClickListener {
            val intent = Intent(this, Setting::class.java)
            startActivity(intent)
        }

        // Configura el boton para añadir contacto
        txtAdd_contact.setOnClickListener {
            val intent = Intent(this, AddContact::class.java)
            startActivity(intent)
        }

        // Configura el boton de aceptar
        btnAccept.setOnClickListener {
            val intent = Intent(this, Setting::class.java)
            startActivity(intent)
        }
    }

    // Carga los contactos e inicializa el RecyclerView cuando la actividad se reanuda
    override fun onResume() {
        super.onResume()
        loadContact()
        initRecyclerView()
    }

    // Inicializa el RecyclerView con el adaptador y la configuracion de layout
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

    // Elimina un contacto de la lista y actualiza los datos almacenados
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
            currentUser.contacts.removeAt(position)
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

    // Muestra los detalles de un contacto en un AlertDialog
    private fun showContactDetails(contact: Contact) {
        AlertDialog.Builder(this)
            .setTitle(contact.name)
            .setMessage(contact.number)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    // Carga los contactos del almacenamiento compartido
    private fun loadContact() {
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
            contactList.clear()
            currentUser.contacts.let {
                contactList.addAll(it)
            }
        } else {
            Toast.makeText(this, "No se encontró el usuario actual", Toast.LENGTH_SHORT).show()
        }
    }
}