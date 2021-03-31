package com.example.nushlibrary

data class User(
    val email: String,
    val displayName: String,
    val booksBorrowed: ArrayList<*>?,
    val toReadList: ArrayList<*>?,
    val admin: Boolean) {
}