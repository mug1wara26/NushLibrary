package com.example.nushlibrary

data class User(
    val id: String = "",
    val email: String = "",
    val displayName: String = "",
    val booksBorrowed: ArrayList<String> = arrayListOf(),
    val toReadList: ArrayList<String> = arrayListOf(),
    val admin: Boolean = false) {
}