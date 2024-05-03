package com.example.shesave

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shesave.adapter.RecordingAdapter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

private val recordingList = mutableListOf<Recording>()
private lateinit var adapter: RecordingAdapter

class Recordings : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_recordings)

        val imgBack = findViewById<ImageButton>(R.id.imgBack)

        imgBack.setOnClickListener {
            onBackPressed()
        }

        loadRecording()
        initRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        loadRecording()
    }

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

    private fun onDeletedItem(position: Int) {
        recordingList.removeAt(position)
        adapter.notifyItemRemoved(position)
        saveRecording()
    }

    private fun saveRecording() {
        val sharedPreferences = getSharedPreferences("Recordings", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(recordingList)
        editor.putString("RECORDING_LIST", json)
        editor.apply()
    }

    private fun loadRecording() {
        val sharedPreferences = getSharedPreferences("Recordings", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("RECORDING_LIST", null)
        val type = object : TypeToken<MutableList<Recording>>() {}.type
        recordingList.clear()
        if (json != null) {
            recordingList.addAll(gson.fromJson(json, type))
        }
    }
}