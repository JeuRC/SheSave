package com.example.shesave

data class User(
    val email: String,
    val password: String,
    var contacts: MutableList<Contact> = mutableListOf()
)