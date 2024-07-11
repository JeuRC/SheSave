package com.example.shesave

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shesave.adapter.RecordingAdapter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// Lista mutable de grabaciones
private val recordingList = mutableListOf<Recording>()
private lateinit var adapter: RecordingAdapter

class Recordings : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide() // Oculta la barra de accion
        setContentView(R.layout.activity_recordings)

        // Inicializacion de vistas
        val imgBack = findViewById<ImageButton>(R.id.imgBack)

        // Configura el boton de retroceso
        imgBack.setOnClickListener {
            onBackPressed()
        }

        // Carga y configura el RecyclerView
        loadRecording()
        initRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        // Carga las grabaciones al resumir la actividad
        loadRecording()
    }

    // Inicializa el RecyclerView con el adaptador y decoracion
    private fun initRecyclerView() {
        adapter = RecordingAdapter(
            list = recordingList,
            onClickDelete = { position -> onDeletedItem(position) })
        val manager = LinearLayoutManager(this)
        val decoration = DividerItemDecoration(this, manager.orientation)
        val recyclerView = findViewById<RecyclerView>(R.id.rvwRecordings)
        recyclerView.layoutManager = manager
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(decoration)
    }

    // Accion al eliminar un elemento de la lista
    private fun onDeletedItem(position: Int) {
        recordingList.removeAt(position)
        adapter.notifyItemRemoved(position)
        saveRecording()
    }

    // Guarda la lista de grabaciones en SharedPreferences
    private fun saveRecording() {
        val sharedPreferences = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(recordingList)
        editor.putString("RECORDING_LIST", json)
        editor.apply()
    }

    // Carga la lista de grabaciones desde SharedPreferences
    private fun loadRecording() {
        val sharedPreferences = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("RECORDING_LIST", null)
        val type = object : TypeToken<MutableList<Recording>>() {}.type
        recordingList.clear()
        if (json != null) {
            recordingList.addAll(gson.fromJson(json, type))
        }
    }
}