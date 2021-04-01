package com.example.nushlibrary

data class User(
    val id: String = "",
    val email: String = "",
    val displayName: String = "",
    val booksBorrowed: ArrayList<*>? = arrayListOf<String>(),
    val toReadList: ArrayList<*>? = arrayListOf<String>(),
    val admin: Boolean = false) {
}