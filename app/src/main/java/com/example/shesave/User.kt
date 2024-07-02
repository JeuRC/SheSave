package com.example.shesave

data class User(
    var email: String,
    var password: String,
    var contacts: MutableList<Contact> = mutableListOf()
)